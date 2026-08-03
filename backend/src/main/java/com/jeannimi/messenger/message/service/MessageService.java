package com.jeannimi.messenger.message.service;

import com.jeannimi.messenger.message.dto.MessageDto;
import com.jeannimi.messenger.message.dto.MessagePageDto;
import com.jeannimi.messenger.message.dto.ReadResult;
import java.util.List;

public interface MessageService {

  MessageDto sendMessage(Long chatId, Long senderId, String content);

  MessagePageDto getMessages(Long chatId, Long userId, Long cursor, int limit);

  MessageDto getMessage(Long chatId, Long messageId, Long userId);

  ReadResult markAsRead(Long chatId, Long messageId, Long userId);

  void deleteMessage(Long chatId, Long messageId, Long userId);

  void deleteAllByChat(Long chatId);
}
