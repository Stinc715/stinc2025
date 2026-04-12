package com.clubportal.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "club_chat_kb_entry")
public class ClubChatKbEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "club_id", nullable = false)
    private Integer clubId;

    @Column(name = "question_title", nullable = false, length = 255)
    private String questionTitle;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "answer_text", nullable = false)
    private String answerText;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "question_embedding")
    private String questionEmbedding;

    @Column(name = "embedding_model", length = 120)
    private String embeddingModel;

    @Column(name = "embedding_dim")
    private Integer embeddingDim;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "trigger_keywords")
    private String triggerKeywords;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "example_questions")
    private String exampleQuestions;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "language", nullable = false, length = 10)
    private ClubChatKbLanguage language = ClubChatKbLanguage.ANY;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = Boolean.TRUE;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClubId() {
        return clubId;
    }

    public void setClubId(Integer clubId) {
        this.clubId = clubId;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getTriggerKeywords() {
        return triggerKeywords;
    }

    public String getQuestionEmbedding() {
        return questionEmbedding;
    }

    public void setQuestionEmbedding(String questionEmbedding) {
        this.questionEmbedding = questionEmbedding;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public Integer getEmbeddingDim() {
        return embeddingDim;
    }

    public void setEmbeddingDim(Integer embeddingDim) {
        this.embeddingDim = embeddingDim;
    }

    public void setTriggerKeywords(String triggerKeywords) {
        this.triggerKeywords = triggerKeywords;
    }

    public String getExampleQuestions() {
        return exampleQuestions;
    }

    public void setExampleQuestions(String exampleQuestions) {
        this.exampleQuestions = exampleQuestions;
    }

    public ClubChatKbLanguage getLanguage() {
        return language;
    }

    public void setLanguage(ClubChatKbLanguage language) {
        this.language = language;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
