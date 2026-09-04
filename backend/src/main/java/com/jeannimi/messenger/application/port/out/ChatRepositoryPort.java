package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.chat.entity.Chat;
import java.util.List;
import java.util.Optional;

public interface ChatRepositoryPort {

  List<Chat> findByIdsWithMembers(List<Long> ids);

  Optional<Chat> findByIdWithMembers(Long chatId);

  Optional<Chat> findByPrivateKey(String privateKey);

  Chat save(Chat chat);

  Optional<Chat> findById(Long chatId);

  void delete(Chat chat);

  boolean existsById(Long chatId);
}
