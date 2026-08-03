package ru.bank.auth_service.infrastructure.security.token;

import java.util.UUID;

public record TokenInvalidationResult(
        UUID userId,
        String sessionId
){}
