package ru.bank.auth_service.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginPhoneNumberRequestDto {
    @NotNull(message = "phoneNumber не может быть пустым")
    private String phoneNumber;
    @NotNull(message = "password не может быть пустым")
    private String password;
}
