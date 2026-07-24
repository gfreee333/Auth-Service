package ru.bank.auth_service.model.dto.response;

import ru.bank.auth_service.model.enums.UserStatus;

public record UserProfile(
    String firstName,
    String lastName,
    String email,
    UserStatus status,
    String phoneNumber
){}
