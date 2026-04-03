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
    void chineseHighRiskKeywordIsRejected() {
        ClubChatKbResponseGuard guard = new ClubChatKbResponseGuard(
                new ClubChatKbGuardProperties(),
                new ClubChatKbMatcherProperties(),
                normalizer
        );

        ClubChatKbMatchResult matchResult = ClubChatKbMatchResult.hit(
                18,
                "预约后可以取消吗？",
                "请联系工作人员。",
                0.93d,
                0.41d,
                "预约后可以取消吗"
        );

        ClubChatKbResponseGuard.Decision decision = guard.evaluate("我想退款", matchResult);

        assertFalse(decision.allow());
        assertEquals(ClubChatKbGuardRejectReason.HIGH_RISK_KEYWORD, decision.rejectReason());
    }

    @Test
    void chineseRealtimeKeywordIsRejected() {
        ClubChatKbResponseGuard guard = new ClubChatKbResponseGuard(
                new ClubChatKbGuardProperties(),
                new ClubChatKbMatcherProperties(),
                normalizer
        );

        ClubChatKbMatchResult matchResult = ClubChatKbMatchResult.hit(
                19,
                "你们什么时候营业？",
                "周一到周五 10:00-22:00。",
                0.91d,
                0.40d,
                "你们什么时候营业"
        );

        ClubChatKbResponseGuard.Decision decision = guard.evaluate("今天现在营业吗？", matchResult);

        assertFalse(decision.allow());
        assertEquals(ClubChatKbGuardRejectReason.REALTIME_KEYWORD, decision.rejectReason());
    }
}
