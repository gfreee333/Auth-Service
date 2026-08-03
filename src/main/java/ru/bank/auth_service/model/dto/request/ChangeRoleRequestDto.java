package ru.bank.auth_service.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.bank.auth_service.model.enums.Role;

@Data
public class ChangeRoleRequestDto {
    @NotNull(message = "Роль не может быть пустой")
    private Role role;
}
