package com.jeannimi.messenger.adapter.in.web.pagination;

import com.jeannimi.messenger.adapter.in.web.pagination.validation.ValidCursorPageRequest;
import jakarta.validation.constraints.Positive;
import java.time.Instant;

@ValidCursorPageRequest
public record CursorPageRequest(
    @Positive(message = "cursorId must be positive") Long cursorId,
    Instant cursorTime,
    @Positive(message = "limit must be positive") Integer limit) {

  public CursorPageRequest {

    if (limit == null) {
      limit = CursorPageConstants.DEFAULT_PAGE_SIZE;
    }
  }
}
