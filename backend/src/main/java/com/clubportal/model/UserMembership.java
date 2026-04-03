package com.clubportal.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_membership")
public class UserMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_membership_id")
    private Integer userMembershipId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "plan_id", nullable = false)
    private Integer planId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "included_bookings_total")
    private Integer includedBookingsTotal;

    @Column(name = "remaining_bookings")
    private Integer remainingBookings;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Integer getUserMembershipId() { return userMembershipId; }
    public void setUserMembershipId(Integer userMembershipId) { this.userMembershipId = userMembershipId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getPlanId() { return planId; }
    public void setPlanId(Integer planId) { this.planId = planId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getIncludedBookingsTotal() { return includedBookingsTotal; }
    public void setIncludedBookingsTotal(Integer includedBookingsTotal) { this.includedBookingsTotal = includedBookingsTotal; }
    public Integer getRemainingBookings() { return remainingBookings; }
    public void setRemainingBookings(Integer remainingBookings) { this.remainingBookings = remainingBookings; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
