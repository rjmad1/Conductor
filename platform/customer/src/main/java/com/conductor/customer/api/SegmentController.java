package com.conductor.customer.api;

import com.conductor.customer.domain.CustomerSegment;
import com.conductor.customer.domain.Segment;
import com.conductor.customer.service.SegmentService;
import com.conductor.shared.customer.SegmentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@PreAuthorize(
    "hasAnyAuthority('ROLE_TENANT_OWNER', 'ROLE_TENANT_ADMIN', 'ROLE_TENANT_AGENT', 'ROLE_PLATFORM_ADMIN')")
public class SegmentController {

  private final SegmentService segmentService;

  public SegmentController(SegmentService segmentService) {
    this.segmentService = segmentService;
  }

  // ── Global Segment CRUD ───────────────────────────────────────────────────

  @GetMapping("/segments")
  public ResponseEntity<List<SegmentResponse>> listSegments() {
    List<SegmentResponse> responses =
        segmentService.listSegments().stream().map(this::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @PostMapping("/segments")
  public ResponseEntity<SegmentResponse> createSegment(
      @Valid @RequestBody CreateSegmentRequest request) {
    Segment segment =
        segmentService.createSegment(
            request.name(), request.type(), request.rules(), request.description());
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(segment));
  }

  @PutMapping("/segments/{id}")
  public ResponseEntity<SegmentResponse> updateSegment(
      @PathVariable UUID id, @Valid @RequestBody UpdateSegmentRequest request) {
    Segment segment =
        segmentService.updateSegment(id, request.name(), request.rules(), request.description());
    return ResponseEntity.ok(toResponse(segment));
  }

  @DeleteMapping("/segments/{id}")
  public ResponseEntity<Void> deleteSegment(@PathVariable UUID id) {
    segmentService.deleteSegment(id);
    return ResponseEntity.noContent().build();
  }

  // ── Segment Membership & Recomputation ────────────────────────────────────

  @GetMapping("/customers/{customerId}/segments")
  public ResponseEntity<List<CustomerSegmentResponse>> getCustomerSegments(
      @PathVariable UUID customerId) {
    List<CustomerSegmentResponse> segments =
        segmentService.getCustomerSegments(customerId).stream()
            .map(this::toCustomerSegmentResponse)
            .collect(Collectors.toList());
    return ResponseEntity.ok(segments);
  }

  @GetMapping("/segments/{id}/customers")
  public ResponseEntity<List<UUID>> getSegmentCustomerIds(@PathVariable UUID id) {
    List<UUID> customerIds = segmentService.getSegmentCustomerIds(id);
    return ResponseEntity.ok(customerIds);
  }

  @PostMapping("/segments/{id}/customers")
  public ResponseEntity<Void> addCustomerToSegment(
      @PathVariable UUID id, @RequestParam UUID customerId, Principal principal) {
    String addedBy = (principal != null) ? principal.getName() : "system";
    segmentService.addCustomerToSegment(customerId, id, addedBy);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/segments/{id}/customers/{customerId}")
  public ResponseEntity<Void> removeCustomerFromSegment(
      @PathVariable UUID id, @PathVariable UUID customerId) {
    segmentService.removeCustomerFromSegment(customerId, id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/segments/{id}/recompute")
  public ResponseEntity<Void> recomputeSegment(@PathVariable UUID id, @RequestParam UUID tagId) {
    segmentService.recomputeTagBasedSegment(id, tagId);
    return ResponseEntity.noContent().build();
  }

  private SegmentResponse toResponse(Segment segment) {
    return new SegmentResponse(
        segment.getId(),
        segment.getTenantId(),
        segment.getName(),
        segment.getSlug(),
        segment.getType(),
        segment.getRules(),
        segment.getDescription(),
        segment.getCustomerCount(),
        segment.getLastComputedAt(),
        segment.getCreatedAt(),
        segment.getUpdatedAt());
  }

  private CustomerSegmentResponse toCustomerSegmentResponse(CustomerSegment cs) {
    return new CustomerSegmentResponse(
        cs.getId(),
        cs.getCustomerId(),
        cs.getSegmentId(),
        cs.getAddedAt(),
        cs.getAddedBy(),
        cs.getSource());
  }

  public record CreateSegmentRequest(
      @NotBlank String name, @NotNull SegmentType type, String rules, String description) {}

  public record UpdateSegmentRequest(@NotBlank String name, String rules, String description) {}

  public record SegmentResponse(
      UUID id,
      UUID tenantId,
      String name,
      String slug,
      SegmentType type,
      String rules,
      String description,
      long customerCount,
      Instant lastComputedAt,
      Instant createdAt,
      Instant updatedAt) {}

  public record CustomerSegmentResponse(
      UUID id, UUID customerId, UUID segmentId, Instant addedAt, String addedBy, String source) {}
}
