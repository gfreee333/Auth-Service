package ru.bank.auth_service.model.dto.response;

public record TokenPair(
        String accessToken,
        String refreshToken
){}
