package ru.bank.auth_service.model.dto.response;

public record RegistrationResponseDto(
        String firstName,
        String lastName,
        String message
){}
