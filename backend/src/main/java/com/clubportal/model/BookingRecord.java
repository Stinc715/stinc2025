package com.clubportal.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_record")
public class BookingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer bookingId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "timeslot_id", nullable = false)
    private Integer timeslotId;

    @Column(name = "booking_time", insertable = false, updatable = false)
    private LocalDateTime bookingTime;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "price_paid", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal pricePaid;

    @Column(name = "user_membership_id")
    private Integer userMembershipId;

    @Column(name = "booking_verification_code", length = 6)
    private String bookingVerificationCode;

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getTimeslotId() { return timeslotId; }
    public void setTimeslotId(Integer timeslotId) { this.timeslotId = timeslotId; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.math.BigDecimal getPricePaid() { return pricePaid; }
    public void setPricePaid(java.math.BigDecimal pricePaid) { this.pricePaid = pricePaid; }
    public Integer getUserMembershipId() { return userMembershipId; }
    public void setUserMembershipId(Integer userMembershipId) { this.userMembershipId = userMembershipId; }
    public String getBookingVerificationCode() { return bookingVerificationCode; }
    public void setBookingVerificationCode(String bookingVerificationCode) { this.bookingVerificationCode = bookingVerificationCode; }
}
