package com.jeannimi.messenger.chat.controller;

import com.jeannimi.messenger.application.chat.command.ChatCreateCommand;
import com.jeannimi.messenger.application.chat.command.RenameChatCommand;
import com.jeannimi.messenger.application.chat.dto.ChatMemberResult;
import com.jeannimi.messenger.application.chat.dto.ChatResult;
import com.jeannimi.messenger.chat.dto.ChatCreateRequest;
import com.jeannimi.messenger.chat.dto.ChatDto;
import com.jeannimi.messenger.chat.dto.ChatMemberDto;
import com.jeannimi.messenger.chat.dto.RenameChatRequest;
import com.jeannimi.messenger.chat.service.ChatService;
import com.jeannimi.messenger.common.pagination.CursorDto;
import com.jeannimi.messenger.common.pagination.CursorPageRequest;
import com.jeannimi.messenger.common.pagination.CursorPageResponse;
import com.jeannimi.messenger.user.dto.CustomUserDetails;
import com.jeannimi.messenger.user.dto.UserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Validated
public class ChatController {

  private final ChatService chatService;

  @PostMapping
  public ChatDto createChat(
      @RequestBody @Valid ChatCreateRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {

    ChatCreateCommand command =
        new ChatCreateCommand(request.name(), request.memberIds(), request.type());
    ChatResult result = chatService.createChat(command, user.id());

    return toDto(result);
  }

  @GetMapping
  public CursorPageResponse<ChatDto, CursorDto> getUserChats(
      @Valid @org.springframework.web.bind.annotation.ModelAttribute CursorPageRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {

    CursorPageResponse<ChatResult, CursorDto> result = chatService.getUserChats(user.id(), request);

    return toDto(result);
  }

  @GetMapping("/{chatId}")
  public ChatDto getChat(
      @PathVariable @Positive Long chatId, @AuthenticationPrincipal CustomUserDetails user) {

    ChatResult result = chatService.getChat(chatId, user.id());

    return toDto(result);
  }

  @PostMapping("/{chatId}/members")
  public ResponseEntity<Void> addMember(
      @PathVariable @Positive Long chatId,
      @RequestParam @Positive Long userId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {

    chatService.addMember(chatId, userId, currentUser.id());

    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{chatId}/members/{userId}")
  public ResponseEntity<Void> removeMember(
      @PathVariable @Positive Long chatId,
      @PathVariable @Positive Long userId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {

    chatService.removeMember(chatId, userId, currentUser.id());

    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{chatId}")
  public ResponseEntity<Void> deleteChat(
      @PathVariable @Positive Long chatId, @AuthenticationPrincipal CustomUserDetails currentUser) {

    chatService.deleteChat(chatId, currentUser.id());

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{chatId}/members")
  public List<ChatMemberDto> getMembers(
      @PathVariable @Positive Long chatId, @AuthenticationPrincipal CustomUserDetails currentUser) {

    List<ChatMemberResult> results = chatService.getMembers(chatId, currentUser.id());

    return results.stream().map(this::toDto).toList();
  }

  @DeleteMapping("/{chatId}/leave")
  public ResponseEntity<Void> leaveChat(
      @PathVariable @Positive Long chatId, @AuthenticationPrincipal CustomUserDetails currentUser) {

    chatService.leaveChat(chatId, currentUser.id());

    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{chatId}/name")
  public ResponseEntity<Void> renameChat(
      @PathVariable @Positive Long chatId,
      @RequestBody @Valid RenameChatRequest request,
      @AuthenticationPrincipal CustomUserDetails currentUser) {

    RenameChatCommand command = new RenameChatCommand(request.name());

    chatService.renameChat(chatId, command, currentUser.id());

    return ResponseEntity.noContent().build();
  }

  private ChatDto toDto(ChatResult result) {

    List<ChatMemberDto> members =
        result.members() == null ? List.of() : result.members().stream().map(this::toDto).toList();

    return new ChatDto(
        result.id(),
        result.name(),
        result.type(),
        members,
        result.createdAt(),
        result.lastMessageAt());
  }

  private ChatMemberDto toDto(ChatMemberResult result) {

    return new ChatMemberDto(
        new UserDto(result.user().id(), result.user().username(), result.user().role()),
        result.chatRole(),
        result.joinedAt(),
        result.lastReadMessageId());
  }

  private CursorPageResponse<ChatDto, CursorDto> toDto(
      CursorPageResponse<ChatResult, CursorDto> result) {

    return new CursorPageResponse<>(
        result.content().stream().map(this::toDto).toList(), result.nextCursor(), result.hasNext());
  }
}
