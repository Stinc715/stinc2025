package com.clubportal.service;

import com.clubportal.model.BookingRecord;
import com.clubportal.model.ChatMessage;
import com.clubportal.model.ChatMode;
import com.clubportal.model.ChatSession;
import com.clubportal.model.CheckoutSession;
import com.clubportal.model.ProfileDeletionRequest;
import com.clubportal.model.TransactionRecord;
import com.clubportal.model.User;
import com.clubportal.model.UserMembership;
import com.clubportal.repository.BookingRecordRepository;
import com.clubportal.repository.ChatMessageRepository;
import com.clubportal.repository.ChatSessionRepository;
import com.clubportal.repository.CheckoutSessionRepository;
import com.clubportal.repository.ProfileDeletionRequestRepository;
import com.clubportal.repository.TransactionRecordRepository;
import com.clubportal.repository.UserMembershipRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfileDataRightsServiceTest {

    @Test
    void buildExportIncludesProfileBookingsMembershipsPaymentsChatAndDeletionRequests() {
        UserMembershipRepository membershipRepository = mock(UserMembershipRepository.class);
        BookingRecordRepository bookingRepository = mock(BookingRecordRepository.class);
        ChatSessionRepository chatSessionRepository = mock(ChatSessionRepository.class);
        ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
        CheckoutSessionRepository checkoutSessionRepository = mock(CheckoutSessionRepository.class);
        TransactionRecordRepository transactionRecordRepository = mock(TransactionRecordRepository.class);
        ProfileDeletionRequestRepository deletionRequestRepository = mock(ProfileDeletionRequestRepository.class);
        UserAvatarService userAvatarService = mock(UserAvatarService.class);

        ProfileDataRightsService service = new ProfileDataRightsService(
                membershipRepository,
                bookingRepository,
                chatSessionRepository,
                chatMessageRepository,
                checkoutSessionRepository,
                transactionRecordRepository,
                deletionRequestRepository,
                userAvatarService
        );

        User user = new User();
        user.setUserId(11);
        user.setUsername("Member");
        user.setEmail("member@example.com");
        user.setPhone("123");
        user.setRole(User.Role.USER);
        user.setCreatedAt(LocalDateTime.of(2026, 4, 1, 12, 0));

        UserMembership membership = new UserMembership();
        membership.setUserMembershipId(5);
        membership.setPlanId(9);
        membership.setStatus("ACTIVE");
        membership.setStartDate(LocalDate.of(2026, 4, 1));
        membership.setEndDate(LocalDate.of(2026, 5, 1));
        membership.setIncludedBookingsTotal(8);
        membership.setRemainingBookings(6);
        membership.setCreatedAt(LocalDateTime.of(2026, 4, 1, 12, 0));

        BookingRecord booking = new BookingRecord();
        booking.setBookingId(17);
        booking.setTimeslotId(3);
        booking.setStatus("APPROVED");
        booking.setPricePaid(new BigDecimal("9.50"));
        booking.setUserMembershipId(5);
        booking.setMembershipCreditUsed(true);
        booking.setBookingVerificationCode("123456");
        booking.setBookingTime(LocalDateTime.of(2026, 4, 2, 10, 0));

        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(88);
        chatSession.setClubId(7);
        chatSession.setUserId(11);
        chatSession.setChatMode(ChatMode.HUMAN);
        chatSession.setClubUnreadCount(1);
        chatSession.setCreatedAt(LocalDateTime.of(2026, 4, 3, 9, 0));
        chatSession.setUpdatedAt(LocalDateTime.of(2026, 4, 3, 9, 30));

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessageId(99);
        chatMessage.setClubId(7);
        chatMessage.setUserId(11);
        chatMessage.setSender("USER");
        chatMessage.setMessageText("Hello");
        chatMessage.setAnswerSource("AI");
        chatMessage.setMatchedFaqId(4);
        chatMessage.setHandoffSuggested(true);
        chatMessage.setReadByClub(false);
        chatMessage.setReadByUser(true);
        chatMessage.setCreatedAt(LocalDateTime.of(2026, 4, 3, 9, 5));

        CheckoutSession checkoutSession = new CheckoutSession();
        checkoutSession.setSessionId("chk_1");
        checkoutSession.setOrderNo("MB-1");
        checkoutSession.setClubId(7);
        checkoutSession.setType("MEMBERSHIP");
        checkoutSession.setMembershipPlanId(9);
        checkoutSession.setUserMembershipId(5);
        checkoutSession.setTransactionId(12);
        checkoutSession.setAmount(new BigDecimal("49.00"));
        checkoutSession.setCurrency("GBP");
        checkoutSession.setStatus("PAID");
        checkoutSession.setProvider("VIRTUAL_CHECKOUT");
        checkoutSession.setProviderSessionId("provider_1");
        checkoutSession.setExpiresAt(Instant.parse("2026-04-03T10:00:00Z"));
        checkoutSession.setCompletedAt(Instant.parse("2026-04-03T09:15:00Z"));
        checkoutSession.setCreatedAt(Instant.parse("2026-04-03T09:00:00Z"));
        checkoutSession.setUpdatedAt(Instant.parse("2026-04-03T09:15:00Z"));

        TransactionRecord transaction = new TransactionRecord();
        transaction.setTransactionId(12);
        transaction.setUserMembershipId(5);
        transaction.setAmount(new BigDecimal("49.00"));
        transaction.setPaymentMethod("VIRTUAL");
        transaction.setStatus("PAID");
        transaction.setTransactionTime(LocalDateTime.of(2026, 4, 3, 9, 15));

        ProfileDeletionRequest deletionRequest = new ProfileDeletionRequest();
        deletionRequest.setRequestId(3);
        deletionRequest.setUserId(11);
        deletionRequest.setEmailSnapshot("member@example.com");
        deletionRequest.setDisplayNameSnapshot("Member");
        deletionRequest.setRoleSnapshot("USER");
        deletionRequest.setReason("Delete me");
        deletionRequest.setStatus("PENDING");
        deletionRequest.setRequestedAt(LocalDateTime.of(2026, 4, 4, 12, 0));

        when(userAvatarService.publicAvatarUrl(user)).thenReturn("/uploads/avatar.png");
        when(membershipRepository.findByUserIdOrderByCreatedAtDesc(11)).thenReturn(List.of(membership));
        when(bookingRepository.findByUserIdOrderByBookingTimeDesc(11)).thenReturn(List.of(booking));
        when(chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(11)).thenReturn(List.of(chatSession));
        when(chatMessageRepository.findByClubIdAndUserIdOrderByCreatedAtAscMessageIdAsc(7, 11)).thenReturn(List.of(chatMessage));
        when(checkoutSessionRepository.findByUserIdOrderByCreatedAtDesc(11)).thenReturn(List.of(checkoutSession));
        when(transactionRecordRepository.findByUserIdOrderByTransactionTimeDesc(11)).thenReturn(List.of(transaction));
        when(deletionRequestRepository.findByUserIdOrderByRequestedAtDesc(11)).thenReturn(List.of(deletionRequest));

        Map<String, Object> export = service.buildExport(user);

        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) export.get("profile");
        assertEquals("Member", profile.get("displayName"));
        assertEquals("/uploads/avatar.png", profile.get("avatarUrl"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> memberships = (List<Map<String, Object>>) export.get("memberships");
        assertEquals(1, memberships.size());
        assertEquals(5, memberships.get(0).get("userMembershipId"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> bookings = (List<Map<String, Object>>) export.get("bookings");
        assertEquals("123456", bookings.get(0).get("bookingVerificationCode"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sessions = (List<Map<String, Object>>) export.get("chatSessions");
        assertEquals(1, sessions.size());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) sessions.get(0).get("messages");
        assertEquals("Hello", messages.get(0).get("messageText"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> checkoutSessions = (List<Map<String, Object>>) export.get("checkoutSessions");
        assertEquals("chk_1", checkoutSessions.get(0).get("sessionId"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> transactions = (List<Map<String, Object>>) export.get("transactions");
        assertEquals("VIRTUAL", transactions.get(0).get("paymentMethod"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> deletionRequests = (List<Map<String, Object>>) export.get("deletionRequests");
        assertEquals("Delete me", deletionRequests.get(0).get("reason"));
    }

    @Test
    void submitDeletionRequestCreatesOnceAndDeduplicatesPendingRequests() {
        UserMembershipRepository membershipRepository = mock(UserMembershipRepository.class);
        BookingRecordRepository bookingRepository = mock(BookingRecordRepository.class);
        ChatSessionRepository chatSessionRepository = mock(ChatSessionRepository.class);
        ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
        CheckoutSessionRepository checkoutSessionRepository = mock(CheckoutSessionRepository.class);
        TransactionRecordRepository transactionRecordRepository = mock(TransactionRecordRepository.class);
        ProfileDeletionRequestRepository deletionRequestRepository = mock(ProfileDeletionRequestRepository.class);
        UserAvatarService userAvatarService = mock(UserAvatarService.class);

        ProfileDataRightsService service = new ProfileDataRightsService(
                membershipRepository,
                bookingRepository,
                chatSessionRepository,
                chatMessageRepository,
                checkoutSessionRepository,
                transactionRecordRepository,
                deletionRequestRepository,
                userAvatarService
        );

        User user = new User();
        user.setUserId(11);
        user.setUsername("Member");
        user.setEmail("member@example.com");
        user.setRole(User.Role.USER);

        ProfileDeletionRequest saved = new ProfileDeletionRequest();
        saved.setRequestId(6);
        saved.setUserId(11);
        saved.setEmailSnapshot("member@example.com");
        saved.setDisplayNameSnapshot("Member");
        saved.setRoleSnapshot("USER");
        saved.setReason("Please delete my account");
        saved.setStatus("PENDING");
        saved.setRequestedAt(LocalDateTime.of(2026, 4, 13, 21, 0));

        when(deletionRequestRepository.findFirstByUserIdAndStatusOrderByRequestedAtDesc(11, "PENDING"))
                .thenReturn(Optional.empty(), Optional.of(saved));
        when(deletionRequestRepository.save(any(ProfileDeletionRequest.class))).thenReturn(saved);

        ProfileDataRightsService.DeletionRequestSubmission first = service.submitDeletionRequest(user, "Please delete my account");
        ProfileDataRightsService.DeletionRequestSubmission second = service.submitDeletionRequest(user, "Another reason");

        assertTrue(first.created());
        assertEquals(6, first.request().getRequestId());
        assertFalse(second.created());
        assertEquals(6, second.request().getRequestId());
    }
}
