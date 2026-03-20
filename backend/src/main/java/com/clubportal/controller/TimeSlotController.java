package com.clubportal.controller;

import com.clubportal.dto.TimeSlotResponse;
import com.clubportal.dto.TimeSlotUpsertRequest;
import com.clubportal.model.TimeSlot;
import com.clubportal.model.User;
import com.clubportal.model.Venue;
import com.clubportal.repository.BookingRecordRepository;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.repository.TimeSlotRepository;
import com.clubportal.repository.VenueRepository;
import com.clubportal.service.CurrentUserService;
import com.clubportal.service.MembershipService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/clubs/{clubId}")
public class TimeSlotController {

    private static final List<String> ACTIVE_BOOKING_STATUSES = List.of("PENDING", "APPROVED", "CHECKED");

    private final ClubRepository clubRepo;
    private final VenueRepository venueRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final BookingRecordRepository bookingRepo;
    private final ClubAdminRepository clubAdminRepo;
    private final CurrentUserService currentUserService;
    private final MembershipService membershipService;

    public TimeSlotController(ClubRepository clubRepo,
                              VenueRepository venueRepo,
                              TimeSlotRepository timeSlotRepo,
                              BookingRecordRepository bookingRepo,
                              ClubAdminRepository clubAdminRepo,
                              CurrentUserService currentUserService,
                              MembershipService membershipService) {
        this.clubRepo = clubRepo;
        this.venueRepo = venueRepo;
        this.timeSlotRepo = timeSlotRepo;
        this.bookingRepo = bookingRepo;
        this.clubAdminRepo = clubAdminRepo;
        this.currentUserService = currentUserService;
        this.membershipService = membershipService;
    }

    // User-facing query: list time slots for a club in a date range.
    @GetMapping("/timeslots")
    public ResponseEntity<?> listClubTimeslots(@PathVariable Integer clubId,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (!clubRepo.existsById(clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }
        if (to.isBefore(from)) {
            return ResponseEntity.badRequest().body("Invalid date range");
        }

        List<Venue> venues = venueRepo.findByClubId(clubId);
        if (venues.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        Map<Integer, String> venueNameById = new HashMap<>();
        List<Integer> venueIds = new ArrayList<>();
        for (Venue v : venues) {
            venueIds.add(v.getVenueId());
            venueNameById.put(v.getVenueId(), safe(v.getVenueName()));
        }

        LocalDateTime startInclusive = from.atStartOfDay();
        LocalDateTime endExclusive = to.plusDays(1).atStartOfDay();
        List<TimeSlot> slots = timeSlotRepo.findByVenueIdInAndStartTimeBetween(venueIds, startInclusive, endExclusive);

        List<Integer> slotIds = slots.stream().map(TimeSlot::getTimeslotId).toList();
        Map<Integer, Long> bookedBySlotId = new HashMap<>();
        if (!slotIds.isEmpty()) {
            for (BookingRecordRepository.TimeslotCount c : bookingRepo.countByTimeslotIdsAndStatuses(slotIds, ACTIVE_BOOKING_STATUSES)) {
                bookedBySlotId.put(c.getTimeslotId(), c.getCnt());
            }
        }

        User viewer = currentUserService.findUserOrNull();
        final Optional<MembershipService.ActiveMembershipContext> activeMembership =
                viewer != null && viewer.getRole() == User.Role.USER
                        ? membershipService.findActiveMembership(viewer.getUserId(), clubId, LocalDate.now())
                        : Optional.empty();
        final String activeMembershipPlanName = activeMembership.map(ctx -> safe(ctx.plan().getPlanName())).orElse("");
        final BigDecimal activeMembershipDiscount = activeMembership
                .map(ctx -> membershipService.normalizeDiscount(ctx.plan().getDiscountPercent()))
                .orElse(membershipService.normalizeDiscount(null));
        final boolean activeMembershipApplied = activeMembership.isPresent();

        List<TimeSlotResponse> out = slots.stream()
                .sorted(Comparator
                        .comparing(TimeSlot::getStartTime)
                        .thenComparing(TimeSlot::getVenueId)
                        .thenComparing(TimeSlot::getTimeslotId))
                .map(ts -> {
                    long booked = bookedBySlotId.getOrDefault(ts.getTimeslotId(), 0L);
                    long remaining = Math.max(0L, (long) ts.getMaxCapacity() - booked);
                    BigDecimal basePrice = membershipService.normalizePrice(ts.getPrice());
                    BigDecimal effectivePrice = activeMembership
                            .map(ctx -> membershipService.calculateDiscountedPrice(basePrice, ctx.plan().getDiscountPercent()))
                            .orElse(basePrice);
                    return new TimeSlotResponse(
                            ts.getTimeslotId(),
                            ts.getVenueId(),
                            clubId,
                            venueNameById.getOrDefault(ts.getVenueId(), ""),
                            ts.getStartTime(),
                            ts.getEndTime(),
                            ts.getMaxCapacity(),
                            effectivePrice,
                            booked,
                            remaining,
                            basePrice,
                            activeMembershipPlanName,
                            activeMembershipDiscount,
                            activeMembershipApplied
                    );
                })
                .toList();

        return ResponseEntity.ok(out);
    }

    // Admin: publish a new time slot under a venue.
    @PostMapping("/venues/{venueId}/timeslots")
    public ResponseEntity<?> createTimeslot(@PathVariable Integer clubId,
                                            @PathVariable Integer venueId,
                                            @RequestBody TimeSlotUpsertRequest req) {
        if (!clubRepo.existsById(clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        Venue venue = venueRepo.findById(venueId).orElse(null);
        if (venue == null || !clubId.equals(venue.getClubId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Venue not found");
        }

        if (req.getStartTime() == null || req.getEndTime() == null) {
            return ResponseEntity.badRequest().body("Missing startTime/endTime");
        }
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            return ResponseEntity.badRequest().body("endTime must be later than startTime");
        }

        Integer maxCap = req.getMaxCapacity();
        if (maxCap == null) maxCap = 1;
        if (maxCap <= 0) {
            return ResponseEntity.badRequest().body("maxCapacity must be > 0");
        }
        BigDecimal price = normalizePrice(req.getPrice());
        if (price.signum() < 0) {
            return ResponseEntity.badRequest().body("price must be >= 0");
        }

        TimeSlot ts = new TimeSlot();
        ts.setVenueId(venueId);
        ts.setStartTime(req.getStartTime());
        ts.setEndTime(req.getEndTime());
        ts.setMaxCapacity(maxCap);
        ts.setPrice(price);

        TimeSlot saved = timeSlotRepo.save(ts);
        return ResponseEntity.ok(new TimeSlotResponse(
                saved.getTimeslotId(),
                saved.getVenueId(),
                clubId,
                safe(venue.getVenueName()),
                saved.getStartTime(),
                saved.getEndTime(),
                saved.getMaxCapacity(),
                normalizePrice(saved.getPrice()),
                0L,
                saved.getMaxCapacity(),
                normalizePrice(saved.getPrice()),
                "",
                membershipService.normalizeDiscount(null),
                false
        ));
    }

    @PutMapping("/venues/{venueId}/timeslots/{timeslotId}")
    public ResponseEntity<?> updateTimeslot(@PathVariable Integer clubId,
                                            @PathVariable Integer venueId,
                                            @PathVariable Integer timeslotId,
                                            @RequestBody TimeSlotUpsertRequest req) {
        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        Venue venue = venueRepo.findById(venueId).orElse(null);
        if (venue == null || !clubId.equals(venue.getClubId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Venue not found");
        }

        TimeSlot ts = timeSlotRepo.findById(timeslotId).orElse(null);
        if (ts == null || !venueId.equals(ts.getVenueId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Time slot not found");
        }

        if (req.getStartTime() == null || req.getEndTime() == null) {
            return ResponseEntity.badRequest().body("Missing startTime/endTime");
        }
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            return ResponseEntity.badRequest().body("endTime must be later than startTime");
        }

        Integer maxCap = req.getMaxCapacity();
        if (maxCap == null) maxCap = ts.getMaxCapacity();
        if (maxCap == null || maxCap <= 0) {
            return ResponseEntity.badRequest().body("maxCapacity must be > 0");
        }

        long booked = bookingRepo.countByTimeslotIdAndStatusIn(ts.getTimeslotId(), ACTIVE_BOOKING_STATUSES);
        if (booked > maxCap) {
            return ResponseEntity.badRequest().body("maxCapacity cannot be less than current booked count");
        }

        BigDecimal price = req.getPrice() == null ? normalizePrice(ts.getPrice()) : normalizePrice(req.getPrice());
        if (price.signum() < 0) {
            return ResponseEntity.badRequest().body("price must be >= 0");
        }

        ts.setStartTime(req.getStartTime());
        ts.setEndTime(req.getEndTime());
        ts.setMaxCapacity(maxCap);
        ts.setPrice(price);

        TimeSlot saved = timeSlotRepo.save(ts);
        long remaining = Math.max(0L, (long) saved.getMaxCapacity() - booked);
        return ResponseEntity.ok(new TimeSlotResponse(
                saved.getTimeslotId(),
                saved.getVenueId(),
                clubId,
                safe(venue.getVenueName()),
                saved.getStartTime(),
                saved.getEndTime(),
                saved.getMaxCapacity(),
                normalizePrice(saved.getPrice()),
                booked,
                remaining,
                normalizePrice(saved.getPrice()),
                "",
                membershipService.normalizeDiscount(null),
                false
        ));
    }

    @DeleteMapping("/venues/{venueId}/timeslots/{timeslotId}")
    public ResponseEntity<?> deleteTimeslot(@PathVariable Integer clubId,
                                            @PathVariable Integer venueId,
                                            @PathVariable Integer timeslotId) {
        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        Venue venue = venueRepo.findById(venueId).orElse(null);
        if (venue == null || !clubId.equals(venue.getClubId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Venue not found");
        }

        TimeSlot ts = timeSlotRepo.findById(timeslotId).orElse(null);
        if (ts == null || !venueId.equals(ts.getVenueId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Time slot not found");
        }

        timeSlotRepo.delete(ts);
        return ResponseEntity.ok(java.util.Map.of("deleted", true));
    }

    private ResponseEntity<?> requireClubAdmin(User me, Integer clubId) {
        if (me.getRole() == null || me.getRole() == User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only club accounts can manage time slots");
        }

        boolean isAdmin = me.getRole() == User.Role.ADMIN;
        boolean isClubAdmin = clubAdminRepo.existsByUserIdAndClubId(me.getUserId(), clubId);
        if (!isAdmin && !isClubAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only manage your own club");
        }
        return null;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static BigDecimal normalizePrice(BigDecimal price) {
        if (price == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return price.setScale(2, RoundingMode.HALF_UP);
    }
}
