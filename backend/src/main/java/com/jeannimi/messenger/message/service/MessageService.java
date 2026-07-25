package com.jeannimi.messenger.message.service;

import com.jeannimi.messenger.message.dto.MessageDto;
import com.jeannimi.messenger.message.dto.ReadResult;
import java.util.List;

public interface MessageService {

  MessageDto sendMessage(Long chatId, Long senderId, String content);

  List<MessageDto> getMessages(Long chatId, Long userId, Long cursor);

  MessageDto getMessage(Long chatId, Long messageId, Long userId);

  ReadResult markAsRead(Long chatId, Long messageId, Long userId);

  void deleteMessage(Long chatId, Long messageId, Long userId);

  void deleteAllByChat(Long chatId);
}
