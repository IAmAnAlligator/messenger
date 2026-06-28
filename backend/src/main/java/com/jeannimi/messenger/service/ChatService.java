package com.jeannimi.messenger.service;

import com.jeannimi.messenger.dto.ChatCreateRequest;
import com.jeannimi.messenger.dto.ChatDto;
import java.util.List;

public interface ChatService {

  ChatDto createChat(ChatCreateRequest request, Long currentUserId);

  List<ChatDto> getUserChats(Long userId);

  ChatDto getChat(Long chatId, Long userId);

  void addMember(Long chatId, Long userId, Long currentUserId);

  void removeMember(Long chatId, Long userId, Long currentUserId);

  boolean isParticipant(Long chatId, Long userId);

  void deleteChat(Long chatId, Long currentUserId);
}
