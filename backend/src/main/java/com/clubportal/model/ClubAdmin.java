package com.clubportal.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "club_admin", uniqueConstraints = {
        @UniqueConstraint(name = "uk_club_admin_user_club", columnNames = {"user_id", "club_id"})
})
public class ClubAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_admin_id")
    private Integer clubAdminId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "club_id", nullable = false)
    private Integer clubId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Integer getClubAdminId() { return clubAdminId; }
    public void setClubAdminId(Integer clubAdminId) { this.clubAdminId = clubAdminId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getClubId() { return clubId; }
    public void setClubId(Integer clubId) { this.clubId = clubId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
