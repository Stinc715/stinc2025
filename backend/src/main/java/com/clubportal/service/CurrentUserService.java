package com.clubportal.service;

import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class CurrentUserService {

    private final UserRepository userRepo;

    public CurrentUserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User requireUser() {
        User current = findUserOrNull();
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }
        return current;
    }

    public User findUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }

        String email = String.valueOf(auth.getPrincipal());
        if (email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            return null;
        }
        List<User> candidates = userRepo.findAllByEmailIgnoreCase(email);
        if (candidates.isEmpty()) {
            return null;
        }

        boolean wantsClub = auth.getAuthorities() != null && auth.getAuthorities().stream()
                .anyMatch(a -> {
                    String v = String.valueOf(a.getAuthority());
                    return "ROLE_CLUB".equalsIgnoreCase(v) || "ROLE_ADMIN".equalsIgnoreCase(v);
                });

        return candidates.stream()
                .sorted(Comparator
                        .comparingInt((User u) -> roleRank(u.getRole(), wantsClub))
                        .thenComparing(User::getUserId, java.util.Comparator.nullsLast(Integer::compareTo)))
                .findFirst()
                .orElse(null);
    }

    private static int roleRank(User.Role role, boolean wantsClub) {
        boolean isClub = role == User.Role.CLUB || role == User.Role.ADMIN;
        if (wantsClub) return isClub ? 0 : 1;
        return isClub ? 1 : 0;
    }
}
