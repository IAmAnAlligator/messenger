package com.jeannimi.messenger.chat.dto;

import java.util.List;

public record ChatPageDto(List<ChatDto> items, ChatCursorDto nextCursor, boolean hasMore) {}
