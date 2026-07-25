package com.jeannimi.messenger.chat.dto;

import com.jeannimi.messenger.chat.entity.ChatMember;
import com.jeannimi.messenger.chat.entity.ChatRole;
import com.jeannimi.messenger.user.dto.UserDto;
import java.time.Instant;

public record ChatMemberDto(UserDto user, ChatRole chatRole, Instant joinedAt) {

  public static ChatMemberDto toDto(ChatMember chatMember) {

    return new ChatMemberDto(
        UserDto.toDto(chatMember.getUser()), chatMember.getRole(), chatMember.getJoinedAt());
  }
}
