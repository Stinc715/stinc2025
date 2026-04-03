package com.clubportal.repository;

import com.clubportal.model.ClubCommunityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubCommunityQuestionRepository extends JpaRepository<ClubCommunityQuestion, Integer> {
    List<ClubCommunityQuestion> findByClubIdOrderByUpdatedAtDescQuestionIdDesc(Integer clubId);
    Optional<ClubCommunityQuestion> findByQuestionIdAndClubId(Integer questionId, Integer clubId);
}
