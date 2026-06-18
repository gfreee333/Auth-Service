package ru.bank.auth_service.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationRequestDto {
    @NotNull(message = "firstName не может быть пустым")
    @Size(max = 100, message = "Максимальная длина firstName 100 символов")
    private String firstName;
    @NotNull(message = "lastName не может быть пустым")
    @Size(max = 100, message = "Максимальная длина lastName 100 символов")
    private String lastName;
    @Email(message = "Неверный формат Email")
    @NotNull(message = "Email не может быть пустым")
    private String email;
    @NotNull(message = "phoneNumber не может быть пустым")
    private String phoneNumber;
}
