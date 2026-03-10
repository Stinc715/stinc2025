package com.clubportal.repository;

import com.clubportal.model.BookingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRecordRepository extends JpaRepository<BookingRecord, Integer> {
    List<BookingRecord> findByUserId(Integer userId);
    List<BookingRecord> findByUserIdOrderByBookingTimeDesc(Integer userId);
    Optional<BookingRecord> findFirstByUserIdAndTimeslotIdAndStatusInOrderByBookingTimeDesc(
            Integer userId,
            Integer timeslotId,
            Collection<String> statuses
    );

    long countByTimeslotIdAndStatusIn(Integer timeslotId, Collection<String> statuses);
    boolean existsByUserIdAndTimeslotIdAndStatusIn(Integer userId, Integer timeslotId, Collection<String> statuses);
    List<BookingRecord> findByTimeslotIdInAndStatusInOrderByBookingTimeAsc(Collection<Integer> timeslotIds, Collection<String> statuses);

    interface TimeslotCount {
        Integer getTimeslotId();
        long getCnt();
    }

    @Query("""
            select b.timeslotId as timeslotId, count(b) as cnt
            from BookingRecord b
            where b.timeslotId in :timeslotIds and b.status in :statuses
            group by b.timeslotId
            """)
    List<TimeslotCount> countByTimeslotIdsAndStatuses(@Param("timeslotIds") List<Integer> timeslotIds,
                                                      @Param("statuses") Collection<String> statuses);
}
