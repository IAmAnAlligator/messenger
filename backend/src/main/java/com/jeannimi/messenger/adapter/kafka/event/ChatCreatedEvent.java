package com.jeannimi.messenger.adapter.kafka.event;

import com.jeannimi.messenger.domain.chat.ChatType;
import java.util.List;

public record ChatCreatedEvent(Long chatId, String name, ChatType type, List<Long> memberIds) {

}
