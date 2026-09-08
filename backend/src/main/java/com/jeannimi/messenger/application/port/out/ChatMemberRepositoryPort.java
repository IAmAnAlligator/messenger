package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.chat.ChatMember;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ChatMemberRepositoryPort {

  List<ChatMember> findAllByChatId(Long chatId);

  int updateLastReadMessageId(Long chatId, Long userId, Long lastReadMessageId);

  Optional<ChatMember> findByChatIdAndUserId(Long chatId, Long userId);

  boolean existsByChatIdAndUserId(Long chatId, Long userId);

  List<Long> findFirstPageIds(Long userId, int limit);

  List<Long> findNextPageIds(Long userId, Instant cursorTime, Long cursorId, int limit);
}
