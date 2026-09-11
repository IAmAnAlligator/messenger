package com.jeannimi.messenger.application.message.service;

import com.jeannimi.messenger.application.common.pagination.CursorPageQuery;
import com.jeannimi.messenger.application.common.pagination.CursorPageResult;
import com.jeannimi.messenger.application.message.command.FileUploadCommand;
import com.jeannimi.messenger.application.message.dto.FileDownloadResult;
import com.jeannimi.messenger.application.message.dto.MessageResult;
import com.jeannimi.messenger.application.message.dto.ReadResult;

public interface MessageService {

  FileDownloadResult getFile(Long chatId, Long messageId, Long userId);

  MessageResult sendFile(Long chatId, Long senderId, FileUploadCommand file);

  MessageResult sendMessage(Long chatId, Long senderId, String content);

  CursorPageResult<MessageResult> getMessages(
      Long chatId, Long userId, CursorPageQuery query);

  MessageResult getMessage(Long chatId, Long messageId, Long userId);

  ReadResult markAsRead(Long chatId, Long messageId, Long userId);

  void deleteMessage(Long chatId, Long messageId, Long userId);

  void deleteAllByChat(Long chatId);
}
