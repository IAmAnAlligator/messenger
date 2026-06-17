package com.jeannimi.messenger.dto;

import com.jeannimi.messenger.entity.ChatRole;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMemberDto {

  private UserDto user;
  private ChatRole chatRole;
  private Instant joinedAt;
}
