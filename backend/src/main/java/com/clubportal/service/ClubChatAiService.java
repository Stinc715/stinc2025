package com.clubportal.service;

import com.clubportal.dto.ClubChatContextDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClubChatAiService {

    private static final Logger log = LoggerFactory.getLogger(ClubChatAiService.class);

    private final ClubChatContextService clubChatContextService;
    private final ChatIntentRouter chatIntentRouter;
    private final ChatResponseBuilder chatResponseBuilder;
    private final ClubOpenAiReplyService clubOpenAiReplyService;
    private final ClubChatKbMatcherService clubChatKbMatcherService;
    private final ClubChatKbSafetyGuard clubChatKbSafetyGuard;
    private final ClubChatKbResponseGuard clubChatKbResponseGuard;

    public ClubChatAiService(ClubChatContextService clubChatContextService,
                             ChatIntentRouter chatIntentRouter,
                             ChatResponseBuilder chatResponseBuilder,
                             ClubOpenAiReplyService clubOpenAiReplyService,
                             ClubChatKbMatcherService clubChatKbMatcherService,
                             ClubChatKbSafetyGuard clubChatKbSafetyGuard,
                             ClubChatKbResponseGuard clubChatKbResponseGuard) {
        this.clubChatContextService = clubChatContextService;
        this.chatIntentRouter = chatIntentRouter;
        this.chatResponseBuilder = chatResponseBuilder;
        this.clubOpenAiReplyService = clubOpenAiReplyService;
        this.clubChatKbMatcherService = clubChatKbMatcherService;
        this.clubChatKbSafetyGuard = clubChatKbSafetyGuard;
        this.clubChatKbResponseGuard = clubChatKbResponseGuard;
    }

    public String buildReply(Integer clubId, Integer userId, String userMessage) {
        return buildReplyDecision(clubId, userId, userMessage).replyText();
    }

    public ClubChatAiReplyDecision buildReplyDecision(Integer clubId, Integer userId, String userMessage) {
        try {
            ClubChatContextDto context = clubChatContextService.buildContext(clubId, userId);
            ChatIntentRoute route = chatIntentRouter.route(userMessage, context);
            ClubChatKbMatchResult kbMatchResult = clubChatKbMatcherService.matchClubFaq(clubId, userMessage);
            ClubChatKbSafetyGuard.Decision safePathDecision = clubChatKbSafetyGuard.evaluate(userMessage, route).orElse(null);

            log.info("[CLUB_CHAT_DEBUG] FAQ semantic result: clubId={}, userId={}, hit={}, matchedEntryId={}, bestScore={}, secondBestScore={}, rejectReason={}",
                    clubId,
                    userId,
                    kbMatchResult.hit(),
                    kbMatchResult.matchedEntryId(),
                    scoreText(kbMatchResult.bestScore()),
                    scoreText(kbMatchResult.secondBestScore()),
                    kbMatchResult.rejectReason());

            if (kbMatchResult.hit()) {
                ClubChatKbResponseGuard.Decision thirdLayerDecision = clubChatKbResponseGuard.evaluate(userMessage, kbMatchResult);
                if (!thirdLayerDecision.allow()) {
                    log.info("[CLUB_CHAT_DEBUG] FAQ third-layer reject: clubId={}, userId={}, matchedEntryId={}, bestScore={}, secondBestScore={}, reason={}, detail={}",
                            clubId,
                            userId,
                            kbMatchResult.matchedEntryId(),
                            scoreText(kbMatchResult.bestScore()),
                            scoreText(kbMatchResult.secondBestScore()),
                            thirdLayerDecision.rejectReason(),
                            safe(thirdLayerDecision.detail()));
                } else if (safePathDecision != null) {
                    log.info("[CLUB_CHAT_DEBUG] FAQ bypass existing safe-path: clubId={}, userId={}, matchedEntryId={}, intent={}, reason={}",
                            clubId,
                            userId,
                            kbMatchResult.matchedEntryId(),
                            route == null ? ChatIntentType.FALLBACK : route.intentType(),
                            safePathDecision.reason());
                } else {
                    ClubChatAiReplyDecision decision = ClubChatAiReplyDecision.clubFaq(
                            kbMatchResult.matchedReply(),
                            kbMatchResult.matchedEntryId(),
                            kbMatchResult.matchedQuestion(),
                            kbMatchResult.bestScore(),
                            kbMatchResult.secondBestScore()
                    );
                    logFinalDecision(clubId, userId, route, decision, "direct-faq");
                    return decision;
                }
            }

            if (safePathDecision != null) {
                log.info("[CLUB_CHAT_DEBUG] KB_MATCH bypass: clubId={}, userId={}, reason={}", clubId, userId, safePathDecision.reason());
            }

            String answerSkeleton = chatResponseBuilder.buildReply(route, context, userMessage);
            boolean allowOpenAiRewrite = route.intentType().isLowRisk()
                    && (safePathDecision == null || !safePathDecision.forceDeterministic());
            if (allowOpenAiRewrite) {
                String rewrittenReply = clubOpenAiReplyService.rewriteReply(route.intentType(), answerSkeleton, userMessage);
                String answerSource = rewrittenReply.equals(answerSkeleton) ? "CLUB_CHAT_FALLBACK" : "CLUB_CHAT_OPENAI";
                ClubChatAiReplyDecision decision = ClubChatAiReplyDecision.standard(
                        rewrittenReply,
                        answerSource,
                        shouldSuggestHandoff(route, safePathDecision)
                );
                logFinalDecision(clubId, userId, route, decision, rewrittenReply.equals(answerSkeleton) ? "fallback" : "gpt-polished");
                return decision;
            }
            String answerSource = safePathDecision != null ? "CLUB_CHAT_SAFE_PATH" : "CLUB_CHAT_DETERMINISTIC";
            ClubChatAiReplyDecision decision = ClubChatAiReplyDecision.standard(
                    answerSkeleton,
                    answerSource,
                    shouldSuggestHandoff(route, safePathDecision)
            );
            logFinalDecision(clubId, userId, route, decision, "deterministic");
            return decision;
        } catch (Exception ex) {
            log.warn("Club chat AI fallback for club {} user {}: {} - {}", clubId, userId, ex.getClass().getSimpleName(), ex.getMessage());
            String fallbackReply = chatResponseBuilder.fallbackReply();
            ClubChatAiReplyDecision decision = ClubChatAiReplyDecision.standard(fallbackReply, "CLUB_CHAT_EXCEPTION_FALLBACK");
            logFinalDecision(clubId, userId, new ChatIntentRoute(ChatIntentType.FALLBACK, null), decision, "exception-fallback");
            return decision;
        }
    }

    private void logFinalDecision(Integer clubId,
                                  Integer userId,
                                  ChatIntentRoute route,
                                  ClubChatAiReplyDecision decision,
                                  String replyType) {
        log.info("[CLUB_CHAT_DEBUG] final reply: clubId={}, userId={}, intent={}, answerSource={}, matchedFaqId={}, similarityScore={}, secondBestScore={}, replyType={}, text=\"{}\"",
                clubId,
                userId,
                route == null ? ChatIntentType.FALLBACK : route.intentType(),
                decision.answerSource(),
                decision.matchedFaqId(),
                scoreText(decision.similarityScore()),
                scoreText(decision.secondBestScore()),
                replyType,
                safe(decision.replyText()));
    }

    private String scoreText(Double value) {
        return value == null ? "null" : String.format(java.util.Locale.ROOT, "%.4f", value);
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace("\"", "\\\"").trim();
    }

    private static boolean shouldSuggestHandoff(ChatIntentRoute route,
                                                ClubChatKbSafetyGuard.Decision safePathDecision) {
        if (route != null && route.intentType() == ChatIntentType.HUMAN_HANDOFF) {
            return true;
        }
        return safePathDecision != null && safePathDecision.requiresHuman();
    }
}
