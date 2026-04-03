package com.clubportal.service;

import com.clubportal.dto.ClubChatContextDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ChatResponseBuilder.class);
    private static final DateTimeFormatter EN_TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.UK);
    private static final DateTimeFormatter ZH_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("mm", Locale.CHINA);

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
        ChatLanguage language = chatLanguageDetector.detect(userMessage);
        return switch (intentType) {
            case MEMBER_PRICE_EXPLANATION -> buildMemberPriceReply(route == null ? null : route.matchedTimeslot(), context, userMessage, language);
            case BOOKING_IN_CHAT -> buildBookingInChatReply(route == null ? null : route.matchedTimeslot(), context, userMessage, language);
            case REFUND_OR_PAYMENT_ISSUE -> refundReply(language);
            case MISSING_POLICY -> missingPolicyReply(language);
            case HUMAN_HANDOFF -> humanHandoffReply(language);
            case CLUB_BASIC_INFO -> buildClubBasicInfoReply(context, userMessage, language);
            case VISIBLE_SLOT_INFO -> buildVisibleSlotInfoReply(route == null ? null : route.matchedTimeslot(), context, language);
            case VISIBLE_SLOT_DISCOVERY -> buildVisibleSlotDiscoveryReply(context, userMessage, language);
            case MEMBERSHIP_PLAN_INFO -> buildMembershipPlanInfoReply(context, userMessage, language);
            case FALLBACK -> fallbackReply(language);
        };
    }

    public String fallbackReply() {
        return fallbackReply(ChatLanguage.EN);
    }

    private String buildMemberPriceReply(ClubChatContextDto.VisibleTimeslot slot,
                                         ClubChatContextDto context,
                                         String userMessage,
                                         ChatLanguage language) {
        log.info("[CLUB_CHAT_DEBUG] member-price reply slot: {}",
                slot == null ? "none" : summarizeSlot(slot));
        if (slot != null && slot.membershipApplied() && slot.price() != null && slot.basePrice() != null) {
            if (isBookingPack(slot.membershipBenefitType())) {
                int remainingCredits = Math.max(0, slot.membershipRemainingBookings() == null ? 0 : slot.membershipRemainingBookings());
                if (language == ChatLanguage.ZH) {
                    return "是的。这个时段包含在你的会员次卡权益内。原价是 " + money(context, slot.basePrice())
                            + "，预订会使用 1 次额度，你当前还剩 " + remainingCredits + " 次。";
                }
                return "Yes. This slot is included with your membership booking pack. The standard price is "
                        + money(context, slot.basePrice()) + ", and booking it uses 1 credit. You currently have "
                        + remainingCredits + " credits left.";
            }
            if (language == ChatLanguage.ZH) {
                return "是的，当前显示的 " + money(context, slot.price())
                        + " 已经包含你的会员折扣。原价是 " + money(context, slot.basePrice()) + "。";
            }
            return "Yes. The displayed " + money(context, slot.price())
                    + " price already reflects your membership discount. The base price is "
                    + money(context, slot.basePrice()) + ".";
        }

        String visiblePrice = chatSlotSummaryBuilder.summarizeVisiblePrice(context, userMessage, language);
        if (!visiblePrice.isBlank()) {
            if (language == ChatLanguage.ZH) {
                return "我目前无法从当前时段数据中确认会员价格是否已经应用。根据我当前能看到的已发布时段，" + visiblePrice;
            }
            return "I cannot confirm from the current slot data that membership pricing has already been applied. "
                    + "From the currently published slots I can see here, " + visiblePrice;
        }

        if (language == ChatLanguage.ZH) {
            return "我目前无法从当前时段数据中确认会员价格是否已经应用。";
        }
        return "I cannot confirm from the current slot data that membership pricing has already been applied.";
    }

    private String buildBookingInChatReply(ClubChatContextDto.VisibleTimeslot slot,
                                           ClubChatContextDto context,
                                           String userMessage,
                                           ChatLanguage language) {
        log.info("[CLUB_CHAT_DEBUG] booking reply slot: {}",
                slot == null ? "none" : summarizeSlot(slot));
        boolean requiresUserLogin = context == null || context.viewer() == null || !context.viewer().isUserAccount();
        if (slot != null) {
            if (language == ChatLanguage.ZH) {
                if (requiresUserLogin) {
                    return "我不能在聊天中直接替你完成预约。"
                            + formatVenueName(slot) + " " + formatTimeRange(slot, language)
                            + " 这个已发布时段当前显示价格为 " + money(context, slot.price())
                            + "。你需要先使用用户账号登录，然后才能继续通过应用内的预约结算流程预订。";
                }
                return "我不能在聊天中直接替你完成预约。"
                        + formatVenueName(slot) + " " + formatTimeRange(slot, language)
                        + " 这个已发布时段当前显示价格为 " + money(context, slot.price())
                        + "。请通过应用内的预约结算流程完成预订。";
            }
            if (requiresUserLogin) {
                return "I cannot complete bookings in chat. The "
                        + formatTimeRange(slot, language) + " " + formatVenueName(slot)
                        + " slot is currently published at " + money(context, slot.price())
                        + ". You need to sign in with a user account before continuing to booking checkout.";
            }
            return "I cannot complete bookings in chat. The "
                    + formatTimeRange(slot, language) + " " + formatVenueName(slot)
                    + " slot is currently published at " + money(context, slot.price())
                    + ", and you can book it through the app's booking checkout flow.";
        }

        String summary = chatSlotSummaryBuilder.summarizeSlots(context, userMessage, language, 3);
        if (!summary.isBlank()) {
            if (language == ChatLanguage.ZH) {
                if (requiresUserLogin) {
                    return "我不能在聊天中直接替你完成预约。根据我当前能看到的已发布时段，可选时段包括："
                            + summary
                            + "。请先使用用户账号登录，再通过应用内的预约结算流程预订相关时段。";
                }
                return "我不能在聊天中直接替你完成预约。根据我当前能看到的已发布时段，可选时段包括："
                        + summary
                        + "。请通过应用内的预约结算流程预订相关的已发布时段。";
            }
            if (requiresUserLogin) {
                return "I cannot complete bookings in chat. From the currently published slots I can see here, "
                        + "the available options include: " + summary
                        + ". You need to sign in with a user account before continuing to booking checkout.";
            }
            return "I cannot complete bookings in chat. From the currently published slots I can see here, "
                    + "the available options include: " + summary
                    + ". Please use the app's booking checkout flow for the relevant published slot.";
        }

        if (language == ChatLanguage.ZH) {
            if (requiresUserLogin) {
                return "我不能在聊天中直接替你完成预约。请先使用用户账号登录，再通过应用内的预约结算流程预订相关的已发布时段。";
            }
            return "我不能在聊天中直接替你完成预约。请通过应用内的预约结算流程预订相关的已发布时段。";
        }
        if (requiresUserLogin) {
            return "I cannot complete bookings in chat. Please sign in with a user account and use the app's booking checkout flow for the relevant published slot.";
        }
        return "I cannot complete bookings in chat. Please use the app's booking checkout flow for the relevant published slot.";
    }

    private String buildClubBasicInfoReply(ClubChatContextDto context, String userMessage, ChatLanguage language) {
        String normalized = normalize(userMessage);
        ClubChatContextDto.ClubInfo club = context == null ? null : context.club();
        List<String> replies = new ArrayList<>();

        boolean asksLocation = containsAny(normalized, "where", "located", "location", "address", "地址", "位置");
        boolean asksHours = containsAny(normalized, "opening hours", "hours", "open", "营业时间", "开放时间");
        boolean asksContact = containsAny(normalized, "contact", "email", "phone", "call", "联系方式", "电话", "邮箱");
        boolean asksBeginner = containsAny(normalized, "beginner", "新手");
        boolean asksCategory = containsAny(normalized, "category", "tags", "分类", "标签");
        boolean specificAsk = asksLocation || asksHours || asksContact || asksBeginner || asksCategory;

        if (asksLocation || !specificAsk) {
            if (club != null && !club.location().isBlank()) {
                replies.add(language == ChatLanguage.ZH
                        ? "我们的位置是 " + club.location() + "。"
                        : "We are located at " + club.location() + ".");
            } else if (asksLocation) {
                replies.add(language == ChatLanguage.ZH
                        ? "我暂时无法从当前俱乐部数据中确认位置。"
                        : "I cannot confirm the location from the current club data here.");
            }
        }

        if (asksHours || !specificAsk) {
            if (club != null && !club.openingStart().isBlank() && !club.openingEnd().isBlank()) {
                replies.add(language == ChatLanguage.ZH
                        ? "当前显示的营业时间是 " + club.openingStart() + " 到 " + club.openingEnd() + "。"
                        : "Our listed opening hours are " + club.openingStart() + " to " + club.openingEnd() + ".");
            } else if (asksHours) {
                replies.add(language == ChatLanguage.ZH
                        ? "我暂时无法从当前俱乐部数据中确认营业时间。"
                        : "I cannot confirm the opening hours from the current club data here.");
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
                replies.add(language == ChatLanguage.ZH
                        ? "你可以通过 " + String.join(" 或 ", contacts) + " 联系我们。"
                        : "You can reach us at " + String.join(" or ", contacts) + ".");
            } else if (asksContact) {
                replies.add(language == ChatLanguage.ZH
                        ? "我暂时无法从当前俱乐部数据中确认联系方式。"
                        : "I cannot confirm the contact details from the current club data here.");
            }
        }

        if (asksBeginner) {
            if (isBeginnerFriendly(club)) {
                replies.add(language == ChatLanguage.ZH
                        ? "当前资料显示我们对新手比较友好。"
                        : "We are described as beginner-friendly.");
            } else {
                replies.add(language == ChatLanguage.ZH
                        ? "我暂时无法从当前俱乐部数据中确认是否适合新手。"
                        : "I cannot confirm from the current club data whether we are beginner-friendly.");
            }
        }

        if (asksCategory) {
            if (club != null && (!club.category().isBlank() || !club.tags().isEmpty())) {
                String category = club.category().isBlank() ? (language == ChatLanguage.ZH ? "俱乐部" : "club") : club.category();
                if (club.tags().isEmpty()) {
                    replies.add(language == ChatLanguage.ZH
                            ? "当前分类是 " + category + "。"
                            : "We are listed under " + category + ".");
                } else {
                    replies.add(language == ChatLanguage.ZH
                            ? "当前分类是 " + category + "，标签包括：" + String.join("、", club.tags()) + "。"
                            : "We are listed under " + category + " with tags: " + String.join(", ", club.tags()) + ".");
                }
            } else {
                replies.add(language == ChatLanguage.ZH
                        ? "我暂时无法从当前俱乐部数据中确认分类信息。"
                        : "I cannot confirm the category details from the current club data here.");
            }
        }

        if (!replies.isEmpty()) {
            return String.join(" ", replies);
        }
        return language == ChatLanguage.ZH
                ? "我暂时无法从当前俱乐部数据中确认这一信息。"
                : "I cannot confirm that detail from the current club data here.";
    }

    private String buildVisibleSlotInfoReply(ClubChatContextDto.VisibleTimeslot matchedSlot,
                                             ClubChatContextDto context,
                                             ChatLanguage language) {
        List<ClubChatContextDto.VisibleTimeslot> slots = context == null ? List.of() : context.visibleTimeslots();
        if (slots.isEmpty()) {
            return language == ChatLanguage.ZH
                    ? "根据我当前能看到的已发布时段，这里暂时没有可见时段。"
                    : "From the currently published schedule, I cannot see any visible slots right now.";
        }

        if (matchedSlot != null) {
            if (language == ChatLanguage.ZH) {
                return "根据我当前能看到的已发布时段，"
                        + formatVenueName(matchedSlot) + " " + formatTimeRange(matchedSlot, language)
                        + " 这个时段还有 " + matchedSlot.remaining() + " 个名额，当前显示价格为 "
                        + money(context, matchedSlot.price()) + "。";
            }
            return "From the currently published schedule, the "
                    + formatTimeRange(matchedSlot, language) + " " + formatVenueName(matchedSlot)
                    + " slot has " + matchedSlot.remaining() + " places remaining and is showing "
                    + money(context, matchedSlot.price()) + ".";
        }

        String summary = chatSlotSummaryBuilder.summarizeSlots(context, "", language, 3);
        if (language == ChatLanguage.ZH) {
            return "根据我当前能看到的已发布时段，可见时段包括：" + summary + "。";
        }
        return "From the currently published schedule, I can see visible slots including: " + summary + ".";
    }

    private String buildVisibleSlotDiscoveryReply(ClubChatContextDto context,
                                                  String userMessage,
                                                  ChatLanguage language) {
        List<ClubChatContextDto.VisibleTimeslot> slots = context == null ? List.of() : context.visibleTimeslots();
        if (slots.isEmpty()) {
            return language == ChatLanguage.ZH
                    ? "根据我当前能看到的已发布时段，这里暂时没有可见时段。"
                    : "From the currently published schedule, I cannot see any visible slots right now.";
        }

        ChatSlotSummaryBuilder.DiscoverySelection discovery = chatSlotSummaryBuilder.inspectDiscovery(context, userMessage, 3);
        log.info("[CLUB_CHAT_DEBUG] SLOT_DISCOVERY request: language={}, tomorrow={}, timeOfDay={}, message=\"{}\"",
                language, discovery.tomorrow(), discovery.timeOfDay(), userMessage);
        log.info("[CLUB_CHAT_DEBUG] SLOT_DISCOVERY afterDateFilter: count={}", discovery.afterDateFilter().size());
        logDiscoverySlots("afterDateFilter", discovery.afterDateFilter());
        log.info("[CLUB_CHAT_DEBUG] SLOT_DISCOVERY afterTimeOfDayFilter: timeOfDay={}, count={}",
                discovery.timeOfDay(), discovery.afterTimeOfDayFilter().size());
        logDiscoverySlots("afterTimeOfDayFilter", discovery.afterTimeOfDayFilter());
        log.info("[CLUB_CHAT_DEBUG] SLOT_DISCOVERY summarySlots: count={}", discovery.summarySlots().size());
        logDiscoverySlots("summarySlots", discovery.summarySlots());
        if (discovery.fallbackReason() != null) {
            log.info("[CLUB_CHAT_DEBUG] SLOT_DISCOVERY fallbackUsed: reason={}", discovery.fallbackReason());
        }

        String summary = chatSlotSummaryBuilder.summarizeSlots(context, discovery.summarySlots(), language);
        if (summary.isBlank()) {
            if (chatSlotSummaryBuilder.hasTimeOfDayHint(userMessage)) {
                return discoveryNoMatchReply(userMessage, language);
            }
            return language == ChatLanguage.ZH
                    ? "根据我当前能看到的已发布时段，这里暂时没有可见时段。"
                    : "From the currently published schedule, I cannot see any visible slots right now.";
        }

        String lead = discoveryLead(userMessage, language);
        return lead + summary + (language == ChatLanguage.ZH ? "。" : ".");
    }

    private String buildMembershipPlanInfoReply(ClubChatContextDto context, String userMessage, ChatLanguage language) {
        List<ClubChatContextDto.MembershipPlanInfo> plans = context == null ? List.of() : context.membershipPlans();
        if (plans.isEmpty()) {
            return language == ChatLanguage.ZH
                    ? "我当前看不到这个俱乐部已发布的会员方案。"
                    : "I cannot see any published membership plans for this club right now.";
        }

        ClubChatContextDto.MembershipPlanInfo matchedPlan = findMatchedPlan(plans, userMessage);
        if (matchedPlan != null) {
            if (isBookingPack(matchedPlan.benefitType())) {
                int includedBookings = Math.max(0, matchedPlan.includedBookings() == null ? 0 : matchedPlan.includedBookings());
                if (language == ChatLanguage.ZH) {
                    String reply = matchedPlan.planName() + " 当前价格是 " + money(context, matchedPlan.price())
                            + "，包含 " + includedBookings + " 次预付预订额度，有效期 " + matchedPlan.durationDays() + " 天。";
                    if (!matchedPlan.description().isBlank()) {
                        reply += " " + matchedPlan.description();
                    }
                    return reply;
                }
                String reply = "The " + matchedPlan.planName() + " is listed at " + money(context, matchedPlan.price())
                        + " and includes " + includedBookings + " prepaid bookings valid for " + matchedPlan.durationDays() + " days.";
                if (!matchedPlan.description().isBlank()) {
                    reply += " " + matchedPlan.description();
                }
                return reply;
            }
            if (language == ChatLanguage.ZH) {
                String reply = matchedPlan.planName() + " 当前价格是 " + money(context, matchedPlan.price())
                        + "，时长 " + matchedPlan.durationDays() + " 天，折扣为 " + percent(matchedPlan.discountPercent()) + "。";
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
                .map(plan -> language == ChatLanguage.ZH
                        ? plan.planName() + "：" + money(context, plan.price()) + "，" + plan.durationDays()
                        + " 天，折扣 " + percent(plan.discountPercent())
                        : plan.planName() + ": " + money(context, plan.price())
                        + " for " + plan.durationDays() + " days with a " + percent(plan.discountPercent()) + " discount")
                .toList();
        if (language == ChatLanguage.ZH) {
            return "当前已发布的会员方案包括：" + String.join("；", summaries) + "。";
        }
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
        if (club.tags().stream().anyMatch(tag -> normalize(tag).contains("beginner") || tag.contains("新手"))) {
            return true;
        }
        return normalize(club.description()).contains("beginner") || club.description().contains("新手");
    }

    private String discoveryLead(String userMessage, ChatLanguage language) {
        String normalized = normalize(userMessage);
        boolean tomorrow = containsAny(normalized, "tomorrow", "明天");
        boolean today = containsAny(normalized, "today", "今天");
        boolean evening = containsAny(normalized, "evening", "tonight", "晚上", "傍晚");
        boolean morning = containsAny(normalized, "morning", "上午", "早上");
        boolean afternoon = containsAny(normalized, "afternoon", "noon", "下午", "中午");

        if (language == ChatLanguage.ZH) {
            if (tomorrow && evening) {
                return "根据我当前能看到的已发布时段，明天晚上的可选时段包括：";
            }
            if (tomorrow && afternoon) {
                return "根据我当前能看到的已发布时段，明天下午的可选时段包括：";
            }
            if (tomorrow && morning) {
                return "根据我当前能看到的已发布时段，明天上午的可选时段包括：";
            }
            if (tomorrow) {
                return "根据我当前能看到的已发布时段，明天的可选时段包括：";
            }
            if (today) {
                return "根据我当前能看到的已发布时段，今天的可选时段包括：";
            }
            if (afternoon) {
                return "根据我当前能看到的已发布时段，下午的可选时段包括：";
            }
            if (evening) {
                return "根据我当前能看到的已发布时段，晚上的可选时段包括：";
            }
            if (morning) {
                return "根据我当前能看到的已发布时段，上午的可选时段包括：";
            }
            return "根据我当前能看到的已发布时段，可选时段包括：";
        }

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

    private String discoveryNoMatchReply(String userMessage, ChatLanguage language) {
        String normalized = normalize(userMessage);
        boolean tomorrow = containsAny(normalized, "tomorrow", "明天");
        boolean morning = containsAny(normalized, "morning", "上午", "早上");
        boolean afternoon = containsAny(normalized, "afternoon", "noon", "下午", "中午");
        boolean evening = containsAny(normalized, "evening", "tonight", "晚上", "傍晚");

        if (language == ChatLanguage.ZH) {
            if (tomorrow && evening) {
                return "根据我当前能看到的已发布时段，我暂时没有看到明天晚上的可选时段。";
            }
            if (tomorrow && afternoon) {
                return "根据我当前能看到的已发布时段，我暂时没有看到明天下午的可选时段。";
            }
            if (tomorrow && morning) {
                return "根据我当前能看到的已发布时段，我暂时没有看到明天上午的可选时段。";
            }
            if (evening) {
                return "根据我当前能看到的已发布时段，我暂时没有看到晚上的可选时段。";
            }
            if (afternoon) {
                return "根据我当前能看到的已发布时段，我暂时没有看到下午的可选时段。";
            }
            if (morning) {
                return "根据我当前能看到的已发布时段，我暂时没有看到上午的可选时段。";
            }
        } else {
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
        }

        return language == ChatLanguage.ZH
                ? "根据我当前能看到的已发布时段，这里暂时没有可见时段。"
                : "From the currently published schedule, I cannot see any visible slots right now.";
    }

    private String refundReply(ChatLanguage language) {
        return language == ChatLanguage.ZH
                ? "这类问题需要俱乐部工作人员进一步协助。请切换到真人聊天，或直接联系俱乐部。"
                : "This type of issue needs help from club staff. Please switch to human chat or contact the club directly.";
    }

    private String missingPolicyReply(ChatLanguage language) {
        return language == ChatLanguage.ZH
                ? "我暂时无法从当前俱乐部数据中确认这一信息。请直接联系俱乐部工作人员获取详情。"
                : "I cannot confirm that detail from the current club data here. Please contact the club directly for that information.";
    }

    private String humanHandoffReply(ChatLanguage language) {
        return language == ChatLanguage.ZH
                ? "当然可以。如果你希望由工作人员继续协助，请切换到真人聊天。"
                : "Of course. Please switch to human chat if you would like a staff member to continue helping you.";
    }

    private String fallbackReply(ChatLanguage language) {
        return language == ChatLanguage.ZH
                ? "我可以帮助你查看俱乐部信息、可见时段、会员方案和预约指引。如果你需要工作人员协助，请切换到真人聊天。"
                : "I can help with club details, visible slots, membership plans, and booking guidance. If you need a staff member, please switch to human chat.";
    }

    private String formatTimeRange(ClubChatContextDto.VisibleTimeslot slot, ChatLanguage language) {
        if (language == ChatLanguage.ZH) {
            return formatTime(slot.startTime(), language) + "到" + formatTime(slot.endTime(), language);
        }
        return formatTime(slot.startTime(), language) + " to " + formatTime(slot.endTime(), language);
    }

    private String formatTime(LocalDateTime value, ChatLanguage language) {
        if (value == null) {
            return language == ChatLanguage.ZH ? "时间未提供" : "time unavailable";
        }
        if (language == ChatLanguage.ZH) {
            int hour = value.getHour();
            String prefix;
            if (hour < 6) {
                prefix = "凌晨";
            } else if (hour < 12) {
                prefix = "上午";
            } else if (hour < 18) {
                prefix = "下午";
            } else {
                prefix = "晚上";
            }
            int displayHour = hour % 12 == 0 ? 12 : hour % 12;
            return prefix + displayHour + ":" + value.format(ZH_MINUTE_FORMATTER);
        }
        return value.format(EN_TIME_FORMATTER).toLowerCase(Locale.UK);
    }

    private String formatVenueName(ClubChatContextDto.VisibleTimeslot slot) {
        return slot == null || slot.venueName().isBlank() ? "slot" : slot.venueName();
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

    private String summarizeSlot(ClubChatContextDto.VisibleTimeslot slot) {
        return "venueName=" + slot.venueName()
                + ", startTime=" + slot.startTime()
                + ", endTime=" + slot.endTime()
                + ", price=" + slot.price()
                + ", basePrice=" + slot.basePrice()
                + ", membershipApplied=" + slot.membershipApplied();
    }

    private void logDiscoverySlots(String label, List<ClubChatContextDto.VisibleTimeslot> slots) {
        for (int i = 0; i < slots.size(); i++) {
            ClubChatContextDto.VisibleTimeslot slot = slots.get(i);
            log.info("[CLUB_CHAT_DEBUG] SLOT_DISCOVERY {}[{}]: venue={}, start={}, end={}, remaining={}, price={}, basePrice={}, membershipApplied={}, membershipPlanName={}, membershipDiscountPercent={}",
                    label,
                    i,
                    slot.venueName(),
                    slot.startTime(),
                    slot.endTime(),
                    slot.remaining(),
                    slot.price(),
                    slot.basePrice(),
                    slot.membershipApplied(),
                    slot.membershipPlanName(),
                    slot.membershipDiscountPercent());
        }
    }
}
