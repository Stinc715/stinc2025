package com.clubportal.service;

import com.clubportal.dto.ClubChatContextDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ChatIntentRouter {

    private static final Logger log = LoggerFactory.getLogger(ChatIntentRouter.class);

    private final ChatSlotMatcher chatSlotMatcher;
    private final ChatLanguageDetector chatLanguageDetector;

    public ChatIntentRouter() {
        this(new ChatSlotMatcher(), new ChatLanguageDetector());
    }

    public ChatIntentRouter(ChatSlotMatcher chatSlotMatcher) {
        this(chatSlotMatcher, new ChatLanguageDetector());
    }

    public ChatIntentRouter(ChatSlotMatcher chatSlotMatcher, ChatLanguageDetector chatLanguageDetector) {
        this.chatSlotMatcher = chatSlotMatcher;
        this.chatLanguageDetector = chatLanguageDetector;
    }

    public ChatIntentRoute route(String userMessage, ClubChatContextDto context) {
        String normalized = normalize(userMessage);
        ChatLanguage language = chatLanguageDetector.detect(userMessage);
        ClubChatContextDto.VisibleTimeslot relevantVisibleSlot = chatSlotMatcher.findRelevantVisibleSlot(userMessage, context);

        log.info("[CLUB_CHAT_DEBUG] language detected: {}, message=\"{}\"",
                language,
                safe(userMessage));

        if (containsAny(normalized,
                "speak to a human",
                "speak with a human",
                "real person",
                "human chat",
                "staff member",
                "真人",
                "人工",
                "人工客服",
                "真人聊天",
                "工作人员")) {
            return routed(ChatIntentType.HUMAN_HANDOFF, null, userMessage);
        }

        if (containsAny(normalized,
                "parking policy",
                "refund policy",
                "cancellation policy",
                "special policy",
                "policy",
                "parking",
                "dress code",
                "政策",
                "停车",
                "规定")) {
            return routed(ChatIntentType.MISSING_POLICY, null, userMessage);
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
                "dispute",
                "退款",
                "支付失败",
                "扣费",
                "账单",
                "支付问题")) {
            return routed(ChatIntentType.REFUND_OR_PAYMENT_ISSUE, null, userMessage);
        }

        if (isVisibleSlotDiscoveryRequest(normalized, context, relevantVisibleSlot)) {
            return routed(ChatIntentType.VISIBLE_SLOT_DISCOVERY, relevantVisibleSlot, userMessage);
        }

        if (containsAny(normalized,
                "book",
                "reserve",
                "sign me up",
                "complete booking",
                "book this slot",
                "book it for me",
                "reserve it for me",
                "预约",
                "预订",
                "帮我订",
                "帮我预订")) {
            return routed(ChatIntentType.BOOKING_IN_CHAT, chatSlotMatcher.findBookingSlot(userMessage, context), userMessage);
        }

        ClubChatContextDto.VisibleTimeslot memberPriceSlot = chatSlotMatcher.findMemberPriceSlot(userMessage, context);
        if (isMemberPriceExplanation(normalized, memberPriceSlot)) {
            return routed(ChatIntentType.MEMBER_PRICE_EXPLANATION, memberPriceSlot, userMessage);
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
                "discount percent",
                "会员",
                "套餐",
                "月卡",
                "年卡",
                "折扣")) {
            return routed(ChatIntentType.MEMBERSHIP_PLAN_INFO, null, userMessage);
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
                "tags",
                "地址",
                "营业时间",
                "联系方式",
                "电话",
                "邮箱",
                "新手")) {
            return routed(ChatIntentType.CLUB_BASIC_INFO, null, userMessage);
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
                "what time",
                "时段",
                "场地",
                "可用",
                "还有位置")) {
            return routed(ChatIntentType.VISIBLE_SLOT_INFO, relevantVisibleSlot, userMessage);
        }

        log.info("[CLUB_CHAT_DEBUG] generic fallback chosen: reason=no intent keywords matched, message=\"{}\"",
                safe(userMessage));
        return routed(ChatIntentType.FALLBACK, null, userMessage);
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
                "why is it",
                "会员价",
                "会员价格",
                "折扣",
                "已经是我的会员价");
        if (directMemberPriceQuestion) {
            return true;
        }
        if (!containsAny(normalized, "discount", "折扣", "优惠")) {
            return false;
        }
        return matchedTimeslot != null || containsAny(normalized, "slot", "court", "price", "gbp", "时段", "场地", "价格");
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
                "tonight",
                "明天",
                "今天",
                "晚上",
                "下午",
                "上午",
                "场地",
                "场次",
                "时段",
                "空位",
                "还有位置",
                "预定",
                "预约",
                "可订",
                "可预订",
                "有没有",
                "有什么时段");
        if (!asksDiscovery) {
            return false;
        }

        boolean directBookingAction = containsAny(normalized,
                "book this slot",
                "complete booking",
                "for me here",
                "book it for me",
                "reserve it for me",
                "sign me up",
                "帮我直接订",
                "帮我预约这个");
        if (directBookingAction) {
            return false;
        }

        boolean genericBookingSearch = containsAny(normalized,
                "book",
                "reserve",
                "booking",
                "订",
                "预定",
                "预约",
                "预订");
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

    private ChatIntentRoute routed(ChatIntentType intentType,
                                   ClubChatContextDto.VisibleTimeslot matchedTimeslot,
                                   String userMessage) {
        log.info("[CLUB_CHAT_DEBUG] intent routed: intent={}, message=\"{}\"",
                intentType,
                safe(userMessage));
        return new ChatIntentRoute(intentType, matchedTimeslot);
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace("\"", "\\\"").trim();
    }
}
