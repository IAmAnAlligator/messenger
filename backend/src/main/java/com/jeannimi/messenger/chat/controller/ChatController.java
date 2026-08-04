package com.jeannimi.messenger.chat.controller;

import com.jeannimi.messenger.chat.dto.ChatCreateRequest;
import com.jeannimi.messenger.chat.dto.ChatDto;
import com.jeannimi.messenger.chat.dto.ChatMemberDto;
import com.jeannimi.messenger.chat.dto.ChatPageDto;
import com.jeannimi.messenger.chat.dto.ChatPageRequest;
import com.jeannimi.messenger.chat.dto.RenameChatRequest;
import com.jeannimi.messenger.chat.service.ChatService;
import com.jeannimi.messenger.user.dto.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    return chatService.createChat(request, user.id());
  }

  @GetMapping
  public ChatPageDto getUserChats(
      @Valid @ModelAttribute ChatPageRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {

    return chatService.getUserChats(
        user.id(), request.cursorTime(), request.cursorId(), request.limit());
  }

  @GetMapping("/{chatId}")
  public ChatDto getChat(
      @PathVariable @Positive Long chatId, @AuthenticationPrincipal CustomUserDetails user) {
    return chatService.getChat(chatId, user.id());
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

    return chatService.getMembers(chatId, currentUser.id());
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

    chatService.renameChat(chatId, request.name(), currentUser.id());

    return ResponseEntity.noContent().build();
  }
}
