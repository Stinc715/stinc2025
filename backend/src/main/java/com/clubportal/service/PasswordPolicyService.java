package com.clubportal.service;

import com.clubportal.config.PasswordPolicyProperties;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PasswordPolicyService {

    private final PasswordPolicyProperties properties;

    public PasswordPolicyService(PasswordPolicyProperties properties) {
        this.properties = properties;
    }

    public Optional<String> validate(String password) {
        if (password == null || password.isBlank()) {
            return Optional.of("Password is required");
        }
        if (password.length() < properties.getMinLength()) {
            return Optional.of(requirementMessage());
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);
            if (Character.isUpperCase(ch)) hasUppercase = true;
            if (Character.isLowerCase(ch)) hasLowercase = true;
            if (Character.isDigit(ch)) hasDigit = true;
        }
        if (hasUppercase && hasLowercase && hasDigit) {
            return Optional.empty();
        }
        return Optional.of(requirementMessage());
    }

    public String requirementMessage() {
        return "Password must be at least " + properties.getMinLength()
                + " characters and include uppercase, lowercase, and a number";
    }
}
