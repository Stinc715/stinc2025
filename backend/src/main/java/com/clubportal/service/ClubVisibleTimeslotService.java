package com.clubportal.service;

import com.clubportal.dto.TimeSlotResponse;
import com.clubportal.model.TimeSlot;
import com.clubportal.model.User;
import com.clubportal.model.Venue;
import com.clubportal.repository.BookingRecordRepository;
import com.clubportal.repository.TimeSlotRepository;
import com.clubportal.repository.VenueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ClubVisibleTimeslotService {

    private static final Logger log = LoggerFactory.getLogger(ClubVisibleTimeslotService.class);
    private static final List<String> ACTIVE_BOOKING_STATUSES = List.of("PENDING", "APPROVED", "CHECKED");

    private final VenueRepository venueRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingRecordRepository bookingRecordRepository;
    private final MembershipService membershipService;
    private final CheckoutSessionService checkoutSessionService;

    public ClubVisibleTimeslotService(VenueRepository venueRepository,
                                      TimeSlotRepository timeSlotRepository,
                                      BookingRecordRepository bookingRecordRepository,
                                      MembershipService membershipService,
                                      CheckoutSessionService checkoutSessionService) {
        this.venueRepository = venueRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.bookingRecordRepository = bookingRecordRepository;
        this.membershipService = membershipService;
        this.checkoutSessionService = checkoutSessionService;
    }

    public List<TimeSlotResponse> listVisibleTimeslots(Integer clubId,
                                                       User viewerUser,
                                                       LocalDate from,
                                                       LocalDate to,
                                                       String source) {
        String normalizedSource = safe(source);
        Integer userId = viewerUser == null ? null : viewerUser.getUserId();
        log.info("[CLUB_CHAT_DEBUG] visible-timeslot source call: source={}, method=ClubVisibleTimeslotService.listVisibleTimeslots, clubId={}, userId={}, from={}, to={}",
                normalizedSource,
                clubId,
                userId,
                from,
                to);

        if (clubId == null || from == null || to == null || to.isBefore(from)) {
            log.info("[CLUB_CHAT_DEBUG] visible-timeslot source result: source={}, reason=invalid-query, clubId={}, userId={}, from={}, to={}",
                    normalizedSource,
                    clubId,
                    userId,
                    from,
                    to);
            return List.of();
        }

        List<Venue> venues = venueRepository.findByClubId(clubId);
        if (venues.isEmpty()) {
            log.info("[CLUB_CHAT_DEBUG] visible-timeslot source result: source={}, reason=no-venues, clubId={}, userId={}",
                    normalizedSource,
                    clubId,
                    userId);
            return List.of();
        }

        Map<Integer, String> venueNameById = new HashMap<>();
        List<Integer> venueIds = new ArrayList<>();
        for (Venue venue : venues) {
            if (venue == null || venue.getVenueId() == null) {
                continue;
            }
            venueIds.add(venue.getVenueId());
            venueNameById.put(venue.getVenueId(), safe(venue.getVenueName()));
        }
        if (venueIds.isEmpty()) {
            log.info("[CLUB_CHAT_DEBUG] visible-timeslot source result: source={}, reason=no-valid-venue-ids, clubId={}, userId={}",
                    normalizedSource,
                    clubId,
                    userId);
            return List.of();
        }

        LocalDateTime startInclusive = from.atStartOfDay();
        LocalDateTime endExclusive = to.plusDays(1L).atStartOfDay();
        log.info("[CLUB_CHAT_DEBUG] visible-timeslot raw query: source={}, clubId={}, userId={}, venueIds={}, startInclusive={}, endExclusive={}",
                normalizedSource,
                clubId,
                userId,
                venueIds,
                startInclusive,
                endExclusive);

        List<TimeSlot> slots = timeSlotRepository.findByVenueIdInAndStartTimeBetween(venueIds, startInclusive, endExclusive);
        log.info("[CLUB_CHAT_DEBUG] visible-timeslot raw result count: source={}, clubId={}, userId={}, count={}",
                normalizedSource,
                clubId,
                userId,
                slots.size());
        for (int i = 0; i < slots.size(); i++) {
            TimeSlot slot = slots.get(i);
            log.info("[CLUB_CHAT_DEBUG] visible-timeslot raw[{}]: source={}, timeslotId={}, venueId={}, venueName={}, startTime={}, endTime={}, rawPrice={}, maxCapacity={}",
                    i,
                    normalizedSource,
                    slot == null ? null : slot.getTimeslotId(),
                    slot == null ? null : slot.getVenueId(),
                    slot == null ? null : venueNameById.get(slot.getVenueId()),
                    slot == null ? null : slot.getStartTime(),
                    slot == null ? null : slot.getEndTime(),
                    slot == null ? null : slot.getPrice(),
                    slot == null ? null : slot.getMaxCapacity());
        }

        if (slots.isEmpty()) {
            return List.of();
        }

        List<Integer> slotIds = slots.stream()
                .map(TimeSlot::getTimeslotId)
                .filter(java.util.Objects::nonNull)
                .toList();

        Map<Integer, Long> bookedBySlotId = new HashMap<>();
        Map<Integer, Long> heldBySlotId = new HashMap<>();
        if (!slotIds.isEmpty()) {
            for (BookingRecordRepository.TimeslotCount count : bookingRecordRepository.countByTimeslotIdsAndStatuses(slotIds, ACTIVE_BOOKING_STATUSES)) {
                bookedBySlotId.put(count.getTimeslotId(), count.getCnt());
            }
            heldBySlotId.putAll(checkoutSessionService.countActiveBookingHolds(slotIds, Instant.now()));
        }

        Optional<MembershipService.ActiveMembershipContext> activeMembership =
                viewerUser != null && viewerUser.getRole() == User.Role.USER
                        ? membershipService.findActiveMembership(viewerUser.getUserId(), clubId, LocalDate.now())
                        : Optional.empty();
        String membershipStatus = activeMembership
                .map(ctx -> membershipService.effectiveStatus(ctx.membership(), LocalDate.now()))
                .orElse("INACTIVE");
        String membershipPlanName = activeMembership.map(ctx -> safe(ctx.plan().getPlanName())).orElse("");
        BigDecimal membershipPlanPrice = activeMembership
                .map(ctx -> membershipService.normalizePrice(ctx.plan().getPrice()))
                .orElse(membershipService.normalizePrice(null));
        BigDecimal membershipDiscountPercent = activeMembership
                .map(ctx -> membershipService.normalizeDiscount(ctx.plan().getDiscountPercent()))
                .orElse(membershipService.normalizeDiscount(null));
        boolean membershipApplied = activeMembership.isPresent();
        log.info("[CLUB_CHAT_DEBUG] visible-timeslot pricing context: source={}, clubId={}, userId={}, membershipApplied={}, membershipPlanName={}, membershipDiscountPercent={}",
                normalizedSource,
                clubId,
                userId,
                membershipApplied,
                membershipPlanName,
                membershipDiscountPercent);
        log.info("[CLUB_CHAT_DEBUG] MEMBERSHIP_CONTEXT: source={}, clubId={}, userId={}, hasActiveMembership={}, planId={}, planCode={}, planName={}, planPrice={}, discountPercent={}, enabled={}, membershipStatus={}",
                normalizedSource,
                clubId,
                userId,
                membershipApplied,
                activeMembership.map(ctx -> ctx.plan().getPlanId()).orElse(null),
                activeMembership.map(ctx -> safe(ctx.plan().getPlanCode())).orElse(""),
                membershipPlanName,
                membershipPlanPrice,
                membershipDiscountPercent,
                activeMembership.map(ctx -> ctx.plan().getEnabled()).orElse(null),
                membershipStatus);

        List<TimeSlotResponse> responses = slots.stream()
                .sorted(Comparator
                        .comparing(TimeSlot::getStartTime, Comparator.nullsLast(LocalDateTime::compareTo))
                        .thenComparing(TimeSlot::getVenueId, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(TimeSlot::getTimeslotId, Comparator.nullsLast(Integer::compareTo)))
                .map(slot -> {
                    long booked = bookedBySlotId.getOrDefault(slot.getTimeslotId(), 0L);
                    long held = heldBySlotId.getOrDefault(slot.getTimeslotId(), 0L);
                    long remaining = Math.max(0L, (long) normalizeCapacity(slot.getMaxCapacity()) - booked - held);
                    BigDecimal basePrice = membershipService.normalizePrice(slot.getPrice());
                    log.info("[CLUB_CHAT_DEBUG] SLOT_PRICE_SOURCE: source={}, clubId={}, userId={}, timeslotId={}, venueName={}, rawBasePrice={}, rawPrice={}, normalizedBasePrice={}",
                            normalizedSource,
                            clubId,
                            userId,
                            slot.getTimeslotId(),
                            venueNameById.getOrDefault(slot.getVenueId(), ""),
                            slot.getPrice(),
                            slot.getPrice(),
                            basePrice);
                    BigDecimal effectivePrice = activeMembership
                            .map(ctx -> membershipService.calculateDiscountedPrice(basePrice, ctx.plan().getDiscountPercent()))
                            .orElse(basePrice);
                    log.info("[CLUB_CHAT_DEBUG] PRICE_CALC: source={}, clubId={}, userId={}, timeslotId={}, basePrice={}, discountPercent={}, membershipApplied={}, finalPrice={}",
                            normalizedSource,
                            clubId,
                            userId,
                            slot.getTimeslotId(),
                            basePrice,
                            membershipDiscountPercent,
                            membershipApplied,
                            effectivePrice);
                    if (membershipApplied) {
                        log.info("[CLUB_CHAT_DEBUG] MEMBERSHIP_APPLIED_REASON: source={}, clubId={}, userId={}, timeslotId={}, reason=ACTIVE_MEMBERSHIP_FOUND",
                                normalizedSource,
                                clubId,
                                userId,
                                slot.getTimeslotId());
                    }
                    if (membershipDiscountPercent.compareTo(BigDecimal.valueOf(100).setScale(2)) == 0) {
                        log.warn("[CLUB_CHAT_DEBUG] WARNING membershipDiscountPercent=100.00: source={}, clubId={}, userId={}, timeslotId={}, basePrice={}, finalPrice={}",
                                normalizedSource,
                                clubId,
                                userId,
                                slot.getTimeslotId(),
                                basePrice,
                                effectivePrice);
                    }
                    return new TimeSlotResponse(
                            slot.getTimeslotId(),
                            slot.getVenueId(),
                            clubId,
                            venueNameById.getOrDefault(slot.getVenueId(), ""),
                            slot.getStartTime(),
                            slot.getEndTime(),
                            normalizeCapacity(slot.getMaxCapacity()),
                            effectivePrice,
                            booked,
                            remaining,
                            basePrice,
                            membershipPlanName,
                            membershipDiscountPercent,
                            membershipApplied
                    );
                })
                .toList();

        for (int i = 0; i < responses.size(); i++) {
            TimeSlotResponse slot = responses.get(i);
            log.info("[CLUB_CHAT_DEBUG] visible-timeslot computed[{}]: source={}, timeslotId={}, venueName={}, startTime={}, endTime={}, price={}, basePrice={}, bookedCount={}, remaining={}, membershipApplied={}, membershipPlanName={}, membershipDiscountPercent={}",
                    i,
                    normalizedSource,
                    slot == null ? null : slot.timeslotId(),
                    slot == null ? null : slot.venueName(),
                    slot == null ? null : slot.startTime(),
                    slot == null ? null : slot.endTime(),
                    slot == null ? null : slot.price(),
                    slot == null ? null : slot.basePrice(),
                    slot == null ? null : slot.bookedCount(),
                    slot == null ? null : slot.remaining(),
                    slot == null ? null : slot.membershipApplied(),
                    slot == null ? null : slot.membershipPlanName(),
                    slot == null ? null : slot.membershipDiscountPercent());
        }

        return responses;
    }

    private static int normalizeCapacity(Integer raw) {
        return raw == null || raw < 0 ? 0 : raw;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
