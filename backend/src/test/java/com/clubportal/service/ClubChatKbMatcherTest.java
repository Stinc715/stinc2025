package com.clubportal.service;

import com.clubportal.model.ClubChatKbEntry;
import com.clubportal.model.ClubChatKbLanguage;
import com.clubportal.repository.ClubChatKbEntryRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClubChatKbMatcherTest {

    private final ClubChatKbSupport support = new ClubChatKbSupport();
    private final ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
    private final ClubChatKbMatcher matcher = new ClubChatKbMatcher(
            repository,
            support,
            new NoOpClubChatKbSemanticScorer()
    );

    @Test
    void currentClubHitsOwnEntry() {
        when(repository.findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(2))
                .thenReturn(List.of(entry(
                        7,
                        2,
                        "Equipment policy",
                        "Yes. You may bring your own racket.",
                        List.of("bring your own racket", "own racket"),
                        List.of("Can I bring my own racket?"),
                        ClubChatKbLanguage.EN,
                        5,
                        true
                )));

        Optional<ClubChatKbMatcher.KbMatch> match = matcher.findBestMatch(2, "Can I bring my own racket?");

        assertTrue(match.isPresent());
        assertEquals("Yes. You may bring your own racket.", match.get().answerText());
    }

    @Test
    void sameQuestionCanResolveToDifferentClubs() {
        when(repository.findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(2))
                .thenReturn(List.of(entry(
                        11,
                        2,
                        "Guest policy",
                        "Club A allows one guest per booking.",
                        List.of("bring a guest", "guest policy"),
                        List.of("Can I bring a guest?"),
                        ClubChatKbLanguage.EN,
                        4,
                        true
                )));
        when(repository.findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(9))
                .thenReturn(List.of(entry(
                        12,
                        9,
                        "Guest policy",
                        "Club B does not allow guests on member courts.",
                        List.of("bring a guest", "guest policy"),
                        List.of("Can I bring a guest?"),
                        ClubChatKbLanguage.EN,
                        4,
                        true
                )));

        ClubChatKbMatcher.KbMatch clubTwoMatch = matcher.findBestMatch(2, "Can I bring a guest?").orElseThrow();
        ClubChatKbMatcher.KbMatch clubNineMatch = matcher.findBestMatch(9, "Can I bring a guest?").orElseThrow();

        assertEquals("Club A allows one guest per booking.", clubTwoMatch.answerText());
        assertEquals("Club B does not allow guests on member courts.", clubNineMatch.answerText());
    }

    @Test
    void disabledEntriesAreIgnored() {
        when(repository.findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(3))
                .thenReturn(List.of(entry(
                        21,
                        3,
                        "Parking policy",
                        "Parking is free after 6pm.",
                        List.of("parking"),
                        List.of("Do you have parking?"),
                        ClubChatKbLanguage.EN,
                        5,
                        false
                )));

        Optional<ClubChatKbMatcher.KbMatch> match = matcher.findBestMatch(3, "Do you have parking?");

        assertTrue(match.isEmpty());
    }

    @Test
    void englishQuestionsPreferEnglishOrAnyEntries() {
        when(repository.findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(5))
                .thenReturn(List.of(
                        entry(
                                31,
                                5,
                                "Equipment policy",
                                "Use the English equipment reply.",
                                List.of("bring your own racket"),
                                List.of("Can I bring my own racket?"),
                                ClubChatKbLanguage.EN,
                                4,
                                true
                        ),
                        entry(
                                32,
                                5,
                                "Equipment policy",
                                "Use the ANY equipment reply.",
                                List.of("bring your own racket"),
                                List.of("Can I bring my own racket?"),
                                ClubChatKbLanguage.ANY,
                                4,
                                true
                        )
                ));

        ClubChatKbMatcher.KbMatch match = matcher.findBestMatch(5, "Can I bring my own racket?").orElseThrow();

        assertEquals("Use the English equipment reply.", match.answerText());
    }

    @Test
    void chineseQuestionsPreferChineseOrAnyEntries() {
        when(repository.findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(6))
                .thenReturn(List.of(
                        entry(
                                41,
                                6,
                                "器材政策",
                                "使用中文资料库回复。",
                                List.of("自带球拍"),
                                List.of("我可以自带球拍吗"),
                                ClubChatKbLanguage.ZH,
                                4,
                                true
                        ),
                        entry(
                                42,
                                6,
                                "Equipment policy",
                                "Use the ANY equipment reply.",
                                List.of("自带球拍"),
                                List.of("我可以自带球拍吗"),
                                ClubChatKbLanguage.ANY,
                                4,
                                true
                        )
                ));

        ClubChatKbMatcher.KbMatch match = matcher.findBestMatch(6, "我可以自带球拍吗？").orElseThrow();

        assertEquals("使用中文资料库回复。", match.answerText());
    }

    private ClubChatKbEntry entry(int id,
                                  int clubId,
                                  String title,
                                  String answer,
                                  List<String> keywords,
                                  List<String> examples,
                                  ClubChatKbLanguage language,
                                  int priority,
                                  boolean enabled) {
        ClubChatKbEntry entry = new ClubChatKbEntry();
        entry.setId(id);
        entry.setClubId(clubId);
        entry.setQuestionTitle(title);
        entry.setAnswerText(answer);
        entry.setTriggerKeywords(support.encodeList(keywords));
        entry.setExampleQuestions(support.encodeList(examples));
        entry.setLanguage(language);
        entry.setPriority(priority);
        entry.setEnabled(enabled);
        return entry;
    }
}
