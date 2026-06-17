package com.jeannimi.messenger.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatDto {
  private Long id;
  private String name;
  private String type;

  private List<ChatMemberDto> members;

  private Instant createdAt;
  private Instant lastMessageAt;
}
