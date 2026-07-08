package ru.bank.auth_service.infrastructure.strategy.logout;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.model.enums.ClientType;

@Component
@RequiredArgsConstructor
@Slf4j
public class MobileLogoutProcessor implements LogoutProcessorStrategy{

    @Override
    public LogoutTokens extractTokens(HttpServletRequest request) {
        String accessToken = null;
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            accessToken = authHeader.substring(7);
        }
        String refreshToken = request.getHeader("X-Refresh-Token");
        log.debug("Токены для Mobile клиента получены");
        return new LogoutTokens(accessToken, refreshToken);
    }

    @Override
    public void clearClientTokens(HttpServletResponse response) {
        log.debug("Mobile клиент сам очистит токены");
    }

    @Override
    public ClientType getClientType() {
        return ClientType.MOBILE;
    }
}
