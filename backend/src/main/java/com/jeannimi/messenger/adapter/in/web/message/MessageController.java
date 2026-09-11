package com.jeannimi.messenger.adapter.in.web.message;

import com.jeannimi.messenger.adapter.in.web.mapper.CursorPaginationMapper;
import com.jeannimi.messenger.application.common.pagination.CursorPageQuery;
import com.jeannimi.messenger.application.common.pagination.CursorPageResult;
import com.jeannimi.messenger.application.message.dto.FileDownloadResult;
import com.jeannimi.messenger.application.message.dto.MessageResult;
import com.jeannimi.messenger.application.user.dto.UserResult;
import com.jeannimi.messenger.common.pagination.CursorDto;
import com.jeannimi.messenger.common.pagination.CursorPageRequest;
import com.jeannimi.messenger.common.pagination.CursorPageResponse;
import com.jeannimi.messenger.adapter.in.web.message.dto.FileAttachmentDto;
import com.jeannimi.messenger.adapter.in.web.message.dto.MessageDto;
import com.jeannimi.messenger.adapter.in.web.message.dto.MessageSendRequest;
import com.jeannimi.messenger.adapter.in.web.message.mapper.FileDownloadMapper;
import com.jeannimi.messenger.adapter.in.web.message.mapper.FileUploadMapper;
import com.jeannimi.messenger.application.message.service.MessageService;
import com.jeannimi.messenger.adapter.in.security.CustomUserDetails;
import com.jeannimi.messenger.adapter.in.web.user.dto.UserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
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
@Validated
public class MessageController {

  private final MessageService messageService;
  private final CursorPaginationMapper cursorMapper;
  private final FileUploadMapper fileUploadMapper;
  private final FileDownloadMapper fileDownloadMapper;

  @GetMapping("/{messageId}/file")
  public ResponseEntity<InputStreamResource> getFile(
      @PathVariable @Positive Long chatId,
      @PathVariable @Positive Long messageId,
      @AuthenticationPrincipal CustomUserDetails user) {

    FileDownloadResult file =
        messageService.getFile(chatId, messageId, user.id());

    return fileDownloadMapper.toResponse(file);
  }

  @PostMapping("/file")
  public MessageDto sendFile(
      @PathVariable @Positive Long chatId,
      @RequestPart("file") MultipartFile file,
      @AuthenticationPrincipal CustomUserDetails user) {

    MessageResult result =
        messageService.sendFile(
            chatId,
            user.id(),
            fileUploadMapper.toFileUploadCommand(file));

    return MessageDto.fromResult(result);
  }

  @PostMapping
  public MessageDto sendMessage(
      @PathVariable @Positive Long chatId,
      @RequestBody @Valid MessageSendRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {

    MessageResult result =
        messageService.sendMessage(
            chatId,
            user.id(),
            request.content());

    return MessageDto.fromResult(result);
  }

  @GetMapping
  public CursorPageResponse<MessageDto, CursorDto> getMessages(
      @PathVariable @Positive Long chatId,
      @Valid @ModelAttribute CursorPageRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {

    CursorPageQuery query = cursorMapper.toQuery(request);

    CursorPageResult<MessageResult> result =
        messageService.getMessages(
            chatId,
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
      @PathVariable @Positive Long chatId,
      @PathVariable @Positive Long messageId,
      @AuthenticationPrincipal CustomUserDetails user) {

    MessageResult result =
        messageService.getMessage(
            chatId,
            messageId,
            user.id());

    return MessageDto.fromResult(result);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{messageId}")
  public void deleteMessage(
      @PathVariable @Positive Long chatId,
      @PathVariable @Positive Long messageId,
      @AuthenticationPrincipal CustomUserDetails user) {

    messageService.deleteMessage(
        chatId,
        messageId,
        user.id());
  }

  private MessageDto toDto(MessageResult result, Long chatId) {

    FileAttachmentDto attachmentDto =
        result.attachment() == null
            ? null
            : new FileAttachmentDto(
                result.attachment().id(),
                result.attachment().originalFileName(),
                result.attachment().contentType(),
                result.attachment().size(),
                "/api/chats/" + chatId + "/messages/" + result.id() + "/file");

    UserResult sender = result.sender();

    UserDto senderDto = new UserDto(sender.id(), sender.username(), sender.role());

    return new MessageDto(
        result.id(),
        result.chatId(),
        senderDto,
        result.content(),
        result.createdAt(),
        attachmentDto);
  }

  private CursorPageResponse<MessageDto, CursorDto> toDto(
      CursorPageResult<MessageResult> result, Long chatId) {

    return new CursorPageResponse<>(
        result.content().stream()
            .map(message -> toDto(message, chatId))
            .toList(),
        cursorMapper.toDto(result.nextCursor()),
        result.hasNext());
  }
}
