package com.conductor.events.repository;

import com.conductor.events.domain.ReplayAuditLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplayAuditLogRepository extends JpaRepository<ReplayAuditLog, UUID> {}
