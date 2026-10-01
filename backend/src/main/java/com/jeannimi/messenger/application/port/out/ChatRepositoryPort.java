package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.chat.Chat;
import com.jeannimi.messenger.domain.chat.ChatId;
import java.util.List;
import java.util.Optional;

public interface ChatRepositoryPort {

  List<Chat> findByIdsWithMembers(List<ChatId> ids);

  Optional<Chat> findByIdWithMembers(ChatId chatId);

  Optional<Chat> findByPrivateKey(String privateKey);

  Chat save(Chat chat);

  Optional<Chat> findById(ChatId chatId);

  void delete(Chat chat);
}
