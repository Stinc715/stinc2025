package com.clubportal.service;

import com.clubportal.config.PasswordPolicyProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordPolicyServiceTest {

    @Test
    void validateRejectsShortPasswordEvenIfPatternLooksStrongEnough() {
        PasswordPolicyProperties properties = new PasswordPolicyProperties();
        properties.setMinLength(8);
        PasswordPolicyService service = new PasswordPolicyService(properties);

        String message = service.validate("Aa1").orElseThrow();

        assertEquals(
                "Password must be at least 8 characters and include uppercase, lowercase, and a number",
                message
        );
    }

    @Test
    void validateAcceptsPasswordMeetingSharedPolicy() {
        PasswordPolicyProperties properties = new PasswordPolicyProperties();
        properties.setMinLength(8);
        PasswordPolicyService service = new PasswordPolicyService(properties);

        assertTrue(service.validate("Password1").isEmpty());
    }
}
