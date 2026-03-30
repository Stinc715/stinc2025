package com.clubportal.controller;

import com.clubportal.dto.ClubTimeslotBookingResponse;
import com.clubportal.dto.MyBookingResponse;
import com.clubportal.model.BookingRecord;
import com.clubportal.model.Club;
import com.clubportal.model.TimeSlot;
import com.clubportal.model.User;
import com.clubportal.model.Venue;
import com.clubportal.repository.BookingRecordRepository;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.repository.MembershipPlanRepository;
import com.clubportal.repository.TimeSlotRepository;
import com.clubportal.repository.UserMembershipRepository;
import com.clubportal.repository.UserRepository;
import com.clubportal.repository.VenueRepository;
import com.clubportal.service.CheckoutSessionService;
import com.clubportal.service.CurrentUserService;
import com.clubportal.service.MembershipService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookingControllerTest {

    @Test
    void myBookingsIncludesVerificationCode() {
        TimeSlotRepository timeSlotRepo = mock(TimeSlotRepository.class);
        BookingRecordRepository bookingRepo = mock(BookingRecordRepository.class);
        VenueRepository venueRepo = mock(VenueRepository.class);
        ClubRepository clubRepo = mock(ClubRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        ClubAdminRepository clubAdminRepo = mock(ClubAdminRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        UserMembershipRepository userMembershipRepo = mock(UserMembershipRepository.class);
        MembershipPlanRepository membershipPlanRepo = mock(MembershipPlanRepository.class);
        MembershipService membershipService = mock(MembershipService.class);
        CheckoutSessionService checkoutSessionService = mock(CheckoutSessionService.class);

        BookingController controller = new BookingController(
                timeSlotRepo,
                bookingRepo,
                venueRepo,
                clubRepo,
                userRepo,
                clubAdminRepo,
                currentUserService,
                userMembershipRepo,
                membershipPlanRepo,
                membershipService,
                checkoutSessionService
        );

        User me = new User();
        me.setUserId(6);
        me.setRole(User.Role.USER);

        BookingRecord booking = new BookingRecord();
        booking.setBookingId(24);
        booking.setUserId(6);
        booking.setTimeslotId(148);
        booking.setStatus("PENDING");
        booking.setPricePaid(new BigDecimal("0.80"));
        booking.setBookingVerificationCode("482905");

        TimeSlot slot = new TimeSlot();
        slot.setTimeslotId(148);
        slot.setVenueId(9);
        slot.setPrice(new BigDecimal("1.00"));
        slot.setMaxCapacity(12);
        slot.setStartTime(LocalDateTime.of(2026, 3, 29, 15, 0));
        slot.setEndTime(LocalDateTime.of(2026, 3, 29, 16, 0));

        Venue venue = new Venue();
        venue.setVenueId(9);
        venue.setClubId(2);
        venue.setVenueName("Court A");

        Club club = new Club();
        club.setClubId(2);
        club.setClubName("manba basketball");

        when(currentUserService.requireUser()).thenReturn(me);
        when(bookingRepo.findByUserIdOrderByBookingTimeDesc(6)).thenReturn(List.of(booking));
        when(timeSlotRepo.findAllById(List.of(148))).thenReturn(List.of(slot));
        when(venueRepo.findAllById(java.util.Set.of(9))).thenReturn(List.of(venue));
        when(clubRepo.findAllById(java.util.Set.of(2))).thenReturn(List.of(club));

        ResponseEntity<?> response = controller.myBookings();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(List.class, response.getBody());
        @SuppressWarnings("unchecked")
        List<MyBookingResponse> rows = (List<MyBookingResponse>) response.getBody();
        assertEquals(1, rows.size());
        assertEquals("482905", rows.get(0).bookingVerificationCode());
    }

    @Test
    void clubTimeslotBookingsIncludesVerificationCode() {
        TimeSlotRepository timeSlotRepo = mock(TimeSlotRepository.class);
        BookingRecordRepository bookingRepo = mock(BookingRecordRepository.class);
        VenueRepository venueRepo = mock(VenueRepository.class);
        ClubRepository clubRepo = mock(ClubRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        ClubAdminRepository clubAdminRepo = mock(ClubAdminRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        UserMembershipRepository userMembershipRepo = mock(UserMembershipRepository.class);
        MembershipPlanRepository membershipPlanRepo = mock(MembershipPlanRepository.class);
        MembershipService membershipService = mock(MembershipService.class);
        CheckoutSessionService checkoutSessionService = mock(CheckoutSessionService.class);

        BookingController controller = new BookingController(
                timeSlotRepo,
                bookingRepo,
                venueRepo,
                clubRepo,
                userRepo,
                clubAdminRepo,
                currentUserService,
                userMembershipRepo,
                membershipPlanRepo,
                membershipService,
                checkoutSessionService
        );

        User clubUser = new User();
        clubUser.setUserId(10);
        clubUser.setRole(User.Role.CLUB);

        Venue venue = new Venue();
        venue.setVenueId(9);
        venue.setClubId(2);
        venue.setVenueName("Court A");

        TimeSlot slot = new TimeSlot();
        slot.setTimeslotId(148);
        slot.setVenueId(9);
        slot.setPrice(new BigDecimal("1.00"));
        slot.setMaxCapacity(12);
        slot.setStartTime(LocalDateTime.of(2026, 3, 29, 15, 0));
        slot.setEndTime(LocalDateTime.of(2026, 3, 29, 16, 0));

        BookingRecord booking = new BookingRecord();
        booking.setBookingId(24);
        booking.setUserId(6);
        booking.setTimeslotId(148);
        booking.setStatus("PENDING");
        booking.setPricePaid(new BigDecimal("0.80"));
        booking.setBookingVerificationCode("482905");

        User member = new User();
        member.setUserId(6);
        member.setUsername("A");
        member.setEmail("member@example.com");

        when(clubRepo.existsById(2)).thenReturn(true);
        when(currentUserService.requireUser()).thenReturn(clubUser);
        when(clubAdminRepo.existsByUserIdAndClubId(10, 2)).thenReturn(true);
        when(venueRepo.findByClubId(2)).thenReturn(List.of(venue));
        when(timeSlotRepo.findByVenueIdInAndStartTimeBetween(
                List.of(9),
                LocalDate.of(2026, 3, 29).atStartOfDay(),
                LocalDate.of(2026, 3, 30).atStartOfDay()
        )).thenReturn(List.of(slot));
        when(bookingRepo.findByTimeslotIdInOrderByBookingTimeAsc(List.of(148))).thenReturn(List.of(booking));
        when(userRepo.findAllById(java.util.Set.of(6))).thenReturn(List.of(member));

        ResponseEntity<?> response = controller.listClubTimeslotBookings(
                2,
                LocalDate.of(2026, 3, 29),
                LocalDate.of(2026, 3, 29)
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(List.class, response.getBody());
        @SuppressWarnings("unchecked")
        List<ClubTimeslotBookingResponse> rows = (List<ClubTimeslotBookingResponse>) response.getBody();
        assertEquals(1, rows.size());
        assertEquals("482905", rows.get(0).members().get(0).bookingVerificationCode());
    }
}
