package com.clubportal.repository;

import com.clubportal.model.ChatMode;
import com.clubportal.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Integer> {
    Optional<ChatSession> findByClubIdAndUserId(Integer clubId, Integer userId);
    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(Integer userId);
    List<ChatSession> findByClubIdAndChatModeInOrderByUpdatedAtDesc(Integer clubId, List<ChatMode> chatModes);
    List<ChatSession> findByClubIdAndUserIdIn(Integer clubId, Collection<Integer> userIds);
}
