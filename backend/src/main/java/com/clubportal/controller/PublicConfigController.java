package com.clubportal.controller;

import com.clubportal.service.CheckoutSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicConfigController {

    @Value("${app.public.google-maps-api-key:}")
    private String googleMapsApiKey;

    @Value("${google.oauth.client-id:}")
    private String googleOauthClientId;

    @Value("${app.payments.currency:GBP}")
    private String paymentCurrency;

    private final CheckoutSessionService checkoutSessionService;

    @Autowired
    public PublicConfigController(CheckoutSessionService checkoutSessionService) {
        this.checkoutSessionService = checkoutSessionService;
    }

    @GetMapping("/config")
    public Map<String, Object> getPublicConfig() {
        String key = safe(googleMapsApiKey);
        String oauthClientId = firstClientId(googleOauthClientId);
        return Map.of(
                "googleMapsApiKey", key,
                "googleMapsEnabled", !key.isBlank(),
                "googleOauthClientId", oauthClientId,
                "googleOauthEnabled", !oauthClientId.isBlank(),
                "paymentsEnabled", checkoutSessionService.paymentsEnabled(),
                "paymentProvider", checkoutSessionService.paymentProvider(),
                "paymentCurrency", safe(paymentCurrency).isBlank() ? "GBP" : safe(paymentCurrency).toUpperCase()
        );
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String firstClientId(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String[] parts = raw.split(",");
        for (String part : parts) {
            String trimmed = safe(part);
            if (!trimmed.isBlank()) {
                return trimmed;
            }
        }
        return "";
    }
}
