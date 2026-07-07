package ru.bank.auth_service.model.dto.response;

import java.util.UUID;

public record LoginResponseDto(
        String message,
        UUID userId,
        String role,
        boolean firstLogin
){}
