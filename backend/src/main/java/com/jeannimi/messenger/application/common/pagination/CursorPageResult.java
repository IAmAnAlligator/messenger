package com.jeannimi.messenger.application.common.pagination;

import java.util.List;

public record CursorPageResult<T>(
    List<T> content,
    Cursor nextCursor,
    boolean hasNext) {}