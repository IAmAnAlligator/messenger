package com.jeannimi.messenger.application.chat.service;

import com.jeannimi.messenger.application.chat.command.ChatCreateCommand;
import com.jeannimi.messenger.application.chat.command.RenameChatCommand;
import com.jeannimi.messenger.application.chat.dto.ChatMemberResult;
import com.jeannimi.messenger.application.chat.dto.ChatResult;
import com.jeannimi.messenger.application.common.pagination.CursorPageQuery;
import com.jeannimi.messenger.application.common.pagination.CursorPageResult;
import com.jeannimi.messenger.application.user.dto.UserProfileResult;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.List;

public interface ChatService {

  ChatResult createChat(ChatCreateCommand request, UserId currentUserId);

  CursorPageResult<ChatResult, ChatId> getUserChats(UserId userId, CursorPageQuery<ChatId> query);

  ChatResult getChat(ChatId chatId, UserId userId);

  void addMember(ChatId chatId, UserId userId, UserId currentUserId);

  void removeMember(ChatId chatId, UserId userId, UserId currentUserId);

  boolean isParticipant(ChatId chatId, UserId userId);

  void deleteChat(ChatId chatId, UserId currentUserId);

  void leaveChat(ChatId chatId, UserId currentUserId);

  List<ChatMemberResult> getMembers(ChatId chatId, UserId currentUserId);

  void renameChat(ChatId chatId, RenameChatCommand command, UserId currentUserId);

  UserProfileResult getMemberProfile(
      ChatId chatId,
      UserId currentUserId,
      UserId memberUserId);
}
