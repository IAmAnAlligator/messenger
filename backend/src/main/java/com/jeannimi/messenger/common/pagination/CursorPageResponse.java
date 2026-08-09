package com.jeannimi.messenger.common.pagination;

import java.util.List;

public record CursorPageResponse<T, C>(List<T> content, C nextCursor, boolean hasNext) {}
