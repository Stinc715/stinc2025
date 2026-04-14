package com.clubportal.service;

import com.clubportal.model.BookingRecord;
import com.clubportal.model.ChatMessage;
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
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProfileDataRightsService {

    private static final String STATUS_PENDING = "PENDING";

    private final UserMembershipRepository userMembershipRepository;
    private final BookingRecordRepository bookingRecordRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final ProfileDeletionRequestRepository profileDeletionRequestRepository;
    private final UserAvatarService userAvatarService;

    public ProfileDataRightsService(UserMembershipRepository userMembershipRepository,
                                    BookingRecordRepository bookingRecordRepository,
                                    ChatSessionRepository chatSessionRepository,
                                    ChatMessageRepository chatMessageRepository,
                                    CheckoutSessionRepository checkoutSessionRepository,
                                    TransactionRecordRepository transactionRecordRepository,
                                    ProfileDeletionRequestRepository profileDeletionRequestRepository,
                                    UserAvatarService userAvatarService) {
        this.userMembershipRepository = userMembershipRepository;
        this.bookingRecordRepository = bookingRecordRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.checkoutSessionRepository = checkoutSessionRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.profileDeletionRequestRepository = profileDeletionRequestRepository;
        this.userAvatarService = userAvatarService;
    }

    public Map<String, Object> buildExport(User user) {
        Map<String, Object> export = new LinkedHashMap<>();
        export.put("generatedAt", Instant.now());
        export.put("profile", toProfilePayload(user));
        export.put("memberships", toMembershipPayloads(userMembershipRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId())));
        export.put("bookings", toBookingPayloads(bookingRecordRepository.findByUserIdOrderByBookingTimeDesc(user.getUserId())));
        export.put("checkoutSessions", toCheckoutSessionPayloads(checkoutSessionRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId())));
        export.put("transactions", toTransactionPayloads(transactionRecordRepository.findByUserIdOrderByTransactionTimeDesc(user.getUserId())));
        export.put("chatSessions", toChatSessionPayloads(user));
        export.put("deletionRequests", toDeletionRequestPayloads(profileDeletionRequestRepository.findByUserIdOrderByRequestedAtDesc(user.getUserId())));
        return export;
    }

    public DeletionRequestSubmission submitDeletionRequest(User user, String rawReason) {
        ProfileDeletionRequest existing = profileDeletionRequestRepository
                .findFirstByUserIdAndStatusOrderByRequestedAtDesc(user.getUserId(), STATUS_PENDING)
                .orElse(null);
        if (existing != null) {
            return new DeletionRequestSubmission(existing, false);
        }

        ProfileDeletionRequest request = new ProfileDeletionRequest();
        request.setUserId(user.getUserId());
        request.setEmailSnapshot(safe(user.getEmail()));
        request.setDisplayNameSnapshot(safe(user.getUsername()));
        request.setRoleSnapshot(user.getRole() == null ? "USER" : user.getRole().name());
        request.setReason(normalizeReason(rawReason));
        request.setStatus(STATUS_PENDING);
        ProfileDeletionRequest saved = profileDeletionRequestRepository.save(request);
        return new DeletionRequestSubmission(saved, true);
    }

    public Map<String, Object> toDeletionRequestPayload(ProfileDeletionRequest request) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("requestId", request.getRequestId());
        out.put("status", safe(request.getStatus()));
        out.put("reason", safe(request.getReason()));
        out.put("requestedAt", request.getRequestedAt());
        out.put("resolvedAt", request.getResolvedAt());
        out.put("resolutionNote", safe(request.getResolutionNote()));
        out.put("emailSnapshot", safe(request.getEmailSnapshot()));
        out.put("displayNameSnapshot", safe(request.getDisplayNameSnapshot()));
        out.put("roleSnapshot", safe(request.getRoleSnapshot()));
        return out;
    }

    private Map<String, Object> toProfilePayload(User user) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", user.getUserId());
        out.put("displayName", safe(user.getUsername()));
        out.put("email", safe(user.getEmail()));
        out.put("phone", safe(user.getPhone()));
        out.put("role", user.getRole() == null ? "USER" : user.getRole().name());
        out.put("createdAt", user.getCreatedAt());
        String avatarUrl = safe(userAvatarService.publicAvatarUrl(user));
        if (!avatarUrl.isBlank()) {
            out.put("avatarUrl", avatarUrl);
        }
        return out;
    }

    private List<Map<String, Object>> toMembershipPayloads(List<UserMembership> memberships) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (UserMembership membership : memberships) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("userMembershipId", membership.getUserMembershipId());
            row.put("planId", membership.getPlanId());
            row.put("status", membership.getStatus());
            row.put("startDate", membership.getStartDate());
            row.put("endDate", membership.getEndDate());
            row.put("includedBookingsTotal", membership.getIncludedBookingsTotal());
            row.put("remainingBookings", membership.getRemainingBookings());
            row.put("createdAt", membership.getCreatedAt());
            out.add(row);
        }
        return out;
    }

    private List<Map<String, Object>> toBookingPayloads(List<BookingRecord> bookings) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (BookingRecord booking : bookings) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("bookingId", booking.getBookingId());
            row.put("timeslotId", booking.getTimeslotId());
            row.put("status", booking.getStatus());
            row.put("pricePaid", booking.getPricePaid());
            row.put("userMembershipId", booking.getUserMembershipId());
            row.put("membershipCreditUsed", booking.getMembershipCreditUsed());
            row.put("bookingVerificationCode", safe(booking.getBookingVerificationCode()));
            row.put("bookingTime", booking.getBookingTime());
            out.add(row);
        }
        return out;
    }

    private List<Map<String, Object>> toCheckoutSessionPayloads(List<CheckoutSession> sessions) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (CheckoutSession session : sessions) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("sessionId", safe(session.getSessionId()));
            row.put("orderNo", safe(session.getOrderNo()));
            row.put("clubId", session.getClubId());
            row.put("type", safe(session.getType()));
            row.put("timeslotId", session.getTimeslotId());
            row.put("membershipPlanId", session.getMembershipPlanId());
            row.put("bookingId", session.getBookingId());
            row.put("userMembershipId", session.getUserMembershipId());
            row.put("transactionId", session.getTransactionId());
            row.put("amount", session.getAmount());
            row.put("currency", safe(session.getCurrency()));
            row.put("status", safe(session.getStatus()));
            row.put("provider", safe(session.getProvider()));
            row.put("providerSessionId", safe(session.getProviderSessionId()));
            row.put("expiresAt", session.getExpiresAt());
            row.put("completedAt", session.getCompletedAt());
            row.put("cancelledAt", session.getCancelledAt());
            row.put("createdAt", session.getCreatedAt());
            row.put("updatedAt", session.getUpdatedAt());
            out.add(row);
        }
        return out;
    }

    private List<Map<String, Object>> toTransactionPayloads(List<TransactionRecord> transactions) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (TransactionRecord transaction : transactions) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("transactionId", transaction.getTransactionId());
            row.put("userMembershipId", transaction.getUserMembershipId());
            row.put("amount", transaction.getAmount());
            row.put("paymentMethod", safe(transaction.getPaymentMethod()));
            row.put("status", safe(transaction.getStatus()));
            row.put("transactionTime", transaction.getTransactionTime());
            out.add(row);
        }
        return out;
    }

    private List<Map<String, Object>> toChatSessionPayloads(User user) {
        List<Map<String, Object>> out = new ArrayList<>();
        List<ChatSession> sessions = chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(user.getUserId());
        for (ChatSession session : sessions) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("sessionId", session.getSessionId());
            row.put("clubId", session.getClubId());
            row.put("chatMode", session.getChatMode() == null ? null : session.getChatMode().name());
            row.put("handoffRequestedAt", session.getHandoffRequestedAt());
            row.put("handoffReason", session.getHandoffReason() == null ? null : session.getHandoffReason().name());
            row.put("clubUnreadCount", session.getClubUnreadCount());
            row.put("createdAt", session.getCreatedAt());
            row.put("updatedAt", session.getUpdatedAt());
            row.put("messages", toChatMessagePayloads(chatMessageRepository.findByClubIdAndUserIdOrderByCreatedAtAscMessageIdAsc(
                    session.getClubId(),
                    user.getUserId()
            )));
            out.add(row);
        }
        return out;
    }

    private List<Map<String, Object>> toChatMessagePayloads(List<ChatMessage> messages) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ChatMessage message : messages) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("messageId", message.getMessageId());
            row.put("sender", safe(message.getSender()));
            row.put("messageText", safe(message.getMessageText()));
            row.put("answerSource", safe(message.getAnswerSource()));
            row.put("matchedFaqId", message.getMatchedFaqId());
            row.put("handoffSuggested", message.isHandoffSuggested());
            row.put("readByClub", message.isReadByClub());
            row.put("readByUser", message.isReadByUser());
            row.put("createdAt", message.getCreatedAt());
            out.add(row);
        }
        return out;
    }

    private List<Map<String, Object>> toDeletionRequestPayloads(List<ProfileDeletionRequest> requests) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ProfileDeletionRequest request : requests) {
            out.add(toDeletionRequestPayload(request));
        }
        return out;
    }

    private static String normalizeReason(String reason) {
        String trimmed = safe(reason);
        if (trimmed.isBlank()) {
            return "";
        }
        return trimmed.length() > 500 ? trimmed.substring(0, 500) : trimmed;
    }

    private static String safe(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    public record DeletionRequestSubmission(ProfileDeletionRequest request, boolean created) {
    }
}
