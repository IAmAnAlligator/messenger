package com.jeannimi.messenger.chat.dto;

import com.jeannimi.messenger.chat.ChatConstants;
import com.jeannimi.messenger.validation.ValidChatPageRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import java.time.Instant;

@ValidChatPageRequest
public record ChatPageRequest(
    Instant cursorTime,
    @Positive Long cursorId,
    @Positive @Max(ChatConstants.MAX_CHAT_PAGE_SIZE) Integer limit) {

  public ChatPageRequest {

    if (limit == null) {
      limit = 30;
    }
  }
}
