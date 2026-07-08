package ru.bank.auth_service.infrastructure.strategy.logout;

public record LogoutTokens(
        String accessToken,
        String refreshToken
) {}
