package com.conductor.workflow.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Generic paginated response following API_GOVERNANCE.md cursor pagination standard.
 */
@Data
@Builder
public class PageResponse<T> {

    private List<T> data;
    private int count;
    private boolean hasMore;
    private String nextCursor;
}
