package ru.bank.auth_service.infrastructure.strategy.client;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.infrastructure.storage.cookies.CookieManager;
import ru.bank.auth_service.model.dto.response.LoginResponseDto;
import ru.bank.auth_service.infrastructure.security.token.TokenPair;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.model.enums.ClientType;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebClientProcessor implements ClientStrategy {

    private final CookieManager cookieManager;

    /** <p><b>Метод: processLoginResponse</b></p>
     * <p><b>Описание: Генерация ответа пользователя для Web клиента</b></p>
     * @return {@link LoginResponseDto} - ответ пользователю, после успешного входа в систему
     */
    @Override
    public LoginResponseDto processLoginResponse(Users user,
                                                 String accessToken,
                                                 String refreshToken,
                                                 HttpServletResponse response) {
        cookieManager.addAccessTokenCookie(response, accessToken);
        cookieManager.addRefreshTokenCookie(response, refreshToken);
        log.debug("Web пользователь: {} успешно вошел в систему", user.getId());
        return new LoginResponseDto(
                "Успешный вход в систему",
                user.getId(),
                user.getRole().name(),
                null,
                null,
                ClientType.WEB
        );
    }

    /** <p><b>Метод: processRefreshResponse</b></p>
     * <p><b>Описание: Генерация ответа пользователю, <br>
     * после успешного обновления токенов в системе</b></p>
     * @return {@link LoginResponseDto} - ответ пользователю после успешного обновления токенов
     */
    @Override
    public LoginResponseDto processRefreshResponse(Users user,
                                                   String accessToken,
                                                   String refreshToken,
                                                   HttpServletResponse response) {
        cookieManager.addAccessTokenCookie(response, accessToken);
        cookieManager.addRefreshTokenCookie(response, refreshToken);
        log.debug("Web токены обновлены для пользователя: {}", user.getId());
        return new LoginResponseDto(
                "Токены успешно обновлены",
                user.getId(),
                user.getRole().name(),
                null,
                null,
                ClientType.WEB
        );
    }

    /** <p><b>Метод: extractTokens</b></p>
     * <p><b>Описание: Извлечение refresh/access токена из Cookies</b></p>
     * @return {@link TokenPair}
     */
    @Override
    public TokenPair extractTokens(HttpServletRequest request) {
        String accessToken = cookieManager.getAccessTokenFromCookie(request);
        String refreshToken = cookieManager.getRefreshTokenFromCookie(request);
        log.debug("Токены извлечены из Cookies для Web клиента");
        return new TokenPair(accessToken, refreshToken);
    }

    /** <p><b>Метод: clearClientTokens</b></p>
     * <p><b>Описание: Очистка токенов в Cookies на стороне Web клиента</b></p>
     */
    @Override
    public void clearClientTokens(HttpServletResponse response) {
        cookieManager.deleteAccessTokenCookies(response);
        cookieManager.deleteRefreshTokenCookies(response);
        log.debug("Cookies очищены для Web клиента");
    }

    @Override
    public ClientType getClientType() {
        return ClientType.WEB;
    }
}
