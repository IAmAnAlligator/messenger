package com.jeannimi.messenger.adapter.in.web.pagination;

import java.time.Instant;

public record CursorDto(Instant cursorTime, Long cursorId) {}
