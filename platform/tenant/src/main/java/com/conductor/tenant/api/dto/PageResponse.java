package com.conductor.tenant.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageResponse<T> {
  private List<T> data;
  private int count;
  private boolean hasMore;
  private String nextCursor;
}
