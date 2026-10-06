package com.jeannimi.messenger.application.message.service;

import com.jeannimi.messenger.application.chat.service.ChatService;
import com.jeannimi.messenger.application.message.command.FileUploadCommand;
import com.jeannimi.messenger.application.message.dto.MessageResult;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PrivateMessageServiceImpl implements PrivateMessageService {

  private final ChatService chatService;
  private final MessageService messageService;

  @Override
  @Transactional
  public MessageResult sendMessage(UserId currentUserId, UserId recipientUserId, String content) {

    ChatId chatId = chatService.getOrCreatePrivateChat(currentUserId, recipientUserId).id();

    return messageService.sendMessage(chatId, currentUserId, content);
  }

  @Override
  @Transactional
  public MessageResult sendFile(
      UserId currentUserId, UserId recipientUserId, FileUploadCommand command) {
    ChatId chatId = chatService.getOrCreatePrivateChat(currentUserId, recipientUserId).id();
    return messageService.sendFile(chatId, currentUserId, command);
  }
}
