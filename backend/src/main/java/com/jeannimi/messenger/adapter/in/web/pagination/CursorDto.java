package com.jeannimi.messenger.adapter.in.web.pagination;

import java.time.Instant;
import java.util.UUID;

public record CursorDto(Instant cursorTime, UUID cursorId) {}
