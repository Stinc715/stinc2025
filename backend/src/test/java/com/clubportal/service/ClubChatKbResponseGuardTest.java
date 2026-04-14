package com.clubportal.service;

import com.clubportal.config.ClubChatKbGuardProperties;
import com.clubportal.config.ClubChatKbMatcherProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubChatKbResponseGuardTest {

    private final ClubChatKbEmbeddingTextNormalizer normalizer = new ClubChatKbEmbeddingTextNormalizer();

    @Test
    void ambiguousHitIsRejectedWithoutRecomputingEmbedding() {
        ClubChatKbMatcherProperties matcherProperties = new ClubChatKbMatcherProperties();
        matcherProperties.setMinScoreGap(0.05d);

        ClubChatKbResponseGuard guard = new ClubChatKbResponseGuard(
                new ClubChatKbGuardProperties(),
                matcherProperties,
                normalizer
        );

        ClubChatKbMatchResult matchResult = ClubChatKbMatchResult.hit(
                12,
                "How do I contact you?",
                "Email us.",
                0.90d,
                0.87d,
                "how do i contact you?"
        );

        ClubChatKbResponseGuard.Decision decision = guard.evaluate("How do I contact you?", matchResult);

        assertFalse(decision.allow());
        assertEquals(ClubChatKbGuardRejectReason.AMBIGUOUS_MATCH, decision.rejectReason());
    }

    @Test
    void plainLowRiskQuestionPassesThirdLayer() {
        ClubChatKbResponseGuard guard = new ClubChatKbResponseGuard(
                new ClubChatKbGuardProperties(),
                new ClubChatKbMatcherProperties(),
                normalizer
        );

        ClubChatKbMatchResult matchResult = ClubChatKbMatchResult.hit(
                7,
                "How do I contact you?",
                "Email us.",
                0.92d,
                0.60d,
                "how do i contact you?"
        );

        ClubChatKbResponseGuard.Decision decision = guard.evaluate("How do I contact you?", matchResult);

        assertTrue(decision.allow());
        assertEquals(ClubChatKbGuardRejectReason.NONE, decision.rejectReason());
    }

    @Test
    void refundKeywordIsRejected() {
        ClubChatKbResponseGuard guard = new ClubChatKbResponseGuard(
                new ClubChatKbGuardProperties(),
                new ClubChatKbMatcherProperties(),
                normalizer
        );

        ClubChatKbMatchResult matchResult = ClubChatKbMatchResult.hit(
                18,
                "What is your refund policy?",
                "Please contact the club directly for refund support.",
                0.93d,
                0.41d,
                "what is your refund policy?"
        );

        ClubChatKbResponseGuard.Decision decision = guard.evaluate("Can I get a refund after booking?", matchResult);

        assertFalse(decision.allow());
        assertEquals(ClubChatKbGuardRejectReason.HIGH_RISK_KEYWORD, decision.rejectReason());
    }

    @Test
    void realtimeKeywordIsRejected() {
        ClubChatKbResponseGuard guard = new ClubChatKbResponseGuard(
                new ClubChatKbGuardProperties(),
                new ClubChatKbMatcherProperties(),
                normalizer
        );

        ClubChatKbMatchResult matchResult = ClubChatKbMatchResult.hit(
                19,
                "What are your opening hours today?",
                "We are open from 10:00 to 22:00.",
                0.91d,
                0.40d,
                "what are your opening hours today?"
        );

        ClubChatKbResponseGuard.Decision decision = guard.evaluate("What slots are available today?", matchResult);

        assertFalse(decision.allow());
        assertEquals(ClubChatKbGuardRejectReason.REALTIME_KEYWORD, decision.rejectReason());
    }
}
