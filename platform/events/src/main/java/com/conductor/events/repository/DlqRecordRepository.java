package com.conductor.events.repository;

import com.conductor.events.domain.DlqRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DlqRecordRepository extends JpaRepository<DlqRecord, UUID> {
    List<DlqRecord> findByStatus(String status);
}
