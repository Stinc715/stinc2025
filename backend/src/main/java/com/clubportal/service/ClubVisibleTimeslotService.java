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

        if (clubId == null || from == null || to == null || to.isBefore(from)) {
            return List.of();
        }

        List<Venue> venues = venueRepository.findByClubId(clubId);
        if (venues.isEmpty()) {
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
            return List.of();
        }

        LocalDateTime startInclusive = from.atStartOfDay();
        LocalDateTime endExclusive = to.plusDays(1L).atStartOfDay();

        List<TimeSlot> slots = timeSlotRepository.findByVenueIdInAndStartTimeBetween(venueIds, startInclusive, endExclusive);
        for (int i = 0; i < slots.size(); i++) {
            TimeSlot slot = slots.get(i);
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
        String membershipBenefitType = activeMembership
                .map(ctx -> membershipService.normalizeBenefitType(ctx.plan().getBenefitType()))
                .orElse(MembershipService.BENEFIT_DISCOUNT);
        BigDecimal membershipPlanPrice = activeMembership
                .map(ctx -> membershipService.normalizePrice(ctx.plan().getPrice()))
                .orElse(membershipService.normalizePrice(null));
        BigDecimal membershipDiscountPercent = activeMembership
                .map(ctx -> membershipService.normalizeDiscount(ctx.plan().getDiscountPercent()))
                .orElse(membershipService.normalizeDiscount(null));
        Integer membershipIncludedBookings = activeMembership
                .map(ctx -> membershipService.normalizeIncludedBookings(ctx.plan().getIncludedBookings()))
                .orElse(0);
        Integer membershipRemainingBookings = activeMembership
                .map(ctx -> membershipService.normalizeRemainingBookings(ctx.membership().getRemainingBookings()))
                .orElse(0);
        boolean membershipApplied = activeMembership.isPresent();

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
                    BigDecimal effectivePrice = activeMembership
                            .map(ctx -> membershipService.calculateBookingPrice(basePrice, ctx))
                            .orElse(basePrice);
                    if (membershipApplied) {
                    }
                    if (membershipDiscountPercent.compareTo(BigDecimal.valueOf(100).setScale(2)) == 0) {
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
                            membershipBenefitType,
                            membershipDiscountPercent,
                            membershipIncludedBookings,
                            membershipRemainingBookings,
                            membershipApplied
                    );
                })
                .toList();

        for (int i = 0; i < responses.size(); i++) {
            TimeSlotResponse slot = responses.get(i);
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
