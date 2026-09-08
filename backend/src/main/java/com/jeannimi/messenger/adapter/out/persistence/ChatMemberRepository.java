package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.mapper.ChatMemberPersistenceMapper;
import com.jeannimi.messenger.application.port.out.ChatMemberRepositoryPort;
import com.jeannimi.messenger.domain.chat.ChatMember;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatMemberRepository implements ChatMemberRepositoryPort {

  private final ChatMemberJpaRepository chatMemberJpaRepository;
  private final ChatMemberPersistenceMapper chatMemberPersistenceMapper;

  @Override
  public List<ChatMember> findAllByChatId(Long chatId) {

    return chatMemberJpaRepository.findAllByChat_Id(chatId).stream()
        .map(chatMemberPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public int updateLastReadMessageId(Long chatId, Long userId, Long lastReadMessageId) {

    return chatMemberJpaRepository.updateLastReadMessageId(chatId, userId, lastReadMessageId);
  }

  @Override
  public Optional<ChatMember> findByChatIdAndUserId(Long chatId, Long userId) {

    return chatMemberJpaRepository
        .findByChat_IdAndUser_Id(chatId, userId)
        .map(chatMemberPersistenceMapper::toDomain);
  }

  @Override
  public boolean existsByChatIdAndUserId(Long chatId, Long userId) {

    return chatMemberJpaRepository.existsByChat_IdAndUser_Id(chatId, userId);
  }

  @Override
  public List<Long> findFirstPageIds(Long userId, int limit) {

    return chatMemberJpaRepository.findFirstPageIds(userId, PageRequest.of(0, limit));
  }

  @Override
  public List<Long> findNextPageIds(Long userId, Instant cursorTime, Long cursorId, int limit) {

    return chatMemberJpaRepository.findNextPageIds(
        userId, cursorTime, cursorId, PageRequest.of(0, limit));
  }
}
