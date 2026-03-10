package com.clubportal.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public class ClubUpsertRequest {

    @JsonAlias({"clubName", "name", "title"})
    private String name;

    @JsonAlias({"category", "sportType", "sport"})
    private String category;

    @JsonAlias({"desc", "description"})
    private String description;

    private String email;

    private String phone;

    @JsonAlias({"location", "displayLocation", "venueLocation"})
    private String location;

    @JsonAlias({"placeId", "googlePlaceId"})
    private String placeId;

    @JsonAlias({"locationLat", "lat", "latitude"})
    private Double locationLat;

    @JsonAlias({"locationLng", "lng", "longitude", "lon"})
    private Double locationLng;

    @JsonAlias({"openingStart", "openStart", "openFrom", "openingFrom"})
    private String openingStart;

    @JsonAlias({"openingEnd", "openEnd", "openTo", "closingTime"})
    private String openingEnd;

    @JsonAlias({"courtsCount", "courts", "displayCourts"})
    private Integer courtsCount;

    private List<String> tags;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getPlaceId() { return placeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }
    public Double getLocationLat() { return locationLat; }
    public void setLocationLat(Double locationLat) { this.locationLat = locationLat; }
    public Double getLocationLng() { return locationLng; }
    public void setLocationLng(Double locationLng) { this.locationLng = locationLng; }
    public String getOpeningStart() { return openingStart; }
    public void setOpeningStart(String openingStart) { this.openingStart = openingStart; }
    public String getOpeningEnd() { return openingEnd; }
    public void setOpeningEnd(String openingEnd) { this.openingEnd = openingEnd; }
    public Integer getCourtsCount() { return courtsCount; }
    public void setCourtsCount(Integer courtsCount) { this.courtsCount = courtsCount; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
