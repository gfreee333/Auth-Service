package ru.bank.auth_service.infrastructure.strategy.login;

import ru.bank.auth_service.model.dto.request.LoginRequestDto;
import ru.bank.auth_service.model.entity.Users;

public interface LoginProcessorStrategy {
    Users authenticate(LoginRequestDto request);
    boolean supports(LoginRequestDto request);
}
