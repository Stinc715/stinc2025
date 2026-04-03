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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ClubCommunityQaService {

    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final ClubAdminRepository clubAdminRepository;
    private final BookingRecordRepository bookingRecordRepository;
    private final ClubCommunityQuestionRepository questionRepository;
    private final ClubCommunityAnswerRepository answerRepository;
    private final CurrentUserService currentUserService;

    public ClubCommunityQaService(ClubRepository clubRepository,
                                  UserRepository userRepository,
                                  ClubAdminRepository clubAdminRepository,
                                  BookingRecordRepository bookingRecordRepository,
                                  ClubCommunityQuestionRepository questionRepository,
                                  ClubCommunityAnswerRepository answerRepository,
                                  CurrentUserService currentUserService) {
        this.clubRepository = clubRepository;
        this.userRepository = userRepository;
        this.clubAdminRepository = clubAdminRepository;
        this.bookingRecordRepository = bookingRecordRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public ClubCommunityQuestionBoardResponse getBoard(Integer clubId) {
        Club club = requireClub(clubId);
        User viewer = currentUserService.findUserOrNull();
        boolean canAsk = canAsk(viewer);
        boolean canReplyAsClub = canReplyAsClub(viewer, clubId);
        boolean canAnswer = canReplyAsClub || canReplyAsMember(viewer, clubId);

        List<ClubCommunityQuestion> questions = questionRepository.findByClubIdOrderByUpdatedAtDescQuestionIdDesc(clubId);
        if (questions.isEmpty()) {
            return new ClubCommunityQuestionBoardResponse(clubId, canAsk, canAnswer, canReplyAsClub, 0, List.of());
        }

        List<Integer> questionIds = questions.stream()
                .map(ClubCommunityQuestion::getQuestionId)
                .filter(java.util.Objects::nonNull)
                .toList();
        List<ClubCommunityAnswer> answers = answerRepository.findByQuestionIdInOrderByCreatedAtAscAnswerIdAsc(questionIds);

        Map<Integer, List<ClubCommunityAnswer>> answersByQuestionId = new LinkedHashMap<>();
        Map<Integer, User> userById = loadUsers(questions, answers);

        for (ClubCommunityAnswer answer : answers) {
            answersByQuestionId.computeIfAbsent(answer.getQuestionId(), ignored -> new ArrayList<>()).add(answer);
        }

        List<ClubCommunityQuestionResponse> responseQuestions = questions.stream()
                .map(question -> toQuestionResponse(question, answersByQuestionId.getOrDefault(question.getQuestionId(), List.of()), userById, club, viewer, clubId))
                .toList();

        return new ClubCommunityQuestionBoardResponse(
                clubId,
                canAsk,
                canAnswer,
                canReplyAsClub,
                responseQuestions.size(),
                responseQuestions
        );
    }

    @Transactional
    public ClubCommunityQuestionResponse askQuestion(Integer clubId, ClubCommunityQuestionCreateRequest request) {
        Club club = requireClub(clubId);
        User me = currentUserService.requireUser();
        if (!canAsk(me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only member accounts can ask questions");
        }

        String questionText = normalizeQuestionText(request == null ? null : request.questionText());
        if (questionText == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question text is required (5-400 chars)");
        }

        LocalDateTime now = LocalDateTime.now();
        ClubCommunityQuestion question = new ClubCommunityQuestion();
        question.setClubId(clubId);
        question.setUserId(me.getUserId());
        question.setQuestionText(questionText);
        question.setCreatedAt(now);
        question.setUpdatedAt(now);

        ClubCommunityQuestion saved = questionRepository.save(question);
        Map<Integer, User> userById = Map.of(me.getUserId(), me);
        return toQuestionResponse(saved, List.of(), userById, club, me, clubId);
    }

    @Transactional
    public ClubCommunityQuestionResponse updateQuestion(Integer clubId,
                                                        Integer questionId,
                                                        ClubCommunityQuestionCreateRequest request) {
        Club club = requireClub(clubId);
        User me = currentUserService.requireUser();
        ClubCommunityQuestion question = questionRepository.findByQuestionIdAndClubId(questionId, clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        requireQuestionOwner(me, question);

        String questionText = normalizeQuestionText(request == null ? null : request.questionText());
        if (questionText == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question text is required (5-400 chars)");
        }

        question.setQuestionText(questionText);
        question.setUpdatedAt(LocalDateTime.now());
        ClubCommunityQuestion saved = questionRepository.save(question);
        List<ClubCommunityAnswer> answers = answerRepository.findByQuestionIdOrderByCreatedAtAscAnswerIdAsc(saved.getQuestionId());
        Map<Integer, User> userById = loadUsers(List.of(saved), answers);
        return toQuestionResponse(saved, answers, userById, club, me, clubId);
    }

    @Transactional
    public void deleteQuestion(Integer clubId, Integer questionId) {
        User me = currentUserService.requireUser();
        ClubCommunityQuestion question = questionRepository.findByQuestionIdAndClubId(questionId, clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        requireQuestionOwner(me, question);
        answerRepository.deleteByQuestionId(question.getQuestionId());
        questionRepository.delete(question);
    }

    @Transactional
    public ClubCommunityAnswerResponse answerQuestion(Integer clubId,
                                                      Integer questionId,
                                                      ClubCommunityAnswerCreateRequest request) {
        Club club = requireClub(clubId);
        User me = currentUserService.requireUser();
        boolean canReplyAsClub = canReplyAsClub(me, clubId);
        boolean canReplyAsMember = canReplyAsMember(me, clubId);
        if (!canReplyAsClub && !canReplyAsMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only club staff or booked members can answer");
        }

        String answerText = normalizeAnswerText(request == null ? null : request.answerText());
        if (answerText == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Answer text is required (2-500 chars)");
        }

        ClubCommunityQuestion question = questionRepository.findByQuestionIdAndClubId(questionId, clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

        LocalDateTime now = LocalDateTime.now();
        ClubCommunityAnswer answer = new ClubCommunityAnswer();
        answer.setQuestionId(question.getQuestionId());
        answer.setClubId(clubId);
        answer.setUserId(me.getUserId());
        answer.setResponderType(canReplyAsClub ? ClubCommunityAnswer.ResponderType.CLUB : ClubCommunityAnswer.ResponderType.USER);
        answer.setAnswerText(answerText);
        answer.setCreatedAt(now);

        ClubCommunityAnswer saved = answerRepository.save(answer);
        question.setUpdatedAt(now);
        questionRepository.save(question);

        Map<Integer, User> userById = Map.of(me.getUserId(), me);
        return toAnswerResponse(saved, userById, club, me, clubId);
    }

    @Transactional
    public ClubCommunityAnswerResponse updateAnswer(Integer clubId,
                                                    Integer questionId,
                                                    Integer answerId,
                                                    ClubCommunityAnswerCreateRequest request) {
        Club club = requireClub(clubId);
        User me = currentUserService.requireUser();
        ClubCommunityQuestion question = questionRepository.findByQuestionIdAndClubId(questionId, clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        ClubCommunityAnswer answer = answerRepository.findByAnswerIdAndQuestionIdAndClubId(answerId, questionId, clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found"));
        requireAnswerManager(me, answer, clubId);

        String answerText = normalizeAnswerText(request == null ? null : request.answerText());
        if (answerText == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Answer text is required (2-500 chars)");
        }

        answer.setAnswerText(answerText);
        ClubCommunityAnswer saved = answerRepository.save(answer);
        question.setUpdatedAt(LocalDateTime.now());
        questionRepository.save(question);

        Map<Integer, User> userById = Map.of(me.getUserId(), me);
        return toAnswerResponse(saved, userById, club, me, clubId);
    }

    @Transactional
    public void deleteAnswer(Integer clubId, Integer questionId, Integer answerId) {
        User me = currentUserService.requireUser();
        ClubCommunityQuestion question = questionRepository.findByQuestionIdAndClubId(questionId, clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        ClubCommunityAnswer answer = answerRepository.findByAnswerIdAndQuestionIdAndClubId(answerId, questionId, clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found"));
        requireAnswerManager(me, answer, clubId);
        answerRepository.delete(answer);
        question.setUpdatedAt(LocalDateTime.now());
        questionRepository.save(question);
    }

    private Club requireClub(Integer clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found"));
    }

    private boolean canAsk(User viewer) {
        return viewer != null && viewer.getRole() == User.Role.USER;
    }

    private boolean canReplyAsClub(User viewer, Integer clubId) {
        if (viewer == null || viewer.getUserId() == null) {
            return false;
        }
        if (viewer.getRole() == User.Role.ADMIN) {
            return true;
        }
        return viewer.getRole() == User.Role.CLUB
                && clubAdminRepository.existsByUserIdAndClubId(viewer.getUserId(), clubId);
    }

    private boolean canReplyAsMember(User viewer, Integer clubId) {
        if (viewer == null || viewer.getUserId() == null) {
            return false;
        }
        if (viewer.getRole() != User.Role.USER) {
            return false;
        }
        return bookingRecordRepository.countEligibleBookingByUserIdAndClubId(viewer.getUserId(), clubId) > 0;
    }

    private void requireQuestionOwner(User viewer, ClubCommunityQuestion question) {
        if (viewer == null || viewer.getUserId() == null || viewer.getRole() != User.Role.USER
                || !Objects.equals(viewer.getUserId(), question.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the member who asked this question can edit or delete it");
        }
    }

    private void requireAnswerManager(User viewer, ClubCommunityAnswer answer, Integer clubId) {
        if (viewer == null || viewer.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot edit or delete this reply");
        }
        if (answer.getResponderType() == ClubCommunityAnswer.ResponderType.CLUB) {
            if (canReplyAsClub(viewer, clubId)) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only club staff can edit or delete club replies");
        }
        if (viewer.getRole() == User.Role.USER && Objects.equals(viewer.getUserId(), answer.getUserId())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the member who posted this reply can edit or delete it");
    }

    private Map<Integer, User> loadUsers(List<ClubCommunityQuestion> questions, List<ClubCommunityAnswer> answers) {
        Map<Integer, User> userById = new HashMap<>();
        List<Integer> userIds = new ArrayList<>();
        for (ClubCommunityQuestion question : questions) {
            if (question.getUserId() != null) {
                userIds.add(question.getUserId());
            }
        }
        for (ClubCommunityAnswer answer : answers) {
            if (answer.getUserId() != null) {
                userIds.add(answer.getUserId());
            }
        }
        if (userIds.isEmpty()) {
            return userById;
        }
        for (User user : userRepository.findAllById(userIds)) {
            userById.put(user.getUserId(), user);
        }
        return userById;
    }

    private ClubCommunityQuestionResponse toQuestionResponse(ClubCommunityQuestion question,
                                                             List<ClubCommunityAnswer> answers,
                                                             Map<Integer, User> userById,
                                                             Club club,
                                                             User viewer,
                                                             Integer clubId) {
        List<ClubCommunityAnswerResponse> answerResponses = answers.stream()
                .map(answer -> toAnswerResponse(answer, userById, club, viewer, clubId))
                .toList();

        boolean clubAnswered = answers.stream()
                .anyMatch(answer -> answer.getResponderType() == ClubCommunityAnswer.ResponderType.CLUB);
        boolean canEdit = viewer != null
                && viewer.getRole() == User.Role.USER
                && Objects.equals(viewer.getUserId(), question.getUserId());

        return new ClubCommunityQuestionResponse(
                question.getQuestionId(),
                question.getUserId(),
                resolveUserName(userById.get(question.getUserId()), question.getUserId()),
                safe(question.getQuestionText()),
                clubAnswered,
                answerResponses.size(),
                canEdit,
                canEdit,
                question.getCreatedAt(),
                question.getUpdatedAt(),
                answerResponses
        );
    }

    private ClubCommunityAnswerResponse toAnswerResponse(ClubCommunityAnswer answer,
                                                         Map<Integer, User> userById,
                                                         Club club,
                                                         User viewer,
                                                         Integer clubId) {
        String authorName = answer.getResponderType() == ClubCommunityAnswer.ResponderType.CLUB
                ? safe(club.getClubName())
                : resolveUserName(userById.get(answer.getUserId()), answer.getUserId());
        boolean canEdit = answer.getResponderType() == ClubCommunityAnswer.ResponderType.CLUB
                ? canReplyAsClub(viewer, clubId)
                : viewer != null
                    && viewer.getRole() == User.Role.USER
                    && Objects.equals(viewer.getUserId(), answer.getUserId());
        return new ClubCommunityAnswerResponse(
                answer.getAnswerId(),
                answer.getUserId(),
                authorName,
                answer.getResponderType().name(),
                safe(answer.getAnswerText()),
                canEdit,
                canEdit,
                answer.getCreatedAt()
        );
    }

    private static String resolveUserName(User user, Integer fallbackUserId) {
        if (user == null) {
            return fallbackUserId == null ? "Member" : "Member #" + fallbackUserId;
        }
        return safe(user.getUsername()).isBlank() ? ("Member #" + user.getUserId()) : safe(user.getUsername());
    }

    private static String normalizeQuestionText(String raw) {
        String normalized = normalizeText(raw);
        if (normalized == null || normalized.length() < 5 || normalized.length() > 400) {
            return null;
        }
        return normalized;
    }

    private static String normalizeAnswerText(String raw) {
        String normalized = normalizeText(raw);
        if (normalized == null || normalized.length() < 2 || normalized.length() > 500) {
            return null;
        }
        return normalized;
    }

    private static String normalizeText(String raw) {
        String value = safe(raw).replaceAll("\\s+", " ").trim();
        return value.isBlank() ? null : value;
    }

    private static String safe(String raw) {
        return raw == null ? "" : raw.trim();
    }
}
