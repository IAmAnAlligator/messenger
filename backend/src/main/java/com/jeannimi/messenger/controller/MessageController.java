package com.jeannimi.messenger.controller;

import com.jeannimi.messenger.dto.CustomUserDetails;
import com.jeannimi.messenger.dto.MessageDto;
import com.jeannimi.messenger.dto.MessageSendRequest;
import com.jeannimi.messenger.service.MessageService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/chats/{chatId}/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  @PostMapping
  public MessageDto sendMessage(
      @PathVariable Long chatId,
      @RequestBody @Valid MessageSendRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {
    return messageService.sendMessage(chatId, user.id(), request.content());
  }

  // GET LIST
  @GetMapping
  public List<MessageDto> getMessages(
      @PathVariable Long chatId,
      @RequestParam(required = false) Long cursor,
      @AuthenticationPrincipal CustomUserDetails user) {
    return messageService.getMessages(chatId, user.id(), cursor);
  }

  // GET ONE
  @GetMapping("/{messageId}")
  public MessageDto getMessage(
      @PathVariable Long chatId,
      @PathVariable Long messageId,
      @AuthenticationPrincipal CustomUserDetails user) {
    return messageService.getMessage(chatId, messageId, user.id());
  }

  // DELETE
  //  @DeleteMapping("/{messageId}")
  //  public void deleteMessage(
  //      @PathVariable Long chatId,
  //      @PathVariable Long messageId,
  //      @AuthenticationPrincipal CustomUserDetails user) {
  //    messageService.deleteMessage(chatId, messageId, user.getId());
  //  }
}
