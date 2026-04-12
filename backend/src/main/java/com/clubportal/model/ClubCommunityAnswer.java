package com.clubportal.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "club_community_answer")
public class ClubCommunityAnswer {

    public enum ResponderType {
        USER,
        CLUB
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Integer answerId;

    @Column(name = "question_id", nullable = false)
    private Integer questionId;

    @Column(name = "club_id", nullable = false)
    private Integer clubId;

    @Column(name = "user_id")
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "responder_type", nullable = false, length = 20)
    private ResponderType responderType = ResponderType.USER;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "answer_text", nullable = false)
    private String answerText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Integer getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Integer answerId) {
        this.answerId = answerId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getClubId() {
        return clubId;
    }

    public void setClubId(Integer clubId) {
        this.clubId = clubId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public ResponderType getResponderType() {
        return responderType;
    }

    public void setResponderType(ResponderType responderType) {
        this.responderType = responderType;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
