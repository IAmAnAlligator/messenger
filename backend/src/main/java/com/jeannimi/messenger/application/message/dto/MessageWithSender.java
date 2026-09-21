package com.jeannimi.messenger.application.message.dto;

import com.jeannimi.messenger.domain.message.Message;
import com.jeannimi.messenger.domain.user.User;

public record MessageWithSender(Message message, User sender) {}
