package com.jeannimi.messenger.adapter.in.web.chat;

import com.jeannimi.messenger.adapter.in.security.CustomUserDetails;
import com.jeannimi.messenger.adapter.in.web.chat.dto.ChatCreateRequest;
import com.jeannimi.messenger.adapter.in.web.chat.dto.ChatDto;
import com.jeannimi.messenger.adapter.in.web.chat.dto.ChatMemberDto;
import com.jeannimi.messenger.adapter.in.web.chat.dto.RenameChatRequest;
import com.jeannimi.messenger.adapter.in.web.mapper.CursorPaginationMapper;
import com.jeannimi.messenger.adapter.in.web.pagination.CursorDto;
import com.jeannimi.messenger.adapter.in.web.pagination.CursorPageRequest;
import com.jeannimi.messenger.adapter.in.web.pagination.CursorPageResponse;
import com.jeannimi.messenger.adapter.in.web.user.dto.UserDto;
import com.jeannimi.messenger.application.chat.command.ChatCreateCommand;
import com.jeannimi.messenger.application.chat.command.RenameChatCommand;
import com.jeannimi.messenger.application.chat.dto.ChatMemberResult;
import com.jeannimi.messenger.application.chat.dto.ChatResult;
import com.jeannimi.messenger.application.chat.service.ChatService;
import com.jeannimi.messenger.application.common.pagination.CursorPageQuery;
import com.jeannimi.messenger.application.common.pagination.CursorPageResult;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.user.UserId;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
  private final CursorPaginationMapper cursorMapper;

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
      @Valid @ModelAttribute CursorPageRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {

    CursorPageQuery<ChatId> query = cursorMapper.toQuery(request, ChatId::new);

    CursorPageResult<ChatResult, ChatId> result = chatService.getUserChats(user.id(), query);

    return toDto(result);
  }

  @GetMapping("/{chatId}")
  public ChatDto getChat(
      @PathVariable UUID chatId, @AuthenticationPrincipal CustomUserDetails user) {

    ChatResult result = chatService.getChat(new ChatId(chatId), user.id());

    return toDto(result);
  }

  @PostMapping("/{chatId}/members")
  public ResponseEntity<Void> addMember(
      @PathVariable UUID chatId,
      @RequestParam UUID userId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {

    chatService.addMember(new ChatId(chatId), new UserId(userId), currentUser.id());

    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{chatId}/members/{userId}")
  public ResponseEntity<Void> removeMember(
      @PathVariable UUID chatId,
      @PathVariable UUID userId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {

    chatService.removeMember(new ChatId(chatId), new UserId(userId), currentUser.id());

    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{chatId}")
  public ResponseEntity<Void> deleteChat(
      @PathVariable UUID chatId, @AuthenticationPrincipal CustomUserDetails currentUser) {

    chatService.deleteChat(new ChatId(chatId), currentUser.id());

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{chatId}/members")
  public List<ChatMemberDto> getMembers(
      @PathVariable UUID chatId, @AuthenticationPrincipal CustomUserDetails currentUser) {

    List<ChatMemberResult> results = chatService.getMembers(new ChatId(chatId), currentUser.id());

    return results.stream().map(this::toDto).toList();
  }

  @DeleteMapping("/{chatId}/leave")
  public ResponseEntity<Void> leaveChat(
      @PathVariable UUID chatId, @AuthenticationPrincipal CustomUserDetails currentUser) {

    chatService.leaveChat(new ChatId(chatId), currentUser.id());

    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{chatId}/name")
  public ResponseEntity<Void> renameChat(
      @PathVariable UUID chatId,
      @RequestBody @Valid RenameChatRequest request,
      @AuthenticationPrincipal CustomUserDetails currentUser) {

    RenameChatCommand command = new RenameChatCommand(request.name());

    chatService.renameChat(new ChatId(chatId), command, currentUser.id());

    return ResponseEntity.noContent().build();
  }

  private ChatDto toDto(ChatResult result) {

    List<ChatMemberDto> members =
        result.members() == null ? List.of() : result.members().stream().map(this::toDto).toList();

    return new ChatDto(
        result.id().value(),
        result.name(),
        result.type(),
        members,
        result.createdAt(),
        result.lastMessageAt());
  }

  private ChatMemberDto toDto(ChatMemberResult result) {

    UUID lastReadMessageId =
        result.lastReadMessageId() == null ? null : result.lastReadMessageId().value();

    return new ChatMemberDto(
        new UserDto(
            result.user().id().value(),
            result.user().handle(),
            result.user().username(),
            result.user().role()),
        result.chatRole(),
        result.joinedAt(),
        lastReadMessageId);
  }

  private CursorPageResponse<ChatDto, CursorDto> toDto(
      CursorPageResult<ChatResult, ChatId> result) {

    List<ChatDto> content = result.content().stream().map(this::toDto).toList();

    CursorDto nextCursor =
        result.nextCursor() == null
            ? null
            : new CursorDto(result.nextCursor().time(), result.nextCursor().id().value());

    return new CursorPageResponse<>(content, nextCursor, result.hasNext());
  }
}
