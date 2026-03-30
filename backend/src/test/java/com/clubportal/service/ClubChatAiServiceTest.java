package com.clubportal.service;

import com.clubportal.config.ClubChatKbGuardProperties;
import com.clubportal.config.ClubChatKbMatcherProperties;
import com.clubportal.dto.ClubChatContextDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ClubChatAiServiceTest {

    @Test
    void faqHitReturnsConfiguredReplyBeforeGenericAiPipeline() {
        ClubChatContextService contextService = mock(ClubChatContextService.class);
        ChatIntentRouter router = mock(ChatIntentRouter.class);
        ChatResponseBuilder responseBuilder = mock(ChatResponseBuilder.class);
        ClubOpenAiReplyService openAiReplyService = mock(ClubOpenAiReplyService.class);
        ClubChatKbMatcherService kbMatcherService = mock(ClubChatKbMatcherService.class);
        ClubChatKbSafetyGuard kbSafetyGuard = mock(ClubChatKbSafetyGuard.class);

        ClubChatContextDto context = emptyContext();
        ChatIntentRoute route = new ChatIntentRoute(ChatIntentType.CLUB_BASIC_INFO, null);

        when(contextService.buildContext(2, 88)).thenReturn(context);
        when(router.route("What are your opening hours?", context)).thenReturn(route);
        when(kbMatcherService.matchClubFaq(2, "What are your opening hours?"))
                .thenReturn(ClubChatKbMatchResult.hit(
                        7,
                        "When are you open?",
                        "We are open from 8am to 10pm on weekdays.",
                        0.94d,
                        0.70d,
                        "what are your opening hours?"
                ));
        when(kbSafetyGuard.evaluate("What are your opening hours?", route)).thenReturn(Optional.empty());

        ClubChatAiService service = new ClubChatAiService(
                contextService,
                router,
                responseBuilder,
                openAiReplyService,
                kbMatcherService,
                kbSafetyGuard,
                responseGuard()
        );

        ClubChatAiReplyDecision reply = service.buildReplyDecision(2, 88, "What are your opening hours?");

        assertEquals("We are open from 8am to 10pm on weekdays.", reply.replyText());
        assertEquals("CLUB_FAQ", reply.answerSource());
        assertEquals(Integer.valueOf(7), reply.matchedFaqId());
        assertEquals("When are you open?", reply.matchedQuestion());
        assertEquals(0.94d, reply.similarityScore());
        assertEquals(0.70d, reply.secondBestScore());
        assertEquals(false, reply.handoffSuggested());
        verifyNoInteractions(responseBuilder, openAiReplyService);
    }

    @Test
    void faqHitButHighRiskKeywordFallsBackToExistingSafePath() {
        ClubChatContextService contextService = mock(ClubChatContextService.class);
        ChatIntentRouter router = mock(ChatIntentRouter.class);
        ChatResponseBuilder responseBuilder = mock(ChatResponseBuilder.class);
        ClubOpenAiReplyService openAiReplyService = mock(ClubOpenAiReplyService.class);
        ClubChatKbMatcherService kbMatcherService = mock(ClubChatKbMatcherService.class);
        ClubChatKbSafetyGuard kbSafetyGuard = mock(ClubChatKbSafetyGuard.class);

        ClubChatContextDto context = emptyContext();
        ChatIntentRoute route = new ChatIntentRoute(ChatIntentType.REFUND_OR_PAYMENT_ISSUE, null);

        when(contextService.buildContext(2, 88)).thenReturn(context);
        when(router.route("Can I get a refund after booking?", context)).thenReturn(route);
        when(kbMatcherService.matchClubFaq(2, "Can I get a refund after booking?"))
                .thenReturn(ClubChatKbMatchResult.hit(
                        8,
                        "Can I cancel a booking?",
                        "You can cancel a booking from the app.",
                        0.93d,
                        0.72d,
                        "can i get a refund after booking?"
                ));
        when(kbSafetyGuard.evaluate("Can I get a refund after booking?", route))
                .thenReturn(Optional.of(new ClubChatKbSafetyGuard.Decision(
                        "existing high-risk intent: REFUND_OR_PAYMENT_ISSUE",
                        true,
                        true
                )));
        when(responseBuilder.buildReply(route, context, "Can I get a refund after booking?"))
                .thenReturn("This type of issue needs help from club staff.");

        ClubChatAiService service = new ClubChatAiService(
                contextService,
                router,
                responseBuilder,
                openAiReplyService,
                kbMatcherService,
                kbSafetyGuard,
                responseGuard()
        );

        ClubChatAiReplyDecision reply = service.buildReplyDecision(2, 88, "Can I get a refund after booking?");

        assertEquals("This type of issue needs help from club staff.", reply.replyText());
        assertEquals("CLUB_CHAT_SAFE_PATH", reply.answerSource());
        assertEquals(true, reply.handoffSuggested());
        assertNull(reply.matchedFaqId());
        verify(responseBuilder).buildReply(route, context, "Can I get a refund after booking?");
        verifyNoInteractions(openAiReplyService);
    }

    @Test
    void faqHitButRealtimeKeywordDoesNotDirectlyReturnStaticFaq() {
        ClubChatContextService contextService = mock(ClubChatContextService.class);
        ChatIntentRouter router = mock(ChatIntentRouter.class);
        ChatResponseBuilder responseBuilder = mock(ChatResponseBuilder.class);
        ClubOpenAiReplyService openAiReplyService = mock(ClubOpenAiReplyService.class);
        ClubChatKbMatcherService kbMatcherService = mock(ClubChatKbMatcherService.class);
        ClubChatKbSafetyGuard kbSafetyGuard = mock(ClubChatKbSafetyGuard.class);

        ClubChatContextDto context = emptyContext();
        ChatIntentRoute route = new ChatIntentRoute(ChatIntentType.CLUB_BASIC_INFO, null);

        when(contextService.buildContext(2, 88)).thenReturn(context);
        when(router.route("Are you open today now?", context)).thenReturn(route);
        when(kbMatcherService.matchClubFaq(2, "Are you open today now?"))
                .thenReturn(ClubChatKbMatchResult.hit(
                        9,
                        "When are you open?",
                        "We are open from 8am to 10pm on weekdays.",
                        0.92d,
                        0.75d,
                        "are you open today now?"
                ));
        when(kbSafetyGuard.evaluate("Are you open today now?", route)).thenReturn(Optional.empty());
        when(responseBuilder.buildReply(route, context, "Are you open today now?"))
                .thenReturn("Our listed opening hours are 08:00 to 22:00.");
        when(openAiReplyService.rewriteReply(ChatIntentType.CLUB_BASIC_INFO, "Our listed opening hours are 08:00 to 22:00.", "Are you open today now?"))
                .thenReturn("Our listed opening hours are 08:00 to 22:00.");

        ClubChatAiService service = new ClubChatAiService(
                contextService,
                router,
                responseBuilder,
                openAiReplyService,
                kbMatcherService,
                kbSafetyGuard,
                responseGuard()
        );

        ClubChatAiReplyDecision reply = service.buildReplyDecision(2, 88, "Are you open today now?");

        assertEquals("Our listed opening hours are 08:00 to 22:00.", reply.replyText());
        assertEquals("CLUB_CHAT_FALLBACK", reply.answerSource());
        assertNull(reply.matchedFaqId());
        verify(responseBuilder).buildReply(route, context, "Are you open today now?");
        verify(openAiReplyService).rewriteReply(ChatIntentType.CLUB_BASIC_INFO, "Our listed opening hours are 08:00 to 22:00.", "Are you open today now?");
    }

    @Test
    void faqMissFallsBackToExistingChatPipeline() {
        ClubChatContextService contextService = mock(ClubChatContextService.class);
        ChatIntentRouter router = mock(ChatIntentRouter.class);
        ChatResponseBuilder responseBuilder = mock(ChatResponseBuilder.class);
        ClubOpenAiReplyService openAiReplyService = mock(ClubOpenAiReplyService.class);
        ClubChatKbMatcherService kbMatcherService = mock(ClubChatKbMatcherService.class);
        ClubChatKbSafetyGuard kbSafetyGuard = mock(ClubChatKbSafetyGuard.class);

        ClubChatContextDto context = emptyContext();
        ChatIntentRoute route = new ChatIntentRoute(ChatIntentType.CLUB_BASIC_INFO, null);

        when(contextService.buildContext(2, 88)).thenReturn(context);
        when(router.route("Where are you located?", context)).thenReturn(route);
        when(kbMatcherService.matchClubFaq(2, "Where are you located?"))
                .thenReturn(ClubChatKbMatchResult.miss(
                        ClubChatKbMatchRejectReason.LOW_SIMILARITY,
                        0.40d,
                        0.35d,
                        "where are you located?"
                ));
        when(kbSafetyGuard.evaluate("Where are you located?", route)).thenReturn(Optional.empty());
        when(responseBuilder.buildReply(route, context, "Where are you located?"))
                .thenReturn("We are located at Sports Hall.");
        when(openAiReplyService.rewriteReply(ChatIntentType.CLUB_BASIC_INFO, "We are located at Sports Hall.", "Where are you located?"))
                .thenReturn("We are located at Sports Hall, Campus West.");

        ClubChatAiService service = new ClubChatAiService(
                contextService,
                router,
                responseBuilder,
                openAiReplyService,
                kbMatcherService,
                kbSafetyGuard,
                responseGuard()
        );

        ClubChatAiReplyDecision reply = service.buildReplyDecision(2, 88, "Where are you located?");

        assertEquals("We are located at Sports Hall, Campus West.", reply.replyText());
        assertEquals("CLUB_CHAT_OPENAI", reply.answerSource());
        verify(kbMatcherService).matchClubFaq(2, "Where are you located?");
        verify(openAiReplyService).rewriteReply(ChatIntentType.CLUB_BASIC_INFO, "We are located at Sports Hall.", "Where are you located?");
    }

    private ClubChatContextDto emptyContext() {
        return new ClubChatContextDto(
                new ClubChatContextDto.ClubInfo(2, "Club", "", "", List.of(), "", "", "", "", ""),
                List.of(),
                List.of(),
                new ClubChatContextDto.ViewerInfo(false, "", null, "", null),
                new ClubChatContextDto.PolicyInfo("GBP", "Europe/London", 500)
        );
    }

    private ClubChatKbResponseGuard responseGuard() {
        ClubChatKbGuardProperties guardProperties = new ClubChatKbGuardProperties();
        ClubChatKbMatcherProperties matcherProperties = new ClubChatKbMatcherProperties();
        matcherProperties.setBestScoreThreshold(0.84d);
        matcherProperties.setMinScoreGap(0.05d);
        return new ClubChatKbResponseGuard(
                guardProperties,
                matcherProperties,
                new ClubChatKbEmbeddingTextNormalizer()
        );
    }
}
