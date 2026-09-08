package com.jeannimi.messenger.message.service;

import com.jeannimi.messenger.application.message.command.FileUploadCommand;
import com.jeannimi.messenger.application.message.dto.FileDownloadResult;
import com.jeannimi.messenger.application.message.dto.MessageResult;
import com.jeannimi.messenger.application.message.dto.ReadResult;
import com.jeannimi.messenger.common.pagination.CursorDto;
import com.jeannimi.messenger.common.pagination.CursorPageRequest;
import com.jeannimi.messenger.common.pagination.CursorPageResponse;

public interface MessageService {

  FileDownloadResult getFile(Long chatId, Long messageId, Long userId);

  MessageResult sendFile(Long chatId, Long senderId, FileUploadCommand file);

  MessageResult sendMessage(Long chatId, Long senderId, String content);

  CursorPageResponse<MessageResult, CursorDto> getMessages(
      Long chatId, Long userId, CursorPageRequest request);

  MessageResult getMessage(Long chatId, Long messageId, Long userId);

  ReadResult markAsRead(Long chatId, Long messageId, Long userId);

  void deleteMessage(Long chatId, Long messageId, Long userId);

  void deleteAllByChat(Long chatId);
}
