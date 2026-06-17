package com.jeannimi.messenger.service;

import com.jeannimi.messenger.dto.MessageDto;
import java.util.List;

public interface MessageService {

  MessageDto sendMessage(Long chatId, Long senderId, String content);

  List<MessageDto> getMessages(Long chatId, Long userId, Long cursor);

  MessageDto getMessage(Long chatId, Long messageId, Long userId);

  void markAsRead(Long chatId, Long messageId, Long userId);

  void deleteMessage(Long chatId, Long messageId, Long userId);
}
