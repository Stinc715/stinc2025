package com.clubportal.repository;

import com.clubportal.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    List<ChatMessage> findByClubIdAndUserIdOrderByCreatedAtAscMessageIdAsc(Integer clubId, Integer userId);
    List<ChatMessage> findByClubIdOrderByCreatedAtDescMessageIdDesc(Integer clubId);
    long countByClubIdAndUserIdAndSenderAndReadByUserFalse(Integer clubId, Integer userId, String sender);
    long countByClubIdAndUserIdAndSenderAndReadByClubFalse(Integer clubId, Integer userId, String sender);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ChatMessage m
               set m.readByUser = true
             where m.clubId = :clubId
               and m.userId = :userId
               and m.sender = 'CLUB'
               and m.readByUser = false
            """)
    int markClubMessagesReadByUser(@Param("clubId") Integer clubId, @Param("userId") Integer userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ChatMessage m
               set m.readByClub = true
             where m.clubId = :clubId
               and m.userId = :userId
               and m.sender = 'USER'
               and m.readByClub = false
            """)
    int markUserMessagesReadByClub(@Param("clubId") Integer clubId, @Param("userId") Integer userId);
}

