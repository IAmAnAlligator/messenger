package com.jeannimi.messenger.application.message.service;

import com.jeannimi.messenger.application.common.pagination.CursorPageQuery;
import com.jeannimi.messenger.application.common.pagination.CursorPageResult;
import com.jeannimi.messenger.application.message.command.FileUploadCommand;
import com.jeannimi.messenger.application.message.dto.FileDownloadResult;
import com.jeannimi.messenger.application.message.dto.MessageResult;
import com.jeannimi.messenger.application.message.dto.ReadResult;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.user.UserId;

public interface MessageService {

  FileDownloadResult getFile(ChatId chatId, MessageId messageId, UserId userId);

  MessageResult sendFile(ChatId chatId, UserId senderId, FileUploadCommand file);

  MessageResult sendMessage(ChatId chatId, UserId senderId, String content);

  CursorPageResult<MessageResult, MessageId> getMessages(
      ChatId chatId, UserId userId, CursorPageQuery<MessageId> query);

  MessageResult getMessage(ChatId chatId, MessageId messageId, UserId userId);

  ReadResult markAsRead(ChatId chatId, MessageId messageId, UserId userId);

  void deleteMessage(ChatId chatId, MessageId messageId, UserId userId);

  void deleteAllByChat(ChatId chatId);
}
