package com.jeannimi.messenger.application.common.pagination;

import com.jeannimi.messenger.domain.common.DomainId;
import java.time.Instant;

public record Cursor<T extends DomainId>(Instant time, T id) {}
