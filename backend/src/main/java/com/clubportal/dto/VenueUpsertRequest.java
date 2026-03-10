package com.clubportal.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class VenueUpsertRequest {

    @JsonAlias({"venueName", "name", "courtName", "court"})
    private String name;

    private String location;

    private Integer capacity;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}

