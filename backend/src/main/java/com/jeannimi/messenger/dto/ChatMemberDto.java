package com.jeannimi.messenger.dto;

import com.jeannimi.messenger.entity.ChatMember;
import com.jeannimi.messenger.entity.ChatRole;
import java.time.Instant;

public record ChatMemberDto(UserDto user, ChatRole chatRole, Instant joinedAt) {

  public static ChatMemberDto toDto(ChatMember chatMember) {

    return new ChatMemberDto(
        UserDto.toDto(chatMember.getUser()), chatMember.getRole(), chatMember.getJoinedAt());
  }
}
