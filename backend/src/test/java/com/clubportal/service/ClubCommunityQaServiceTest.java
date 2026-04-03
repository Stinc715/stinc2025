package com.clubportal.service;

import com.clubportal.dto.ClubCommunityAnswerCreateRequest;
import com.clubportal.dto.ClubCommunityAnswerResponse;
import com.clubportal.dto.ClubCommunityQuestionBoardResponse;
import com.clubportal.dto.ClubCommunityQuestionCreateRequest;
import com.clubportal.dto.ClubCommunityQuestionResponse;
import com.clubportal.model.Club;
import com.clubportal.model.ClubCommunityAnswer;
import com.clubportal.model.ClubCommunityQuestion;
import com.clubportal.model.User;
import com.clubportal.repository.BookingRecordRepository;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubCommunityAnswerRepository;
import com.clubportal.repository.ClubCommunityQuestionRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClubCommunityQaServiceTest {

    @Test
    void getBoardBuildsFlagsAndCounts() {
        ClubRepository clubRepository = mock(ClubRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ClubAdminRepository clubAdminRepository = mock(ClubAdminRepository.class);
        BookingRecordRepository bookingRecordRepository = mock(BookingRecordRepository.class);
        ClubCommunityQuestionRepository questionRepository = mock(ClubCommunityQuestionRepository.class);
        ClubCommunityAnswerRepository answerRepository = mock(ClubCommunityAnswerRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);

        Club club = club(7, "manba basketball");
        User viewer = user(14, User.Role.USER, "alice");
        ClubCommunityQuestion question = question(31, 7, 14, "Is parking available?");
        ClubCommunityAnswer memberAnswer = answer(90, 31, 7, 14, ClubCommunityAnswer.ResponderType.USER, "Yes, there is street parking.");
        ClubCommunityAnswer clubAnswer = answer(91, 31, 7, 22, ClubCommunityAnswer.ResponderType.CLUB, "We also have a small car park.");

        when(clubRepository.findById(7)).thenReturn(Optional.of(club));
        when(currentUserService.findUserOrNull()).thenReturn(viewer);
        when(bookingRecordRepository.countEligibleBookingByUserIdAndClubId(14, 7)).thenReturn(1L);
        when(questionRepository.findByClubIdOrderByUpdatedAtDescQuestionIdDesc(7)).thenReturn(List.of(question));
        when(answerRepository.findByQuestionIdInOrderByCreatedAtAscAnswerIdAsc(List.of(31))).thenReturn(List.of(memberAnswer, clubAnswer));
        when(userRepository.findAllById(any())).thenReturn(List.of(viewer, user(22, User.Role.CLUB, "clubstaff")));

        ClubCommunityQaService service = new ClubCommunityQaService(
                clubRepository,
                userRepository,
                clubAdminRepository,
                bookingRecordRepository,
                questionRepository,
                answerRepository,
                currentUserService
        );

        ClubCommunityQuestionBoardResponse board = service.getBoard(7);

        assertTrue(board.canAsk());
        assertTrue(board.canAnswer());
        assertFalse(board.canReplyAsClub());
        assertEquals(1, board.questionCount());
        assertEquals(2, board.questions().get(0).answerCount());
        assertTrue(board.questions().get(0).clubAnswered());
        assertEquals("alice", board.questions().get(0).authorName());
        assertTrue(board.questions().get(0).canEdit());
        assertTrue(board.questions().get(0).canDelete());
        assertTrue(board.questions().get(0).answers().get(0).canEdit());
        assertTrue(board.questions().get(0).answers().get(0).canDelete());
        assertFalse(board.questions().get(0).answers().get(1).canEdit());
        assertEquals("manba basketball", board.questions().get(0).answers().get(1).authorName());
        assertEquals("CLUB", board.questions().get(0).answers().get(1).responderType());
    }

    @Test
    void bookedMemberCanAnswerButUnbookedMemberCannot() {
        ClubRepository clubRepository = mock(ClubRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ClubAdminRepository clubAdminRepository = mock(ClubAdminRepository.class);
        BookingRecordRepository bookingRecordRepository = mock(BookingRecordRepository.class);
        ClubCommunityQuestionRepository questionRepository = mock(ClubCommunityQuestionRepository.class);
        ClubCommunityAnswerRepository answerRepository = mock(ClubCommunityAnswerRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);

        Club club = club(4, "Hull Sports Centre");
        User member = user(9, User.Role.USER, "member9");
        ClubCommunityQuestion question = question(18, 4, 12, "Do you provide rackets?");
        when(clubRepository.findById(4)).thenReturn(Optional.of(club));
        when(currentUserService.requireUser()).thenReturn(member);
        when(questionRepository.findByQuestionIdAndClubId(18, 4)).thenReturn(Optional.of(question));
        when(answerRepository.save(any(ClubCommunityAnswer.class))).thenAnswer(invocation -> {
            ClubCommunityAnswer saved = invocation.getArgument(0);
            saved.setAnswerId(55);
            return saved;
        });
        when(userRepository.findAllById(any())).thenReturn(List.of(member));

        ClubCommunityQaService service = new ClubCommunityQaService(
                clubRepository,
                userRepository,
                clubAdminRepository,
                bookingRecordRepository,
                questionRepository,
                answerRepository,
                currentUserService
        );

        when(bookingRecordRepository.countEligibleBookingByUserIdAndClubId(9, 4)).thenReturn(0L);
        ResponseStatusException forbidden = assertThrows(ResponseStatusException.class, () ->
                service.answerQuestion(4, 18, new ClubCommunityAnswerCreateRequest("You can rent them at reception.")));
        assertEquals(HttpStatus.FORBIDDEN, forbidden.getStatusCode());

        when(bookingRecordRepository.countEligibleBookingByUserIdAndClubId(9, 4)).thenReturn(1L);
        ClubCommunityAnswerResponse response = service.answerQuestion(4, 18, new ClubCommunityAnswerCreateRequest("You can rent them at reception."));

        assertEquals("USER", response.responderType());
        assertEquals("member9", response.authorName());
        assertTrue(response.canEdit());
        assertTrue(response.canDelete());
        verify(questionRepository, atLeastOnce()).save(argThat(saved ->
                saved.getQuestionId().equals(18) && saved.getUpdatedAt() != null
        ));
    }

    @Test
    void clubAdminAnswerUsesClubIdentity() {
        ClubRepository clubRepository = mock(ClubRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ClubAdminRepository clubAdminRepository = mock(ClubAdminRepository.class);
        BookingRecordRepository bookingRecordRepository = mock(BookingRecordRepository.class);
        ClubCommunityQuestionRepository questionRepository = mock(ClubCommunityQuestionRepository.class);
        ClubCommunityAnswerRepository answerRepository = mock(ClubCommunityAnswerRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);

        Club club = club(4, "Hull Sports Centre");
        User clubUser = user(22, User.Role.CLUB, "clubstaff");
        ClubCommunityQuestion question = question(18, 4, 12, "Do you provide rackets?");
        when(clubRepository.findById(4)).thenReturn(Optional.of(club));
        when(currentUserService.requireUser()).thenReturn(clubUser);
        when(clubAdminRepository.existsByUserIdAndClubId(22, 4)).thenReturn(true);
        when(questionRepository.findByQuestionIdAndClubId(18, 4)).thenReturn(Optional.of(question));
        when(answerRepository.save(any(ClubCommunityAnswer.class))).thenAnswer(invocation -> {
            ClubCommunityAnswer saved = invocation.getArgument(0);
            saved.setAnswerId(61);
            return saved;
        });

        ClubCommunityQaService service = new ClubCommunityQaService(
                clubRepository,
                userRepository,
                clubAdminRepository,
                bookingRecordRepository,
                questionRepository,
                answerRepository,
                currentUserService
        );

        ClubCommunityAnswerResponse response = service.answerQuestion(4, 18, new ClubCommunityAnswerCreateRequest("Yes, racket hire is available."));

        assertEquals("CLUB", response.responderType());
        assertEquals("Hull Sports Centre", response.authorName());
        assertTrue(response.canEdit());
        assertTrue(response.canDelete());
        verify(answerRepository).save(argThat(saved ->
                saved.getResponderType() == ClubCommunityAnswer.ResponderType.CLUB
                        && saved.getUserId().equals(22)
                        && saved.getClubId().equals(4)
        ));
    }

    @Test
    void askQuestionRequiresMemberRole() {
        ClubRepository clubRepository = mock(ClubRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ClubAdminRepository clubAdminRepository = mock(ClubAdminRepository.class);
        BookingRecordRepository bookingRecordRepository = mock(BookingRecordRepository.class);
        ClubCommunityQuestionRepository questionRepository = mock(ClubCommunityQuestionRepository.class);
        ClubCommunityAnswerRepository answerRepository = mock(ClubCommunityAnswerRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);

        when(clubRepository.findById(3)).thenReturn(Optional.of(club(3, "Club Three")));
        when(currentUserService.requireUser()).thenReturn(user(41, User.Role.CLUB, "club"));

        ClubCommunityQaService service = new ClubCommunityQaService(
                clubRepository,
                userRepository,
                clubAdminRepository,
                bookingRecordRepository,
                questionRepository,
                answerRepository,
                currentUserService
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.askQuestion(3, new ClubCommunityQuestionCreateRequest("Can guests watch the games?")));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void memberCanUpdateAndDeleteOwnQuestion() {
        ClubRepository clubRepository = mock(ClubRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ClubAdminRepository clubAdminRepository = mock(ClubAdminRepository.class);
        BookingRecordRepository bookingRecordRepository = mock(BookingRecordRepository.class);
        ClubCommunityQuestionRepository questionRepository = mock(ClubCommunityQuestionRepository.class);
        ClubCommunityAnswerRepository answerRepository = mock(ClubCommunityAnswerRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);

        Club club = club(7, "manba basketball");
        User member = user(14, User.Role.USER, "alice");
        ClubCommunityQuestion question = question(31, 7, 14, "Old question");
        when(clubRepository.findById(7)).thenReturn(Optional.of(club));
        when(currentUserService.requireUser()).thenReturn(member);
        when(questionRepository.findByQuestionIdAndClubId(31, 7)).thenReturn(Optional.of(question));
        when(answerRepository.findByQuestionIdOrderByCreatedAtAscAnswerIdAsc(31)).thenReturn(List.of());
        when(questionRepository.save(any(ClubCommunityQuestion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClubCommunityQaService service = new ClubCommunityQaService(
                clubRepository,
                userRepository,
                clubAdminRepository,
                bookingRecordRepository,
                questionRepository,
                answerRepository,
                currentUserService
        );

        ClubCommunityQuestionResponse response = service.updateQuestion(7, 31, new ClubCommunityQuestionCreateRequest("Updated question"));

        assertEquals("Updated question", response.questionText());
        service.deleteQuestion(7, 31);
        verify(answerRepository).deleteByQuestionId(31);
        verify(questionRepository).delete(question);
    }

    @Test
    void memberCanUpdateAndDeleteOwnAnswer() {
        ClubRepository clubRepository = mock(ClubRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ClubAdminRepository clubAdminRepository = mock(ClubAdminRepository.class);
        BookingRecordRepository bookingRecordRepository = mock(BookingRecordRepository.class);
        ClubCommunityQuestionRepository questionRepository = mock(ClubCommunityQuestionRepository.class);
        ClubCommunityAnswerRepository answerRepository = mock(ClubCommunityAnswerRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);

        Club club = club(7, "manba basketball");
        User member = user(14, User.Role.USER, "alice");
        ClubCommunityQuestion question = question(31, 7, 15, "Is parking available?");
        ClubCommunityAnswer answer = answer(90, 31, 7, 14, ClubCommunityAnswer.ResponderType.USER, "Old reply");
        when(clubRepository.findById(7)).thenReturn(Optional.of(club));
        when(currentUserService.requireUser()).thenReturn(member);
        when(questionRepository.findByQuestionIdAndClubId(31, 7)).thenReturn(Optional.of(question));
        when(answerRepository.findByAnswerIdAndQuestionIdAndClubId(90, 31, 7)).thenReturn(Optional.of(answer));
        when(answerRepository.save(any(ClubCommunityAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClubCommunityQaService service = new ClubCommunityQaService(
                clubRepository,
                userRepository,
                clubAdminRepository,
                bookingRecordRepository,
                questionRepository,
                answerRepository,
                currentUserService
        );

        ClubCommunityAnswerResponse response = service.updateAnswer(7, 31, 90, new ClubCommunityAnswerCreateRequest("Updated reply"));

        assertEquals("Updated reply", response.answerText());
        service.deleteAnswer(7, 31, 90);
        verify(answerRepository).delete(answer);
    }

    @Test
    void clubStaffCanUpdateAndDeleteClubReply() {
        ClubRepository clubRepository = mock(ClubRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ClubAdminRepository clubAdminRepository = mock(ClubAdminRepository.class);
        BookingRecordRepository bookingRecordRepository = mock(BookingRecordRepository.class);
        ClubCommunityQuestionRepository questionRepository = mock(ClubCommunityQuestionRepository.class);
        ClubCommunityAnswerRepository answerRepository = mock(ClubCommunityAnswerRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);

        Club club = club(4, "Hull Sports Centre");
        User clubUser = user(22, User.Role.CLUB, "clubstaff");
        ClubCommunityQuestion question = question(18, 4, 12, "Do you provide rackets?");
        ClubCommunityAnswer answer = answer(61, 18, 4, 22, ClubCommunityAnswer.ResponderType.CLUB, "Old answer");
        when(clubRepository.findById(4)).thenReturn(Optional.of(club));
        when(currentUserService.requireUser()).thenReturn(clubUser);
        when(clubAdminRepository.existsByUserIdAndClubId(22, 4)).thenReturn(true);
        when(questionRepository.findByQuestionIdAndClubId(18, 4)).thenReturn(Optional.of(question));
        when(answerRepository.findByAnswerIdAndQuestionIdAndClubId(61, 18, 4)).thenReturn(Optional.of(answer));
        when(answerRepository.save(any(ClubCommunityAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClubCommunityQaService service = new ClubCommunityQaService(
                clubRepository,
                userRepository,
                clubAdminRepository,
                bookingRecordRepository,
                questionRepository,
                answerRepository,
                currentUserService
        );

        ClubCommunityAnswerResponse response = service.updateAnswer(4, 18, 61, new ClubCommunityAnswerCreateRequest("Updated answer"));

        assertEquals("Updated answer", response.answerText());
        service.deleteAnswer(4, 18, 61);
        verify(answerRepository).delete(answer);
    }

    private Club club(int clubId, String clubName) {
        Club club = new Club();
        club.setClubId(clubId);
        club.setClubName(clubName);
        return club;
    }

    private User user(int userId, User.Role role, String username) {
        User user = new User();
        user.setUserId(userId);
        user.setRole(role);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hash");
        return user;
    }

    private ClubCommunityQuestion question(int questionId, int clubId, int userId, String text) {
        ClubCommunityQuestion question = new ClubCommunityQuestion();
        question.setQuestionId(questionId);
        question.setClubId(clubId);
        question.setUserId(userId);
        question.setQuestionText(text);
        question.setCreatedAt(LocalDateTime.now().minusHours(1));
        question.setUpdatedAt(LocalDateTime.now().minusMinutes(5));
        return question;
    }

    private ClubCommunityAnswer answer(int answerId,
                                       int questionId,
                                       int clubId,
                                       int userId,
                                       ClubCommunityAnswer.ResponderType responderType,
                                       String text) {
        ClubCommunityAnswer answer = new ClubCommunityAnswer();
        answer.setAnswerId(answerId);
        answer.setQuestionId(questionId);
        answer.setClubId(clubId);
        answer.setUserId(userId);
        answer.setResponderType(responderType);
        answer.setAnswerText(text);
        answer.setCreatedAt(LocalDateTime.now().minusMinutes(answerId));
        return answer;
    }
}
