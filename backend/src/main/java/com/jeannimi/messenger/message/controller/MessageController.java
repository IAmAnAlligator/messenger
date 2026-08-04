package com.jeannimi.messenger.message.controller;

import com.jeannimi.messenger.message.dto.MessageDto;
import com.jeannimi.messenger.message.dto.MessagePageDto;
import com.jeannimi.messenger.message.dto.MessageSendRequest;
import com.jeannimi.messenger.message.service.MessageService;
import com.jeannimi.messenger.message.service.MessageServiceImpl;
import com.jeannimi.messenger.user.dto.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/chats/{chatId}/messages")
@RequiredArgsConstructor
@Validated
public class MessageController {

  private final MessageService messageService;

  @PostMapping
  public MessageDto sendMessage(
      @PathVariable @Positive Long chatId,
      @RequestBody @Valid MessageSendRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {
    return messageService.sendMessage(chatId, user.id(), request.content());
  }

  // GET LIST
  @GetMapping
  public MessagePageDto getMessages(
      @PathVariable @Positive Long chatId,
      @RequestParam(required = false) @Positive Long cursor,
      @RequestParam(defaultValue = "30") @Positive @Max(MessageServiceImpl.MAX_MESSAGE_PAGE_SIZE)
          int limit,
      @AuthenticationPrincipal CustomUserDetails user) {
    return messageService.getMessages(chatId, user.id(), cursor, limit);
  }

  // GET ONE
  @GetMapping("/{messageId}")
  public MessageDto getMessage(
      @PathVariable @Positive Long chatId,
      @PathVariable @Positive Long messageId,
      @AuthenticationPrincipal CustomUserDetails user) {
    return messageService.getMessage(chatId, messageId, user.id());
  }

  // DELETE
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{messageId}")
  public void deleteMessage(
      @PathVariable @Positive Long chatId,
      @PathVariable @Positive Long messageId,
      @AuthenticationPrincipal CustomUserDetails user) {
    messageService.deleteMessage(chatId, messageId, user.id());
  }
}
