package com.clubportal.repository;

import com.clubportal.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Integer> {
    List<TimeSlot> findByVenueIdAndStartTimeBetween(Integer venueId, LocalDateTime startInclusive, LocalDateTime endExclusive);
    List<TimeSlot> findByVenueIdInAndStartTimeBetween(List<Integer> venueIds, LocalDateTime startInclusive, LocalDateTime endExclusive);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TimeSlot t where t.timeslotId = :id")
    Optional<TimeSlot> findByIdForUpdate(@Param("id") Integer id);
}
