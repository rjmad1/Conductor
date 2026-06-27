package com.conductor.customer.api;

import com.conductor.customer.domain.CustomerTimeline;
import com.conductor.customer.service.CustomerTimelineService;
import com.conductor.shared.customer.TimelineEventType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/timeline")
@PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_PLATFORM_ADMIN')")
@SuppressWarnings("null")
public class TimelineController {

  private final CustomerTimelineService timelineService;

  public TimelineController(CustomerTimelineService timelineService) {
    this.timelineService = timelineService;
  }

  @GetMapping
  public ResponseEntity<Page<TimelineResponse>> getTimeline(
      @PathVariable UUID customerId,
      @RequestParam(required = false) TimelineEventType type,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      Pageable pageable) {

    if (type != null) {
      List<CustomerTimeline> list = timelineService.getTimelineByEventType(customerId, type);
      // Since this returns list, we wrap in page to match response type consistency
      List<TimelineResponse> mapped =
          list.stream().map(this::toResponse).collect(Collectors.toList());
      return ResponseEntity.ok(new PageImpl<>(mapped, pageable, mapped.size()));
    }

    if (from != null && to != null) {
      List<CustomerTimeline> list = timelineService.getTimelineInRange(customerId, from, to);
      List<TimelineResponse> mapped =
          list.stream().map(this::toResponse).collect(Collectors.toList());
      return ResponseEntity.ok(new PageImpl<>(mapped, pageable, mapped.size()));
    }

    Page<TimelineResponse> page =
        timelineService.getTimeline(customerId, pageable).map(this::toResponse);
    return ResponseEntity.ok(page);
  }

  private TimelineResponse toResponse(CustomerTimeline timeline) {
    return new TimelineResponse(
        timeline.getId(),
        timeline.getCustomerId(),
        timeline.getEventType(),
        timeline.getEventSource(),
        timeline.getSummary(),
        timeline.getMetadata(),
        timeline.getOccurredAt());
  }

  public record TimelineResponse(
      UUID id,
      UUID customerId,
      TimelineEventType eventType,
      String eventSource,
      String summary,
      String metadata,
      Instant occurredAt) {}
}
