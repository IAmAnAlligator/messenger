package com.jeannimi.messenger.application.message.service;

import com.jeannimi.messenger.application.message.command.FileUploadCommand;
import com.jeannimi.messenger.application.message.dto.MessageResult;
import com.jeannimi.messenger.domain.user.UserId;

public interface PrivateMessageService {

  MessageResult sendMessage(UserId currentUserId, UserId recipientUserId, String content);

  MessageResult sendFile(UserId currentUserId, UserId recipientUserId, FileUploadCommand command);
}
