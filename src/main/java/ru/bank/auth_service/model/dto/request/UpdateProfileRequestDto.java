package ru.bank.auth_service.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequestDto {
    @Size(max = 100, message = "Максимальная длина firstName 100 символов")
    private String firstName;
    @Size(max = 100, message = "Максимальная длина lastName 100 символов")
    private String lastName;
    @Email(message = "Неверный формат email")
    private String email;
    @Size(max = 20, message = "Неверный формат phoneNumber")
    @Pattern(regexp = "^\\+7\\d{1,14}$", message = "Телефон должен иметь вид: +7XX...XX)")
    private String phoneNumber;
}
