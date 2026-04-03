package com.clubportal.repository;

import com.clubportal.model.ClubCommunityAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClubCommunityAnswerRepository extends JpaRepository<ClubCommunityAnswer, Integer> {
    List<ClubCommunityAnswer> findByQuestionIdInOrderByCreatedAtAscAnswerIdAsc(Collection<Integer> questionIds);
    List<ClubCommunityAnswer> findByQuestionIdOrderByCreatedAtAscAnswerIdAsc(Integer questionId);
    Optional<ClubCommunityAnswer> findByAnswerIdAndQuestionIdAndClubId(Integer answerId, Integer questionId, Integer clubId);
    void deleteByQuestionId(Integer questionId);
}
