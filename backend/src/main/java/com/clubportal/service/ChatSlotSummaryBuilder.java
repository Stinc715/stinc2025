package com.clubportal.service;

import com.clubportal.dto.ClubChatContextDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatSlotSummaryBuilder {

    private static final DateTimeFormatter EN_TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.UK);
    private static final Pattern GBP_PRICE_PATTERN = Pattern.compile("(?i)gbp\\s*(\\d+(?:\\.\\d{1,2})?)");
    private static final Pattern PLAIN_PRICE_PATTERN = Pattern.compile("(?<!\\d)(\\d{1,3}(?:\\.\\d{1,2})?)(?!\\d)");
    private static final Pattern HOUR_24_PATTERN = Pattern.compile("(?<!\\d)([01]?\\d|2[0-3]):(?:[0-5]\\d)(?!\\d)");
    private static final Pattern HOUR_PM_PATTERN = Pattern.compile("(?i)(?<!\\d)(1[0-2]|0?[1-9])\\s*(?:pm|p\\.m\\.)");
    private static final Pattern HOUR_AM_PATTERN = Pattern.compile("(?i)(?<!\\d)(1[0-2]|0?[1-9])\\s*(?:am|a\\.m\\.)");

    public List<ClubChatContextDto.VisibleTimeslot> selectRelevantSlots(ClubChatContextDto context,
                                                                        String userMessage,
                                                                        int limit) {
        return inspectDiscovery(context, userMessage, limit).summarySlots();
    }

    public ClubChatContextDto.VisibleTimeslot selectRelevantSlot(ClubChatContextDto context, String userMessage) {
        return selectRelevantSlots(context, userMessage, 1).stream().findFirst().orElse(null);
    }

    public String summarizeSlots(ClubChatContextDto context,
                                 String userMessage,
                                 ChatLanguage language,
                                 int limit) {
        return summarizeSlots(context, inspectDiscovery(context, userMessage, limit).summarySlots(), language);
    }

    public String summarizeSlots(ClubChatContextDto context,
                                 List<ClubChatContextDto.VisibleTimeslot> slots,
                                 ChatLanguage language) {
        if (slots == null || slots.isEmpty()) {
            return "";
        }
        return slots.stream()
                .map(slot -> summarizeSlot(context, slot))
                .collect(java.util.stream.Collectors.joining("; "));
    }

    public String summarizeVisiblePrice(ClubChatContextDto context,
                                        String userMessage,
                                        ChatLanguage language) {
        ClubChatContextDto.VisibleTimeslot slot = selectRelevantSlot(context, userMessage);
        if (slot == null) {
            return "";
        }
        return "the visible price is " + money(context, slot.price()) + " for "
                + formatVenueName(slot) + " at " + formatTime(slot.startTime()) + ".";
    }

    public boolean hasTimeOfDayHint(String userMessage) {
        return SlotHints.from(null, userMessage, List.of()).hasTimeOfDayHint();
    }

    public DiscoverySelection inspectDiscovery(ClubChatContextDto context,
                                               String userMessage,
                                               int limit) {
        List<ClubChatContextDto.VisibleTimeslot> slots = context == null || context.visibleTimeslots() == null
                ? List.of()
                : context.visibleTimeslots();
        if (slots.isEmpty() || limit <= 0) {
            return new DiscoverySelection(false, TimeOfDay.NONE, List.of(), List.of(), List.of(), null);
        }

        SlotHints hints = SlotHints.from(context, userMessage, slots);
        List<ClubChatContextDto.VisibleTimeslot> afterDateFilter = applyDateFilter(slots, hints);
        List<ClubChatContextDto.VisibleTimeslot> afterTimeOfDayFilter = applyTimeOfDayFilter(afterDateFilter, hints);

        if (hints.hasTimeOfDayHint() && afterTimeOfDayFilter.isEmpty()) {
            return new DiscoverySelection(
                    hints.targetDate() != null,
                    hints.timeOfDay(),
                    afterDateFilter,
                    afterTimeOfDayFilter,
                    List.of(),
                    "NO_TIME_OF_DAY_MATCH"
            );
        }

        List<ClubChatContextDto.VisibleTimeslot> source = selectSourceSlots(slots, afterDateFilter, afterTimeOfDayFilter, hints);
        List<ClubChatContextDto.VisibleTimeslot> summarySlots = scoreSlots(source, hints, limit);
        return new DiscoverySelection(
                hints.targetDate() != null,
                hints.timeOfDay(),
                afterDateFilter,
                afterTimeOfDayFilter,
                summarySlots,
                null
        );
    }

    private List<ClubChatContextDto.VisibleTimeslot> selectSourceSlots(List<ClubChatContextDto.VisibleTimeslot> allSlots,
                                                                       List<ClubChatContextDto.VisibleTimeslot> afterDateFilter,
                                                                       List<ClubChatContextDto.VisibleTimeslot> afterTimeOfDayFilter,
                                                                       SlotHints hints) {
        if (hints.hasTimeOfDayHint()) {
            return afterTimeOfDayFilter;
        }
        if (hints.targetDate() != null) {
            return afterDateFilter;
        }
        return allSlots;
    }

    private List<ClubChatContextDto.VisibleTimeslot> scoreSlots(List<ClubChatContextDto.VisibleTimeslot> slots,
                                                                SlotHints hints,
                                                                int limit) {
        if (slots.isEmpty()) {
            return List.of();
        }

        List<ScoredSlot> scored = slots.stream()
                .map(slot -> new ScoredSlot(slot, score(slot, hints)))
                .sorted(Comparator
                        .comparingInt(ScoredSlot::score)
                        .reversed()
                        .thenComparing(scoredSlot -> scoredSlot.slot().startTime(), Comparator.nullsLast(LocalDateTime::compareTo))
                        .thenComparing(scoredSlot -> safe(scoredSlot.slot().venueName())))
                .toList();

        boolean hasRelevantMatch = scored.stream().anyMatch(item -> item.score() > 0);
        return (hasRelevantMatch ? scored.stream().filter(item -> item.score() > 0) : scored.stream())
                .map(ScoredSlot::slot)
                .limit(limit)
                .toList();
    }

    private List<ClubChatContextDto.VisibleTimeslot> applyDateFilter(List<ClubChatContextDto.VisibleTimeslot> slots,
                                                                     SlotHints hints) {
        if (hints.targetDate() == null) {
            return slots;
        }
        return slots.stream()
                .filter(slot -> slot != null
                        && slot.startTime() != null
                        && hints.targetDate().equals(slot.startTime().toLocalDate()))
                .toList();
    }

    private List<ClubChatContextDto.VisibleTimeslot> applyTimeOfDayFilter(List<ClubChatContextDto.VisibleTimeslot> slots,
                                                                          SlotHints hints) {
        if (!hints.hasTimeOfDayHint()) {
            return slots;
        }
        return slots.stream()
                .filter(slot -> slot != null && slot.startTime() != null)
                .filter(slot -> matchesTimeOfDay(slot.startTime().getHour(), hints))
                .toList();
    }

    private boolean matchesTimeOfDay(int hour, SlotHints hints) {
        return (hints.morning() && isMorning(hour))
                || (hints.afternoon() && isAfternoon(hour))
                || (hints.evening() && isEvening(hour))
                || (hints.night() && isNight(hour));
    }

    private int score(ClubChatContextDto.VisibleTimeslot slot, SlotHints hints) {
        if (slot == null) {
            return Integer.MIN_VALUE;
        }

        int score = 0;
        LocalDateTime startTime = slot.startTime();
        if (slot.remaining() > 0 && !hints.hasPrimaryHint()) {
            score += 3;
        }
        if (hints.targetDate() != null) {
            LocalDate slotDate = startTime == null ? null : startTime.toLocalDate();
            if (hints.targetDate().equals(slotDate)) {
                score += 120;
            }
        }
        if (hints.hourHint() != null && startTime != null && hints.hourHint().intValue() == startTime.getHour()) {
            score += 90;
        }
        if (startTime != null) {
            int hour = startTime.getHour();
            if (hints.evening() && isEvening(hour)) {
                score += 45;
            }
            if (hints.morning() && isMorning(hour)) {
                score += 45;
            }
            if (hints.afternoon() && isAfternoon(hour)) {
                score += 45;
            }
            if (hints.night() && isNight(hour)) {
                score += 45;
            }
        }
        if (!hints.venueHint().isBlank() && safe(slot.venueName()).toLowerCase(Locale.ROOT).contains(hints.venueHint())) {
            score += 65;
        }
        if (hints.priceHint() != null) {
            BigDecimal normalizedPriceHint = normalizeMoney(hints.priceHint());
            if (normalizedPriceHint.compareTo(normalizeMoney(slot.price())) == 0) {
                score += 80;
            }
            if (slot.basePrice() != null && normalizedPriceHint.compareTo(normalizeMoney(slot.basePrice())) == 0) {
                score += 50;
            }
        }
        return score;
    }

    private String summarizeSlot(ClubChatContextDto context, ClubChatContextDto.VisibleTimeslot slot) {
        return formatVenueName(slot) + " at " + formatTime(slot.startTime()) + ", " + money(context, slot.price());
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return "time unavailable";
        }
        return time.format(EN_TIME_FORMATTER).toLowerCase(Locale.UK);
    }

    private String formatVenueName(ClubChatContextDto.VisibleTimeslot slot) {
        if (slot == null || slot.venueName() == null || slot.venueName().isBlank()) {
            return "slot";
        }
        return slot.venueName();
    }

    private String money(ClubChatContextDto context, BigDecimal amount) {
        String currency = context == null || context.policy() == null || context.policy().currency() == null || context.policy().currency().isBlank()
                ? "GBP"
                : context.policy().currency();
        return currency + " " + normalizeMoney(amount).toPlainString();
    }

    private static BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isMorning(int hour) {
        return hour >= 5 && hour < 12;
    }

    private static boolean isAfternoon(int hour) {
        return hour >= 12 && hour < 17;
    }

    private static boolean isEvening(int hour) {
        return hour >= 17 && hour < 22;
    }

    private static boolean isNight(int hour) {
        return hour >= 22 || hour < 5;
    }

    public enum TimeOfDay {
        NONE,
        MORNING,
        AFTERNOON,
        EVENING,
        NIGHT
    }

    public record DiscoverySelection(boolean tomorrow,
                                     TimeOfDay timeOfDay,
                                     List<ClubChatContextDto.VisibleTimeslot> afterDateFilter,
                                     List<ClubChatContextDto.VisibleTimeslot> afterTimeOfDayFilter,
                                     List<ClubChatContextDto.VisibleTimeslot> summarySlots,
                                     String fallbackReason) {
    }

    private record ScoredSlot(ClubChatContextDto.VisibleTimeslot slot, int score) {
    }

    private record SlotHints(LocalDate targetDate,
                             Integer hourHint,
                             boolean evening,
                             boolean morning,
                             boolean afternoon,
                             boolean night,
                             String venueHint,
                             BigDecimal priceHint) {

        boolean hasPrimaryHint() {
            return targetDate != null
                    || hourHint != null
                    || evening
                    || morning
                    || afternoon
                    || night
                    || !venueHint.isBlank()
                    || priceHint != null;
        }

        boolean hasTimeOfDayHint() {
            return evening || morning || afternoon || night;
        }

        TimeOfDay timeOfDay() {
            if (evening) {
                return TimeOfDay.EVENING;
            }
            if (afternoon) {
                return TimeOfDay.AFTERNOON;
            }
            if (morning) {
                return TimeOfDay.MORNING;
            }
            if (night) {
                return TimeOfDay.NIGHT;
            }
            return TimeOfDay.NONE;
        }

        static SlotHints from(ClubChatContextDto context,
                              String userMessage,
                              List<ClubChatContextDto.VisibleTimeslot> slots) {
            String normalized = normalize(userMessage);
            return new SlotHints(
                    resolveTargetDate(context, normalized),
                    extractHourHint(normalized),
                    containsAny(normalized, "evening", "tonight"),
                    containsAny(normalized, "morning"),
                    containsAny(normalized, "afternoon", "noon"),
                    containsAny(normalized, "night", "late night"),
                    detectVenueHint(normalized, slots),
                    extractPriceHint(normalized)
            );
        }

        private static LocalDate resolveTargetDate(ClubChatContextDto context, String normalized) {
            LocalDate today = LocalDate.now(resolveZoneId(context));
            if (containsAny(normalized, "tomorrow")) {
                return today.plusDays(1);
            }
            if (containsAny(normalized, "today")) {
                return today;
            }
            return null;
        }

        private static ZoneId resolveZoneId(ClubChatContextDto context) {
            String zone = context == null || context.policy() == null ? "" : safe(context.policy().timezone());
            if (zone.isBlank()) {
                return ZoneId.systemDefault();
            }
            try {
                return ZoneId.of(zone);
            } catch (Exception ignored) {
                return ZoneId.systemDefault();
            }
        }

        private static Integer extractHourHint(String normalized) {
            Matcher hour24 = HOUR_24_PATTERN.matcher(normalized);
            if (hour24.find()) {
                return Integer.parseInt(hour24.group(1));
            }

            Matcher pm = HOUR_PM_PATTERN.matcher(normalized);
            if (pm.find()) {
                int raw = Integer.parseInt(pm.group(1));
                return raw == 12 ? 12 : raw + 12;
            }

            Matcher am = HOUR_AM_PATTERN.matcher(normalized);
            if (am.find()) {
                int raw = Integer.parseInt(am.group(1));
                return raw == 12 ? 0 : raw;
            }

            return null;
        }

        private static String detectVenueHint(String normalized, List<ClubChatContextDto.VisibleTimeslot> slots) {
            return slots.stream()
                    .map(ClubChatContextDto.VisibleTimeslot::venueName)
                    .filter(name -> name != null && !name.isBlank())
                    .map(name -> name.toLowerCase(Locale.ROOT))
                    .filter(normalized::contains)
                    .findFirst()
                    .orElse("");
        }

        private static BigDecimal extractPriceHint(String normalized) {
            Matcher gbp = GBP_PRICE_PATTERN.matcher(normalized);
            if (gbp.find()) {
                return new BigDecimal(gbp.group(1)).setScale(2, RoundingMode.HALF_UP);
            }
            Matcher plain = PLAIN_PRICE_PATTERN.matcher(normalized);
            while (plain.find()) {
                String candidate = plain.group(1);
                if (candidate.contains(".")) {
                    return new BigDecimal(candidate).setScale(2, RoundingMode.HALF_UP);
                }
            }
            return null;
        }

        private static boolean containsAny(String text, String... values) {
            for (String value : values) {
                if (text.contains(value)) {
                    return true;
                }
            }
            return false;
        }

        private static String normalize(String value) {
            return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        }
    }
}
