package com.jeannimi.messenger.chat.dto;

import com.jeannimi.messenger.chat.entity.Chat;
import java.time.Instant;
import java.util.List;

public record ChatDto(
    Long id,
    String name,
    String type,
    List<ChatMemberDto> members,
    Instant createdAt,
    Instant lastMessageAt) {

  public static ChatDto toDto(Chat chat) {

    List<ChatMemberDto> members =
        chat.getMembers() == null
            ? List.of()
            : chat.getMembers().stream().map(ChatMemberDto::toDto).toList();

    return new ChatDto(
        chat.getId(),
        chat.getName(),
        chat.getType().name(),
        members,
        chat.getCreatedAt(),
        chat.getLastMessageAt());
  }
}
