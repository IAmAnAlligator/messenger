package com.jeannimi.messenger.dto;

import com.jeannimi.messenger.entity.ChatType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ChatCreateRequest(

    // Тип чата: PRIVATE / GROUP
    @NotNull ChatType type,

    // Название (только для GROUP)
    String name,

    // Участники (кроме текущего пользователя)
    @NotEmpty List<Long> memberIds) {}
