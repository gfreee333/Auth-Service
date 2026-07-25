package ru.bank.auth_service.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.bank.auth_service.exception.custom.auth.AuthException;
import ru.bank.auth_service.infrastructure.security.JwtTokenProvider;
import ru.bank.auth_service.infrastructure.storage.cookies.CookieManager;
import ru.bank.auth_service.infrastructure.storage.redis.RedisTokenStore;
import ru.bank.auth_service.infrastructure.strategy.refresh.RefreshResponseProcessorFactory;
import ru.bank.auth_service.infrastructure.strategy.refresh.RefreshResponseProcessorStrategy;
import ru.bank.auth_service.model.dto.response.TokenPair;
import ru.bank.auth_service.model.dto.response.LoginResponseDto;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.model.enums.ClientType;
import ru.bank.auth_service.repository.UsersRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenRefreshService {

    private final RefreshResponseProcessorFactory refreshResponseProcessorFactory;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenStore redisTokenStore;
    private final UsersRepository usersRepository;
    private final CookieManager cookieManager;

    // todo: refresh - логика для обновления токенов пользователя
    public LoginResponseDto refreshToken(HttpServletRequest request, HttpServletResponse response, ClientType clientType) {
        log.info("Попытка получения токенов для клиента: {}", clientType);
        String refreshToken = extractRefreshToken(request, clientType);
        validateRefreshTokenExists(refreshToken);
        if (jwtTokenProvider.isInvalidToken(refreshToken)) {
            log.warn("Невалидный refresh токен");
            throw new AuthException("Невалидный refresh токен");
        }
        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String sessionId = jwtTokenProvider.getSessionIdFromToken(refreshToken);
        validateRefreshTokenData(userId, sessionId);
        validateRefreshTokenOwnership(userId, sessionId, refreshToken);
        Users user = findAndValidate(userId);
        invalidateOldAccessToken(request, clientType);
        TokenPair tokenPair = generatedAndStoreNewTokens(user, sessionId, refreshToken);
        RefreshResponseProcessorStrategy processor = refreshResponseProcessorFactory.getProcessor(clientType);
        return processor.processRefreshResponse(user, tokenPair.accessToken(), tokenPair.refreshToken(), response);
    }


    // todo: Извлечение получение refresh токена
    private String extractRefreshToken(HttpServletRequest request, ClientType clientType) {
        if (clientType.isWeb()) {
            return cookieManager.getRefreshTokenFromCookie(request);
        } else {
            return request.getHeader("X-Refresh-Token");
        }
    }

    // todo: Получение access токена
    private String extractAccessToken(HttpServletRequest request, ClientType clientType) {
        if (clientType.isWeb()) {
            return cookieManager.getAccessTokenFromCookie(request);
        } else {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        return null;
    }

    // todo: Проверка и получения данных о пользователе
    private Users findAndValidate(UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new AuthException("Пользователь не найден")); // Добавить другое исключение
        if (user.getStatus().isBlocked()) {
            log.warn("Пользователь: {} со статусом: {}", userId, user.getStatus());
            throw new AuthException("Пользователь имеет статус DELETE OR BLOCKED"); // Добавить другое исключение
        }
        return user;
    }

    // todo: Проверка наличия refresh токена
    private void validateRefreshTokenExists(String refreshToken) {
        if (refreshToken == null) {
            log.warn("Refresh токен не найден");
            throw new AuthException("Refresh токен не найден");
        }
    }

    // todo: Проверка валидности refresh токена
    private void validateRefreshTokenData(UUID userId, String sessionId) {
        if (userId == null || sessionId == null) {
            log.warn("Не удалось получить данные из refresh токена");
            throw new AuthException("Невалидный refresh токен");
        }
    }

    // todo: Проверка принадлежности refresh токена пользователю
    private void validateRefreshTokenOwnership(UUID userId, String sessionId, String refreshToken) {
        if (!redisTokenStore.validateRefreshToken(userId, sessionId, refreshToken)) {
            log.warn("Refresh токен не принадлежит пользователю: {}", userId);
            throw new AuthException("Refresh токен не принадлежит пользователю");
        }
    }

    // todo: Проверка валидности и наличия access токена, в случае если токен был, добавляем его в blackList
    private void invalidateOldAccessToken(HttpServletRequest request, ClientType clientType) {
        String oldAccessToken = extractAccessToken(request, clientType);
        if (oldAccessToken == null) {
            log.debug("Access токен отсутствует в запросе");
            return;
        }
        if (jwtTokenProvider.isInvalidToken(oldAccessToken)) {
            log.debug("Старый access токен невалидный");
            return;
        }
        Long ttl = jwtTokenProvider.getExpirationFromToken(oldAccessToken);
        if (ttl > 0) {
            redisTokenStore.addAccessTokenInBlackList(oldAccessToken, ttl);
            log.debug("Старый access токен помещен в blacklist");
        }
    }

    // todo: Генерация новой пары токенов (access, refresh)
    private TokenPair generatedAndStoreNewTokens(Users user, String sessionId, String refreshToken) {
        TokenPair newTokenPair = jwtTokenProvider.generatedTokenPair(user);
        String newSessionId = jwtTokenProvider.getSessionIdFromToken(newTokenPair.refreshToken());
        Long refreshTtl = jwtTokenProvider.getExpirationFromToken(newTokenPair.refreshToken());
        redisTokenStore.deleteRefreshToken(user.getId(), sessionId);
        redisTokenStore.saveRefreshToken(user.getId(), newSessionId, newTokenPair.refreshToken(), refreshTtl);
        log.debug("Новая обновленная пара токенов: (access: {}, refresh: {}, новая сессия: {}"
                , newTokenPair.accessToken(), newTokenPair.refreshToken(), newSessionId);
        return newTokenPair;
    }

}
