package com.jeannimi.messenger.message.dto;

import com.jeannimi.messenger.chat.dto.ChatMemberReadDto;

public record ReadResult(ChatMemberReadDto read, boolean changed) {}
