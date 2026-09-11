package com.jeannimi.messenger.application.common.pagination;

import java.time.Instant;

public record CursorPageQuery(
    Long cursorId,
    Instant cursorTime,
    int limit) {}