package ru.bank.auth_service.model.dto.response;

import ru.bank.auth_service.model.enums.Role;
import ru.bank.auth_service.model.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserInformationDto(
        UUID id,
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        Role role,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime activatedAt
){}
