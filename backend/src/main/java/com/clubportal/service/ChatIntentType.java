package com.clubportal.service;

public enum ChatIntentType {
    MEMBER_PRICE_EXPLANATION,
    BOOKING_IN_CHAT,
    REFUND_OR_PAYMENT_ISSUE,
    MISSING_POLICY,
    HUMAN_HANDOFF,
    CLUB_BASIC_INFO,
    VISIBLE_SLOT_INFO,
    VISIBLE_SLOT_DISCOVERY,
    MEMBERSHIP_PLAN_INFO,
    FALLBACK;

    public boolean isLowRisk() {
        return this == CLUB_BASIC_INFO
                || this == VISIBLE_SLOT_INFO
                || this == VISIBLE_SLOT_DISCOVERY
                || this == MEMBERSHIP_PLAN_INFO;
    }
}
