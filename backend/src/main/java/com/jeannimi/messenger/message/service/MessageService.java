package com.jeannimi.messenger.message.service;

import com.jeannimi.messenger.common.pagination.CursorDto;
import com.jeannimi.messenger.common.pagination.CursorPageRequest;
import com.jeannimi.messenger.common.pagination.CursorPageResponse;
import com.jeannimi.messenger.message.dto.FileDownload;
import com.jeannimi.messenger.message.dto.FileUpload;
import com.jeannimi.messenger.message.dto.MessageDto;
import com.jeannimi.messenger.message.dto.ReadResult;

public interface MessageService {

  FileDownload getFile(Long chatId, Long messageId, Long userId);

  MessageDto sendFile(Long chatId, Long senderId, FileUpload file);

  MessageDto sendMessage(Long chatId, Long senderId, String content);

  CursorPageResponse<MessageDto, CursorDto> getMessages(
      Long chatId, Long userId, CursorPageRequest request);

  MessageDto getMessage(Long chatId, Long messageId, Long userId);

  ReadResult markAsRead(Long chatId, Long messageId, Long userId);

  void deleteMessage(Long chatId, Long messageId, Long userId);

  void deleteAllByChat(Long chatId);
}
