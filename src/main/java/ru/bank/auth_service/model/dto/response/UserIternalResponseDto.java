package ru.bank.auth_service.model.dto.response;

import ru.bank.auth_service.model.enums.UserStatus;

import java.util.UUID;

public record UserIternalResponseDto(
        UUID userId,
        UserStatus status,
        String email
){}
