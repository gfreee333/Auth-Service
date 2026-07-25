package ru.bank.auth_service.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.bank.auth_service.exception.custom.auth.InvalidTokenException;
import ru.bank.auth_service.exception.custom.auth.TokenInBlackListException;
import ru.bank.auth_service.exception.custom.user.UserBlockedException;
import ru.bank.auth_service.exception.custom.user.UserNotFoundException;
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
    public LoginResponseDto refreshToken(HttpServletRequest request,
                                         HttpServletResponse response,
                                         ClientType clientType) {
        log.info("Попытка получения токенов для клиента: {}", clientType);
        TokenPair oldTokenPair = extractTokens(request, clientType);
        if (jwtTokenProvider.isInvalidToken(oldTokenPair.refreshToken())) {
            log.warn("Невалидный refresh токен");
            throw new InvalidTokenException("Невалидный refresh токен");
        }
        invalidateOldAccessToken(oldTokenPair.accessToken());
        UUID oldUserId = jwtTokenProvider.getUserIdFromToken(oldTokenPair.refreshToken());
        String oldSessionId = jwtTokenProvider.getSessionIdFromToken(oldTokenPair.refreshToken());
        Users user = findAndValidateUser(oldUserId);
        TokenPair newTokenPair = generatedNewTokens(user, oldSessionId);
        RefreshResponseProcessorStrategy processor = refreshResponseProcessorFactory.getProcessor(clientType);
        return processor.processRefreshResponse(user, newTokenPair.accessToken(), newTokenPair.refreshToken(), response);
    }


    // todo: Извлечение пары токенов access + refresh в зависимости от типа клиента
    private TokenPair extractTokens(HttpServletRequest request, ClientType clientType) {
        String accessToken = extractRefreshToken(request, clientType);
        String refreshToken = extractAccessToken(request, clientType);
        return new TokenPair(accessToken, refreshToken);
    }

    // todo: Извлечение Refresh токена в зависимости от типа клиента
    private String extractRefreshToken(HttpServletRequest request, ClientType clientType) {
        if (clientType.isWeb()) {
            return cookieManager.getRefreshTokenFromCookie(request);
        } else {
            return request.getHeader("X-Refresh-Token");
        }
    }

    // todo: Извлечение Access токена в зависимости от типа клиента
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

    // todo: Проверка валидности access токена + добавление его в черный список
    private void invalidateOldAccessToken(String oldAccessToken) {
        if (oldAccessToken == null) {
            log.debug("Access токен отсутствует в запросе");
            return;
        }
        if (redisTokenStore.checkAccessTokenBlackList(oldAccessToken)) {
            log.warn("Access токен находиться в черном списке");
            throw new TokenInBlackListException("Токен находиться в черном списке");
        }
        if (jwtTokenProvider.isInvalidToken(oldAccessToken)) {
            log.warn("Access токен невалидный");
            throw new InvalidTokenException("Access токен невалиден");
        }
        Long ttl = jwtTokenProvider.getExpirationFromToken(oldAccessToken);
        redisTokenStore.addAccessTokenInBlackList(oldAccessToken, ttl);
        log.debug("Старый access токен добавлен в BlackList");
    }


    // todo: Проверка и получения данных о пользователе
    private Users findAndValidateUser(UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id: {} " + userId + " не найден"));
        if (user.getStatus().isBlocked()) {
            log.warn("Пользователь: {} со статусом: {}", userId, user.getStatus());
            throw new UserBlockedException("Пользователь имеет статус BLOCKED");
        }
        return user;
    }

    // todo: Генерация новой пары токенов (access, refresh)
    private TokenPair generatedNewTokens(Users user, String oldSessionId) {
        TokenPair newTokenPair = jwtTokenProvider.generatedTokenPair(user);
        String newSessionId = jwtTokenProvider.getSessionIdFromToken(newTokenPair.refreshToken());
        Long refreshTtl = jwtTokenProvider.getExpirationFromToken(newTokenPair.refreshToken());
        redisTokenStore.deleteRefreshToken(user.getId(), oldSessionId);
        redisTokenStore.saveRefreshToken(user.getId(), newSessionId, newTokenPair.refreshToken(), refreshTtl);
        log.debug("Новая обновленная пара токенов: (access: {}, refresh: {}, новая сессия: {}"
                , newTokenPair.accessToken(), newTokenPair.refreshToken(), newSessionId);
        return newTokenPair;
    }

}
