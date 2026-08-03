package ru.bank.auth_service.infrastructure.security.token;

public record TokenPair(
        String accessToken,
        String refreshToken
){}
