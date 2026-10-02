package com.jeannimi.messenger.application.common.pagination;

import com.jeannimi.messenger.domain.common.DomainId;
import java.time.Instant;

public record CursorPageQuery<T extends DomainId>(T cursorId, Instant cursorTime, int limit) {}
