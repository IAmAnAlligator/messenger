package com.jeannimi.messenger.auth.dto;

import com.jeannimi.messenger.user.entity.Password;
import com.jeannimi.messenger.user.entity.Username;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "Username is required")
        @Size(
            min = Username.MIN_USERNAME_LENGTH,
            max = Username.MAX_USERNAME_LENGTH,
            message = "Username must be between {min} and {max} characters")
        String username,
    @NotBlank(message = "Password is required")
        @Size(
            min = Password.MIN_PASSWORD_LENGTH,
            max = Password.MAX_PASSWORD_LENGTH,
            message = "Password must be between {min} and {max} characters")
        String password) {}
