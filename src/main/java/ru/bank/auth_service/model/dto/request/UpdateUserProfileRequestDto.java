package ru.bank.auth_service.model.dto.request;

import lombok.Data;

@Data
public class UpdateUserProfileRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
}
