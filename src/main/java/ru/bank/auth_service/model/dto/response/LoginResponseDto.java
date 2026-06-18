package ru.bank.auth_service.model.dto.response;

import ru.bank.auth_service.model.enumerate.Role;

public record LoginResponseDto(
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Role role,
        String message
){}
