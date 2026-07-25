package ru.bank.auth_service.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
        UUID userId = jwtTokenProvider.getUserIdFromToken(tokens.refreshToken());
        String sessionId = jwtTokenProvider.getSessionIdFromToken(tokens.refreshToken());
        invalidateRefreshToken(tokens.refreshToken(), userId, sessionId);
        invalidateAccessToken(tokens.accessToken());
        clearClientTokens(response, clientType);
        log.info("Выход выполнен для пользователя: {}, сессия: {}", userId, sessionId);
    }

    // todo: Извлечение токена из запроса в зависимости от типа клиента
    private TokenPair extractTokens(HttpServletRequest request, ClientType clientType){
        LogoutProcessorStrategy processor = logoutProcessorFactory.getProcessor(clientType);
        return processor.extractTokens(request);
    }

    // todo: Добавления access токена в blackList
    private void invalidateAccessToken(String accessToken){
        if (!jwtTokenProvider.isValidToken(accessToken)){
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
        if(!jwtTokenProvider.isValidToken(refreshToken)){
            return;
        }
        redisTokenStore.deleteRefreshToken(userId, sessionId);
        log.debug("Refresh токен удален с userId: {}, сессия: {}", userId, sessionId);
    }

    // todo: Очистка токенов на стороне клиента
    private void clearClientTokens(HttpServletResponse response, ClientType clientType){
        LogoutProcessorStrategy processor = logoutProcessorFactory.getProcessor(clientType);
        processor.clearClientTokens(response);
    }

}
