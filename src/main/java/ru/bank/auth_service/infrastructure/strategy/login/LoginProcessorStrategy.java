package ru.bank.auth_service.infrastructure.strategy.login;

import ru.bank.auth_service.model.dto.request.LoginRequestDto;
import ru.bank.auth_service.model.entity.Users;

public interface LoginProcessorStrategy {
    /**
     * Аутентификация пользователя в системе
     */
    Users authenticate(LoginRequestDto request);
    /**
     * Проверка поддерживает ли стратегия данный запрос
     */
    boolean supports(LoginRequestDto request);
}
