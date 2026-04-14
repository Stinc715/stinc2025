package com.clubportal.service;

import com.clubportal.dto.ClubChatContextDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ChatResponseBuilder {

    private static final DateTimeFormatter EN_TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.UK);

    private final ChatLanguageDetector chatLanguageDetector;
    private final ChatSlotSummaryBuilder chatSlotSummaryBuilder;

    public ChatResponseBuilder() {
        this(new ChatLanguageDetector(), new ChatSlotSummaryBuilder());
    }

    ChatResponseBuilder(ChatLanguageDetector chatLanguageDetector,
                        ChatSlotSummaryBuilder chatSlotSummaryBuilder) {
        this.chatLanguageDetector = chatLanguageDetector;
        this.chatSlotSummaryBuilder = chatSlotSummaryBuilder;
    }

    public String buildReply(ChatIntentRoute route, ClubChatContextDto context, String userMessage) {
        ChatIntentType intentType = route == null ? ChatIntentType.FALLBACK : route.intentType();
        chatLanguageDetector.detect(userMessage);
        return switch (intentType) {
            case MEMBER_PRICE_EXPLANATION -> buildMemberPriceReply(route == null ? null : route.matchedTimeslot(), context, userMessage);
            case BOOKING_IN_CHAT -> buildBookingInChatReply(route == null ? null : route.matchedTimeslot(), context, userMessage);
            case REFUND_OR_PAYMENT_ISSUE -> refundReply();
            case MISSING_POLICY -> missingPolicyReply();
            case HUMAN_HANDOFF -> humanHandoffReply();
            case CLUB_BASIC_INFO -> buildClubBasicInfoReply(context, userMessage);
            case VISIBLE_SLOT_INFO -> buildVisibleSlotInfoReply(route == null ? null : route.matchedTimeslot(), context);
            case VISIBLE_SLOT_DISCOVERY -> buildVisibleSlotDiscoveryReply(context, userMessage);
            case MEMBERSHIP_PLAN_INFO -> buildMembershipPlanInfoReply(context, userMessage);
            case FALLBACK -> fallbackReply();
        };
    }

    public String fallbackReply() {
        return "I can help with club details, visible slots, membership plans, and booking guidance. If you need a staff member, please switch to human chat.";
    }

    private String buildMemberPriceReply(ClubChatContextDto.VisibleTimeslot slot,
                                         ClubChatContextDto context,
                                         String userMessage) {
        if (slot != null && slot.membershipApplied() && slot.price() != null && slot.basePrice() != null) {
            if (isBookingPack(slot.membershipBenefitType())) {
                int remainingCredits = Math.max(0, slot.membershipRemainingBookings() == null ? 0 : slot.membershipRemainingBookings());
                return "Yes. This slot is included with your membership booking pack. The standard price is "
                        + money(context, slot.basePrice()) + ", and booking it uses 1 credit. You currently have "
                        + remainingCredits + " credits left.";
            }
            return "Yes. The displayed " + money(context, slot.price())
                    + " price already reflects your membership discount. The base price is "
                    + money(context, slot.basePrice()) + ".";
        }

        String visiblePrice = chatSlotSummaryBuilder.summarizeVisiblePrice(context, userMessage, ChatLanguage.EN);
        if (!visiblePrice.isBlank()) {
            return "I cannot confirm from the current slot data that membership pricing has already been applied. "
                    + "From the currently published slots I can see here, " + visiblePrice;
        }

        return "I cannot confirm from the current slot data that membership pricing has already been applied.";
    }

    private String buildBookingInChatReply(ClubChatContextDto.VisibleTimeslot slot,
                                           ClubChatContextDto context,
                                           String userMessage) {
        boolean requiresUserLogin = context == null || context.viewer() == null || !context.viewer().isUserAccount();
        if (slot != null) {
            if (requiresUserLogin) {
                return "I cannot complete bookings in chat. The "
                        + formatTimeRange(slot) + " " + formatVenueName(slot)
                        + " slot is currently published at " + money(context, slot.price())
                        + ". You need to sign in with a user account before continuing to booking checkout.";
            }
            return "I cannot complete bookings in chat. The "
                    + formatTimeRange(slot) + " " + formatVenueName(slot)
                    + " slot is currently published at " + money(context, slot.price())
                    + ", and you can book it through the app's booking checkout flow.";
        }

        String summary = chatSlotSummaryBuilder.summarizeSlots(context, userMessage, ChatLanguage.EN, 3);
        if (!summary.isBlank()) {
            if (requiresUserLogin) {
                return "I cannot complete bookings in chat. From the currently published slots I can see here, "
                        + "the available options include: " + summary
                        + ". You need to sign in with a user account before continuing to booking checkout.";
            }
            return "I cannot complete bookings in chat. From the currently published slots I can see here, "
                    + "the available options include: " + summary
                    + ". Please use the app's booking checkout flow for the relevant published slot.";
        }

        if (requiresUserLogin) {
            return "I cannot complete bookings in chat. Please sign in with a user account and use the app's booking checkout flow for the relevant published slot.";
        }
        return "I cannot complete bookings in chat. Please use the app's booking checkout flow for the relevant published slot.";
    }

    private String buildClubBasicInfoReply(ClubChatContextDto context, String userMessage) {
        String normalized = normalize(userMessage);
        ClubChatContextDto.ClubInfo club = context == null ? null : context.club();
        List<String> replies = new ArrayList<>();

        boolean asksLocation = containsAny(normalized, "where", "located", "location", "address");
        boolean asksHours = containsAny(normalized, "opening hours", "hours", "open");
        boolean asksContact = containsAny(normalized, "contact", "email", "phone", "call");
        boolean asksBeginner = containsAny(normalized, "beginner");
        boolean asksCategory = containsAny(normalized, "category", "tags");
        boolean specificAsk = asksLocation || asksHours || asksContact || asksBeginner || asksCategory;

        if (asksLocation || !specificAsk) {
            if (club != null && !club.location().isBlank()) {
                replies.add("We are located at " + club.location() + ".");
            } else if (asksLocation) {
                replies.add("I cannot confirm the location from the current club data here.");
            }
        }

        if (asksHours || !specificAsk) {
            if (club != null && !club.openingStart().isBlank() && !club.openingEnd().isBlank()) {
                replies.add("Our listed opening hours are " + club.openingStart() + " to " + club.openingEnd() + ".");
            } else if (asksHours) {
                replies.add("I cannot confirm the opening hours from the current club data here.");
            }
        }

        if (asksContact || !specificAsk) {
            List<String> contacts = new ArrayList<>();
            if (club != null && !club.email().isBlank()) {
                contacts.add(club.email());
            }
            if (club != null && !club.phone().isBlank()) {
                contacts.add(club.phone());
            }
            if (!contacts.isEmpty()) {
                replies.add("You can reach us at " + String.join(" or ", contacts) + ".");
            } else if (asksContact) {
                replies.add("I cannot confirm the contact details from the current club data here.");
            }
        }

        if (asksBeginner) {
            if (isBeginnerFriendly(club)) {
                replies.add("We are described as beginner-friendly.");
            } else {
                replies.add("I cannot confirm from the current club data whether we are beginner-friendly.");
            }
        }

        if (asksCategory) {
            if (club != null && (!club.category().isBlank() || !club.tags().isEmpty())) {
                String category = club.category().isBlank() ? "club" : club.category();
                if (club.tags().isEmpty()) {
                    replies.add("We are listed under " + category + ".");
                } else {
                    replies.add("We are listed under " + category + " with tags: " + String.join(", ", club.tags()) + ".");
                }
            } else {
                replies.add("I cannot confirm the category details from the current club data here.");
            }
        }

        if (!replies.isEmpty()) {
            return String.join(" ", replies);
        }
        return "I cannot confirm that detail from the current club data here.";
    }

    private String buildVisibleSlotInfoReply(ClubChatContextDto.VisibleTimeslot matchedSlot,
                                             ClubChatContextDto context) {
        List<ClubChatContextDto.VisibleTimeslot> slots = context == null ? List.of() : context.visibleTimeslots();
        if (slots.isEmpty()) {
            return "From the currently published schedule, I cannot see any visible slots right now.";
        }

        if (matchedSlot != null) {
            return "From the currently published schedule, the "
                    + formatTimeRange(matchedSlot) + " " + formatVenueName(matchedSlot)
                    + " slot has " + matchedSlot.remaining() + " places remaining and is showing "
                    + money(context, matchedSlot.price()) + ".";
        }

        String summary = chatSlotSummaryBuilder.summarizeSlots(context, "", ChatLanguage.EN, 3);
        return "From the currently published schedule, I can see visible slots including: " + summary + ".";
    }

    private String buildVisibleSlotDiscoveryReply(ClubChatContextDto context, String userMessage) {
        List<ClubChatContextDto.VisibleTimeslot> slots = context == null ? List.of() : context.visibleTimeslots();
        if (slots.isEmpty()) {
            return "From the currently published schedule, I cannot see any visible slots right now.";
        }

        ChatSlotSummaryBuilder.DiscoverySelection discovery = chatSlotSummaryBuilder.inspectDiscovery(context, userMessage, 3);
        String summary = chatSlotSummaryBuilder.summarizeSlots(context, discovery.summarySlots(), ChatLanguage.EN);
        if (summary.isBlank()) {
            if (chatSlotSummaryBuilder.hasTimeOfDayHint(userMessage)) {
                return discoveryNoMatchReply(userMessage);
            }
            return "From the currently published schedule, I cannot see any visible slots right now.";
        }

        return discoveryLead(userMessage) + summary + ".";
    }

    private String buildMembershipPlanInfoReply(ClubChatContextDto context, String userMessage) {
        List<ClubChatContextDto.MembershipPlanInfo> plans = context == null ? List.of() : context.membershipPlans();
        if (plans.isEmpty()) {
            return "I cannot see any published membership plans for this club right now.";
        }

        ClubChatContextDto.MembershipPlanInfo matchedPlan = findMatchedPlan(plans, userMessage);
        if (matchedPlan != null) {
            if (isBookingPack(matchedPlan.benefitType())) {
                int includedBookings = Math.max(0, matchedPlan.includedBookings() == null ? 0 : matchedPlan.includedBookings());
                String reply = "The " + matchedPlan.planName() + " is listed at " + money(context, matchedPlan.price())
                        + " and includes " + includedBookings + " prepaid bookings valid for " + matchedPlan.durationDays() + " days.";
                if (!matchedPlan.description().isBlank()) {
                    reply += " " + matchedPlan.description();
                }
                return reply;
            }
            String reply = "The " + matchedPlan.planName() + " is listed at " + money(context, matchedPlan.price())
                    + " for " + matchedPlan.durationDays() + " days with a " + percent(matchedPlan.discountPercent()) + " discount.";
            if (!matchedPlan.description().isBlank()) {
                reply += " " + matchedPlan.description();
            }
            return reply;
        }

        List<String> summaries = plans.stream()
                .limit(3)
                .map(plan -> plan.planName() + ": " + money(context, plan.price())
                        + " for " + plan.durationDays() + " days with a " + percent(plan.discountPercent()) + " discount")
                .toList();
        return "The currently published membership plans are " + String.join("; ", summaries) + ".";
    }

    private ClubChatContextDto.MembershipPlanInfo findMatchedPlan(List<ClubChatContextDto.MembershipPlanInfo> plans, String userMessage) {
        String normalized = normalize(userMessage);
        return plans.stream()
                .filter(plan -> {
                    String planName = plan.planName().toLowerCase(Locale.ROOT);
                    String planCode = plan.planCode().toLowerCase(Locale.ROOT).replace('_', ' ');
                    return normalized.contains(planName) || (!planCode.isBlank() && normalized.contains(planCode));
                })
                .findFirst()
                .orElse(null);
    }

    private boolean isBeginnerFriendly(ClubChatContextDto.ClubInfo club) {
        if (club == null) {
            return false;
        }
        if (club.tags().stream().anyMatch(tag -> normalize(tag).contains("beginner"))) {
            return true;
        }
        return normalize(club.description()).contains("beginner");
    }

    private String discoveryLead(String userMessage) {
        String normalized = normalize(userMessage);
        boolean tomorrow = containsAny(normalized, "tomorrow");
        boolean today = containsAny(normalized, "today");
        boolean evening = containsAny(normalized, "evening", "tonight");
        boolean morning = containsAny(normalized, "morning");
        boolean afternoon = containsAny(normalized, "afternoon", "noon");

        if (tomorrow && evening) {
            return "From the currently published slots I can see here, the available options for tomorrow evening include: ";
        }
        if (tomorrow && afternoon) {
            return "From the currently published slots I can see here, the available options for tomorrow afternoon include: ";
        }
        if (tomorrow && morning) {
            return "From the currently published slots I can see here, the available options for tomorrow morning include: ";
        }
        if (tomorrow) {
            return "From the currently published slots I can see here, the available options for tomorrow include: ";
        }
        if (today) {
            return "From the currently published slots I can see here, the available options for today include: ";
        }
        if (afternoon) {
            return "From the currently published slots I can see here, the available options for the afternoon include: ";
        }
        if (evening) {
            return "From the currently published slots I can see here, the available options for the evening include: ";
        }
        if (morning) {
            return "From the currently published slots I can see here, the available options for the morning include: ";
        }
        return "From the currently published slots I can see here, the available options include: ";
    }

    private String discoveryNoMatchReply(String userMessage) {
        String normalized = normalize(userMessage);
        boolean tomorrow = containsAny(normalized, "tomorrow");
        boolean morning = containsAny(normalized, "morning");
        boolean afternoon = containsAny(normalized, "afternoon", "noon");
        boolean evening = containsAny(normalized, "evening", "tonight");

        if (tomorrow && evening) {
            return "From the currently published slots I can see here, I do not currently see any evening slots for tomorrow.";
        }
        if (tomorrow && afternoon) {
            return "From the currently published slots I can see here, I do not currently see any afternoon slots for tomorrow.";
        }
        if (tomorrow && morning) {
            return "From the currently published slots I can see here, I do not currently see any morning slots for tomorrow.";
        }
        if (evening) {
            return "From the currently published slots I can see here, I do not currently see any evening slots right now.";
        }
        if (afternoon) {
            return "From the currently published slots I can see here, I do not currently see any afternoon slots right now.";
        }
        if (morning) {
            return "From the currently published slots I can see here, I do not currently see any morning slots right now.";
        }
        return "From the currently published schedule, I cannot see any visible slots right now.";
    }

    private String refundReply() {
        return "This type of issue needs help from club staff. Please switch to human chat or contact the club directly.";
    }

    private String missingPolicyReply() {
        return "I cannot confirm that detail from the current club data here. Please contact the club directly for that information.";
    }

    private String humanHandoffReply() {
        return "Of course. Please switch to human chat if you would like a staff member to continue helping you.";
    }

    private String formatTimeRange(ClubChatContextDto.VisibleTimeslot slot) {
        return formatTime(slot.startTime()) + " to " + formatTime(slot.endTime());
    }

    private String formatTime(LocalDateTime value) {
        if (value == null) {
            return "time unavailable";
        }
        return value.format(EN_TIME_FORMATTER).toLowerCase(Locale.UK);
    }

    private String formatVenueName(ClubChatContextDto.VisibleTimeslot slot) {
        if (slot == null || slot.venueName().isBlank()) {
            return "slot";
        }
        return slot.venueName();
    }

    private String money(ClubChatContextDto context, BigDecimal amount) {
        String currency = context == null || context.policy() == null || context.policy().currency().isBlank()
                ? "GBP"
                : context.policy().currency();
        BigDecimal normalized = amount == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : amount.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return currency + " " + normalized.toPlainString();
    }

    private String percent(BigDecimal amount) {
        BigDecimal normalized = amount == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : amount.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return normalized.toPlainString() + "%";
    }

    private static boolean containsAny(String text, String... values) {
        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBookingPack(String benefitType) {
        return "booking_pack".equals(normalize(benefitType));
    }
}
