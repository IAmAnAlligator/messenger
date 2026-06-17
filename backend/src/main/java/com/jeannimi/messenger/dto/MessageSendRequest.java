package com.jeannimi.messenger.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MessageSendRequest {

  private String content;
}
