package com.clubportal.service;

import com.clubportal.dto.ClubChatContextDto;
import com.clubportal.dto.TimeSlotResponse;
import com.clubportal.model.Club;
import com.clubportal.model.MembershipPlan;
import com.clubportal.model.User;
import com.clubportal.model.UserMembership;
import com.clubportal.repository.ClubRepository;
import com.clubportal.repository.MembershipPlanRepository;
import com.clubportal.repository.UserRepository;
import com.clubportal.util.ClubTagCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class ClubChatContextService {

    private static final Logger log = LoggerFactory.getLogger(ClubChatContextService.class);
    private static final int MESSAGE_MAX_CHARS = 500;
    private static final int VISIBLE_SLOT_HORIZON_DAYS = 6;

    private final ClubRepository clubRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final UserRepository userRepository;
    private final MembershipService membershipService;
    private final ClubVisibleTimeslotService clubVisibleTimeslotService;

    @Value("${app.payments.currency:GBP}")
    private String paymentCurrency;

    public ClubChatContextService(ClubRepository clubRepository,
                                  MembershipPlanRepository membershipPlanRepository,
                                  UserRepository userRepository,
                                  MembershipService membershipService,
                                  ClubVisibleTimeslotService clubVisibleTimeslotService) {
        this.clubRepository = clubRepository;
        this.membershipPlanRepository = membershipPlanRepository;
        this.userRepository = userRepository;
        this.membershipService = membershipService;
        this.clubVisibleTimeslotService = clubVisibleTimeslotService;
    }

    public ClubChatContextDto buildContext(Integer clubId, Integer userId) {
        Club club = clubId == null ? null : clubRepository.findById(clubId).orElse(null);
        User viewerUser = userId == null ? null : userRepository.findById(userId).orElse(null);
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zoneId);

        ClubChatContextDto context = new ClubChatContextDto(
                buildClubInfo(club, clubId),
                buildVisibleTimeslots(clubId, viewerUser, today),
                buildMembershipPlans(clubId),
                buildViewerInfo(viewerUser, club, today),
                new ClubChatContextDto.PolicyInfo(
                        safe(paymentCurrency).isBlank() ? "GBP" : safe(paymentCurrency),
                        zoneId.getId(),
                        MESSAGE_MAX_CHARS
                )
        );
        logContext(clubId, userId, context);
        return context;
    }

    private ClubChatContextDto.ClubInfo buildClubInfo(Club club, Integer clubId) {
        if (club == null) {
            return new ClubChatContextDto.ClubInfo(clubId, "", "", "", List.of(), "", "", "", "", "");
        }
        List<String> tags = ClubTagCodec.decode(club.getCategoryTags(), club.getCategory());
        return new ClubChatContextDto.ClubInfo(
                club.getClubId(),
                safe(club.getClubName()),
                safe(club.getDescription()),
                ClubTagCodec.primary(tags, club.getCategory()),
                tags,
                safe(club.getDisplayLocation()),
                safe(club.getOpeningStart()),
                safe(club.getOpeningEnd()),
                safe(club.getEmail()),
                safe(club.getPhone())
        );
    }

    private List<ClubChatContextDto.VisibleTimeslot> buildVisibleTimeslots(Integer clubId, User viewerUser, LocalDate today) {
        LocalDate from = today;
        LocalDate to = today.plusDays(VISIBLE_SLOT_HORIZON_DAYS);
        List<TimeSlotResponse> slots = clubVisibleTimeslotService.listVisibleTimeslots(
                clubId,
                viewerUser,
                from,
                to,
                "club-chat-context"
        );
        if (slots.isEmpty()) {
            return List.of();
        }

        return slots.stream()
                .map(slot -> {
                    return new ClubChatContextDto.VisibleTimeslot(
                            slot.timeslotId(),
                            slot.venueId(),
                            safe(slot.venueName()),
                            slot.startTime(),
                            slot.endTime(),
                            normalizeCapacity(slot.maxCapacity()),
                            slot.bookedCount(),
                            slot.remaining(),
                            slot.price(),
                            slot.basePrice(),
                            safe(slot.membershipPlanName()),
                            safe(slot.membershipBenefitType()),
                            membershipService.normalizeDiscount(slot.membershipDiscountPercent()),
                            membershipService.normalizeIncludedBookings(slot.membershipIncludedBookings()),
                            membershipService.normalizeRemainingBookings(slot.membershipRemainingBookings()),
                            slot.membershipApplied()
                    );
                })
                .toList();
    }

    private List<ClubChatContextDto.MembershipPlanInfo> buildMembershipPlans(Integer clubId) {
        if (clubId == null) {
            return List.of();
        }
        return membershipService.catalogPlans(membershipPlanRepository.findByClubIdAndEnabledTrueOrderByDurationDaysAsc(clubId)).stream()
                .map(this::toMembershipPlanInfo)
                .toList();
    }

    private ClubChatContextDto.ViewerInfo buildViewerInfo(User viewerUser, Club club, LocalDate today) {
        if (viewerUser == null) {
            return new ClubChatContextDto.ViewerInfo(false, "", null, "", null);
        }

        String role = viewerUser.getRole() == null ? "" : viewerUser.getRole().toAccountType();
        ClubChatContextDto.ActiveMembershipInfo activeMembership = null;
        if (viewerUser.getRole() == User.Role.USER && club != null) {
            activeMembership = membershipService.findActiveMembership(viewerUser.getUserId(), club.getClubId(), today)
                    .map(ctx -> toActiveMembershipInfo(ctx.membership(), ctx.plan(), club, today))
                    .orElse(null);
        }

        return new ClubChatContextDto.ViewerInfo(
                true,
                role,
                viewerUser.getUserId(),
                safe(viewerUser.getUsername()),
                activeMembership
        );
    }

    private ClubChatContextDto.MembershipPlanInfo toMembershipPlanInfo(MembershipPlan plan) {
        return new ClubChatContextDto.MembershipPlanInfo(
                plan.getPlanId(),
                safe(plan.getPlanCode()),
                membershipService.normalizeBenefitType(plan.getBenefitType()),
                safe(plan.getPlanName()),
                membershipService.normalizePrice(plan.getPrice()),
                plan.getDurationDays() == null ? 0 : plan.getDurationDays(),
                membershipService.normalizeDiscount(plan.getDiscountPercent()),
                membershipService.normalizeIncludedBookings(plan.getIncludedBookings()),
                membershipService.normalizeEnabled(plan.getEnabled(), false),
                safe(plan.getDescription())
        );
    }

    private ClubChatContextDto.ActiveMembershipInfo toActiveMembershipInfo(UserMembership membership,
                                                                           MembershipPlan plan,
                                                                           Club club,
                                                                           LocalDate today) {
        return new ClubChatContextDto.ActiveMembershipInfo(
                membership.getUserMembershipId(),
                club == null ? null : club.getClubId(),
                club == null ? "" : safe(club.getClubName()),
                plan.getPlanId(),
                safe(plan.getPlanCode()),
                membershipService.normalizeBenefitType(plan.getBenefitType()),
                safe(plan.getPlanName()),
                membershipService.normalizePrice(plan.getPrice()),
                membershipService.normalizeDiscount(plan.getDiscountPercent()),
                membershipService.normalizeIncludedBookings(plan.getIncludedBookings()),
                membershipService.normalizeRemainingBookings(membership.getRemainingBookings()),
                membership.getStartDate(),
                membership.getEndDate(),
                membershipService.effectiveStatus(membership, plan, today)
        );
    }

    private static int normalizeCapacity(Integer raw) {
        return raw == null || raw < 0 ? 0 : raw;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void logContext(Integer clubId, Integer userId, ClubChatContextDto context) {
        List<ClubChatContextDto.VisibleTimeslot> slots = context == null || context.visibleTimeslots() == null
                ? List.of()
                : context.visibleTimeslots();
        List<ClubChatContextDto.MembershipPlanInfo> plans = context == null || context.membershipPlans() == null
                ? List.of()
                : context.membershipPlans();
        ClubChatContextDto.ViewerInfo viewer = context == null ? null : context.viewer();


        for (int i = 0; i < slots.size(); i++) {
            ClubChatContextDto.VisibleTimeslot slot = slots.get(i);
        }
    }
}
