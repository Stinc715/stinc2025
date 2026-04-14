package com.clubportal.repository;

import com.clubportal.model.CheckoutSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, Integer> {
    Optional<CheckoutSession> findBySessionId(String sessionId);
    Optional<CheckoutSession> findByProviderSessionId(String providerSessionId);
    List<CheckoutSession> findByUserIdOrderByCreatedAtDesc(Integer userId);
    boolean existsByOrderNo(String orderNo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from CheckoutSession s where s.sessionId = :sessionId")
    Optional<CheckoutSession> findBySessionIdForUpdate(@Param("sessionId") String sessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from CheckoutSession s where s.providerSessionId = :providerSessionId")
    Optional<CheckoutSession> findByProviderSessionIdForUpdate(@Param("providerSessionId") String providerSessionId);

    @Query("""
            select s
            from CheckoutSession s
            where s.userId = :userId
              and s.timeslotId = :timeslotId
              and s.status in :statuses
              and s.expiresAt > :now
            order by s.createdAt desc
            """)
    List<CheckoutSession> findActiveBookingSessions(@Param("userId") Integer userId,
                                                    @Param("timeslotId") Integer timeslotId,
                                                    @Param("statuses") Collection<String> statuses,
                                                    @Param("now") Instant now);

    @Query("""
            select s
            from CheckoutSession s
            where s.userId = :userId
              and s.clubId = :clubId
              and s.type = :type
              and s.status in :statuses
              and s.expiresAt > :now
            order by s.createdAt desc
            """)
    List<CheckoutSession> findActiveSessionsForUserAndClub(@Param("userId") Integer userId,
                                                           @Param("clubId") Integer clubId,
                                                           @Param("type") String type,
                                                           @Param("statuses") Collection<String> statuses,
                                                           @Param("now") Instant now);

    List<CheckoutSession> findByStatusInAndExpiresAtBefore(Collection<String> statuses, Instant now);

    @Query("""
            select s
            from CheckoutSession s
            where s.bookingId in :bookingIds
              and s.status = :status
            order by s.createdAt desc
            """)
    List<CheckoutSession> findByBookingIdInAndStatusOrderByCreatedAtDesc(@Param("bookingIds") Collection<Integer> bookingIds,
                                                                         @Param("status") String status);

    @Query("""
            select s
            from CheckoutSession s
            where s.userMembershipId in :userMembershipIds
              and s.status = :status
            order by s.createdAt desc
            """)
    List<CheckoutSession> findByUserMembershipIdInAndStatusOrderByCreatedAtDesc(@Param("userMembershipIds") Collection<Integer> userMembershipIds,
                                                                                 @Param("status") String status);
}
