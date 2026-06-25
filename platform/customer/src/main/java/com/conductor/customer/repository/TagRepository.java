package com.conductor.customer.repository;

import com.conductor.customer.domain.Tag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, UUID> {

  Optional<Tag> findBySlug(String slug);

  List<Tag> findByCategory(String category);

  boolean existsBySlug(String slug);
}
