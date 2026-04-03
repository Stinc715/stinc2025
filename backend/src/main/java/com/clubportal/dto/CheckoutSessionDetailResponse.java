package com.clubportal.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record CheckoutSessionDetailResponse(
        String sessionId,
        String orderNo,
        String type,
        String status,
        String provider,
        BigDecimal amount,
        String currency,
        Instant expiresAt,
        Instant completedAt,
        String checkoutUrl,
        boolean canContinueCheckout,
        boolean canCancel,
        String returnUrl,
        Integer clubId,
        String clubName,
        Integer timeslotId,
        String venueName,
        LocalDateTime slotStartTime,
        LocalDateTime slotEndTime,
        Integer membershipPlanId,
        String planName,
        Integer durationDays,
        String benefitType,
        Integer includedBookings,
        String title,
        String subtitle,
        String failureReason
) {
}
