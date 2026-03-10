package com.clubportal.repository;

import com.clubportal.model.ClubImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubImageRepository extends JpaRepository<ClubImage, Integer> {

    List<ClubImage> findByClubId(Integer clubId);

    List<ClubImage> findByClubIdOrderBySortOrderAscImageIdAsc(Integer clubId);

    Optional<ClubImage> findByImageIdAndClubId(Integer imageId, Integer clubId);

    @Query("select coalesce(max(ci.sortOrder), -1) from ClubImage ci where ci.clubId = :clubId")
    Integer findMaxSortOrderByClubId(@Param("clubId") Integer clubId);
}
