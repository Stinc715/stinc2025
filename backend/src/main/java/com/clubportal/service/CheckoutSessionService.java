package com.clubportal.service;

import com.clubportal.dto.CheckoutSessionCreateRequest;
import com.clubportal.dto.CheckoutSessionCreateResponse;
import com.clubportal.dto.CheckoutSessionDetailResponse;
import com.clubportal.model.BookingHold;
import com.clubportal.model.BookingRecord;
import com.clubportal.model.CheckoutSession;
import com.clubportal.model.Club;
import com.clubportal.model.MembershipPlan;
import com.clubportal.model.TimeSlot;
import com.clubportal.model.TransactionRecord;
import com.clubportal.model.User;
import com.clubportal.model.UserMembership;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class CheckoutSessionService {

    public static final String TYPE_BOOKING = "BOOKING";
    public static final String TYPE_MEMBERSHIP = "MEMBERSHIP";

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_PAYING = "PAYING";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CANCELED = "CANCELED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    public static final String PROVIDER_STRIPE_CHECKOUT = "STRIPE_CHECKOUT";
    public static final String PROVIDER_VIRTUAL_CHECKOUT = "VIRTUAL_CHECKOUT";

    private static final String MODE_STRIPE = "STRIPE";
    private static final String MODE_VIRTUAL = "VIRTUAL";

    public static final String HOLD_ACTIVE = "ACTIVE";
    public static final String HOLD_CONSUMED = "CONSUMED";
    public static final String HOLD_RELEASED = "RELEASED";
    public static final String HOLD_EXPIRED = "EXPIRED";

    private static final List<String> ACTIVE_BOOKING_STATUSES = List.of("PENDING", "APPROVED", "CHECKED");
    private static final List<String> ACTIVE_SESSION_STATUSES = List.of(STATUS_CREATED, STATUS_PAYING);
    private static final DateTimeFormatter SLOT_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.UK);

    private final CheckoutSessionRepository checkoutSessionRepo;
    private final BookingHoldRepository bookingHoldRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final VenueRepository venueRepo;
    private final ClubRepository clubRepo;
    private final MembershipPlanRepository membershipPlanRepo;
    private final BookingRecordRepository bookingRepo;
    private final UserMembershipRepository userMembershipRepo;
    private final TransactionRecordRepository transactionRepo;
    private final MembershipService membershipService;
    private final StripeCheckoutGateway stripeCheckoutGateway;
    private final BookingVerificationCodeService bookingVerificationCodeService;
    private final KeyedLockService keyedLockService;
    private final TransactionTemplate transactionTemplate;

    @Value("${app.public.base-url:}")
    private String publicBaseUrl;

    @Value("${app.payments.currency:GBP}")
    private String paymentCurrency;

    @Value("${app.payments.booking-hold-ttl-seconds:600}")
    private long bookingHoldTtlSeconds;

    @Value("${app.payments.mode:VIRTUAL}")
    private String paymentMode;

    public CheckoutSessionService(CheckoutSessionRepository checkoutSessionRepo,
                                  BookingHoldRepository bookingHoldRepo,
                                  TimeSlotRepository timeSlotRepo,
                                  VenueRepository venueRepo,
                                  ClubRepository clubRepo,
                                  MembershipPlanRepository membershipPlanRepo,
                                  BookingRecordRepository bookingRepo,
                                  UserMembershipRepository userMembershipRepo,
                                  TransactionRecordRepository transactionRepo,
                                  MembershipService membershipService,
                                  StripeCheckoutGateway stripeCheckoutGateway,
                                  BookingVerificationCodeService bookingVerificationCodeService,
                                  KeyedLockService keyedLockService,
                                  TransactionTemplate transactionTemplate) {
        this.checkoutSessionRepo = checkoutSessionRepo;
        this.bookingHoldRepo = bookingHoldRepo;
        this.timeSlotRepo = timeSlotRepo;
        this.venueRepo = venueRepo;
        this.clubRepo = clubRepo;
        this.membershipPlanRepo = membershipPlanRepo;
        this.bookingRepo = bookingRepo;
        this.userMembershipRepo = userMembershipRepo;
        this.transactionRepo = transactionRepo;
        this.membershipService = membershipService;
        this.stripeCheckoutGateway = stripeCheckoutGateway;
        this.bookingVerificationCodeService = bookingVerificationCodeService;
        this.keyedLockService = keyedLockService;
        this.transactionTemplate = transactionTemplate;
    }

    public boolean paymentsEnabled() {
        return isConfiguredPublicBaseUrl() && (isVirtualPaymentMode() || stripeCheckoutGateway.isCheckoutReady());
    }

    public String paymentProvider() {
        return isVirtualPaymentMode() ? PROVIDER_VIRTUAL_CHECKOUT : PROVIDER_STRIPE_CHECKOUT;
    }

    public CheckoutSessionCreateResponse createCheckoutSession(User user, CheckoutSessionCreateRequest request) {
        User me = requireEndUser(user);
        if (!paymentsEnabled()) {
            throw new PaymentException(HttpStatus.SERVICE_UNAVAILABLE,
                    isVirtualPaymentMode()
                            ? "Virtual checkout is not fully configured for this environment"
                            : "Stripe Checkout is not fully configured for this environment");
        }

        String type = normalizeType(request == null ? null : request.type());
        if (type.isBlank()) {
            if (request != null && request.planId() != null) type = TYPE_MEMBERSHIP;
            if (request != null && request.timeslotId() != null) type = TYPE_BOOKING;
        }
        if (!TYPE_BOOKING.equals(type) && !TYPE_MEMBERSHIP.equals(type)) {
            throw new PaymentException(HttpStatus.BAD_REQUEST, "Unsupported checkout type");
        }

        final String resolvedType = type;
        List<String> lockKeys = switch (resolvedType) {
            case TYPE_BOOKING -> List.of(lockUser(me.getUserId()), lockTimeslot(request == null ? null : request.timeslotId()));
            case TYPE_MEMBERSHIP -> List.of(lockUser(me.getUserId()), lockClubFromPlan(request == null ? null : request.planId()));
            default -> List.of(lockUser(me.getUserId()));
        };

        return keyedLockService.withLocks(lockKeys, () -> {
            cleanupExpiredSessions();
            PreparedCheckout prepared = inTransaction(() -> prepareCheckoutLocked(me, request, resolvedType));
            if (prepared.reuseResponse() != null) {
                return prepared.reuseResponse();
            }

            if (isVirtualPaymentMode()) {
                return inTransaction(() -> attachVirtualCheckout(prepared.sessionId()));
            }

            StripeCheckoutGateway.CreatedCheckoutSession externalSession;
            try {
                externalSession = stripeCheckoutGateway.createCheckoutSession(new StripeCheckoutGateway.CreateRequest(
                        prepared.sessionId(),
                        prepared.type(),
                        me.getUserId(),
                        prepared.clubId(),
                        prepared.timeslotId(),
                        prepared.membershipPlanId(),
                        safe(me.getEmail()),
                        prepared.currency(),
                        prepared.amount(),
                        prepared.title(),
                        prepared.subtitle(),
                        buildStripeReturnUrl(prepared.sessionId(), "success"),
                        buildStripeReturnUrl(prepared.sessionId(), "cancel")
                ));
            } catch (Exception ex) {
                String message = firstNonBlank(ex.getMessage(), "Unable to start Stripe Checkout right now");
                inTransaction(() -> {
                    markSessionFailed(prepared.sessionId(), message, true);
                    return null;
                });
                throw new PaymentException(HttpStatus.BAD_GATEWAY, message);
            }

            return inTransaction(() -> attachProviderCheckout(prepared.sessionId(), externalSession));
        });
    }

    public CheckoutSessionDetailResponse getCheckoutSession(User user, String sessionId) {
        User me = requireEndUser(user);
        cleanupExpiredSessions();

        CheckoutSession session = checkoutSessionRepo.findBySessionId(safe(sessionId))
                .orElseThrow(() -> new PaymentException(HttpStatus.NOT_FOUND, "Checkout session not found"));
        if (!Objects.equals(session.getUserId(), me.getUserId())) {
            throw new PaymentException(HttpStatus.FORBIDDEN, "You can only view your own checkout sessions");
        }
        return toDetailResponse(session);
    }

    public CheckoutSessionDetailResponse cancelCheckoutSession(User user, String sessionId) {
        User me = requireEndUser(user);
        String normalizedSessionId = safe(sessionId);
        if (normalizedSessionId.isBlank()) {
            throw new PaymentException(HttpStatus.BAD_REQUEST, "Missing checkout session id");
        }

        CheckoutSession cancelled = keyedLockService.withLock(lockSession(normalizedSessionId), () ->
                inTransaction(() -> {
                    cleanupExpiredSessions();
                    CheckoutSession session = checkoutSessionRepo.findBySessionIdForUpdate(normalizedSessionId)
                            .orElseThrow(() -> new PaymentException(HttpStatus.NOT_FOUND, "Checkout session not found"));
                    if (!Objects.equals(session.getUserId(), me.getUserId())) {
                        throw new PaymentException(HttpStatus.FORBIDDEN, "You can only cancel your own checkout sessions");
                    }
                    if (STATUS_PAID.equals(session.getStatus())) {
                        throw new PaymentException(HttpStatus.CONFLICT, "This checkout session has already been paid");
                    }
                    if (!ACTIVE_SESSION_STATUSES.contains(safe(session.getStatus()).toUpperCase(Locale.ROOT))) {
                        return session;
                    }

                    Instant now = Instant.now();
                    session.setStatus(STATUS_CANCELED);
                    session.setCancelledAt(now);
                    session.setFailureReason("Checkout was cancelled");
                    releaseHoldIfPresent(session, HOLD_RELEASED, now);
                    return checkoutSessionRepo.save(session);
                })
        );

        if (!safe(cancelled.getProviderSessionId()).isBlank()) {
            stripeCheckoutGateway.expireCheckoutSession(cancelled.getProviderSessionId());
        }
        return toDetailResponse(cancelled);
    }

    public CheckoutSessionDetailResponse confirmVirtualCheckout(User user, String sessionId) {
        User me = requireEndUser(user);
        String normalizedSessionId = safe(sessionId);
        if (normalizedSessionId.isBlank()) {
            throw new PaymentException(HttpStatus.BAD_REQUEST, "Missing checkout session id");
        }
        if (!isVirtualPaymentMode()) {
            throw new PaymentException(HttpStatus.CONFLICT, "Virtual checkout is not enabled in this environment");
        }

        CheckoutSession completed = keyedLockService.withLock(lockSession(normalizedSessionId), () ->
                inTransaction(() -> {
                    cleanupExpiredSessions();
                    CheckoutSession session = checkoutSessionRepo.findBySessionIdForUpdate(normalizedSessionId)
                            .orElseThrow(() -> new PaymentException(HttpStatus.NOT_FOUND, "Checkout session not found"));
                    if (!Objects.equals(session.getUserId(), me.getUserId())) {
                        throw new PaymentException(HttpStatus.FORBIDDEN, "You can only confirm your own checkout sessions");
                    }
                    if (!PROVIDER_VIRTUAL_CHECKOUT.equals(safe(session.getProvider()).toUpperCase(Locale.ROOT))) {
                        throw new PaymentException(HttpStatus.CONFLICT, "This checkout session does not use virtual payment");
                    }
                    if (STATUS_PAID.equals(safe(session.getStatus()).toUpperCase(Locale.ROOT))) {
                        return session;
                    }
                    if (!ACTIVE_SESSION_STATUSES.contains(safe(session.getStatus()).toUpperCase(Locale.ROOT))) {
                        throw new PaymentException(HttpStatus.CONFLICT, "This checkout session is no longer payable");
                    }
                    if (session.getExpiresAt() == null || !session.getExpiresAt().isAfter(Instant.now())) {
                        expireSession(session, "Virtual checkout expired");
                        throw new PaymentException(HttpStatus.CONFLICT, "This checkout session has expired");
                    }
                    if (safe(session.getProviderSessionId()).isBlank()) {
                        session.setProviderSessionId("virtual_" + normalizedSessionId);
                    }
                    fulfillPaidSession(session);
                    return session;
                })
        );

        return toDetailResponse(completed);
    }

    public void handleStripeWebhook(StripeCheckoutGateway.VerifiedWebhookEvent event) {
        if (event == null) {
            return;
        }

        String key = firstNonBlank(event.providerSessionId(), event.internalSessionId());
        if (key.isBlank()) {
            return;
        }

        keyedLockService.withLock(lockProviderSession(key), () -> {
            inTransaction(() -> {
                CheckoutSession session = findSessionForWebhook(event);
                if (session == null) {
                    return null;
                }
                if (!safe(event.providerSessionId()).isBlank() && safe(session.getProviderSessionId()).isBlank()) {
                    session.setProviderSessionId(event.providerSessionId());
                }

                if (isStripeSuccessEvent(event)) {
                    fulfillPaidSession(session);
                    return null;
                }
                if (STATUS_PAID.equals(session.getStatus())) {
                    return null;
                }
                if (isStripeExpiredEvent(event)) {
                    expireSession(session, "Stripe checkout expired");
                    return null;
                }
                if (isStripeFailureEvent(event)) {
                    markExistingSessionFailed(session, "Stripe reported that payment failed", true);
                }
                return null;
            });
            return null;
        });
    }

    public long countActiveBookingHolds(Integer timeslotId, Instant now) {
        if (timeslotId == null) return 0L;
        return bookingHoldRepo.countActiveByTimeslotId(timeslotId, HOLD_ACTIVE, coalesceInstant(now));
    }

    public Map<Integer, Long> countActiveBookingHolds(List<Integer> timeslotIds, Instant now) {
        if (timeslotIds == null || timeslotIds.isEmpty()) {
            return Map.of();
        }
        return bookingHoldRepo.countActiveByTimeslotIds(timeslotIds, HOLD_ACTIVE, coalesceInstant(now)).stream()
                .collect(java.util.stream.Collectors.toMap(
                        BookingHoldRepository.TimeslotCount::getTimeslotId,
                        BookingHoldRepository.TimeslotCount::getCnt
                ));
    }

    private PreparedCheckout prepareCheckoutLocked(User me, CheckoutSessionCreateRequest request, String type) {
        return TYPE_BOOKING.equals(type)
                ? prepareBookingCheckout(me, request == null ? null : request.timeslotId())
                : prepareMembershipCheckout(me, request == null ? null : request.planId());
    }

    private PreparedCheckout prepareBookingCheckout(User me, Integer timeslotId) {
        if (timeslotId == null) {
            throw new PaymentException(HttpStatus.BAD_REQUEST, "Missing time slot");
        }

        Instant now = Instant.now();
        TimeSlot slot = timeSlotRepo.findByIdForUpdate(timeslotId)
                .orElseThrow(() -> new PaymentException(HttpStatus.NOT_FOUND, "Time slot not found"));
        Venue venue = venueRepo.findById(slot.getVenueId())
                .orElseThrow(() -> new PaymentException(HttpStatus.NOT_FOUND, "Venue not found"));
        Club club = clubRepo.findById(venue.getClubId())
                .orElseThrow(() -> new PaymentException(HttpStatus.NOT_FOUND, "Club not found"));

        CheckoutSession reusable = checkoutSessionRepo.findActiveBookingSessions(me.getUserId(), timeslotId, ACTIVE_SESSION_STATUSES, now).stream()
                .findFirst()
                .orElse(null);
        if (reusable != null) {
            return PreparedCheckout.reuse(toCreateResponse(reusable));
        }

        boolean alreadyBooked = bookingRepo.existsByUserIdAndTimeslotIdAndStatusIn(me.getUserId(), timeslotId, ACTIVE_BOOKING_STATUSES);
        if (alreadyBooked) {
            throw new PaymentException(HttpStatus.CONFLICT, "You already have an active booking for this time slot");
        }

        long booked = bookingRepo.countByTimeslotIdAndStatusIn(timeslotId, ACTIVE_BOOKING_STATUSES);
        long held = bookingHoldRepo.countActiveByTimeslotId(timeslotId, HOLD_ACTIVE, now);
        if (booked + held >= safeCapacity(slot.getMaxCapacity())) {
            throw new PaymentException(HttpStatus.CONFLICT, "Time slot is currently full");
        }

        Optional<MembershipService.ActiveMembershipContext> activeMembership = membershipService.findActiveMembership(
                me.getUserId(),
                club.getClubId(),
                LocalDate.now()
        );
        BigDecimal amount = activeMembership
                .map(ctx -> membershipService.calculateDiscountedPrice(slot.getPrice(), ctx.plan().getDiscountPercent()))
                .orElse(membershipService.normalizePrice(slot.getPrice()));

        Instant expiresAt = now.plusSeconds(Math.max(1L, bookingHoldTtlSeconds));
        String sessionId = newSessionId();

        CheckoutSession session = new CheckoutSession();
        session.setSessionId(sessionId);
        session.setUserId(me.getUserId());
        session.setClubId(club.getClubId());
        session.setType(TYPE_BOOKING);
        session.setTimeslotId(timeslotId);
        session.setAmount(amount);
        session.setCurrency(normalizeCurrency(paymentCurrency));
        session.setStatus(STATUS_CREATED);
        session.setProvider(paymentProvider());
        session.setExpiresAt(expiresAt);
        checkoutSessionRepo.save(session);

        BookingHold hold = new BookingHold();
        hold.setCheckoutSessionId(sessionId);
        hold.setUserId(me.getUserId());
        hold.setClubId(club.getClubId());
        hold.setTimeslotId(timeslotId);
        hold.setStatus(HOLD_ACTIVE);
        hold.setExpiresAt(expiresAt);
        bookingHoldRepo.save(hold);

        String slotWindow = formatSlotWindow(slot.getStartTime(), slot.getEndTime());
        return PreparedCheckout.create(
                sessionId,
                TYPE_BOOKING,
                club.getClubId(),
                timeslotId,
                null,
                amount,
                normalizeCurrency(paymentCurrency),
                "Booking at " + firstNonBlank(venue.getVenueName(), "club venue"),
                firstNonBlank(slotWindow, "Pay to reserve this booking slot"),
                buildClubReturnUrl(club.getClubId())
        );
    }

    private PreparedCheckout prepareMembershipCheckout(User me, Integer planId) {
        if (planId == null) {
            throw new PaymentException(HttpStatus.BAD_REQUEST, "Missing membership plan");
        }

        Instant now = Instant.now();
        MembershipPlan plan = membershipPlanRepo.findById(planId)
                .orElseThrow(() -> new PaymentException(HttpStatus.NOT_FOUND, "Membership plan not found"));
        if (!Boolean.TRUE.equals(plan.getEnabled())) {
            throw new PaymentException(HttpStatus.CONFLICT, "This membership plan is currently unavailable");
        }

        Club club = clubRepo.findById(plan.getClubId())
                .orElseThrow(() -> new PaymentException(HttpStatus.NOT_FOUND, "Club not found"));

        Optional<MembershipService.ActiveMembershipContext> activeMembership = membershipService.findActiveMembership(
                me.getUserId(),
                club.getClubId(),
                LocalDate.now()
        );
        if (activeMembership.isPresent()) {
            throw new PaymentException(HttpStatus.CONFLICT,
                    "You already have an active membership for this club until " + activeMembership.get().membership().getEndDate());
        }

        CheckoutSession reusable = checkoutSessionRepo.findActiveSessionsForUserAndClub(
                me.getUserId(),
                club.getClubId(),
                TYPE_MEMBERSHIP,
                ACTIVE_SESSION_STATUSES,
                now
        ).stream().findFirst().orElse(null);
        if (reusable != null) {
            if (!Objects.equals(reusable.getMembershipPlanId(), plan.getPlanId())) {
                throw new PaymentException(
                        HttpStatus.CONFLICT,
                        "A membership checkout is already in progress for this club. Cancel it before choosing a different plan."
                );
            }
            return PreparedCheckout.reuse(toCreateResponse(reusable));
        }

        String sessionId = newSessionId();
        CheckoutSession session = new CheckoutSession();
        session.setSessionId(sessionId);
        session.setUserId(me.getUserId());
        session.setClubId(club.getClubId());
        session.setType(TYPE_MEMBERSHIP);
        session.setMembershipPlanId(plan.getPlanId());
        session.setAmount(membershipService.normalizePrice(plan.getPrice()));
        session.setCurrency(normalizeCurrency(paymentCurrency));
        session.setStatus(STATUS_CREATED);
        session.setProvider(paymentProvider());
        session.setExpiresAt(now.plusSeconds(Math.max(300L, bookingHoldTtlSeconds)));
        checkoutSessionRepo.save(session);

        return PreparedCheckout.create(
                sessionId,
                TYPE_MEMBERSHIP,
                club.getClubId(),
                null,
                plan.getPlanId(),
                membershipService.normalizePrice(plan.getPrice()),
                normalizeCurrency(paymentCurrency),
                firstNonBlank(plan.getPlanName(), "Membership"),
                firstNonBlank(club.getClubName(), "Club membership"),
                buildClubReturnUrl(club.getClubId())
        );
    }

    private CheckoutSessionCreateResponse attachProviderCheckout(String sessionId, StripeCheckoutGateway.CreatedCheckoutSession externalSession) {
        CheckoutSession session = checkoutSessionRepo.findBySessionIdForUpdate(sessionId)
                .orElseThrow(() -> new PaymentException(HttpStatus.NOT_FOUND, "Checkout session not found"));
        session.setProviderSessionId(externalSession.providerSessionId());
        session.setCheckoutUrl(externalSession.checkoutUrl());
        session.setStatus(STATUS_PAYING);
        session.setFailureReason(null);
        return toCreateResponse(checkoutSessionRepo.save(session));
    }

    private CheckoutSessionCreateResponse attachVirtualCheckout(String sessionId) {
        CheckoutSession session = checkoutSessionRepo.findBySessionIdForUpdate(sessionId)
                .orElseThrow(() -> new PaymentException(HttpStatus.NOT_FOUND, "Checkout session not found"));
        session.setCheckoutUrl(buildPaymentPageUrl(sessionId));
        session.setStatus(STATUS_PAYING);
        session.setFailureReason(null);
        return toCreateResponse(checkoutSessionRepo.save(session));
    }

    private void fulfillPaidSession(CheckoutSession session) {
        if (STATUS_PAID.equals(session.getStatus())) {
            return;
        }
        if (TYPE_BOOKING.equals(session.getType())) {
            fulfillBookingSession(session);
            return;
        }
        if (TYPE_MEMBERSHIP.equals(session.getType())) {
            fulfillMembershipSession(session);
            return;
        }
        throw new PaymentException(HttpStatus.BAD_REQUEST, "Unsupported checkout session type");
    }

    private void fulfillBookingSession(CheckoutSession session) {
        Instant now = Instant.now();
        TimeSlot slot = timeSlotRepo.findByIdForUpdate(session.getTimeslotId())
                .orElseThrow(() -> new PaymentException(HttpStatus.NOT_FOUND, "Time slot not found"));
        Venue venue = venueRepo.findById(slot.getVenueId())
                .orElseThrow(() -> new PaymentException(HttpStatus.NOT_FOUND, "Venue not found"));

        BookingHold hold = bookingHoldRepo.findByCheckoutSessionIdForUpdate(session.getSessionId()).orElse(null);

        Optional<BookingRecord> existingActive = bookingRepo.findFirstByUserIdAndTimeslotIdAndStatusInOrderByBookingTimeDesc(
                session.getUserId(),
                session.getTimeslotId(),
                ACTIVE_BOOKING_STATUSES
        );
        if (existingActive.isPresent()) {
            BookingRecord existing = existingActive.get();
            session.setBookingId(existing.getBookingId());
            session.setStatus(STATUS_PAID);
            session.setCompletedAt(now);
            if (hold != null && HOLD_ACTIVE.equalsIgnoreCase(hold.getStatus())) {
                hold.setStatus(HOLD_CONSUMED);
                hold.setReleasedAt(now);
                bookingHoldRepo.save(hold);
            }
            checkoutSessionRepo.save(session);
            return;
        }

        long booked = bookingRepo.countByTimeslotIdAndStatusIn(slot.getTimeslotId(), ACTIVE_BOOKING_STATUSES);
        boolean holdActive = hold != null && HOLD_ACTIVE.equalsIgnoreCase(hold.getStatus()) && hold.getExpiresAt() != null && hold.getExpiresAt().isAfter(now);
        if (!holdActive && booked >= safeCapacity(slot.getMaxCapacity())) {
            markExistingSessionFailed(session, "Payment completed after the slot became unavailable", false);
            return;
        }

        Optional<MembershipService.ActiveMembershipContext> activeMembership = membershipService.findActiveMembership(
                session.getUserId(),
                venue.getClubId(),
                LocalDate.now()
        );
        BigDecimal amount = activeMembership
                .map(ctx -> membershipService.calculateDiscountedPrice(slot.getPrice(), ctx.plan().getDiscountPercent()))
                .orElse(membershipService.normalizePrice(slot.getPrice()));

        BookingRecord booking = bookingRepo.findFirstByUserIdAndTimeslotIdOrderByBookingTimeDesc(
                session.getUserId(),
                session.getTimeslotId()
        ).orElseGet(BookingRecord::new);
        String previousStatus = normalizeStatus(booking.getStatus());
        if (booking.getBookingId() == null) {
            booking.setUserId(session.getUserId());
            booking.setTimeslotId(session.getTimeslotId());
        }
        booking.setStatus("PENDING");
        booking.setPricePaid(amount);
        booking.setUserMembershipId(activeMembership.map(ctx -> ctx.membership().getUserMembershipId()).orElse(null));
        if (booking.getBookingId() == null
                || safe(booking.getBookingVerificationCode()).isBlank()
                || !ACTIVE_BOOKING_STATUSES.contains(previousStatus)) {
            booking.setBookingVerificationCode(bookingVerificationCodeService.generateUniqueCode());
        }
        BookingRecord saved = bookingRepo.save(booking);

        session.setBookingId(saved.getBookingId());
        session.setAmount(amount);
        session.setStatus(STATUS_PAID);
        session.setCompletedAt(now);
        session.setFailureReason(null);
        checkoutSessionRepo.save(session);

        if (hold != null && HOLD_ACTIVE.equalsIgnoreCase(hold.getStatus())) {
            hold.setStatus(HOLD_CONSUMED);
            hold.setReleasedAt(now);
            bookingHoldRepo.save(hold);
        }
    }

    private void fulfillMembershipSession(CheckoutSession session) {
        Instant now = Instant.now();
        MembershipPlan plan = membershipPlanRepo.findById(session.getMembershipPlanId())
                .orElseThrow(() -> new PaymentException(HttpStatus.NOT_FOUND, "Membership plan not found"));

        if (session.getUserMembershipId() != null && session.getTransactionId() != null) {
            session.setStatus(STATUS_PAID);
            session.setCompletedAt(now);
            checkoutSessionRepo.save(session);
            return;
        }

        Optional<MembershipService.ActiveMembershipContext> activeMembership = membershipService.findActiveMembership(
                session.getUserId(),
                plan.getClubId(),
                LocalDate.now()
        );
        if (activeMembership.isPresent()) {
            markExistingSessionFailed(session, "An active membership already exists for this club", false);
            return;
        }

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(Math.max(1, Optional.ofNullable(plan.getDurationDays()).orElse(30)) - 1L);

        UserMembership membership = new UserMembership();
        membership.setUserId(session.getUserId());
        membership.setPlanId(plan.getPlanId());
        membership.setStartDate(startDate);
        membership.setEndDate(endDate);
        membership.setStatus("ACTIVE");
        UserMembership savedMembership = userMembershipRepo.save(membership);

        TransactionRecord tx = new TransactionRecord();
        tx.setUserId(session.getUserId());
        tx.setUserMembershipId(savedMembership.getUserMembershipId());
        tx.setAmount(membershipService.normalizePrice(plan.getPrice()));
        tx.setPaymentMethod(paymentProvider());
        tx.setStatus("PAID");
        TransactionRecord savedTx = transactionRepo.save(tx);

        session.setAmount(membershipService.normalizePrice(plan.getPrice()));
        session.setUserMembershipId(savedMembership.getUserMembershipId());
        session.setTransactionId(savedTx.getTransactionId());
        session.setStatus(STATUS_PAID);
        session.setCompletedAt(now);
        session.setFailureReason(null);
        checkoutSessionRepo.save(session);
    }

    private void expireSession(CheckoutSession session, String reason) {
        Instant now = Instant.now();
        session.setStatus(STATUS_EXPIRED);
        session.setFailureReason(firstNonBlank(reason, "Checkout session expired"));
        releaseHoldIfPresent(session, HOLD_EXPIRED, now);
        checkoutSessionRepo.save(session);
    }

    private void markSessionFailed(String sessionId, String reason, boolean releaseHold) {
        CheckoutSession session = checkoutSessionRepo.findBySessionIdForUpdate(sessionId).orElse(null);
        if (session == null) {
            return;
        }
        markExistingSessionFailed(session, reason, releaseHold);
    }

    private void markExistingSessionFailed(CheckoutSession session, String reason, boolean releaseHold) {
        if (STATUS_PAID.equals(session.getStatus())) {
            return;
        }
        session.setStatus(STATUS_FAILED);
        session.setFailureReason(firstNonBlank(reason, "Checkout failed"));
        if (releaseHold) {
            releaseHoldIfPresent(session, HOLD_RELEASED, Instant.now());
        }
        checkoutSessionRepo.save(session);
    }

    private void releaseHoldIfPresent(CheckoutSession session, String nextStatus, Instant now) {
        if (!TYPE_BOOKING.equals(session.getType())) {
            return;
        }
        BookingHold hold = bookingHoldRepo.findByCheckoutSessionIdForUpdate(session.getSessionId()).orElse(null);
        if (hold == null || !HOLD_ACTIVE.equalsIgnoreCase(hold.getStatus())) {
            return;
        }
        hold.setStatus(nextStatus);
        hold.setReleasedAt(coalesceInstant(now));
        bookingHoldRepo.save(hold);
    }

    private CheckoutSession findSessionForWebhook(StripeCheckoutGateway.VerifiedWebhookEvent event) {
        if (!safe(event.providerSessionId()).isBlank()) {
            Optional<CheckoutSession> byProvider = checkoutSessionRepo.findByProviderSessionIdForUpdate(event.providerSessionId());
            if (byProvider.isPresent()) {
                return byProvider.get();
            }
        }
        if (!safe(event.internalSessionId()).isBlank()) {
            return checkoutSessionRepo.findBySessionIdForUpdate(event.internalSessionId()).orElse(null);
        }
        return null;
    }

    private void cleanupExpiredSessions() {
        Instant now = Instant.now();
        List<CheckoutSession> expired = checkoutSessionRepo.findByStatusInAndExpiresAtBefore(ACTIVE_SESSION_STATUSES, now);
        for (CheckoutSession row : expired) {
            String sessionId = safe(row.getSessionId());
            if (sessionId.isBlank()) {
                continue;
            }
            keyedLockService.withLock(lockSession(sessionId), () -> {
                inTransaction(() -> {
                    CheckoutSession session = checkoutSessionRepo.findBySessionIdForUpdate(sessionId).orElse(null);
                    if (session == null || STATUS_PAID.equals(session.getStatus()) || coalesceInstant(session.getExpiresAt()).isAfter(now)) {
                        return null;
                    }
                    session.setStatus(STATUS_EXPIRED);
                    session.setFailureReason("Checkout session expired");
                    releaseHoldIfPresent(session, HOLD_EXPIRED, now);
                    checkoutSessionRepo.save(session);
                    return null;
                });
                return null;
            });
        }
    }

    private CheckoutSessionDetailResponse toDetailResponse(CheckoutSession session) {
        Instant now = Instant.now();
        Club club = clubRepo.findById(session.getClubId()).orElse(null);
        String returnUrl = buildClubReturnUrl(session.getClubId());
        String clubName = club == null ? "" : safe(club.getClubName());
        String title = TYPE_BOOKING.equals(session.getType()) ? "Booking payment" : "Membership payment";
        String subtitle = "";
        String venueName = "";
        LocalDateTime slotStart = null;
        LocalDateTime slotEnd = null;
        String planName = "";
        Integer durationDays = null;

        if (TYPE_BOOKING.equals(session.getType()) && session.getTimeslotId() != null) {
            TimeSlot slot = timeSlotRepo.findById(session.getTimeslotId()).orElse(null);
            if (slot != null) {
                Venue venue = venueRepo.findById(slot.getVenueId()).orElse(null);
                venueName = venue == null ? "" : safe(venue.getVenueName());
                slotStart = slot.getStartTime();
                slotEnd = slot.getEndTime();
                title = "Booking at " + firstNonBlank(venueName, "venue");
                subtitle = firstNonBlank(formatSlotWindow(slotStart, slotEnd), "Reserve your selected time slot");
            }
        } else if (TYPE_MEMBERSHIP.equals(session.getType()) && session.getMembershipPlanId() != null) {
            MembershipPlan plan = membershipPlanRepo.findById(session.getMembershipPlanId()).orElse(null);
            if (plan != null) {
                planName = safe(plan.getPlanName());
                durationDays = plan.getDurationDays();
                title = firstNonBlank(planName, "Membership");
                subtitle = firstNonBlank(clubName, "Club membership");
            }
        }

        String status = normalizedSessionStatus(session, now);
        boolean active = ACTIVE_SESSION_STATUSES.contains(status) && session.getExpiresAt() != null && session.getExpiresAt().isAfter(now);
        return new CheckoutSessionDetailResponse(
                session.getSessionId(),
                safe(session.getType()),
                status,
                safe(session.getProvider()),
                membershipService.normalizePrice(session.getAmount()),
                normalizeCurrency(session.getCurrency()),
                session.getExpiresAt(),
                session.getCompletedAt(),
                safe(session.getCheckoutUrl()),
                active && !safe(session.getCheckoutUrl()).isBlank(),
                active,
                returnUrl,
                session.getClubId(),
                clubName,
                session.getTimeslotId(),
                venueName,
                slotStart,
                slotEnd,
                session.getMembershipPlanId(),
                planName,
                durationDays,
                title,
                subtitle,
                safe(session.getFailureReason())
        );
    }

    private CheckoutSessionCreateResponse toCreateResponse(CheckoutSession session) {
        return new CheckoutSessionCreateResponse(
                session.getSessionId(),
                safe(session.getStatus()).toUpperCase(Locale.ROOT),
                safe(session.getProvider()),
                safe(session.getCheckoutUrl()),
                buildPaymentPageUrl(session.getSessionId()),
                session.getExpiresAt()
        );
    }

    private <T> T inTransaction(java.util.function.Supplier<T> action) {
        return transactionTemplate.execute(status -> action.get());
    }

    private static User requireEndUser(User user) {
        if (user == null) {
            throw new PaymentException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }
        if (user.getRole() == null || user.getRole() != User.Role.USER) {
            throw new PaymentException(HttpStatus.FORBIDDEN, "Only user accounts can start checkout");
        }
        return user;
    }

    private static String normalizeType(String value) {
        return safe(value).toUpperCase(Locale.ROOT);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeStatus(String value) {
        return safe(value).toUpperCase(Locale.ROOT);
    }

    private static String firstNonBlank(String first, String fallback) {
        String one = safe(first);
        return one.isBlank() ? safe(fallback) : one;
    }

    private static int safeCapacity(Integer capacity) {
        return capacity == null || capacity < 0 ? 0 : capacity;
    }

    private static Instant coalesceInstant(Instant value) {
        return value == null ? Instant.EPOCH : value;
    }

    private static String newSessionId() {
        return "chk_" + UUID.randomUUID().toString().replace("-", "");
    }

    private static String formatSlotWindow(LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) {
            return "";
        }
        if (start != null && end != null) {
            return SLOT_FORMATTER.format(start) + " - " + end.toLocalTime();
        }
        return start != null ? SLOT_FORMATTER.format(start) : SLOT_FORMATTER.format(end);
    }

    private String buildPaymentPageUrl(String sessionId) {
        String baseUrl = normalizePublicBaseUrl();
        if (baseUrl.isBlank()) {
            throw new PaymentException(HttpStatus.SERVICE_UNAVAILABLE, "APP_PUBLIC_BASE_URL is not configured");
        }
        return baseUrl + "/payment.html?sessionId=" + urlEncode(sessionId);
    }

    private String buildStripeReturnUrl(String sessionId, String step) {
        return buildPaymentPageUrl(sessionId) + "&stripe=" + urlEncode(step);
    }

    private static String buildClubReturnUrl(Integer clubId) {
        return clubId == null ? "club.html" : "club.html?club=" + clubId;
    }

    private String normalizePublicBaseUrl() {
        String base = safe(publicBaseUrl);
        if (base.isBlank()) {
            return "";
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (!base.startsWith("http://") && !base.startsWith("https://")) {
            return "";
        }
        return base;
    }

    private boolean isVirtualPaymentMode() {
        return MODE_VIRTUAL.equalsIgnoreCase(safe(paymentMode));
    }

    private boolean isConfiguredPublicBaseUrl() {
        return !normalizePublicBaseUrl().isBlank();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static String normalizedSessionStatus(CheckoutSession session, Instant now) {
        String status = safe(session.getStatus()).toUpperCase(Locale.ROOT);
        if (ACTIVE_SESSION_STATUSES.contains(status) && session.getExpiresAt() != null && !session.getExpiresAt().isAfter(now)) {
            return STATUS_EXPIRED;
        }
        return status;
    }

    private static boolean isStripeSuccessEvent(StripeCheckoutGateway.VerifiedWebhookEvent event) {
        String type = safe(event.type()).toLowerCase(Locale.ROOT);
        String paymentStatus = safe(event.paymentStatus()).toLowerCase(Locale.ROOT);
        if ("checkout.session.completed".equals(type)) {
            return paymentStatus.isBlank() || "paid".equals(paymentStatus);
        }
        return "checkout.session.async_payment_succeeded".equals(type) && "paid".equals(paymentStatus);
    }

    private static boolean isStripeExpiredEvent(StripeCheckoutGateway.VerifiedWebhookEvent event) {
        return "checkout.session.expired".equals(safe(event.type()).toLowerCase(Locale.ROOT));
    }

    private static boolean isStripeFailureEvent(StripeCheckoutGateway.VerifiedWebhookEvent event) {
        return "checkout.session.async_payment_failed".equals(safe(event.type()).toLowerCase(Locale.ROOT));
    }

    private String lockClubFromPlan(Integer planId) {
        if (planId == null) {
            return "club:null";
        }
        MembershipPlan plan = membershipPlanRepo.findById(planId).orElse(null);
        return lockClub(plan == null ? null : plan.getClubId());
    }

    private static String lockUser(Integer userId) {
        return "user:" + (userId == null ? "null" : userId);
    }

    private static String lockTimeslot(Integer timeslotId) {
        return "timeslot:" + (timeslotId == null ? "null" : timeslotId);
    }

    private static String lockClub(Integer clubId) {
        return "club:" + (clubId == null ? "null" : clubId);
    }

    private static String lockSession(String sessionId) {
        return "checkout-session:" + safe(sessionId);
    }

    private static String lockProviderSession(String providerSessionId) {
        return "checkout-provider:" + safe(providerSessionId);
    }

    private static String normalizeCurrency(String value) {
        String currency = safe(value).toUpperCase(Locale.ROOT);
        return currency.isBlank() ? "GBP" : currency;
    }

    private record PreparedCheckout(
            String sessionId,
            String type,
            Integer clubId,
            Integer timeslotId,
            Integer membershipPlanId,
            BigDecimal amount,
            String currency,
            String title,
            String subtitle,
            String returnUrl,
            CheckoutSessionCreateResponse reuseResponse
    ) {
        private static PreparedCheckout create(String sessionId,
                                               String type,
                                               Integer clubId,
                                               Integer timeslotId,
                                               Integer membershipPlanId,
                                               BigDecimal amount,
                                               String currency,
                                               String title,
                                               String subtitle,
                                               String returnUrl) {
            return new PreparedCheckout(
                    sessionId,
                    type,
                    clubId,
                    timeslotId,
                    membershipPlanId,
                    amount,
                    currency,
                    title,
                    subtitle,
                    returnUrl,
                    null
            );
        }

        private static PreparedCheckout reuse(CheckoutSessionCreateResponse response) {
            return new PreparedCheckout(null, null, null, null, null, null, null, null, null, null, response);
        }
    }
}
