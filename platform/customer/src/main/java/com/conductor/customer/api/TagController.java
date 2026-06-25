package com.conductor.customer.api;

import com.conductor.customer.domain.CustomerTag;
import com.conductor.customer.domain.Tag;
import com.conductor.customer.service.TagService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_PLATFORM_ADMIN')")
public class TagController {

  private final TagService tagService;

  public TagController(TagService tagService) {
    this.tagService = tagService;
  }

  // ── Global Tag CRUD ────────────────────────────────────────────────────────

  @GetMapping("/tags")
  public ResponseEntity<List<TagResponse>> listTags(
      @RequestParam(required = false) String category) {
    List<Tag> tags =
        (category != null) ? tagService.listTagsByCategory(category) : tagService.listTags();
    List<TagResponse> responses = tags.stream().map(this::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @PostMapping("/tags")
  public ResponseEntity<TagResponse> createTag(@Valid @RequestBody CreateTagRequest request) {
    Tag tag =
        tagService.createTag(
            request.name(), request.category(), request.color(), request.description());
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(tag));
  }

  @PutMapping("/tags/{id}")
  public ResponseEntity<TagResponse> updateTag(
      @PathVariable UUID id, @Valid @RequestBody UpdateTagRequest request) {
    Tag tag =
        tagService.updateTag(
            id, request.name(), request.category(), request.color(), request.description());
    return ResponseEntity.ok(toResponse(tag));
  }

  @DeleteMapping("/tags/{id}")
  public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
    tagService.deleteTag(id);
    return ResponseEntity.noContent().build();
  }

  // ── Customer Tag Assignment ───────────────────────────────────────────────

  @GetMapping("/customers/{customerId}/tags")
  public ResponseEntity<List<CustomerTagResponse>> getCustomerTags(@PathVariable UUID customerId) {
    List<CustomerTagResponse> tags =
        tagService.getCustomerTags(customerId).stream()
            .map(this::toCustomerTagResponse)
            .collect(Collectors.toList());
    return ResponseEntity.ok(tags);
  }

  @PostMapping("/customers/{customerId}/tags")
  public ResponseEntity<Void> assignTag(
      @PathVariable UUID customerId, @RequestParam UUID tagId, Principal principal) {
    String assignedBy = (principal != null) ? principal.getName() : "system";
    tagService.assignTag(customerId, tagId, assignedBy);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/customers/{customerId}/tags/{tagId}")
  public ResponseEntity<Void> removeTag(@PathVariable UUID customerId, @PathVariable UUID tagId) {
    tagService.removeTag(customerId, tagId);
    return ResponseEntity.noContent().build();
  }

  private TagResponse toResponse(Tag tag) {
    return new TagResponse(
        tag.getId(),
        tag.getTenantId(),
        tag.getName(),
        tag.getSlug(),
        tag.getCategory(),
        tag.getColor(),
        tag.getDescription(),
        tag.getCreatedAt(),
        tag.getUpdatedAt());
  }

  private CustomerTagResponse toCustomerTagResponse(CustomerTag ct) {
    return new CustomerTagResponse(
        ct.getId(), ct.getCustomerId(), ct.getTagId(), ct.getAssignedAt(), ct.getAssignedBy());
  }

  public record CreateTagRequest(
      @NotBlank String name, String category, String color, String description) {}

  public record UpdateTagRequest(
      @NotBlank String name, String category, String color, String description) {}

  public record TagResponse(
      UUID id,
      UUID tenantId,
      String name,
      String slug,
      String category,
      String color,
      String description,
      Instant createdAt,
      Instant updatedAt) {}

  public record CustomerTagResponse(
      UUID id, UUID customerId, UUID tagId, Instant assignedAt, String assignedBy) {}
}
