package ru.bank.auth_service.model.dto.response;

import java.time.LocalDateTime;

public record ChangePasswordResponseDto(
        String message,
        LocalDateTime changedAt
) {}
