package com.jeannimi.messenger.adapter.in.web.message;

import com.jeannimi.messenger.adapter.in.security.CustomUserDetails;
import com.jeannimi.messenger.adapter.in.web.mapper.CursorPaginationMapper;
import com.jeannimi.messenger.adapter.in.web.message.dto.MessageDto;
import com.jeannimi.messenger.adapter.in.web.message.dto.MessageSendRequest;
import com.jeannimi.messenger.adapter.in.web.message.mapper.FileDownloadMapper;
import com.jeannimi.messenger.adapter.in.web.message.mapper.FileUploadMapper;
import com.jeannimi.messenger.adapter.in.web.pagination.CursorDto;
import com.jeannimi.messenger.adapter.in.web.pagination.CursorPageRequest;
import com.jeannimi.messenger.adapter.in.web.pagination.CursorPageResponse;
import com.jeannimi.messenger.application.common.pagination.CursorPageQuery;
import com.jeannimi.messenger.application.common.pagination.CursorPageResult;
import com.jeannimi.messenger.application.message.dto.FileDownloadResult;
import com.jeannimi.messenger.application.message.dto.MessageResult;
import com.jeannimi.messenger.application.message.service.MessageService;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.message.MessageId;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/chats/{chatId}/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;
  private final CursorPaginationMapper cursorMapper;
  private final FileUploadMapper fileUploadMapper;
  private final FileDownloadMapper fileDownloadMapper;

  @GetMapping("/{messageId}/file")
  public ResponseEntity<InputStreamResource> getFile(
      @PathVariable UUID chatId,
      @PathVariable UUID messageId,
      @AuthenticationPrincipal CustomUserDetails user) {

    FileDownloadResult file =
        messageService.getFile(
            new ChatId(chatId),
            new MessageId(messageId),
            user.id());

    return fileDownloadMapper.toResponse(file);
  }

  @PostMapping("/file")
  public MessageDto sendFile(
      @PathVariable UUID chatId,
      @RequestPart("file") MultipartFile file,
      @AuthenticationPrincipal CustomUserDetails user) {

    MessageResult result =
        messageService.sendFile(
            new ChatId(chatId),
            user.id(),
            fileUploadMapper.toFileUploadCommand(file));

    return MessageDto.fromResult(result);
  }

  @PostMapping
  public MessageDto sendMessage(
      @PathVariable UUID chatId,
      @RequestBody @Valid MessageSendRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {

    MessageResult result =
        messageService.sendMessage(
            new ChatId(chatId),
            user.id(),
            request.content());

    return MessageDto.fromResult(result);
  }

  @GetMapping
  public CursorPageResponse<MessageDto, CursorDto> getMessages(
      @PathVariable UUID chatId,
      @Valid @ModelAttribute CursorPageRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {

    CursorPageQuery<MessageId> query =
        cursorMapper.toQuery(
            request,
            MessageId::new);

    CursorPageResult<MessageResult, MessageId> result =
        messageService.getMessages(
            new ChatId(chatId),
            user.id(),
            query);

    return new CursorPageResponse<>(
        result.content().stream()
            .map(MessageDto::fromResult)
            .toList(),
        cursorMapper.toDto(result.nextCursor()),
        result.hasNext());
  }

  @GetMapping("/{messageId}")
  public MessageDto getMessage(
      @PathVariable UUID chatId,
      @PathVariable UUID messageId,
      @AuthenticationPrincipal CustomUserDetails user) {

    MessageResult result =
        messageService.getMessage(
            new ChatId(chatId),
            new MessageId(messageId),
            user.id());

    return MessageDto.fromResult(result);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{messageId}")
  public void deleteMessage(
      @PathVariable UUID chatId,
      @PathVariable UUID messageId,
      @AuthenticationPrincipal CustomUserDetails user) {

    messageService.deleteMessage(
        new ChatId(chatId),
        new MessageId(messageId),
        user.id());
  }
}
