package ru.bank.auth_service.model.dto.response;

import ru.bank.auth_service.model.enums.ClientType;

import java.util.UUID;

public record LoginResponseDto(
        String message,
        UUID userId,
        String role,
        String accessToken,
        String refreshToken,
        ClientType clientType
){}
