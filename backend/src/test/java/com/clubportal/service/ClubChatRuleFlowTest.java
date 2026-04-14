package com.clubportal.service;

import com.clubportal.config.AppLlmProperties;
import com.clubportal.config.OpenAiConfig;
import com.clubportal.dto.ClubChatContextDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClubChatRuleFlowTest {

    private static final ZoneId TEST_ZONE = ZoneId.of("Europe/London");

    private final ChatSlotMatcher slotMatcher = new ChatSlotMatcher();
    private final ChatIntentRouter router = new ChatIntentRouter(slotMatcher);
    private final ChatResponseBuilder responseBuilder = new ChatResponseBuilder();

    @Test
    void memberPriceExplanationMatchesRelevantSlotByMentionedPrice() {
        ClubChatContextDto context = context(
                true,
                List.of(
                        slot(8001, 2, "B", at(0, 11, 11), at(0, 12, 1), "0.00", "0.00", false),
                        slot(9001, 3, "Court A", at(1, 19, 0), at(1, 20, 0), "8.00", "10.00", true),
                        slot(9002, 4, "Court B", at(1, 19, 0), at(1, 20, 0), "12.00", "12.00", false)
                )
        );

        ChatIntentRoute route = router.route("Is the GBP 8.00 price already my member price?", context);
        String reply = responseBuilder.buildReply(route, context, "Is the GBP 8.00 price already my member price?");

        assertEquals(ChatIntentType.MEMBER_PRICE_EXPLANATION, route.intentType());
        assertNotNull(route.matchedTimeslot());
        assertEquals(9001, route.matchedTimeslot().timeslotId());
        assertEquals("Yes. The displayed GBP 8.00 price already reflects your membership discount. The base price is GBP 10.00.", reply);
    }

    @Test
    void memberPriceExplanationFallbackIncludesVisiblePublishedPriceWhenNoExactMatch() {
        ClubChatContextDto context = context(
                true,
                List.of(slot(9001, 3, "Court A", at(1, 19, 0), at(1, 20, 0), "10.00", "10.00", false))
        );

        ChatIntentRoute route = router.route("Is the GBP 8.00 price already my member price?", context);
        String reply = responseBuilder.buildReply(route, context, "Is the GBP 8.00 price already my member price?");

        assertEquals(ChatIntentType.MEMBER_PRICE_EXPLANATION, route.intentType());
        assertNull(route.matchedTimeslot());
        assertEquals("I cannot confirm from the current slot data that membership pricing has already been applied. From the currently published slots I can see here, the visible price is GBP 10.00 for Court A at 7:00 pm.", reply);
    }

    @Test
    void membershipPlanInfoExplainsBookingPackBenefits() {
        ClubChatContextDto context = new ClubChatContextDto(
                new ClubChatContextDto.ClubInfo(
                        12,
                        "Riverside Badminton Club",
                        "Friendly indoor badminton club for beginner to intermediate players.",
                        "badminton",
                        List.of("badminton", "indoor", "beginner-friendly"),
                        "Sports Hall, Campus West",
                        "08:00",
                        "22:00",
                        "hello@riverside.example",
                        "01234 567890"
                ),
                List.of(),
                List.of(new ClubChatContextDto.MembershipPlanInfo(
                        301,
                        "CUSTOM_PACK_30",
                        MembershipService.BENEFIT_BOOKING_PACK,
                        "30-visit pack",
                        money("45.00"),
                        180,
                        money("0.00"),
                        30,
                        true,
                        "Includes 30 prepaid bookings for this club."
                )),
                new ClubChatContextDto.ViewerInfo(false, "", null, "", null),
                new ClubChatContextDto.PolicyInfo("GBP", TEST_ZONE.getId(), 500)
        );

        ChatIntentRoute route = router.route("Tell me about the 30-visit pack plan", context);
        String reply = responseBuilder.buildReply(route, context, "Tell me about the 30-visit pack plan");

        assertEquals(ChatIntentType.MEMBERSHIP_PLAN_INFO, route.intentType());
        assertEquals("The 30-visit pack is listed at GBP 45.00 and includes 30 prepaid bookings valid for 180 days. Includes 30 prepaid bookings for this club.", reply);
    }

    @Test
    void bookingInChatMatchesVenueAndStartHourInsteadOfFirstSlot() {
        ClubChatContextDto context = context(
                true,
                List.of(
                        slot(8001, 2, "B", at(0, 11, 11), at(0, 12, 1), "0.00", "0.00", false),
                        slot(9001, 3, "Court A", at(1, 19, 0), at(1, 20, 0), "10.00", "10.00", false),
                        slot(9002, 4, "Court B", at(1, 19, 0), at(1, 20, 0), "12.00", "12.00", false)
                )
        );

        ChatIntentRoute route = router.route("Can you book the 7pm Court A slot for me here?", context);
        String reply = responseBuilder.buildReply(route, context, "Can you book the 7pm Court A slot for me here?");

        assertEquals(ChatIntentType.BOOKING_IN_CHAT, route.intentType());
        assertNotNull(route.matchedTimeslot());
        assertEquals(9001, route.matchedTimeslot().timeslotId());
        assertEquals("I cannot complete bookings in chat. The 7:00 pm to 8:00 pm Court A slot is currently published at GBP 10.00, and you can book it through the app's booking checkout flow.", reply);
    }

    @Test
    void bookingInChatFallbackIncludesVisibleSlotSummaryWhenNoExplicitSlotMatchExists() {
        ClubChatContextDto context = context(
                true,
                List.of(
                        slot(9001, 3, "Court A", at(1, 19, 0), at(1, 20, 0), "10.00", "10.00", false),
                        slot(9002, 4, "Court B", at(1, 20, 0), at(1, 21, 0), "12.00", "12.00", false)
                )
        );

        ChatIntentRoute route = router.route("Can you book a slot for me here?", context);
        String reply = responseBuilder.buildReply(route, context, "Can you book a slot for me here?");

        assertEquals(ChatIntentType.BOOKING_IN_CHAT, route.intentType());
        assertNull(route.matchedTimeslot());
        assertEquals("I cannot complete bookings in chat. From the currently published slots I can see here, the available options include: Court A at 7:00 pm, GBP 10.00; Court B at 8:00 pm, GBP 12.00. Please use the app's booking checkout flow for the relevant published slot.", reply);
    }

    @Test
    void bookingInChatLoggedOutFallbackMentionsUserSignIn() {
        ClubChatContextDto context = context(
                false,
                List.of(
                        slot(9001, 3, "Court A", at(1, 19, 0), at(1, 20, 0), "10.00", "10.00", false),
                        slot(9002, 4, "Court B", at(1, 20, 0), at(1, 21, 0), "12.00", "12.00", false)
                )
        );

        ChatIntentRoute route = router.route("Can you book a slot for me here?", context);
        String reply = responseBuilder.buildReply(route, context, "Can you book a slot for me here?");

        assertEquals(ChatIntentType.BOOKING_IN_CHAT, route.intentType());
        assertNull(route.matchedTimeslot());
        assertEquals("I cannot complete bookings in chat. From the currently published slots I can see here, the available options include: Court A at 7:00 pm, GBP 10.00; Court B at 8:00 pm, GBP 12.00. You need to sign in with a user account before continuing to booking checkout.", reply);
    }

    @Test
    void tomorrowEveningQueryUsesVisibleSlotDiscoveryIntent() {
        ClubChatContextDto context = context(
                true,
                List.of(
                        slot(8001, 2, "Court C", at(0, 11, 0), at(0, 12, 0), "6.00", "6.00", false),
                        slot(9001, 3, "Court A", at(1, 19, 0), at(1, 20, 0), "10.00", "10.00", false),
                        slot(9002, 4, "Court B", at(1, 20, 0), at(1, 21, 0), "12.00", "12.00", false)
                )
        );

        ChatIntentRoute route = router.route("Do you have any evening slots tomorrow?", context);
        String reply = responseBuilder.buildReply(route, context, "Do you have any evening slots tomorrow?");

        assertEquals(ChatIntentType.VISIBLE_SLOT_DISCOVERY, route.intentType());
        assertEquals("From the currently published slots I can see here, the available options for tomorrow evening include: Court A at 7:00 pm, GBP 10.00; Court B at 8:00 pm, GBP 12.00.", reply);
    }

    @Test
    void tomorrowEveningQueryOnlyListsEveningSlots() {
        ClubChatContextDto context = context(
                true,
                List.of(
                        slot(9001, 3, "Court A", at(1, 12, 0), at(1, 13, 0), "10.00", "10.00", false),
                        slot(9002, 3, "Court A", at(1, 15, 0), at(1, 16, 0), "11.00", "11.00", false),
                        slot(9003, 3, "Court A", at(1, 19, 0), at(1, 20, 0), "12.00", "12.00", false)
                )
        );

        ChatIntentRoute route = router.route("Do you have any evening slots tomorrow?", context);
        String reply = responseBuilder.buildReply(route, context, "Do you have any evening slots tomorrow?");

        assertEquals(ChatIntentType.VISIBLE_SLOT_DISCOVERY, route.intentType());
        assertEquals("From the currently published slots I can see here, the available options for tomorrow evening include: Court A at 7:00 pm, GBP 12.00.", reply);
    }

    @Test
    void tomorrowAfternoonQueryOnlyListsAfternoonSlots() {
        ClubChatContextDto context = context(
                true,
                List.of(
                        slot(9001, 3, "Court A", at(1, 12, 0), at(1, 13, 0), "10.00", "10.00", false),
                        slot(9002, 3, "Court A", at(1, 15, 0), at(1, 16, 0), "11.00", "11.00", false),
                        slot(9003, 3, "Court A", at(1, 19, 0), at(1, 20, 0), "12.00", "12.00", false)
                )
        );

        ChatIntentRoute route = router.route("Do you have any afternoon slots tomorrow?", context);
        String reply = responseBuilder.buildReply(route, context, "Do you have any afternoon slots tomorrow?");

        assertEquals(ChatIntentType.VISIBLE_SLOT_DISCOVERY, route.intentType());
        assertEquals("From the currently published slots I can see here, the available options for tomorrow afternoon include: Court A at 12:00 pm, GBP 10.00; Court A at 3:00 pm, GBP 11.00.", reply);
    }

    @Test
    void tomorrowEveningQueryReturnsExplicitNoMatchWhenNoEveningSlotsExist() {
        ClubChatContextDto context = context(
                true,
                List.of(
                        slot(9001, 3, "Court A", at(1, 12, 0), at(1, 13, 0), "10.00", "10.00", false),
                        slot(9002, 3, "Court A", at(1, 15, 0), at(1, 16, 0), "11.00", "11.00", false)
                )
        );

        ChatIntentRoute route = router.route("Do you have any evening slots tomorrow?", context);
        String reply = responseBuilder.buildReply(route, context, "Do you have any evening slots tomorrow?");

        assertEquals(ChatIntentType.VISIBLE_SLOT_DISCOVERY, route.intentType());
        assertEquals("From the currently published slots I can see here, I do not currently see any evening slots for tomorrow.", reply);
    }

    @Test
    void genericTomorrowBookingRequestRoutesToDiscoveryInsteadOfStrongBookingRefusal() {
        ClubChatContextDto context = context(
                true,
                List.of(
                        slot(9001, 3, "Court A", at(1, 19, 0), at(1, 20, 0), "10.00", "10.00", false),
                        slot(9002, 4, "Court B", at(1, 20, 0), at(1, 21, 0), "12.00", "12.00", false)
                )
        );

        ChatIntentRoute route = router.route("What slots can I book tomorrow evening?", context);
        String reply = responseBuilder.buildReply(route, context, "What slots can I book tomorrow evening?");

        assertEquals(ChatIntentType.VISIBLE_SLOT_DISCOVERY, route.intentType());
        assertEquals("From the currently published slots I can see here, the available options for tomorrow evening include: Court A at 7:00 pm, GBP 10.00; Court B at 8:00 pm, GBP 12.00.", reply);
    }

    @Test
    void missingParkingPolicyNeverInventsPolicyText() {
        ClubChatContextDto context = context(true, List.of(slot(9001, 3, "Court A", at(1, 19, 0), at(1, 20, 0), "10.00", "10.00", false)));

        ChatIntentRoute route = router.route("What is your parking policy?", context);
        String reply = responseBuilder.buildReply(route, context, "What is your parking policy?");

        assertEquals(ChatIntentType.MISSING_POLICY, route.intentType());
        assertEquals("I cannot confirm that detail from the current club data here. Please contact the club directly for that information.", reply);
    }

    @Test
    void openAiRewriteFallsBackToSkeletonWhenApiKeyMissing() {
        AppLlmProperties properties = new AppLlmProperties();
        properties.setEnabled(true);
        ClubOpenAiReplyService service = new ClubOpenAiReplyService(
                new OpenAiConfig.OpenAiClient(HttpClient.newHttpClient(), URI.create("https://api.openai.com"), ""),
                properties
        );

        String skeleton = "We are located at Sports Hall, Campus West.";
        String reply = service.rewriteReply(ChatIntentType.CLUB_BASIC_INFO, skeleton, "Where are you located?");

        assertEquals(skeleton, reply);
    }

    private ClubChatContextDto context(boolean loggedInUser, List<ClubChatContextDto.VisibleTimeslot> slots) {
        LocalDate today = LocalDate.now(TEST_ZONE);
        ClubChatContextDto.ViewerInfo viewer = loggedInUser
                ? new ClubChatContextDto.ViewerInfo(
                true,
                "user",
                45,
                "Alice",
                new ClubChatContextDto.ActiveMembershipInfo(
                        555,
                        12,
                        "Riverside Badminton Club",
                        101,
                        "MONTHLY",
                        MembershipService.BENEFIT_DISCOUNT,
                        "Monthly Pass",
                        money("49.00"),
                        money("20.00"),
                        0,
                        0,
                        today.minusDays(5),
                        today.plusDays(25),
                        "ACTIVE"
                )
        )
                : new ClubChatContextDto.ViewerInfo(false, "", null, "", null);

        return new ClubChatContextDto(
                new ClubChatContextDto.ClubInfo(
                        12,
                        "Riverside Badminton Club",
                        "Friendly indoor badminton club for beginner to intermediate players.",
                        "badminton",
                        List.of("badminton", "indoor", "beginner-friendly"),
                        "Sports Hall, Campus West",
                        "08:00",
                        "22:00",
                        "hello@riverside.example",
                        "01234 567890"
                ),
                slots,
                List.of(new ClubChatContextDto.MembershipPlanInfo(
                        101,
                        "MONTHLY",
                        MembershipService.BENEFIT_DISCOUNT,
                        "Monthly Pass",
                        money("49.00"),
                        30,
                        money("20.00"),
                        0,
                        true,
                        "Save 20% on eligible bookings during this month."
                )),
                viewer,
                new ClubChatContextDto.PolicyInfo("GBP", TEST_ZONE.getId(), 500)
        );
    }

    private ClubChatContextDto.VisibleTimeslot slot(int timeslotId,
                                                    int venueId,
                                                    String venueName,
                                                    LocalDateTime startTime,
                                                    LocalDateTime endTime,
                                                    String price,
                                                    String basePrice,
                                                    boolean membershipApplied) {
        return new ClubChatContextDto.VisibleTimeslot(
                timeslotId,
                venueId,
                venueName,
                startTime,
                endTime,
                15,
                0,
                15,
                nullableMoney(price),
                nullableMoney(basePrice),
                membershipApplied ? "Monthly Pass" : "",
                membershipApplied ? MembershipService.BENEFIT_DISCOUNT : "",
                membershipApplied ? money("20.00") : money("0.00"),
                0,
                0,
                membershipApplied
        );
    }

    private LocalDateTime at(int daysFromToday, int hour, int minute) {
        return LocalDate.now(TEST_ZONE).plusDays(daysFromToday).atTime(hour, minute);
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal nullableMoney(String value) {
        return value == null ? null : money(value);
    }
}
