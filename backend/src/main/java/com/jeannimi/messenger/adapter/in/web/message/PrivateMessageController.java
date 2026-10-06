package com.jeannimi.messenger.adapter.in.web.message;

import com.jeannimi.messenger.adapter.in.security.CustomUserDetails;
import com.jeannimi.messenger.adapter.in.web.message.dto.MessageDto;
import com.jeannimi.messenger.adapter.in.web.message.dto.MessageSendRequest;
import com.jeannimi.messenger.adapter.in.web.message.mapper.FileUploadMapper;
import com.jeannimi.messenger.application.message.dto.MessageResult;
import com.jeannimi.messenger.application.message.service.PrivateMessageService;
import com.jeannimi.messenger.domain.user.UserId;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/private-messages")
@RequiredArgsConstructor
public class PrivateMessageController {

  private final PrivateMessageService privateMessageService;
  private final FileUploadMapper fileUploadMapper;

  @PostMapping("/{recipientUserId}")
  public MessageDto sendMessage(
      @PathVariable UUID recipientUserId,
      @RequestBody @Valid MessageSendRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {

    MessageResult result =
        privateMessageService.sendMessage(
            user.id(), new UserId(recipientUserId), request.content());

    return MessageDto.fromResult(result);
  }

  @PostMapping("/{recipientUserId}/file")
  public MessageDto sendFile(
      @PathVariable UUID recipientUserId,
      @RequestPart("file") MultipartFile file,
      @AuthenticationPrincipal CustomUserDetails user) {
    MessageResult result =
        privateMessageService.sendFile(
            user.id(), new UserId(recipientUserId), fileUploadMapper.toFileUploadCommand(file));
    return MessageDto.fromResult(result);
  }
}
