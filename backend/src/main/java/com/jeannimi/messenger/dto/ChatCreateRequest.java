package com.jeannimi.messenger.dto;

import com.jeannimi.messenger.entity.ChatType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatCreateRequest {

  // Тип чата: PRIVATE / GROUP
  @NotNull private ChatType type;

  // Название (только для GROUP)
  private String name;

  // Участники (кроме текущего пользователя)
  @NotEmpty private List<Long> memberIds;
}
