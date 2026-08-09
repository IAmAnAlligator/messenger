package com.jeannimi.messenger.common.pagination;

import java.time.Instant;

public record CursorDto(Instant cursorTime, Long cursorId) {}
