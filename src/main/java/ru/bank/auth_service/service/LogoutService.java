package ru.bank.auth_service.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.bank.auth_service.exception.custom.auth.AuthException;
import ru.bank.auth_service.infrastructure.security.JwtTokenProvider;
import ru.bank.auth_service.infrastructure.storage.redis.RedisTokenStore;
import ru.bank.auth_service.infrastructure.strategy.logout.LogoutProcessorFactory;
import ru.bank.auth_service.infrastructure.strategy.logout.LogoutProcessorStrategy;
import ru.bank.auth_service.model.dto.response.TokenPair;
import ru.bank.auth_service.model.enums.ClientType;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService {

    private final LogoutProcessorFactory logoutProcessorFactory;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenStore redisTokenStore;

    // todo: logout - выход пользователя из системы
    public void logout(HttpServletRequest request, HttpServletResponse response, ClientType clientType) {
        log.info("Попытка выхода для клиента: {}", clientType);
        TokenPair tokens = extractTokens(request, clientType);
        UUID userId = extractUserId(tokens);
        String sessionId = extractSessionId(tokens);
        if (userId == null) {
            log.error("Не удалось определить userId при выходе");
            throw new AuthException("Не удалось определить пользователя");
        }
        if (sessionId == null) {
            log.error("Не удалось определить сессию пользователя при выходе");
            throw new AuthException("Не удалось определить сессию пользователя");
        }
        invalidateAccessToken(tokens.accessToken());
        invalidateRefreshToken(tokens.refreshToken(), userId, sessionId);
        clearClientTokens(response, clientType);
        log.info("Выход выполнен для пользователя: {}, сессия: {}", userId, sessionId);
    }

    // todo: Извлечение токена из запроса в зависимости от типа клиента
    private TokenPair extractTokens(HttpServletRequest request, ClientType clientType){
        LogoutProcessorStrategy processor = logoutProcessorFactory.getProcessor(clientType);
        return processor.extractTokens(request);
    }

    // todo: Извлечение userId из access либо refresh токена
    private UUID extractUserId(TokenPair tokens){
        if(isValidToken(tokens.accessToken())){
            return jwtTokenProvider.getUserIdFromToken(tokens.accessToken());
        }
        if(isValidToken(tokens.refreshToken())){
            return jwtTokenProvider.getUserIdFromToken(tokens.refreshToken());
        }
        return null;
    }

    // todo: Извлечение sessionId из access либо refresh токена
    private String extractSessionId(TokenPair tokens){
        if(isValidToken(tokens.accessToken())){
            return jwtTokenProvider.getSessionIdFromToken(tokens.accessToken());
        }
        if(isValidToken(tokens.refreshToken())){
            return jwtTokenProvider.getSessionIdFromToken(tokens.refreshToken());
        }
        return null;
    }

    // todo: Проверка валидности токена
    private boolean isValidToken(String token){
        return token != null && jwtTokenProvider.isValidToken(token);
    }

    // todo: Добавления access токена в blackList
    private void invalidateAccessToken(String accessToken){
        if (!isValidToken(accessToken)){
            return;
        }
        Long ttl = jwtTokenProvider.getExpirationFromToken(accessToken);
        if(ttl > 0){
            redisTokenStore.addAccessTokenInBlackList(accessToken, ttl);
            log.debug("Access токен добавлен в черный список");
        }
    }

    // todo: Удаление refresh токена из Redis для конкретной session
    private void invalidateRefreshToken(String refreshToken, UUID userId, String sessionId){
        if(!isValidToken(refreshToken)){
            return;
        }
        redisTokenStore.deleteRefreshToken(userId, sessionId, refreshToken);
        log.debug("Refresh токен удален с userId: {}, сессия: {}", userId, sessionId);
    }

    // todo: Очистка токенов на стороне клиента
    private void clearClientTokens(HttpServletResponse response, ClientType clientType){
        LogoutProcessorStrategy processor = logoutProcessorFactory.getProcessor(clientType);
        processor.clearClientTokens(response);
    }

}
