package com.clubportal.repository;

import com.clubportal.model.SecurityEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityEventLogRepository extends JpaRepository<SecurityEventLog, Integer> {
}
