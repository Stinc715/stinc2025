package com.clubportal.service;

import com.clubportal.dto.ClubChatContextDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatSlotMatcher {

    private static final Logger log = LoggerFactory.getLogger(ChatSlotMatcher.class);
    private static final Pattern AM_PM_TIME_PATTERN = Pattern.compile("(?i)\\b(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)\\b");
    private static final Pattern TWENTY_FOUR_HOUR_TIME_PATTERN = Pattern.compile("\\b(\\d{1,2}):(\\d{2})\\b");
    private static final Pattern GBP_AMOUNT_PATTERN = Pattern.compile("(?i)(?:gbp|£|拢)\\s*(\\d+(?:\\.\\d{1,2})?)");
    private static final Pattern DECIMAL_AMOUNT_PATTERN = Pattern.compile("(?<![:\\d])(\\d+\\.\\d{1,2})(?!\\d)");

    public ClubChatContextDto.VisibleTimeslot findMemberPriceSlot(String userMessage, ClubChatContextDto context) {
        List<ClubChatContextDto.VisibleTimeslot> membershipCandidates = visibleSlots(context).stream()
                .filter(slot -> slot != null
                        && slot.membershipApplied()
                        && slot.price() != null
                        && slot.basePrice() != null)
                .toList();

        SlotHints hints = extractHints(userMessage, context, membershipCandidates);
        log.info("[CLUB_CHAT_DEBUG] member-price hint: requestedPrice={}",
                hints.amounts().isEmpty() ? null : formatAmount(hints.amounts().get(0)));
        log.info("[CLUB_CHAT_DEBUG] member-price candidates: count={}", membershipCandidates.size());

        if (membershipCandidates.isEmpty()) {
            log.info("[CLUB_CHAT_DEBUG] member-price matched slot: none");
            return null;
        }

        ClubChatContextDto.VisibleTimeslot matchedSlot;
        if (!hints.amounts().isEmpty()) {
            List<ClubChatContextDto.VisibleTimeslot> amountMatched = membershipCandidates.stream()
                    .filter(slot -> matchesMentionedAmount(slot, hints.amounts()))
                    .toList();
            matchedSlot = singleOrNull(disambiguate(amountMatched, hints));
        } else {
            matchedSlot = membershipCandidates.size() == 1 ? membershipCandidates.get(0) : null;
        }

        logMemberPriceMatch(matchedSlot);
        return matchedSlot;
    }

    public ClubChatContextDto.VisibleTimeslot findBookingSlot(String userMessage, ClubChatContextDto context) {
        List<ClubChatContextDto.VisibleTimeslot> slots = visibleSlots(context);
        if (slots.isEmpty()) {
            log.info("[CLUB_CHAT_DEBUG] booking hint: venueHint={}, hourHint={}", null, null);
            log.info("[CLUB_CHAT_DEBUG] booking candidates: count=0");
            log.info("[CLUB_CHAT_DEBUG] booking matched slot: none");
            return null;
        }

        SlotHints hints = extractHints(userMessage, context, slots);
        List<ClubChatContextDto.VisibleTimeslot> datedSlots = applyDateHint(slots, hints.targetDate());
        log.info("[CLUB_CHAT_DEBUG] booking hint: venueHint={}, hourHint={}",
                hints.venueName(),
                hints.startTime() == null ? null : hints.startTime().getHour());
        log.info("[CLUB_CHAT_DEBUG] booking candidates: count={}", datedSlots.size());

        ClubChatContextDto.VisibleTimeslot matchedSlot = null;
        if (hints.venueName() != null && hints.startTime() != null) {
            ClubChatContextDto.VisibleTimeslot exact = singleOrNull(filterByVenueAndTime(datedSlots, hints.venueName(), hints.startTime(), true));
            if (exact != null) {
                matchedSlot = exact;
            } else {
                matchedSlot = singleOrNull(filterByVenueAndTime(datedSlots, hints.venueName(), hints.startTime(), false));
            }
        } else if (hints.startTime() != null) {
            ClubChatContextDto.VisibleTimeslot exact = singleOrNull(filterByTime(datedSlots, hints.startTime(), true));
            if (exact != null) {
                matchedSlot = exact;
            } else {
                matchedSlot = singleOrNull(filterByTime(datedSlots, hints.startTime(), false));
            }
        } else if (hints.venueName() != null) {
            matchedSlot = singleOrNull(filterByVenue(datedSlots, hints.venueName()));
        }

        logBookingMatch(matchedSlot);
        return matchedSlot;
    }

    public ClubChatContextDto.VisibleTimeslot findRelevantVisibleSlot(String userMessage, ClubChatContextDto context) {
        List<ClubChatContextDto.VisibleTimeslot> slots = visibleSlots(context);
        if (slots.isEmpty()) {
            return null;
        }

        SlotHints hints = extractHints(userMessage, context, slots);
        List<ClubChatContextDto.VisibleTimeslot> candidates = disambiguate(slots, hints);
        return candidates.size() == 1 ? candidates.get(0) : null;
    }

    private SlotHints extractHints(String userMessage,
                                   ClubChatContextDto context,
                                   List<ClubChatContextDto.VisibleTimeslot> slots) {
        String normalizedMessage = normalize(userMessage);
        return new SlotHints(
                extractVenueHint(normalizedMessage, slots),
                extractRequestedTime(userMessage),
                extractTargetDate(normalizedMessage, context),
                extractMentionedAmounts(userMessage)
        );
    }

    private List<ClubChatContextDto.VisibleTimeslot> disambiguate(List<ClubChatContextDto.VisibleTimeslot> slots, SlotHints hints) {
        List<ClubChatContextDto.VisibleTimeslot> candidates = applyDateHint(slots, hints.targetDate());
        if (hints.venueName() != null) {
            List<ClubChatContextDto.VisibleTimeslot> venueMatched = filterByVenue(candidates, hints.venueName());
            if (!venueMatched.isEmpty()) {
                candidates = venueMatched;
            }
        }
        if (hints.startTime() != null) {
            List<ClubChatContextDto.VisibleTimeslot> exactTime = filterByTime(candidates, hints.startTime(), true);
            if (!exactTime.isEmpty()) {
                candidates = exactTime;
            } else {
                List<ClubChatContextDto.VisibleTimeslot> byHour = filterByTime(candidates, hints.startTime(), false);
                if (!byHour.isEmpty()) {
                    candidates = byHour;
                }
            }
        }
        return candidates;
    }

    private List<ClubChatContextDto.VisibleTimeslot> applyDateHint(List<ClubChatContextDto.VisibleTimeslot> slots, LocalDate targetDate) {
        if (targetDate == null) {
            return slots;
        }
        return slots.stream()
                .filter(slot -> slot.startTime() != null && targetDate.equals(slot.startTime().toLocalDate()))
                .toList();
    }

    private List<ClubChatContextDto.VisibleTimeslot> filterByVenueAndTime(List<ClubChatContextDto.VisibleTimeslot> slots,
                                                                          String venueName,
                                                                          LocalTime requestedTime,
                                                                          boolean exactMinute) {
        return slots.stream()
                .filter(slot -> matchesVenue(slot, venueName))
                .filter(slot -> matchesTime(slot, requestedTime, exactMinute))
                .toList();
    }

    private List<ClubChatContextDto.VisibleTimeslot> filterByTime(List<ClubChatContextDto.VisibleTimeslot> slots,
                                                                  LocalTime requestedTime,
                                                                  boolean exactMinute) {
        return slots.stream()
                .filter(slot -> matchesTime(slot, requestedTime, exactMinute))
                .toList();
    }

    private List<ClubChatContextDto.VisibleTimeslot> filterByVenue(List<ClubChatContextDto.VisibleTimeslot> slots,
                                                                   String venueName) {
        return slots.stream()
                .filter(slot -> matchesVenue(slot, venueName))
                .toList();
    }

    private boolean matchesMentionedAmount(ClubChatContextDto.VisibleTimeslot slot, List<BigDecimal> amounts) {
        BigDecimal price = normalizeAmount(slot.price());
        BigDecimal basePrice = normalizeAmount(slot.basePrice());
        return amounts.stream().anyMatch(amount -> amount.equals(price) || amount.equals(basePrice));
    }

    private boolean matchesVenue(ClubChatContextDto.VisibleTimeslot slot, String venueName) {
        return slot != null && normalize(slot.venueName()).equals(venueName);
    }

    private boolean matchesTime(ClubChatContextDto.VisibleTimeslot slot, LocalTime requestedTime, boolean exactMinute) {
        if (slot == null || slot.startTime() == null || requestedTime == null) {
            return false;
        }
        LocalTime slotTime = slot.startTime().toLocalTime().withSecond(0).withNano(0);
        if (exactMinute) {
            return slotTime.equals(requestedTime.withSecond(0).withNano(0));
        }
        return slotTime.getHour() == requestedTime.getHour();
    }

    private String extractVenueHint(String normalizedMessage, List<ClubChatContextDto.VisibleTimeslot> slots) {
        return slots.stream()
                .map(ClubChatContextDto.VisibleTimeslot::venueName)
                .filter(name -> name != null && !name.isBlank())
                .map(this::normalize)
                .distinct()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .filter(venueName -> containsVenue(normalizedMessage, venueName))
                .findFirst()
                .orElse(null);
    }

    private boolean containsVenue(String normalizedMessage, String normalizedVenueName) {
        if (normalizedMessage.isBlank() || normalizedVenueName == null || normalizedVenueName.isBlank()) {
            return false;
        }
        if (normalizedVenueName.length() <= 2 || normalizedVenueName.matches("[a-z0-9]+")) {
            Pattern wordPattern = Pattern.compile("(?<![a-z0-9])" + Pattern.quote(normalizedVenueName) + "(?![a-z0-9])");
            return wordPattern.matcher(normalizedMessage).find();
        }
        return normalizedMessage.contains(normalizedVenueName);
    }

    private LocalDate extractTargetDate(String normalizedMessage, ClubChatContextDto context) {
        LocalDate today = resolveToday(context);
        if (normalizedMessage.contains("tomorrow")) {
            return today.plusDays(1);
        }
        if (normalizedMessage.contains("today")) {
            return today;
        }
        return null;
    }

    private LocalDate resolveToday(ClubChatContextDto context) {
        String timezone = context == null || context.policy() == null ? "" : context.policy().timezone();
        try {
            return LocalDate.now(timezone == null || timezone.isBlank() ? ZoneId.systemDefault() : ZoneId.of(timezone));
        } catch (Exception ignored) {
            return LocalDate.now(ZoneId.systemDefault());
        }
    }

    private LocalTime extractRequestedTime(String userMessage) {
        String message = userMessage == null ? "" : userMessage;

        Matcher amPmMatcher = AM_PM_TIME_PATTERN.matcher(message);
        if (amPmMatcher.find()) {
            int hour = Integer.parseInt(amPmMatcher.group(1));
            int minute = amPmMatcher.group(2) == null ? 0 : Integer.parseInt(amPmMatcher.group(2));
            String suffix = amPmMatcher.group(3).toLowerCase(Locale.ROOT);
            if ("am".equals(suffix) && hour == 12) {
                hour = 0;
            } else if ("pm".equals(suffix) && hour < 12) {
                hour += 12;
            }
            return safeTime(hour, minute);
        }

        Matcher twentyFourHourMatcher = TWENTY_FOUR_HOUR_TIME_PATTERN.matcher(message);
        if (twentyFourHourMatcher.find()) {
            int hour = Integer.parseInt(twentyFourHourMatcher.group(1));
            int minute = Integer.parseInt(twentyFourHourMatcher.group(2));
            return safeTime(hour, minute);
        }

        return null;
    }

    private LocalTime safeTime(int hour, int minute) {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            return null;
        }
        return LocalTime.of(hour, minute);
    }

    private List<BigDecimal> extractMentionedAmounts(String userMessage) {
        String message = userMessage == null ? "" : userMessage;
        Set<BigDecimal> amounts = new LinkedHashSet<>();
        Matcher gbpMatcher = GBP_AMOUNT_PATTERN.matcher(message);
        while (gbpMatcher.find()) {
            addAmount(amounts, gbpMatcher.group(1));
        }

        Matcher decimalMatcher = DECIMAL_AMOUNT_PATTERN.matcher(message);
        while (decimalMatcher.find()) {
            addAmount(amounts, decimalMatcher.group(1));
        }

        return new ArrayList<>(amounts);
    }

    private void addAmount(Set<BigDecimal> amounts, String raw) {
        try {
            amounts.add(normalizeAmount(new BigDecimal(raw)));
        } catch (Exception ignored) {
        }
    }

    private BigDecimal normalizeAmount(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    private List<ClubChatContextDto.VisibleTimeslot> visibleSlots(ClubChatContextDto context) {
        return context == null || context.visibleTimeslots() == null ? List.of() : context.visibleTimeslots();
    }

    private ClubChatContextDto.VisibleTimeslot singleOrNull(List<ClubChatContextDto.VisibleTimeslot> slots) {
        return slots.size() == 1 ? slots.get(0) : null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private void logMemberPriceMatch(ClubChatContextDto.VisibleTimeslot slot) {
        if (slot == null) {
            log.info("[CLUB_CHAT_DEBUG] member-price matched slot: none");
            return;
        }
        log.info("[CLUB_CHAT_DEBUG] member-price matched slot: venueName={}, startTime={}, endTime={}, price={}, basePrice={}, membershipApplied={}, membershipPlanName={}, membershipDiscountPercent={}",
                slot.venueName(),
                slot.startTime(),
                slot.endTime(),
                slot.price(),
                slot.basePrice(),
                slot.membershipApplied(),
                slot.membershipPlanName(),
                slot.membershipDiscountPercent());
    }

    private void logBookingMatch(ClubChatContextDto.VisibleTimeslot slot) {
        if (slot == null) {
            log.info("[CLUB_CHAT_DEBUG] booking matched slot: none");
            return;
        }
        log.info("[CLUB_CHAT_DEBUG] booking matched slot: venueName={}, startTime={}, endTime={}, price={}, basePrice={}, membershipApplied={}",
                slot.venueName(),
                slot.startTime(),
                slot.endTime(),
                slot.price(),
                slot.basePrice(),
                slot.membershipApplied());
    }

    private String formatAmount(BigDecimal amount) {
        return amount == null ? null : amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private record SlotHints(
            String venueName,
            LocalTime startTime,
            LocalDate targetDate,
            List<BigDecimal> amounts
    ) {
    }
}
