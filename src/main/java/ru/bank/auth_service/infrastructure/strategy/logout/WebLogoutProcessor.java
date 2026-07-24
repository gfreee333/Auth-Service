package ru.bank.auth_service.infrastructure.strategy.logout;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.infrastructure.storage.cookies.CookieManager;
import ru.bank.auth_service.model.dto.response.TokenPair;
import ru.bank.auth_service.model.enums.ClientType;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebLogoutProcessor implements LogoutProcessorStrategy{

    private final CookieManager cookieManager;

    @Override
    public TokenPair extractTokens(HttpServletRequest request) {
        String accessToken = cookieManager.getAccessTokenFromCookie(request);
        String refreshToken = cookieManager.getRefreshTokenFromCookie(request);
        log.debug("Токены для Web клиента получены");
        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public void clearClientTokens(HttpServletResponse response) {
        cookieManager.deleteRefreshTokenCookies(response);
        cookieManager.deleteAccessTokenCookies(response);
        log.debug("Cookies очищены для Web клиента");
    }

    @Override
    public ClientType getClientType() {
        return ClientType.WEB;
    }

}
