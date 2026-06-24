package com.conductor.customer.repository;

import com.conductor.customer.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findBySlug(String slug);

    List<Tag> findByCategory(String category);

    boolean existsBySlug(String slug);
}
