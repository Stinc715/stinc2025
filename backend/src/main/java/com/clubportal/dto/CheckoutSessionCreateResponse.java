package com.clubportal.dto;

import java.time.Instant;

public record CheckoutSessionCreateResponse(
        String sessionId,
        String orderNo,
        String status,
        String provider,
        String checkoutUrl,
        String paymentPageUrl,
        Instant expiresAt
) {
}
