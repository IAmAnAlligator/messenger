package com.jeannimi.messenger.mapper;

import com.jeannimi.messenger.dto.ChatDto;
import com.jeannimi.messenger.dto.ChatMemberDto;
import com.jeannimi.messenger.dto.UserDto;
import com.jeannimi.messenger.entity.Chat;
import com.jeannimi.messenger.entity.ChatMember;
import com.jeannimi.messenger.entity.ChatType;
import com.jeannimi.messenger.entity.User;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ChatMapper {

  public ChatDto toDto(Chat chat) {

    List<ChatMember> members =
        chat.getMembers() == null ? List.of() : new ArrayList<>(chat.getMembers());

    String chatName = chat.getName();

    if (chat.getType() == ChatType.PRIVATE) {
      chatName =
          members.stream()
              .map(ChatMember::getUser)
              .sorted(Comparator.comparing(User::getId))
              .map(u -> u.getUsername().toString())
              .collect(Collectors.joining("_"));
    }

    return ChatDto.builder()
        .id(chat.getId())
        .name(chatName)
        .type(chat.getType().name())
        .createdAt(chat.getCreatedAt())
        .lastMessageAt(chat.getLastMessageAt())
        .members(
            members.stream()
                .map(
                    cm ->
                        ChatMemberDto.builder()
                            .user(
                                UserDto.builder()
                                    .id(cm.getUser().getId())
                                    .username(cm.getUser().getUsername().getValue())
                                    .role(cm.getUser().getRole())
                                    .build())
                            .chatRole(cm.getRole())
                            .joinedAt(cm.getJoinedAt())
                            .build())
                .toList())
        .build();
  }
}
