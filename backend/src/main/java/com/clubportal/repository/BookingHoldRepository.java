package com.clubportal.repository;

import com.clubportal.model.BookingHold;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingHoldRepository extends JpaRepository<BookingHold, Integer> {

    interface TimeslotCount {
        Integer getTimeslotId();
        long getCnt();
    }

    Optional<BookingHold> findByCheckoutSessionId(String checkoutSessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from BookingHold h where h.checkoutSessionId = :checkoutSessionId")
    Optional<BookingHold> findByCheckoutSessionIdForUpdate(@Param("checkoutSessionId") String checkoutSessionId);

    @Query("""
            select count(h)
            from BookingHold h
            where h.timeslotId = :timeslotId
              and h.status = :status
              and h.expiresAt > :now
            """)
    long countActiveByTimeslotId(@Param("timeslotId") Integer timeslotId,
                                 @Param("status") String status,
                                 @Param("now") Instant now);

    @Query("""
            select h.timeslotId as timeslotId, count(h) as cnt
            from BookingHold h
            where h.timeslotId in :timeslotIds
              and h.status = :status
              and h.expiresAt > :now
            group by h.timeslotId
            """)
    List<TimeslotCount> countActiveByTimeslotIds(@Param("timeslotIds") List<Integer> timeslotIds,
                                                 @Param("status") String status,
                                                 @Param("now") Instant now);
}
