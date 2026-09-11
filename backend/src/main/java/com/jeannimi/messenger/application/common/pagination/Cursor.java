package com.jeannimi.messenger.application.common.pagination;

import java.time.Instant;

public record Cursor(
    Instant time,
    Long id
) {}