package com.conductor.events.repository;

import com.conductor.events.domain.ReplayAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReplayAuditLogRepository extends JpaRepository<ReplayAuditLog, UUID> {
}
