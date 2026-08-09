package com.jeannimi.messenger.chat.service;

import com.jeannimi.messenger.chat.dto.ChatCreateRequest;
import com.jeannimi.messenger.chat.dto.ChatDto;
import com.jeannimi.messenger.chat.dto.ChatMemberDto;
import com.jeannimi.messenger.common.pagination.CursorDto;
import com.jeannimi.messenger.common.pagination.CursorPageRequest;
import com.jeannimi.messenger.common.pagination.CursorPageResponse;
import java.util.List;

public interface ChatService {

  ChatDto createChat(ChatCreateRequest request, Long currentUserId);

  CursorPageResponse<ChatDto, CursorDto> getUserChats(Long userId, CursorPageRequest request);

  ChatDto getChat(Long chatId, Long userId);

  void addMember(Long chatId, Long userId, Long currentUserId);

  void removeMember(Long chatId, Long userId, Long currentUserId);

  boolean isParticipant(Long chatId, Long userId);

  void deleteChat(Long chatId, Long currentUserId);

  void leaveChat(Long chatId, Long currentUserId);

  List<ChatMemberDto> getMembers(Long chatId, Long currentUserId);

  void renameChat(Long chatId, String chatName, Long currentUserId);
}
