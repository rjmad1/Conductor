package com.conductor.events.repository;

import com.conductor.events.domain.DlqRecord;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DlqRecordRepository extends JpaRepository<DlqRecord, UUID> {
  List<DlqRecord> findByStatus(String status);
}
