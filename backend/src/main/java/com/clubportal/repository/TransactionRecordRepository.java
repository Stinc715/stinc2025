package com.clubportal.repository;

import com.clubportal.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Integer> {
    List<TransactionRecord> findByUserId(Integer userId);
    List<TransactionRecord> findByUserIdOrderByTransactionTimeDesc(Integer userId);
    List<TransactionRecord> findByUserMembershipIdIn(Collection<Integer> userMembershipIds);
}
