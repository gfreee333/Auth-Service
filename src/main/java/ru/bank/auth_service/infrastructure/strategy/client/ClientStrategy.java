package ru.bank.auth_service.infrastructure.strategy.client;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.bank.auth_service.model.dto.response.LoginResponseDto;
import ru.bank.auth_service.infrastructure.security.token.TokenPair;
import ru.bank.auth_service.model.entity.Users;

public interface ClientStrategy extends ClientBaseStrategy {

    /**
     * Обработка ответа при логине
     */
    LoginResponseDto processLoginResponse(Users user,
                                          String accessToken,
                                          String refreshToken,
                                          HttpServletResponse response);

    /**
     * Обработка ответа при обновлении токенов
     */
    LoginResponseDto processRefreshResponse(Users user,
                                            String accessToken,
                                            String refreshToken,
                                            HttpServletResponse response);

    /**
     * Извлечение токенов из запроса
     */
    TokenPair extractTokens(HttpServletRequest request);

    /**
     * Очистка токенов на стороне клиента
     */
    void clearClientTokens(HttpServletResponse response);
}
