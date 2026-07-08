package ru.bank.auth_service.infrastructure.strategy.client;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.infrastructure.storage.cookies.CookieManager;
import ru.bank.auth_service.model.dto.response.LoginResponseDto;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.model.enums.ClientType;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebClientResponseProcessor implements ClientResponseProcessorStrategy {

    private final CookieManager cookieManager;

    @Override
    public LoginResponseDto processorLoginResponse(Users user, String accessToken, String refreshToken, HttpServletResponse response) {
        cookieManager.addRefreshTokenCookie(response, refreshToken);
        cookieManager.addAccessTokenCookie(response, accessToken);
        log.info("Web пользователь {} успешно вошел в систему", user.getId());
        return new LoginResponseDto(
                "Успешный вход в систему",
                user.getId(),
                user.getRole().name(),
                user.getStatus().isFirstLogin(),
                null,
                null,
                ClientType.WEB
        );
    }

    @Override
    public ClientType getClientType() {
        return ClientType.WEB;
    }
}
