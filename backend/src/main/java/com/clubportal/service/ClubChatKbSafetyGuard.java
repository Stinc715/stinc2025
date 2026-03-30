package com.clubportal.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

@Service
public class ClubChatKbSafetyGuard {

    public Optional<Decision> evaluate(String userMessage, ChatIntentRoute route) {
        ChatIntentType intentType = route == null ? ChatIntentType.FALLBACK : route.intentType();
        if (intentType == ChatIntentType.REFUND_OR_PAYMENT_ISSUE) {
            return Optional.of(new Decision("existing high-risk intent: REFUND_OR_PAYMENT_ISSUE", true, true));
        }
        if (intentType == ChatIntentType.HUMAN_HANDOFF) {
            return Optional.of(new Decision("existing high-risk intent: HUMAN_HANDOFF", true, true));
        }
        if (intentType == ChatIntentType.BOOKING_IN_CHAT) {
            return Optional.of(new Decision("existing high-risk intent: BOOKING_IN_CHAT", true, false));
        }

        String normalized = normalize(userMessage);
        if (containsAny(normalized,
                "buy membership for me",
                "purchase membership for me",
                "pay for the membership for me",
                "sign me up for the yearly pass",
                "buy the pass for me",
                "帮我买会员",
                "帮我开会员",
                "代我购买会员",
                "直接帮我开通会员")) {
            return Optional.of(new Decision("membership purchase in chat request", true, false));
        }
        if (containsAny(normalized,
                "make an exception",
                "exception request",
                "special exception",
                "waive the rule",
                "override the policy",
                "bend the rules",
                "破例",
                "特殊处理",
                "通融一下",
                "例外申请")) {
            return Optional.of(new Decision("exception request", true, true));
        }
        if (containsAny(normalized,
                "harassment",
                "harass",
                "abuse",
                "unsafe",
                "threat",
                "bully",
                "discrimination",
                "骚扰",
                "辱骂",
                "威胁",
                "歧视",
                "不安全")) {
            return Optional.of(new Decision("harassment or safety escalation", true, true));
        }
        return Optional.empty();
    }

    private static boolean containsAny(String text, String... values) {
        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[\\p{P}\\p{S}]+", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    public record Decision(String reason, boolean forceDeterministic, boolean requiresHuman) {
    }
}
