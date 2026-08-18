package ru.bank.auth_service.model.dto.response;

import ru.bank.auth_service.model.enums.UserStatus;

import java.util.UUID;

public record UserIternalResponseDto(
        UUID userId,
        String firstName,
        String lastName,
        UserStatus status,
        String email
){}
