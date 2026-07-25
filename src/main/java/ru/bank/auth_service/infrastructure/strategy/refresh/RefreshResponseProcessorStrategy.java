package ru.bank.auth_service.infrastructure.strategy.refresh;

import jakarta.servlet.http.HttpServletResponse;
import ru.bank.auth_service.model.dto.response.LoginResponseDto;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.model.enums.ClientType;

public interface RefreshResponseProcessorStrategy {
    LoginResponseDto processRefreshResponse(Users user, String accessToken, String refreshToken, HttpServletResponse response);
    void clearClientTokens(HttpServletResponse response);
    ClientType getClientType();
}
