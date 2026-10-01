package com.jeannimi.messenger.application.common.pagination;

import com.jeannimi.messenger.domain.common.DomainId;
import java.util.List;

public record CursorPageResult<T, ID extends DomainId>(
    List<T> content,
    Cursor<ID> nextCursor,
    boolean hasNext) {}