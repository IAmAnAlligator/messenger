package com.jeannimi.messenger.application.chat.service;

import com.jeannimi.messenger.application.chat.command.ChatCreateCommand;
import com.jeannimi.messenger.application.chat.command.RenameChatCommand;
import com.jeannimi.messenger.application.chat.dto.ChatMemberResult;
import com.jeannimi.messenger.application.chat.dto.ChatResult;
import com.jeannimi.messenger.common.pagination.CursorDto;
import com.jeannimi.messenger.application.common.pagination.CursorPageQuery;
import com.jeannimi.messenger.application.common.pagination.CursorPageResult;
import java.util.List;

public interface ChatService {

  ChatResult createChat(ChatCreateCommand request, Long currentUserId);

  CursorPageResult<ChatResult> getUserChats(Long userId, CursorPageQuery query);

  ChatResult getChat(Long chatId, Long userId);

  void addMember(Long chatId, Long userId, Long currentUserId);

  void removeMember(Long chatId, Long userId, Long currentUserId);

  boolean isParticipant(Long chatId, Long userId);

  void deleteChat(Long chatId, Long currentUserId);

  void leaveChat(Long chatId, Long currentUserId);

  List<ChatMemberResult> getMembers(Long chatId, Long currentUserId);

  void renameChat(Long chatId, RenameChatCommand command, Long currentUserId);
}
