package com.clubportal.repository;

import com.clubportal.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Integer> {
    List<Venue> findByClubId(Integer clubId);
}
