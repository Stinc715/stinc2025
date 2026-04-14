package com.clubportal.controller;

import com.clubportal.security.StreamAuthCookieService;
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

    @Value("${app.public.privacy-contact-email:}")
    private String privacyContactEmail;

    @Value("${app.public.data-controller-name:}")
    private String dataControllerName;

    @Value("${app.public.retention-summary:}")
    private String retentionSummary;

    @Value("${app.public.processors-summary:}")
    private String processorsSummary;

    private final CheckoutSessionService checkoutSessionService;
    private final StreamAuthCookieService streamAuthCookieService;

    @Autowired
    public PublicConfigController(CheckoutSessionService checkoutSessionService,
                                  StreamAuthCookieService streamAuthCookieService) {
        this.checkoutSessionService = checkoutSessionService;
        this.streamAuthCookieService = streamAuthCookieService;
    }

    @GetMapping("/config")
    public Map<String, Object> getPublicConfig() {
        String key = safe(googleMapsApiKey);
        String oauthClientId = firstClientId(googleOauthClientId);
        return Map.ofEntries(
                Map.entry("googleMapsApiKey", key),
                Map.entry("googleMapsEnabled", !key.isBlank()),
                Map.entry("googleOauthClientId", oauthClientId),
                Map.entry("googleOauthEnabled", !oauthClientId.isBlank()),
                Map.entry("paymentsEnabled", checkoutSessionService.paymentsEnabled()),
                Map.entry("paymentProvider", checkoutSessionService.paymentProvider()),
                Map.entry("paymentCurrency", safe(paymentCurrency).isBlank() ? "GBP" : safe(paymentCurrency).toUpperCase()),
                Map.entry("privacyContactEmail", safe(privacyContactEmail)),
                Map.entry("dataControllerName", safe(dataControllerName)),
                Map.entry("retentionSummary", safe(retentionSummary)),
                Map.entry("processorsSummary", safe(processorsSummary)),
                Map.entry("authSessionTtlSeconds", streamAuthCookieService.getAuthTokenTtlSeconds()),
                Map.entry("streamSessionTtlSeconds", streamAuthCookieService.getStreamTokenTtlSeconds())
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
