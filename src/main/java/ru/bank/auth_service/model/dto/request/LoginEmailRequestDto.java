package ru.bank.auth_service.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginEmailRequestDto {
    @Email(message = "Неверный формат email")
    @NotNull(message = "email не может быть пустым")
    private String email;
    @NotNull(message = "Пароль не может быть пустым")
    private String password;
}
