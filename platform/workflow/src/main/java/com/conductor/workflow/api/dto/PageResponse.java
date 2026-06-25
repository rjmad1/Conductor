package com.conductor.workflow.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** Generic paginated response following API_GOVERNANCE.md cursor pagination standard. */
@Data
@Builder
public class PageResponse<T> {

  private List<T> data;
  private int count;
  private boolean hasMore;
  private String nextCursor;
}
