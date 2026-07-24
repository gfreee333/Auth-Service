package ru.bank.auth_service.infrastructure.strategy.logout;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.bank.auth_service.model.dto.response.TokenPair;
import ru.bank.auth_service.model.enums.ClientType;

public interface LogoutProcessorStrategy {
    TokenPair extractTokens(HttpServletRequest request);
    void clearClientTokens(HttpServletResponse response);
    ClientType getClientType();
}
