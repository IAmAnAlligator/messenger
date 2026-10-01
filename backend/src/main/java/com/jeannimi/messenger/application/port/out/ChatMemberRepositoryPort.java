package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.chat.ChatMember;
import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ChatMemberRepositoryPort {

  List<ChatMember> findAllByChatId(ChatId chatId);

  int updateLastReadMessageId(ChatId chatId, UserId userId, MessageId lastReadMessageId);

  Optional<ChatMember> findByChatIdAndUserId(ChatId chatId, UserId userId);

  boolean existsByChatIdAndUserId(ChatId chatId, UserId userId);

  List<ChatId> findFirstPageIds(UserId userId, int limit);

  List<ChatId> findNextPageIds(UserId userId, Instant cursorTime, ChatId cursorId, int limit);
}
