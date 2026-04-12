package com.clubportal.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "club")
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private Integer clubId;

    @Column(name = "club_name", nullable = false, length = 120)
    private String clubName;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "description")
    private String description;

    @Column(name = "category", length = 60)
    private String category;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "category_tags")
    private String categoryTags;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "phone", length = 40)
    private String phone;

    @Column(name = "display_location", length = 255)
    private String displayLocation;

    @Column(name = "google_place_id", length = 255)
    private String googlePlaceId;

    @Column(name = "location_lat")
    private Double locationLat;

    @Column(name = "location_lng")
    private Double locationLng;

    @Column(name = "opening_start", length = 5)
    private String openingStart;

    @Column(name = "opening_end", length = 5)
    private String openingEnd;

    @Column(name = "display_courts")
    private Integer displayCourts;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Integer getClubId() { return clubId; }
    public void setClubId(Integer clubId) { this.clubId = clubId; }
    public String getClubName() { return clubName; }
    public void setClubName(String clubName) { this.clubName = clubName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCategoryTags() { return categoryTags; }
    public void setCategoryTags(String categoryTags) { this.categoryTags = categoryTags; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDisplayLocation() { return displayLocation; }
    public void setDisplayLocation(String displayLocation) { this.displayLocation = displayLocation; }
    public String getGooglePlaceId() { return googlePlaceId; }
    public void setGooglePlaceId(String googlePlaceId) { this.googlePlaceId = googlePlaceId; }
    public Double getLocationLat() { return locationLat; }
    public void setLocationLat(Double locationLat) { this.locationLat = locationLat; }
    public Double getLocationLng() { return locationLng; }
    public void setLocationLng(Double locationLng) { this.locationLng = locationLng; }
    public String getOpeningStart() { return openingStart; }
    public void setOpeningStart(String openingStart) { this.openingStart = openingStart; }
    public String getOpeningEnd() { return openingEnd; }
    public void setOpeningEnd(String openingEnd) { this.openingEnd = openingEnd; }
    public Integer getDisplayCourts() { return displayCourts; }
    public void setDisplayCourts(Integer displayCourts) { this.displayCourts = displayCourts; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
