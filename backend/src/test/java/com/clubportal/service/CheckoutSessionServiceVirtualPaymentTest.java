package com.clubportal.service;

import com.clubportal.dto.CheckoutSessionCreateRequest;
import com.clubportal.dto.CheckoutSessionCreateResponse;
import com.clubportal.dto.CheckoutSessionDetailResponse;
import com.clubportal.model.BookingHold;
import com.clubportal.model.BookingRecord;
import com.clubportal.model.CheckoutSession;
import com.clubportal.model.Club;
import com.clubportal.model.TimeSlot;
import com.clubportal.model.User;
import com.clubportal.model.Venue;
import com.clubportal.repository.BookingHoldRepository;
import com.clubportal.repository.BookingRecordRepository;
import com.clubportal.repository.CheckoutSessionRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.repository.MembershipPlanRepository;
import com.clubportal.repository.TimeSlotRepository;
import com.clubportal.repository.TransactionRecordRepository;
import com.clubportal.repository.UserMembershipRepository;
import com.clubportal.repository.VenueRepository;
import com.clubportal.util.KeyedLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckoutSessionServiceVirtualPaymentTest {

    private CheckoutSessionRepository checkoutSessionRepo;
    private BookingHoldRepository bookingHoldRepo;
    private TimeSlotRepository timeSlotRepo;
    private VenueRepository venueRepo;
    private ClubRepository clubRepo;
    private MembershipPlanRepository membershipPlanRepo;
    private BookingRecordRepository bookingRepo;
    private UserMembershipRepository userMembershipRepo;
    private TransactionRecordRepository transactionRepo;
    private MembershipService membershipService;
    private StripeCheckoutGateway stripeCheckoutGateway;
    private BookingVerificationCodeService bookingVerificationCodeService;
    private TransactionTemplate transactionTemplate;

    private CheckoutSessionService service;

    @BeforeEach
    void setUp() {
        checkoutSessionRepo = mock(CheckoutSessionRepository.class);
        bookingHoldRepo = mock(BookingHoldRepository.class);
        timeSlotRepo = mock(TimeSlotRepository.class);
        venueRepo = mock(VenueRepository.class);
        clubRepo = mock(ClubRepository.class);
        membershipPlanRepo = mock(MembershipPlanRepository.class);
        bookingRepo = mock(BookingRecordRepository.class);
        userMembershipRepo = mock(UserMembershipRepository.class);
        transactionRepo = mock(TransactionRecordRepository.class);
        membershipService = mock(MembershipService.class);
        stripeCheckoutGateway = mock(StripeCheckoutGateway.class);
        bookingVerificationCodeService = mock(BookingVerificationCodeService.class);
        transactionTemplate = mock(TransactionTemplate.class);

        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        service = new CheckoutSessionService(
                checkoutSessionRepo,
                bookingHoldRepo,
                timeSlotRepo,
                venueRepo,
                clubRepo,
                membershipPlanRepo,
                bookingRepo,
                userMembershipRepo,
                transactionRepo,
                membershipService,
                stripeCheckoutGateway,
                bookingVerificationCodeService,
                new KeyedLockService(),
                transactionTemplate
        );

        ReflectionTestUtils.setField(service, "publicBaseUrl", "https://www.club-portal.xyz");
        ReflectionTestUtils.setField(service, "paymentCurrency", "GBP");
        ReflectionTestUtils.setField(service, "bookingHoldTtlSeconds", 600L);
        ReflectionTestUtils.setField(service, "paymentMode", "VIRTUAL");
    }

    @Test
    void createCheckoutSession_virtualModeReturnsHostedPaymentPage() {
        User user = endUser(6);
        TimeSlot slot = timeSlot(11, 15, new BigDecimal("12.50"));
        Venue venue = venue(15, 2, "Main Hall");
        Club club = club(2, "Demo Club");
        final CheckoutSession[] savedSession = new CheckoutSession[1];

        when(checkoutSessionRepo.findByStatusInAndExpiresAtBefore(anyList(), any())).thenReturn(List.of());
        when(timeSlotRepo.findByIdForUpdate(11)).thenReturn(Optional.of(slot));
        when(venueRepo.findById(15)).thenReturn(Optional.of(venue));
        when(clubRepo.findById(2)).thenReturn(Optional.of(club));
        when(checkoutSessionRepo.findActiveBookingSessions(eq(6), eq(11), anyList(), any())).thenReturn(List.of());
        when(bookingRepo.existsByUserIdAndTimeslotIdAndStatusIn(eq(6), eq(11), anyList())).thenReturn(false);
        when(bookingRepo.countByTimeslotIdAndStatusIn(eq(11), anyList())).thenReturn(0L);
        when(bookingHoldRepo.countActiveByTimeslotId(eq(11), anyString(), any())).thenReturn(0L);
        when(membershipService.findActiveMembership(eq(6), eq(2), any())).thenReturn(Optional.empty());
        when(membershipService.normalizePrice(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(checkoutSessionRepo.save(any(CheckoutSession.class))).thenAnswer(invocation -> {
            CheckoutSession row = invocation.getArgument(0);
            savedSession[0] = row;
            return row;
        });
        when(checkoutSessionRepo.findBySessionIdForUpdate(anyString())).thenAnswer(invocation -> Optional.ofNullable(savedSession[0]));
        when(bookingHoldRepo.save(any(BookingHold.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutSessionCreateResponse response = service.createCheckoutSession(user, new CheckoutSessionCreateRequest("BOOKING", 11, null));

        assertEquals("PAYING", response.status());
        assertEquals("VIRTUAL_CHECKOUT", response.provider());
        assertTrue(response.checkoutUrl().contains("/payment.html?sessionId="));
        assertEquals(response.checkoutUrl(), response.paymentPageUrl());
    }

    @Test
    void confirmVirtualCheckout_marksBookingPaid() {
        User user = endUser(6);
        CheckoutSession session = new CheckoutSession();
        session.setSessionId("chk_virtual_1");
        session.setUserId(6);
        session.setClubId(2);
        session.setType(CheckoutSessionService.TYPE_BOOKING);
        session.setTimeslotId(11);
        session.setAmount(new BigDecimal("12.50"));
        session.setCurrency("GBP");
        session.setStatus(CheckoutSessionService.STATUS_PAYING);
        session.setProvider(CheckoutSessionService.PROVIDER_VIRTUAL_CHECKOUT);
        session.setExpiresAt(Instant.now().plusSeconds(600));

        TimeSlot slot = timeSlot(11, 15, new BigDecimal("12.50"));
        Venue venue = venue(15, 2, "Main Hall");
        Club club = club(2, "Demo Club");

        BookingHold hold = new BookingHold();
        hold.setCheckoutSessionId("chk_virtual_1");
        hold.setStatus(CheckoutSessionService.HOLD_ACTIVE);
        hold.setExpiresAt(Instant.now().plusSeconds(600));

        when(checkoutSessionRepo.findByStatusInAndExpiresAtBefore(anyList(), any())).thenReturn(List.of());
        when(checkoutSessionRepo.findBySessionIdForUpdate("chk_virtual_1")).thenReturn(Optional.of(session));
        when(timeSlotRepo.findByIdForUpdate(11)).thenReturn(Optional.of(slot));
        when(timeSlotRepo.findById(11)).thenReturn(Optional.of(slot));
        when(venueRepo.findById(15)).thenReturn(Optional.of(venue));
        when(clubRepo.findById(2)).thenReturn(Optional.of(club));
        when(bookingHoldRepo.findByCheckoutSessionIdForUpdate("chk_virtual_1")).thenReturn(Optional.of(hold));
        when(bookingRepo.findFirstByUserIdAndTimeslotIdAndStatusInOrderByBookingTimeDesc(eq(6), eq(11), anyList())).thenReturn(Optional.empty());
        when(bookingRepo.countByTimeslotIdAndStatusIn(eq(11), anyList())).thenReturn(0L);
        when(membershipService.findActiveMembership(eq(6), eq(2), any())).thenReturn(Optional.empty());
        when(membershipService.normalizePrice(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepo.findFirstByUserIdAndTimeslotIdOrderByBookingTimeDesc(6, 11)).thenReturn(Optional.empty());
        when(bookingVerificationCodeService.generateUniqueCode()).thenReturn("482905");
        when(bookingRepo.save(any(BookingRecord.class))).thenAnswer(invocation -> {
            BookingRecord row = invocation.getArgument(0);
            if (row.getBookingId() == null) {
                row.setBookingId(77);
            }
            return row;
        });
        when(checkoutSessionRepo.save(any(CheckoutSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingHoldRepo.save(any(BookingHold.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutSessionDetailResponse detail = service.confirmVirtualCheckout(user, "chk_virtual_1");

        assertEquals("PAID", detail.status());
        assertEquals("VIRTUAL_CHECKOUT", detail.provider());
        assertEquals("BOOKING", detail.type());
        assertNotNull(session.getCompletedAt());
        assertEquals(Integer.valueOf(77), session.getBookingId());
        verify(bookingRepo).save(argThat(row -> "482905".equals(row.getBookingVerificationCode())));
        verify(bookingVerificationCodeService).generateUniqueCode();
    }

    @Test
    void confirmVirtualCheckout_rebuildsVerificationCodeWhenReusingHistoricalBooking() {
        User user = endUser(6);
        CheckoutSession session = new CheckoutSession();
        session.setSessionId("chk_virtual_2");
        session.setUserId(6);
        session.setClubId(2);
        session.setType(CheckoutSessionService.TYPE_BOOKING);
        session.setTimeslotId(11);
        session.setAmount(new BigDecimal("12.50"));
        session.setCurrency("GBP");
        session.setStatus(CheckoutSessionService.STATUS_PAYING);
        session.setProvider(CheckoutSessionService.PROVIDER_VIRTUAL_CHECKOUT);
        session.setExpiresAt(Instant.now().plusSeconds(600));

        TimeSlot slot = timeSlot(11, 15, new BigDecimal("12.50"));
        Venue venue = venue(15, 2, "Main Hall");
        Club club = club(2, "Demo Club");

        BookingHold hold = new BookingHold();
        hold.setCheckoutSessionId("chk_virtual_2");
        hold.setStatus(CheckoutSessionService.HOLD_ACTIVE);
        hold.setExpiresAt(Instant.now().plusSeconds(600));

        BookingRecord historical = new BookingRecord();
        historical.setBookingId(91);
        historical.setUserId(6);
        historical.setTimeslotId(11);
        historical.setStatus("CANCELLED");
        historical.setPricePaid(new BigDecimal("0.00"));
        historical.setBookingVerificationCode(null);

        when(checkoutSessionRepo.findByStatusInAndExpiresAtBefore(anyList(), any())).thenReturn(List.of());
        when(checkoutSessionRepo.findBySessionIdForUpdate("chk_virtual_2")).thenReturn(Optional.of(session));
        when(timeSlotRepo.findByIdForUpdate(11)).thenReturn(Optional.of(slot));
        when(timeSlotRepo.findById(11)).thenReturn(Optional.of(slot));
        when(venueRepo.findById(15)).thenReturn(Optional.of(venue));
        when(clubRepo.findById(2)).thenReturn(Optional.of(club));
        when(bookingHoldRepo.findByCheckoutSessionIdForUpdate("chk_virtual_2")).thenReturn(Optional.of(hold));
        when(bookingRepo.findFirstByUserIdAndTimeslotIdAndStatusInOrderByBookingTimeDesc(eq(6), eq(11), anyList())).thenReturn(Optional.empty());
        when(bookingRepo.countByTimeslotIdAndStatusIn(eq(11), anyList())).thenReturn(0L);
        when(membershipService.findActiveMembership(eq(6), eq(2), any())).thenReturn(Optional.empty());
        when(membershipService.normalizePrice(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepo.findFirstByUserIdAndTimeslotIdOrderByBookingTimeDesc(6, 11)).thenReturn(Optional.of(historical));
        when(bookingVerificationCodeService.generateUniqueCode()).thenReturn("904211");
        when(bookingRepo.save(any(BookingRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(checkoutSessionRepo.save(any(CheckoutSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingHoldRepo.save(any(BookingHold.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutSessionDetailResponse detail = service.confirmVirtualCheckout(user, "chk_virtual_2");

        assertEquals("PAID", detail.status());
        verify(bookingRepo).save(argThat(row ->
                Integer.valueOf(91).equals(row.getBookingId())
                        && "904211".equals(row.getBookingVerificationCode())
                        && "PENDING".equals(row.getStatus())
        ));
        verify(bookingVerificationCodeService).generateUniqueCode();
    }

    private static User endUser(int userId) {
        User user = new User();
        user.setUserId(userId);
        user.setEmail("user@example.com");
        user.setRole(User.Role.USER);
        return user;
    }

    private static TimeSlot timeSlot(int timeslotId, int venueId, BigDecimal price) {
        TimeSlot slot = new TimeSlot();
        slot.setTimeslotId(timeslotId);
        slot.setVenueId(venueId);
        slot.setPrice(price);
        slot.setMaxCapacity(1);
        slot.setStartTime(LocalDateTime.now().plusDays(1));
        slot.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        return slot;
    }

    private static Venue venue(int venueId, int clubId, String name) {
        Venue venue = new Venue();
        venue.setVenueId(venueId);
        venue.setClubId(clubId);
        venue.setVenueName(name);
        return venue;
    }

    private static Club club(int clubId, String name) {
        Club club = new Club();
        club.setClubId(clubId);
        club.setClubName(name);
        return club;
    }
}
