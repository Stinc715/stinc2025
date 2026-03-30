package com.clubportal.repository;

import com.clubportal.model.ClubChatKbEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubChatKbEntryRepository extends JpaRepository<ClubChatKbEntry, Integer> {
    List<ClubChatKbEntry> findAllByOrderByClubIdAscPriorityDescUpdatedAtDescIdDesc();
    List<ClubChatKbEntry> findByClubIdOrderByPriorityDescUpdatedAtDescIdDesc(Integer clubId);
    List<ClubChatKbEntry> findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(Integer clubId);
    List<ClubChatKbEntry> findByClubIdAndEnabledTrueAndQuestionEmbeddingIsNotNullOrderByPriorityDescUpdatedAtDescIdDesc(Integer clubId);
    Optional<ClubChatKbEntry> findByIdAndClubId(Integer id, Integer clubId);
}
