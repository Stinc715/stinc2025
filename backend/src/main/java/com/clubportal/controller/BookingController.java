package com.clubportal.controller;

import com.clubportal.dto.BookingResponse;
import com.clubportal.dto.ClubTimeslotBookingMemberResponse;
import com.clubportal.dto.ClubTimeslotBookingResponse;
import com.clubportal.dto.MyBookingResponse;
import com.clubportal.model.BookingRecord;
import com.clubportal.model.Club;
import com.clubportal.model.MembershipPlan;
import com.clubportal.model.TimeSlot;
import com.clubportal.model.User;
import com.clubportal.model.UserMembership;
import com.clubportal.model.Venue;
import com.clubportal.repository.BookingRecordRepository;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.repository.MembershipPlanRepository;
import com.clubportal.repository.TimeSlotRepository;
import com.clubportal.repository.UserMembershipRepository;
import com.clubportal.repository.UserRepository;
import com.clubportal.repository.VenueRepository;
import com.clubportal.service.CurrentUserService;
import com.clubportal.service.MembershipService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class BookingController {

    private static final List<String> OCCUPIED_BOOKING_STATUSES = List.of("PENDING", "APPROVED", "CHECKED");
    private static final List<String> USER_CANCELABLE_BOOKING_STATUSES = List.of("PENDING", "APPROVED");
    private static final List<String> CLUB_VISIBLE_BOOKING_STATUSES = List.of("PENDING", "APPROVED", "CHECKED", "CANCELLED");

    private final TimeSlotRepository timeSlotRepo;
    private final BookingRecordRepository bookingRepo;
    private final VenueRepository venueRepo;
    private final ClubRepository clubRepo;
    private final UserRepository userRepo;
    private final ClubAdminRepository clubAdminRepo;
    private final CurrentUserService currentUserService;
    private final UserMembershipRepository userMembershipRepo;
    private final MembershipPlanRepository membershipPlanRepo;
    private final MembershipService membershipService;

    public BookingController(TimeSlotRepository timeSlotRepo,
                             BookingRecordRepository bookingRepo,
                             VenueRepository venueRepo,
                             ClubRepository clubRepo,
                             UserRepository userRepo,
                             ClubAdminRepository clubAdminRepo,
                             CurrentUserService currentUserService,
                             UserMembershipRepository userMembershipRepo,
                             MembershipPlanRepository membershipPlanRepo,
                             MembershipService membershipService) {
        this.timeSlotRepo = timeSlotRepo;
        this.bookingRepo = bookingRepo;
        this.venueRepo = venueRepo;
        this.clubRepo = clubRepo;
        this.userRepo = userRepo;
        this.clubAdminRepo = clubAdminRepo;
        this.currentUserService = currentUserService;
        this.userMembershipRepo = userMembershipRepo;
        this.membershipPlanRepo = membershipPlanRepo;
        this.membershipService = membershipService;
    }

    @PostMapping("/timeslots/{timeslotId}/bookings")
    @Transactional
    public ResponseEntity<?> createBooking(@PathVariable Integer timeslotId) {
        User me = currentUserService.requireUser();
        if (me.getRole() == null || me.getRole() != User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only user accounts can book");
        }

        TimeSlot slot = timeSlotRepo.findByIdForUpdate(timeslotId).orElse(null);
        if (slot == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Time slot not found");
        }

        boolean already = bookingRepo.existsByUserIdAndTimeslotIdAndStatusIn(me.getUserId(), timeslotId, OCCUPIED_BOOKING_STATUSES);
        if (already) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already booked");
        }

        long booked = bookingRepo.countByTimeslotIdAndStatusIn(timeslotId, OCCUPIED_BOOKING_STATUSES);
        if (booked >= slot.getMaxCapacity()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Time slot is full");
        }

        Venue venue = venueRepo.findById(slot.getVenueId()).orElse(null);
        if (venue == null || venue.getClubId() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Venue not found");
        }

        Optional<MembershipService.ActiveMembershipContext> activeMembership = membershipService.findActiveMembership(
                me.getUserId(),
                venue.getClubId(),
                LocalDate.now()
        );
        BigDecimal pricePaid = activeMembership
                .map(ctx -> membershipService.calculateDiscountedPrice(slot.getPrice(), ctx.plan().getDiscountPercent()))
                .orElse(membershipService.normalizePrice(slot.getPrice()));

        BookingRecord br = new BookingRecord();
        br.setUserId(me.getUserId());
        br.setTimeslotId(timeslotId);
        br.setStatus("PENDING");
        br.setPricePaid(pricePaid);
        br.setUserMembershipId(activeMembership.map(ctx -> ctx.membership().getUserMembershipId()).orElse(null));

        BookingRecord saved = bookingRepo.save(br);
        return ResponseEntity.ok(new BookingResponse(saved.getBookingId(), saved.getTimeslotId(), saved.getStatus()));
    }

    @DeleteMapping("/timeslots/{timeslotId}/bookings/me")
    @Transactional
    public ResponseEntity<?> cancelMyBooking(@PathVariable Integer timeslotId) {
        User me = currentUserService.requireUser();
        if (me.getRole() == null || me.getRole() != User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only user accounts can cancel bookings");
        }

        Optional<BookingRecord> active = bookingRepo.findFirstByUserIdAndTimeslotIdAndStatusInOrderByBookingTimeDesc(
                me.getUserId(),
                timeslotId,
                USER_CANCELABLE_BOOKING_STATUSES
        );
        if (active.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active booking found for this time slot");
        }

        BookingRecord booking = active.get();
        booking.setStatus("CANCELLED");
        BookingRecord saved = bookingRepo.save(booking);
        return ResponseEntity.ok(new BookingResponse(saved.getBookingId(), saved.getTimeslotId(), saved.getStatus()));
    }

    @GetMapping("/my/bookings")
    public ResponseEntity<?> myBookings() {
        User me = currentUserService.requireUser();
        if (me.getRole() == null || me.getRole() != User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only user accounts can view personal bookings");
        }

        List<BookingRecord> records = bookingRepo.findByUserIdOrderByBookingTimeDesc(me.getUserId());
        if (records.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Integer> slotIds = records.stream()
                .map(BookingRecord::getTimeslotId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (slotIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        Map<Integer, TimeSlot> slotById = new HashMap<>();
        for (TimeSlot ts : timeSlotRepo.findAllById(slotIds)) {
            slotById.put(ts.getTimeslotId(), ts);
        }

        Set<Integer> venueIds = slotById.values().stream()
                .map(TimeSlot::getVenueId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        Map<Integer, Venue> venueById = new HashMap<>();
        if (!venueIds.isEmpty()) {
            for (Venue v : venueRepo.findAllById(venueIds)) {
                venueById.put(v.getVenueId(), v);
            }
        }

        Set<Integer> clubIds = venueById.values().stream()
                .map(Venue::getClubId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        Map<Integer, Club> clubById = new HashMap<>();
        if (!clubIds.isEmpty()) {
            for (Club c : clubRepo.findAllById(clubIds)) {
                clubById.put(c.getClubId(), c);
            }
        }

        Set<Integer> membershipIds = records.stream()
                .map(BookingRecord::getUserMembershipId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        Map<Integer, UserMembership> membershipById = new HashMap<>();
        if (!membershipIds.isEmpty()) {
            for (UserMembership membership : userMembershipRepo.findAllById(membershipIds)) {
                membershipById.put(membership.getUserMembershipId(), membership);
            }
        }

        Set<Integer> planIds = membershipById.values().stream()
                .map(UserMembership::getPlanId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        Map<Integer, MembershipPlan> planById = new HashMap<>();
        if (!planIds.isEmpty()) {
            for (MembershipPlan plan : membershipPlanRepo.findAllById(planIds)) {
                planById.put(plan.getPlanId(), plan);
            }
        }

        List<MyBookingResponse> out = new ArrayList<>();
        for (BookingRecord record : records) {
            TimeSlot ts = slotById.get(record.getTimeslotId());
            if (ts == null) continue;

            Venue venue = venueById.get(ts.getVenueId());
            Club club = venue == null ? null : clubById.get(venue.getClubId());
            UserMembership membership = membershipById.get(record.getUserMembershipId());
            MembershipPlan plan = membership == null ? null : planById.get(membership.getPlanId());
            BigDecimal basePrice = normalizePrice(ts.getPrice());
            BigDecimal actualPrice = record.getPricePaid() == null ? basePrice : normalizePrice(record.getPricePaid());

            out.add(new MyBookingResponse(
                    record.getBookingId(),
                    record.getTimeslotId(),
                    safe(record.getStatus()),
                    record.getBookingTime(),
                    club == null ? null : club.getClubId(),
                    club == null ? "" : safe(club.getClubName()),
                    venue == null ? null : venue.getVenueId(),
                    venue == null ? "" : safe(venue.getVenueName()),
                    ts.getStartTime(),
                    ts.getEndTime(),
                    ts.getMaxCapacity(),
                    actualPrice,
                    basePrice,
                    plan == null ? "" : safe(plan.getPlanName()),
                    plan == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : membershipService.normalizeDiscount(plan.getDiscountPercent()),
                    plan != null
            ));
        }

        return ResponseEntity.ok(out);
    }

    @GetMapping("/clubs/{clubId}/timeslot-bookings")
    public ResponseEntity<?> listClubTimeslotBookings(@PathVariable Integer clubId,
                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (!clubRepo.existsById(clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }
        if (to.isBefore(from)) {
            return ResponseEntity.badRequest().body("Invalid date range");
        }

        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        List<Venue> venues = venueRepo.findByClubId(clubId);
        if (venues.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Integer> venueIds = new ArrayList<>();
        Map<Integer, String> venueNameById = new HashMap<>();
        for (Venue venue : venues) {
            venueIds.add(venue.getVenueId());
            venueNameById.put(venue.getVenueId(), safe(venue.getVenueName()));
        }

        LocalDateTime startInclusive = from.atStartOfDay();
        LocalDateTime endExclusive = to.plusDays(1).atStartOfDay();
        List<TimeSlot> slots = timeSlotRepo.findByVenueIdInAndStartTimeBetween(venueIds, startInclusive, endExclusive);
        if (slots.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Integer> slotIds = slots.stream()
                .map(TimeSlot::getTimeslotId)
                .filter(Objects::nonNull)
                .toList();

        List<BookingRecord> bookings = bookingRepo.findByTimeslotIdInOrderByBookingTimeAsc(slotIds);
        Map<Integer, TimeSlot> slotById = slots.stream().collect(java.util.stream.Collectors.toMap(TimeSlot::getTimeslotId, slot -> slot));

        Set<Integer> userIds = bookings.stream()
                .map(BookingRecord::getUserId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        Map<Integer, User> userById = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (User user : userRepo.findAllById(userIds)) {
                userById.put(user.getUserId(), user);
            }
        }

        Set<Integer> membershipIds = bookings.stream()
                .map(BookingRecord::getUserMembershipId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        Map<Integer, UserMembership> membershipById = new HashMap<>();
        if (!membershipIds.isEmpty()) {
            for (UserMembership membership : userMembershipRepo.findAllById(membershipIds)) {
                membershipById.put(membership.getUserMembershipId(), membership);
            }
        }

        Set<Integer> planIds = membershipById.values().stream()
                .map(UserMembership::getPlanId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        Map<Integer, MembershipPlan> planById = new HashMap<>();
        if (!planIds.isEmpty()) {
            for (MembershipPlan plan : membershipPlanRepo.findAllById(planIds)) {
                planById.put(plan.getPlanId(), plan);
            }
        }

        Map<Integer, List<ClubTimeslotBookingMemberResponse>> membersBySlot = new HashMap<>();
        for (BookingRecord record : bookings) {
            User user = userById.get(record.getUserId());
            UserMembership membership = membershipById.get(record.getUserMembershipId());
            MembershipPlan plan = membership == null ? null : planById.get(membership.getPlanId());
            TimeSlot memberSlot = slotById.get(record.getTimeslotId());
            BigDecimal pricePaid = record.getPricePaid() == null
                    ? (memberSlot == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : normalizePrice(memberSlot.getPrice()))
                    : normalizePrice(record.getPricePaid());
            ClubTimeslotBookingMemberResponse member = new ClubTimeslotBookingMemberResponse(
                    record.getBookingId(),
                    record.getUserId(),
                    user == null ? "" : safe(user.getUsername()),
                    user == null ? "" : safe(user.getEmail()),
                    safe(record.getStatus()),
                    record.getBookingTime(),
                    pricePaid,
                    plan == null ? "" : safe(plan.getPlanName()),
                    membership == null ? "" : membershipService.effectiveStatus(membership, LocalDate.now())
            );
            membersBySlot.computeIfAbsent(record.getTimeslotId(), k -> new ArrayList<>()).add(member);
        }

        List<ClubTimeslotBookingResponse> out = slots.stream()
                .sorted(Comparator
                        .comparing(TimeSlot::getStartTime)
                        .thenComparing(TimeSlot::getVenueId)
                        .thenComparing(TimeSlot::getTimeslotId))
                .map(slot -> {
                    List<ClubTimeslotBookingMemberResponse> members = membersBySlot.getOrDefault(slot.getTimeslotId(), List.of());
                    return new ClubTimeslotBookingResponse(
                            slot.getTimeslotId(),
                            slot.getVenueId(),
                            venueNameById.getOrDefault(slot.getVenueId(), ""),
                            slot.getStartTime(),
                            slot.getEndTime(),
                            slot.getMaxCapacity(),
                            normalizePrice(slot.getPrice()),
                    members.stream().filter(member -> isOccupiedStatus(member.status())).count(),
                    members
                    );
                })
                .toList();

        return ResponseEntity.ok(out);
    }

    @PatchMapping("/my/clubs/{clubId}/bookings/{bookingId}/status")
    @Transactional
    public ResponseEntity<?> updateClubBookingStatus(@PathVariable Integer clubId,
                                                     @PathVariable Integer bookingId,
                                                     @RequestBody Map<String, Object> body) {
        if (!clubRepo.existsById(clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        BookingRecord booking = bookingRepo.findById(bookingId).orElse(null);
        if (booking == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");
        }

        TimeSlot slot = timeSlotRepo.findById(booking.getTimeslotId()).orElse(null);
        if (slot == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Time slot not found");
        }
        Venue venue = venueRepo.findById(slot.getVenueId()).orElse(null);
        if (venue == null || !clubId.equals(venue.getClubId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only manage your own club bookings");
        }

        String nextStatus = normalizeStatus(body.get("status"));
        if (nextStatus.isBlank()) {
            return ResponseEntity.badRequest().body("Missing booking status");
        }
        if (!isKnownStatus(nextStatus)) {
            return ResponseEntity.badRequest().body("Unsupported booking status");
        }

        String currentStatus = normalizeStatus(booking.getStatus());
        if (!isAllowedTransition(currentStatus, nextStatus)) {
            return ResponseEntity.badRequest().body("Invalid booking status transition");
        }

        if (isOccupiedStatus(nextStatus) && !isOccupiedStatus(currentStatus)) {
            long occupied = bookingRepo.countByTimeslotIdAndStatusIn(slot.getTimeslotId(), OCCUPIED_BOOKING_STATUSES);
            if (occupied >= slot.getMaxCapacity()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Time slot is full");
            }
        }

        booking.setStatus(nextStatus);
        BookingRecord saved = bookingRepo.save(booking);
        return ResponseEntity.ok(new BookingResponse(saved.getBookingId(), saved.getTimeslotId(), saved.getStatus()));
    }

    private ResponseEntity<?> requireClubAdmin(User me, Integer clubId) {
        if (me.getRole() == null || me.getRole() == User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only club accounts can access club booking details");
        }

        boolean isAdmin = me.getRole() == User.Role.ADMIN;
        boolean isClubAdmin = clubAdminRepo.existsByUserIdAndClubId(me.getUserId(), clubId);
        if (!isAdmin && !isClubAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only access your own club booking details");
        }
        return null;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static boolean isKnownStatus(String status) {
        return CLUB_VISIBLE_BOOKING_STATUSES.contains(status);
    }

    private static boolean isOccupiedStatus(String status) {
        return OCCUPIED_BOOKING_STATUSES.contains(normalizeStatus(status));
    }

    private static String normalizeStatus(Object status) {
        return status == null ? "" : String.valueOf(status).trim().toUpperCase();
    }

    private static boolean isAllowedTransition(String currentStatus, String nextStatus) {
        String current = normalizeStatus(currentStatus);
        String next = normalizeStatus(nextStatus);
        if (current.equals(next)) return true;
        return switch (current) {
            case "PENDING" -> next.equals("APPROVED") || next.equals("CANCELLED");
            case "APPROVED" -> next.equals("PENDING") || next.equals("CHECKED") || next.equals("CANCELLED");
            case "CHECKED" -> next.equals("APPROVED") || next.equals("CANCELLED");
            case "CANCELLED" -> next.equals("PENDING") || next.equals("APPROVED");
            default -> false;
        };
    }

    private static BigDecimal normalizePrice(BigDecimal price) {
        if (price == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return price.setScale(2, RoundingMode.HALF_UP);
    }
}
