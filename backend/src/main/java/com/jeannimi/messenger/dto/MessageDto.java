package com.jeannimi.messenger.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {

  private Long id;
  private Long chatId;
  private UserDto sender;

  private String content;
  private Instant createdAt;
  private String status;
}
