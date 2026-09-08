package com.jeannimi.messenger.kafka.event;

import com.jeannimi.messenger.domain.chat.ChatType;
import java.util.List;

public record ChatCreatedEvent(Long chatId, String name, ChatType type, List<Long> memberIds) {

  //  public static ChatCreatedEvent from(ChatResult result) {
  //    return new ChatCreatedEvent(
  //        UUID.randomUUID(),
  //        result.id(),
  //        result.name(),
  //        ChatType.valueOf(result.type()),
  //        result.members().stream()
  //            .map(member -> member.user().id())
  //            .toList());
  //  }

}
