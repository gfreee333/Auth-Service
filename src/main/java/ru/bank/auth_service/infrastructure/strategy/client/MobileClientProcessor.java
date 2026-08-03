package ru.bank.auth_service.infrastructure.strategy.client;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.model.dto.response.LoginResponseDto;
import ru.bank.auth_service.infrastructure.security.token.TokenPair;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.model.enums.ClientType;

@Component
@Slf4j
public class MobileClientProcessor implements ClientStrategy {

    /** <p><b>Метод: processLoginResponse</b></p>
     * <p><b>Описание: Генерация ответа пользователя для Mobile клиента</b></p>
     * @return {@link LoginResponseDto} - ответ пользователю, после успешного входа в систему
     */
    @Override
    public LoginResponseDto processLoginResponse(Users user,
                                                 String accessToken,
                                                 String refreshToken,
                                                 HttpServletResponse response) {
        return new LoginResponseDto(
                "Успешный вход в систему",
                user.getId(),
                user.getRole().name(),
                accessToken,
                refreshToken,
                ClientType.MOBILE
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
        log.debug("Mobile токены обновляются на стороне клиента");
        return new LoginResponseDto(
                "Токены успешно обновлены",
                user.getId(),
                user.getRole().name(),
                accessToken,
                refreshToken,
                ClientType.MOBILE
        );
    }

    /** <p><b>Метод: extractTokens</b></p>
     * <p><b>Описание: Извлечение refresh/access токена из header</b></p>
     * @return {@link TokenPair}
     */
    @Override
    public TokenPair extractTokens(HttpServletRequest request) {
        String accessToken = extractAccessToken(request);
        String refreshToken = request.getHeader("X-Refresh-Token");
        log.debug("Токены извлечены из header для Mobile клиента");
        return new TokenPair(accessToken, refreshToken);
    }

    /** <p><b>Метод: extractAccessToken</b></p>
     * <p><b>Описание: Вспомогательный метод по извлечение access токена</b></p>
     * @return access токен, либо null в случае отсутствия токена в header
     */
    private String extractAccessToken(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            return authHeader.substring(7);
        }
        return null;
    }

    /** <p><b>Метод: clearClientTokens</b></p>
     * <p><b>Описание: Очистка токенов на стороне Mobile клиента</b></p>
     */
    @Override
    public void clearClientTokens(HttpServletResponse response) {
        log.debug("Mobile клиент сам отвечает за очистку токенов");
    }

    @Override
    public ClientType getClientType() {
        return ClientType.MOBILE;
    }
}
