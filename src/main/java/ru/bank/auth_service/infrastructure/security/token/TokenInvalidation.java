package ru.bank.auth_service.infrastructure.security.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.infrastructure.security.JwtTokenProvider;
import ru.bank.auth_service.infrastructure.storage.redis.RedisTokenStore;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenInvalidation {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenStore redisTokenStore;


    // Инвалидация сразу пары токенов
    public void invalidateTokens(TokenPair tokenPair){
        UUID userId = jwtTokenProvider.getUserIdFromToken(tokenPair.refreshToken());
        String sessionId = jwtTokenProvider.getSessionIdFromToken(tokenPair.refreshToken());
        invalidateAccessToken(tokenPair.accessToken());
        invalidateRefreshToken(userId, sessionId);
        log.debug("Токены инвалидированы для пользователя: {}, сессия: {}", userId, sessionId);
    }

    public TokenInvalidationResult invalidateAccessTokenOnly(TokenPair tokenPair){
        UUID userId = jwtTokenProvider.getUserIdFromToken(tokenPair.refreshToken());
        String sessionId = jwtTokenProvider.getSessionIdFromToken(tokenPair.refreshToken());
        invalidateAccessToken(tokenPair.accessToken());
        return new TokenInvalidationResult(userId, sessionId);
    }

    public void invalidateRefreshTokenOnly(UUID userId, String sessionId) {
        invalidateRefreshToken(userId, sessionId);
    }



    /**
     * <p><b>Метод: invalidateAccessToken</b></p>
     * <p><b>Описание: Инвалидация Access токена + проверка на валидность</b></p>
     *
     * <p><b>Основные шаги:</b></p>
     * <ol>
     *   <li>Проверка валидности входного токена</li>
     *   <li>Получения остатка времени жизни из токена</li>
     *   <li>Добавление access токена в черный список</li>
     *   <li>Удаление старого access токена из Redis</li>
     * </ol>
     *
     * @param accessToken коротко живущий access токен
     */

    // Инвалидация access токена
    private void invalidateAccessToken(String accessToken){
        if(accessToken == null){
            return;
        }
        Long ttl = jwtTokenProvider.getExpirationFromToken(accessToken);
        redisTokenStore.addAccessTokenInBlackList(accessToken, ttl);
        log.debug("Access токен добавлен в черный список");
        UUID userId = jwtTokenProvider.getUserIdFromToken(accessToken);
        String sessionId = jwtTokenProvider.getSessionIdFromToken(accessToken);
        redisTokenStore.deleteAccessToken(userId, sessionId);
        log.debug("Не актуальный access токен удален из Redis");
    }


    /**
     * <p><b>Метод: invalidateRefreshToken</b></p>
     * <p><b>Описание: Инвалидация Refresh токена с проверкой на валидность</b></p>
     *
     * <p><b>Основные шаги:</b></p>
     * <ol>
     *   <li>Проверка валидности входного токена</li>
     *   <li>Удаление долгоживущего токена из Redis</li>
     * </ol>
     *
     * @param userId       id пользователя
     * @param sessionId    id конкретной сессии пользователя
     */

    private void invalidateRefreshToken(UUID userId, String sessionId){
        redisTokenStore.deleteRefreshToken(userId, sessionId);
        log.debug("Refresh токен удален для пользователя: {} сессия: {}", userId, sessionId);
    }


}
