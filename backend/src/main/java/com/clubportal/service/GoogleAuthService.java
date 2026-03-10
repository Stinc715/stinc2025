package com.clubportal.service;

import com.clubportal.dto.AuthResponse;
import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import com.clubportal.util.PasswordEncryptionUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class GoogleAuthService {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthService.class);
    private static final String TOKEN_INFO_ENDPOINT = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final UserRepository repo;
    private final PasswordEncryptionUtil passwordUtil;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${google.oauth.client-id}")
    private String googleClientId;

    public GoogleAuthService(UserRepository repo, PasswordEncryptionUtil passwordUtil) {
        this.repo = repo;
        this.passwordUtil = passwordUtil;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Verify Google Identity Services ID token and perform "find-or-create" login.
     */
    @Transactional
    public AuthResponse loginWithGoogleIdToken(String idTokenString) {
        try {
            if (idTokenString == null || idTokenString.isBlank()) {
                throw new IllegalArgumentException("Missing Google ID token");
            }

            List<String> audiences = parseGoogleClientIds();
            if (audiences.isEmpty()) {
                throw new IllegalArgumentException("Google OAuth client ID is not configured");
            }

            GooglePrincipal principal = verifyByLibrary(idTokenString, audiences);
            if (principal == null) {
                principal = verifyByTokenInfo(idTokenString, audiences);
            }
            if (principal == null) {
                throw new IllegalArgumentException("Google token could not be verified");
            }

            return loginWithGoogle(principal.name(), principal.email(), principal.sub());
        } catch (GoogleLoginPolicyException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Google login validation exception: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
            throw new IllegalArgumentException("Google authentication failed");
        }
    }

    @Transactional
    public AuthResponse loginWithGoogle(String fullName, String email, String googleSub) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("Google account email is missing");
        }

        List<User> existing = repo.findAllByEmailIgnoreCase(normalizedEmail);
        boolean hasClubIdentity = existing.stream().anyMatch(u -> isClubIdentity(u.getRole()));
        if (hasClubIdentity) {
            // Locked policy: Google sign-in is USER-only. CLUB/ADMIN must use email+password login.
            throw new GoogleLoginPolicyException(
                    "Google sign-in is only for user accounts. Club accounts must log in with email and password."
            );
        }

        User user = existing.stream()
                .filter(u -> !isClubIdentity(u.getRole()))
                .sorted(Comparator.comparing(User::getUserId, Comparator.nullsLast(Integer::compareTo)))
                .findFirst()
                .orElseGet(() -> createGoogleUser(fullName, normalizedEmail, googleSub));

        return new AuthResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole() == null ? "user" : user.getRole().toAccountType()
        );
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private User createGoogleUser(String fullName, String normalizedEmail, String googleSub) {
        User nu = new User();
        nu.setEmail(normalizedEmail);

        String display = (fullName != null && !fullName.isBlank())
                ? fullName
                : normalizedEmail.substring(0, normalizedEmail.indexOf('@'));
        nu.setUsername(display);
        nu.setRole(User.Role.USER);

        // Google users do not have a local password; store a random placeholder hash.
        String placeholder = "google-oauth:" + (googleSub != null ? googleSub : UUID.randomUUID());
        nu.setPasswordHash(passwordUtil.encodePassword(placeholder));
        return repo.save(nu);
    }

    private static boolean isClubIdentity(User.Role role) {
        return role == User.Role.CLUB || role == User.Role.ADMIN;
    }

    private List<String> parseGoogleClientIds() {
        if (googleClientId == null || googleClientId.isBlank()) {
            return List.of();
        }
        String[] raw = googleClientId.split(",");
        List<String> out = new ArrayList<>(raw.length);
        for (String part : raw) {
            String trimmed = part == null ? "" : part.trim();
            if (!trimmed.isBlank()) {
                out.add(trimmed);
            }
        }
        return out;
    }

    private GooglePrincipal verifyByLibrary(String idTokenString, List<String> audiences) {
        try {
            var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            var jsonFactory = GsonFactory.getDefaultInstance();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
                    .setAudience(Collections.unmodifiableList(audiences))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                return null;
            }

            var payload = idToken.getPayload();
            String email = normalizeEmail(payload.getEmail());
            if (email.isBlank()) {
                email = normalizeEmail(asString(payload.get("email")));
            }
            if (email.isBlank()) {
                throw new IllegalArgumentException("Google account email is missing");
            }
            String name = asString(payload.get("name"));
            String sub = payload.getSubject();
            return new GooglePrincipal(name, email, sub);
        } catch (Exception ex) {
            log.warn("Google verifier failed, falling back to tokeninfo: {}", ex.getMessage());
            return null;
        }
    }

    private GooglePrincipal verifyByTokenInfo(String idTokenString, List<String> audiences) {
        try {
            String encoded = URLEncoder.encode(idTokenString, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_INFO_ENDPOINT + encoded))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                String reason = extractTokenInfoErrorReason(response.body());
                log.warn("Google tokeninfo non-200 response: {} {}", response.statusCode(), reason);
                throw new IllegalArgumentException(
                        reason.isBlank()
                                ? "Google token rejected by Google"
                                : "Google token rejected by Google: " + reason
                );
            }

            JsonNode payload = objectMapper.readTree(response.body());
            String aud = text(payload, "aud");
            if (!audiences.contains(aud)) {
                throw new IllegalArgumentException("Google token audience mismatch");
            }

            String issuer = text(payload, "iss");
            if (!"accounts.google.com".equals(issuer) && !"https://accounts.google.com".equals(issuer)) {
                throw new IllegalArgumentException("Google token issuer mismatch");
            }

            String expRaw = text(payload, "exp");
            if (!expRaw.isBlank()) {
                long expEpoch = Long.parseLong(expRaw);
                if (expEpoch <= Instant.now().getEpochSecond()) {
                    throw new IllegalArgumentException("Google token expired");
                }
            }

            String email = normalizeEmail(text(payload, "email"));
            if (email.isBlank()) {
                throw new IllegalArgumentException("Google account email is missing");
            }

            String emailVerified = text(payload, "email_verified");
            if (!emailVerified.isBlank() && !"true".equalsIgnoreCase(emailVerified)) {
                throw new IllegalArgumentException("Google account email is not verified");
            }

            String sub = text(payload, "sub");
            String name = text(payload, "name");
            if (name.isBlank()) {
                name = null;
            }
            if (sub.isBlank()) {
                sub = null;
            }
            return new GooglePrincipal(name, email, sub);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Google tokeninfo fallback failed: {}", ex.getMessage());
            throw new IllegalArgumentException("Google token validation failed");
        }
    }

    private static String asString(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private static String text(JsonNode payload, String field) {
        JsonNode node = payload == null ? null : payload.get(field);
        return node == null ? "" : node.asText("").trim();
    }

    private String extractTokenInfoErrorReason(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            String description = text(node, "error_description");
            if (!description.isBlank()) {
                return description;
            }
            return text(node, "error");
        } catch (Exception ignored) {
            return "";
        }
    }

    private record GooglePrincipal(String name, String email, String sub) {}
}
