package com.clubportal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StripeCheckoutGateway {

    private static final String DEFAULT_API_BASE_URL = "https://api.stripe.com";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${app.payments.stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${app.payments.stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    @Value("${app.payments.stripe.api-base-url:" + DEFAULT_API_BASE_URL + "}")
    private String stripeApiBaseUrl;

    public StripeCheckoutGateway() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public boolean isConfigured() {
        return !safe(stripeSecretKey).isBlank();
    }

    public boolean isWebhookConfigured() {
        return isConfigured() && !safe(stripeWebhookSecret).isBlank();
    }

    public boolean isCheckoutReady() {
        return isConfigured() && isWebhookConfigured();
    }

    public CreatedCheckoutSession createCheckoutSession(CreateRequest request) {
        if (!isConfigured()) {
            throw new IllegalStateException("Stripe Checkout is not configured");
        }
        if (request == null) {
            throw new IllegalArgumentException("Missing Stripe checkout request");
        }

        try {
            Map<String, String> form = new LinkedHashMap<>();
            form.put("mode", "payment");
            form.put("success_url", request.successUrl());
            form.put("cancel_url", request.cancelUrl());
            form.put("client_reference_id", request.internalSessionId());
            form.put("line_items[0][quantity]", "1");
            form.put("line_items[0][price_data][currency]", request.currency().toLowerCase());
            form.put("line_items[0][price_data][unit_amount]", String.valueOf(toMinorUnits(request.amount())));
            form.put("line_items[0][price_data][product_data][name]", request.title());
            if (!safe(request.subtitle()).isBlank()) {
                form.put("line_items[0][price_data][product_data][description]", request.subtitle());
            }
            if (!safe(request.customerEmail()).isBlank()) {
                form.put("customer_email", request.customerEmail());
            }
            form.put("metadata[club_portal_session_id]", request.internalSessionId());
            form.put("metadata[type]", request.type());
            form.put("metadata[user_id]", String.valueOf(request.userId()));
            if (request.clubId() != null) {
                form.put("metadata[club_id]", String.valueOf(request.clubId()));
            }
            if (request.timeslotId() != null) {
                form.put("metadata[timeslot_id]", String.valueOf(request.timeslotId()));
            }
            if (request.membershipPlanId() != null) {
                form.put("metadata[membership_plan_id]", String.valueOf(request.membershipPlanId()));
            }

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(resolveApiBaseUrl() + "/v1/checkout/sessions"))
                    .timeout(Duration.ofSeconds(12))
                    .header("Authorization", basicAuthValue())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(toFormBody(form)))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException(extractStripeError(response.body()));
            }

            JsonNode body = objectMapper.readTree(response.body());
            String providerSessionId = text(body, "id");
            String checkoutUrl = text(body, "url");
            if (providerSessionId.isBlank() || checkoutUrl.isBlank()) {
                throw new IllegalStateException("Stripe Checkout response was incomplete");
            }
            return new CreatedCheckoutSession(providerSessionId, checkoutUrl);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create Stripe Checkout session: " + ex.getMessage(), ex);
        }
    }

    public void expireCheckoutSession(String providerSessionId) {
        if (!isConfigured() || safe(providerSessionId).isBlank()) {
            return;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resolveApiBaseUrl() + "/v1/checkout/sessions/" + urlEncode(providerSessionId) + "/expire"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", basicAuthValue())
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignored) {
        }
    }

    public VerifiedWebhookEvent verifyWebhook(String payload, String signatureHeader) {
        if (!isWebhookConfigured()) {
            throw new IllegalStateException("Stripe webhook secret is not configured");
        }

        String header = safe(signatureHeader);
        String timestamp = "";
        List<String> v1Values = new ArrayList<>();
        for (String part : header.split(",")) {
            String trimmed = safe(part);
            if (trimmed.startsWith("t=")) {
                timestamp = trimmed.substring(2).trim();
            } else if (trimmed.startsWith("v1=")) {
                v1Values.add(trimmed.substring(3).trim());
            }
        }
        if (timestamp.isBlank() || v1Values.isEmpty()) {
            throw new IllegalArgumentException("Missing Stripe signature");
        }

        String signedPayload = timestamp + "." + (payload == null ? "" : payload);
        String expected = hmacSha256Hex(safe(stripeWebhookSecret), signedPayload);
        boolean matched = v1Values.stream().anyMatch(value -> MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                safe(value).getBytes(StandardCharsets.UTF_8)
        ));
        if (!matched) {
            throw new IllegalArgumentException("Invalid Stripe signature");
        }

        try {
            JsonNode root = objectMapper.readTree(payload == null ? "" : payload);
            String type = text(root, "type");
            JsonNode object = root.path("data").path("object");
            String providerSessionId = text(object, "id");
            String internalSessionId = text(object, "client_reference_id");
            if (internalSessionId.isBlank()) {
                internalSessionId = text(object.path("metadata"), "club_portal_session_id");
            }
            String paymentStatus = text(object, "payment_status");
            return new VerifiedWebhookEvent(type, providerSessionId, internalSessionId, paymentStatus);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid Stripe webhook payload");
        }
    }

    private String resolveApiBaseUrl() {
        String value = safe(stripeApiBaseUrl);
        return value.isBlank() ? DEFAULT_API_BASE_URL : value;
    }

    private String basicAuthValue() {
        return "Basic " + Base64.getEncoder().encodeToString((safe(stripeSecretKey) + ":").getBytes(StandardCharsets.UTF_8));
    }

    private static long toMinorUnits(BigDecimal amount) {
        BigDecimal normalized = amount == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : amount.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return normalized.movePointRight(2).longValueExact();
    }

    private String extractStripeError(String body) {
        if (body == null || body.isBlank()) {
            return "Stripe Checkout request failed";
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            String message = text(root.path("error"), "message");
            return message.isBlank() ? "Stripe Checkout request failed" : message;
        } catch (Exception ignored) {
            return "Stripe Checkout request failed";
        }
    }

    private static String toFormBody(Map<String, String> form) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : form.entrySet()) {
            if (!first) {
                sb.append('&');
            }
            first = false;
            sb.append(urlEncode(entry.getKey())).append('=').append(urlEncode(entry.getValue()));
        }
        return sb.toString();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static String hmacSha256Hex(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(safe(secret).getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal((payload == null ? "" : payload).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to verify Stripe signature", ex);
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null ? "" : value.asText("").trim();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public record CreateRequest(
            String internalSessionId,
            String type,
            Integer userId,
            Integer clubId,
            Integer timeslotId,
            Integer membershipPlanId,
            String customerEmail,
            String currency,
            BigDecimal amount,
            String title,
            String subtitle,
            String successUrl,
            String cancelUrl
    ) {
    }

    public record CreatedCheckoutSession(
            String providerSessionId,
            String checkoutUrl
    ) {
    }

    public record VerifiedWebhookEvent(
            String type,
            String providerSessionId,
            String internalSessionId,
            String paymentStatus
    ) {
    }
}
