package com.jeannimi.messenger.adapter.in.web.auth.dto;

import com.jeannimi.messenger.domain.user.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    @Size(max = com.jeannimi.messenger.domain.user.Email.MAX_EMAIL_LENGTH, message = "Email must not exceed 100 characters")
    String email,

    @NotBlank(message = "Password is required")
    @Size(
        min = Password.MIN_PASSWORD_LENGTH,
        max = Password.MAX_PASSWORD_LENGTH,
        message = "Password must be between {min} and {max} characters")
    String password) {}