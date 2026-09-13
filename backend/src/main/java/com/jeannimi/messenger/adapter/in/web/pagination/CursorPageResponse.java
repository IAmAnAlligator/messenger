package com.jeannimi.messenger.adapter.in.web.pagination;

import java.util.List;

public record CursorPageResponse<T, C>(List<T> content, C nextCursor, boolean hasNext) {}
