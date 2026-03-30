package com.clubportal.repository;

import com.clubportal.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Club c where c.clubId = :id")
    Optional<Club> findByIdForUpdate(@Param("id") Integer id);
}
