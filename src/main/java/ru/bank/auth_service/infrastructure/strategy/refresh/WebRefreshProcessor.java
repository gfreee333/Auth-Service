package ru.bank.auth_service.infrastructure.strategy.refresh;

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
public class WebRefreshProcessor implements RefreshResponseProcessorStrategy{

    private final CookieManager cookieManager;

    @Override
    public LoginResponseDto processRefreshResponse(Users user, String accessToken, String refreshToken, HttpServletResponse response) {
        cookieManager.addAccessTokenCookie(response, accessToken);
        cookieManager.addRefreshTokenCookie(response, refreshToken);
        log.debug("Web токены обновлены для пользователя: {}", user.getId());
        return new LoginResponseDto(
                "Токены успешно обновлены",
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
