package ru.bank.auth_service.model.dto.response;

import java.time.LocalDateTime;

public record UsersProfileResponseDto(
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime activatedAt
){}
