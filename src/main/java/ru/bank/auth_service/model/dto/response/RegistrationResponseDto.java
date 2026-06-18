package ru.bank.auth_service.model.dto.response;

public record RegistrationResponseDto(
        String message,
        String firstName,
        String lastName,
        String phoneNumber,
        String email
){}
