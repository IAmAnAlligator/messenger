package com.jeannimi.messenger.adapter.in.web.chat.dto;

import com.jeannimi.messenger.adapter.in.web.user.dto.UserDto;
import com.jeannimi.messenger.domain.chat.ChatRole;
import java.time.Instant;

public record ChatMemberDto(
    UserDto user, ChatRole chatRole, Instant joinedAt, Long lastReadMessageId) {}
