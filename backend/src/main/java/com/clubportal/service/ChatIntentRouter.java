package com.clubportal.service;

import com.clubportal.dto.ClubChatContextDto;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ChatIntentRouter {

    private final ChatSlotMatcher chatSlotMatcher;

    public ChatIntentRouter() {
        this(new ChatSlotMatcher());
    }

    public ChatIntentRouter(ChatSlotMatcher chatSlotMatcher) {
        this.chatSlotMatcher = chatSlotMatcher;
    }

    public ChatIntentRoute route(String userMessage, ClubChatContextDto context) {
        String normalized = normalize(userMessage);
        ClubChatContextDto.VisibleTimeslot relevantVisibleSlot = chatSlotMatcher.findRelevantVisibleSlot(userMessage, context);

        if (containsAny(normalized,
                "speak to a human",
                "speak with a human",
                "real person",
                "human chat",
                "staff member",
                "human reply",
                "switch to staff")) {
            return routed(ChatIntentType.HUMAN_HANDOFF, null);
        }

        if (containsAny(normalized,
                "parking policy",
                "refund policy",
                "cancellation policy",
                "special policy",
                "policy",
                "parking",
                "dress code")) {
            return routed(ChatIntentType.MISSING_POLICY, null);
        }

        if (containsAny(normalized,
                "refund",
                "payment failed",
                "payment problem",
                "wrong charge",
                "charged",
                "charge dispute",
                "billing issue",
                "billing problem",
                "dispute")) {
            return routed(ChatIntentType.REFUND_OR_PAYMENT_ISSUE, null);
        }

        if (isVisibleSlotDiscoveryRequest(normalized, context, relevantVisibleSlot)) {
            return routed(ChatIntentType.VISIBLE_SLOT_DISCOVERY, relevantVisibleSlot);
        }

        if (containsAny(normalized,
                "book",
                "reserve",
                "sign me up",
                "complete booking",
                "book this slot",
                "book it for me",
                "reserve it for me")) {
            return routed(ChatIntentType.BOOKING_IN_CHAT, chatSlotMatcher.findBookingSlot(userMessage, context));
        }

        ClubChatContextDto.VisibleTimeslot memberPriceSlot = chatSlotMatcher.findMemberPriceSlot(userMessage, context);
        if (isMemberPriceExplanation(normalized, memberPriceSlot)) {
            return routed(ChatIntentType.MEMBER_PRICE_EXPLANATION, memberPriceSlot);
        }

        if (containsAny(normalized,
                "membership",
                "plan",
                "plans",
                "monthly pass",
                "quarterly",
                "half-year",
                "yearly",
                "duration",
                "discount percent")) {
            return routed(ChatIntentType.MEMBERSHIP_PLAN_INFO, null);
        }

        if (containsAny(normalized,
                "located",
                "location",
                "address",
                "opening hours",
                "hours",
                "open",
                "contact",
                "email",
                "phone",
                "call",
                "beginner",
                "category",
                "tags")) {
            return routed(ChatIntentType.CLUB_BASIC_INFO, null);
        }

        if (containsAny(normalized,
                "slot",
                "slots",
                "court",
                "available",
                "availability",
                "remaining",
                "places remain",
                "published schedule",
                "visible price",
                "what time")) {
            return routed(ChatIntentType.VISIBLE_SLOT_INFO, relevantVisibleSlot);
        }

        return routed(ChatIntentType.FALLBACK, null);
    }

    private boolean isMemberPriceExplanation(String normalized, ClubChatContextDto.VisibleTimeslot matchedTimeslot) {
        boolean directMemberPriceQuestion = containsAny(normalized,
                "member price",
                "membership pricing",
                "membership applied",
                "do i get a discount",
                "already my",
                "already the",
                "instead of",
                "why is this",
                "why is it");
        if (directMemberPriceQuestion) {
            return true;
        }
        if (!containsAny(normalized, "discount")) {
            return false;
        }
        return matchedTimeslot != null || containsAny(normalized, "slot", "court", "price", "gbp");
    }

    private boolean isVisibleSlotDiscoveryRequest(String normalized,
                                                  ClubChatContextDto context,
                                                  ClubChatContextDto.VisibleTimeslot relevantVisibleSlot) {
        if (context == null || context.visibleTimeslots() == null || context.visibleTimeslots().isEmpty()) {
            return false;
        }

        boolean asksDiscovery = containsAny(normalized,
                "any slots",
                "what slots",
                "available slot",
                "available slots",
                "availability",
                "do you have any slots",
                "do you have any evening slots",
                "what slots are available",
                "tomorrow",
                "today",
                "evening",
                "morning",
                "afternoon",
                "tonight");
        if (!asksDiscovery) {
            return false;
        }

        boolean directBookingAction = containsAny(normalized,
                "book this slot",
                "complete booking",
                "for me here",
                "book it for me",
                "reserve it for me",
                "sign me up");
        if (directBookingAction) {
            return false;
        }

        boolean genericBookingSearch = containsAny(normalized, "book", "reserve", "booking");
        if (!genericBookingSearch) {
            return true;
        }
        return relevantVisibleSlot == null;
    }

    private static boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (needle != null && !needle.isBlank() && text.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private ChatIntentRoute routed(ChatIntentType intentType, ClubChatContextDto.VisibleTimeslot matchedTimeslot) {
        return new ChatIntentRoute(intentType, matchedTimeslot);
    }
}
