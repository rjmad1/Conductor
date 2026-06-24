package com.conductor.customer.repository;

import com.conductor.customer.domain.Segment;
import com.conductor.shared.customer.SegmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SegmentRepository extends JpaRepository<Segment, UUID> {

    Optional<Segment> findBySlug(String slug);

    List<Segment> findByType(SegmentType type);

    boolean existsBySlug(String slug);
}
