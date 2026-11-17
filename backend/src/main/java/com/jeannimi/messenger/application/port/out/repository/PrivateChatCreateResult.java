package com.jeannimi.messenger.application.port.out.repository;

import com.jeannimi.messenger.domain.chat.Chat;

public record PrivateChatCreateResult(Chat chat, boolean created) {}
