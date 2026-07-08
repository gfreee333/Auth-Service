package ru.bank.auth_service.infrastructure.strategy.login;

import ru.bank.auth_service.model.dto.request.LoginRequestDto;
import ru.bank.auth_service.model.entity.Users;

// Возможность будущего расширения способов входа в систему
public class QRCodeLoginProcessor implements LoginProcessorStrategy {

    @Override
    public Users authenticate(LoginRequestDto request) {
        return null;
    }

    @Override
    public boolean supports(LoginRequestDto request) {
        return false;
    }
}
