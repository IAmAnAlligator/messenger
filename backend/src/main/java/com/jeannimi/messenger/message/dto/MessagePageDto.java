package com.jeannimi.messenger.message.dto;

import java.util.List;

public record MessagePageDto(List<MessageDto> items, Long nextCursor, boolean hasMore) {}
