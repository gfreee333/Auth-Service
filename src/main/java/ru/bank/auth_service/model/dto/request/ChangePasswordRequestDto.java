package ru.bank.auth_service.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDto {
    @NotNull(message = "Текущей пароль не может быть Null")
    private String currentPassword;
    @NotNull(message = "Новый пароль не может быть пустым")
    @Size(min = 10, max = 100, message = "Пароль может содержать от 10 до 100 символов")
    private String newPassword;
    @NotNull(message = "Подтверждение пароля не может быть пустым")
    private String confirmPassword;
}
