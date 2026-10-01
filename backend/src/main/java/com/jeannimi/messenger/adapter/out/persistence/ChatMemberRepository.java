package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.mapper.ChatMemberPersistenceMapper;
import com.jeannimi.messenger.application.port.out.ChatMemberRepositoryPort;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.chat.ChatMember;
import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.user.UserId;
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
  public List<ChatMember> findAllByChatId(ChatId chatId) {

    return chatMemberJpaRepository.findAllByChat_Id(chatId.value()).stream()
        .map(chatMemberPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public int updateLastReadMessageId(ChatId chatId, UserId userId, MessageId lastReadMessageId) {

    return chatMemberJpaRepository.updateLastReadMessageId(
        chatId.value(), userId.value(), lastReadMessageId.value());
  }

  @Override
  public Optional<ChatMember> findByChatIdAndUserId(ChatId chatId, UserId userId) {

    return chatMemberJpaRepository
        .findByChat_IdAndUser_Id(chatId.value(), userId.value())
        .map(chatMemberPersistenceMapper::toDomain);
  }

  @Override
  public boolean existsByChatIdAndUserId(ChatId chatId, UserId userId) {

    return chatMemberJpaRepository.existsByChat_IdAndUser_Id(chatId.value(), userId.value());
  }

  @Override
  public List<ChatId> findFirstPageIds(UserId userId, int limit) {

    return chatMemberJpaRepository
        .findFirstPageIds(userId.value(), PageRequest.of(0, limit))
        .stream()
        .map(ChatId::new)
        .toList();
  }

  @Override
  public List<ChatId> findNextPageIds(
      UserId userId, Instant cursorTime, ChatId cursorId, int limit) {

    return chatMemberJpaRepository
        .findNextPageIds(userId.value(), cursorTime, cursorId.value(), PageRequest.of(0, limit))
        .stream()
        .map(ChatId::new)
        .toList();
  }
}
