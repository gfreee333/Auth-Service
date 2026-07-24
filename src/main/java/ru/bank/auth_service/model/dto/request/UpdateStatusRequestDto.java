package ru.bank.auth_service.model.dto.request;

import lombok.Data;
import ru.bank.auth_service.model.enums.UserStatus;

@Data
public class UpdateStatusRequestDto {
    UserStatus userStatus;
}
