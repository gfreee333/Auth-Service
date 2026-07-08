package ru.bank.auth_service.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequestDto {
    @NotNull(message = "Идентификатор не может быть пустым")
    private String identifier;
    @NotNull(message = "Пароль не может быть пустым")
    private String password;
}
