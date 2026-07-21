package com.jeannimi.messenger.controller;

import com.jeannimi.messenger.dto.ChatCreateRequest;
import com.jeannimi.messenger.dto.ChatDto;
import com.jeannimi.messenger.dto.ChatMemberDto;
import com.jeannimi.messenger.dto.CustomUserDetails;
import com.jeannimi.messenger.dto.RenameChatRequest;
import com.jeannimi.messenger.service.ChatService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  @PostMapping
  public ChatDto createChat(
      @RequestBody ChatCreateRequest request, @AuthenticationPrincipal CustomUserDetails user) {
    return chatService.createChat(request, user.id());
  }

  @GetMapping
  public List<ChatDto> getUserChats(@AuthenticationPrincipal CustomUserDetails user) {
    return chatService.getUserChats(user.id());
  }

  @GetMapping("/{chatId}")
  public ChatDto getChat(
      @PathVariable Long chatId, @AuthenticationPrincipal CustomUserDetails user) {
    return chatService.getChat(chatId, user.id());
  }

  @PostMapping("/{chatId}/members")
  public ResponseEntity<Void> addMember(
      @PathVariable Long chatId,
      @RequestParam Long userId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    chatService.addMember(chatId, userId, currentUser.id());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{chatId}/members/{userId}")
  public ResponseEntity<Void> removeMember(
      @PathVariable Long chatId,
      @PathVariable Long userId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    chatService.removeMember(chatId, userId, currentUser.id());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{chatId}")
  public ResponseEntity<Void> deleteChat(
      @PathVariable Long chatId, @AuthenticationPrincipal CustomUserDetails currentUser) {

    chatService.deleteChat(chatId, currentUser.id());

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{chatId}/members")
  public List<ChatMemberDto> getMembers(
      @PathVariable Long chatId, @AuthenticationPrincipal CustomUserDetails currentUser) {

    return chatService.getMembers(chatId, currentUser.id());
  }

  @DeleteMapping("/{chatId}/leave")
  public ResponseEntity<Void> leaveChat(
      @PathVariable Long chatId, @AuthenticationPrincipal CustomUserDetails currentUser) {

    chatService.leaveChat(chatId, currentUser.id());

    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{chatId}/name")
  public ResponseEntity<Void> renameChat(
      @PathVariable Long chatId,
      @RequestBody RenameChatRequest request,
      @AuthenticationPrincipal CustomUserDetails currentUser) {

    chatService.renameChat(chatId, request.name(), currentUser.id());

    return ResponseEntity.noContent().build();
  }
}
