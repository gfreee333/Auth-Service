package ru.bank.auth_service.infrastructure.storage.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.infrastructure.security.JwtTokenProvider;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisTokenStore {

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private static final String REFRESH_USER_PREFIX = "refresh:user:";
    private static final String ACCESS_USER_PREFIX = "access:user:";
    private static final String ACCESS_BLACK_LIST_PREFIX = "accessBlackList:";

    /** Сохранение в Redis Refresh токена
     * */
    public void saveRefreshToken(UUID userId, String sessionId, String refreshToken, Long ttl) {
        String userKey = REFRESH_USER_PREFIX + userId + ":session:" + sessionId;
        redisTemplate.opsForValue().set(userKey, refreshToken, Duration.ofMillis(ttl));
        log.debug("Refresh token сохранен для пользователя: {}, сессия: {}", userId, sessionId);
    }

    /** Сохранения Access токена в Redis
     * */
    public void saveAccessToken(UUID userId, String sessionId, String accessToken, Long ttl) {
        String userKey = ACCESS_USER_PREFIX + userId + ":session:" + sessionId;
        redisTemplate.opsForValue().set(userKey, accessToken, Duration.ofMillis(ttl));
        log.debug("Access token сохранен для пользователя: {}, сессия: {}", userId, sessionId);
    }

    /** Удаление долгоживущего токена
     * */
    public void deleteRefreshToken(UUID userId, String sessionId) {
        String userKey = REFRESH_USER_PREFIX + userId + ":session:" + sessionId;
        redisTemplate.delete(userKey);
        log.debug("Refresh token удален для пользователя: {}, сессия: {}", userId, sessionId);
    }

    /** Удаление всех Refresh token пользователя
     * */
    public void deleteAllRefreshToken(UUID userId) {
        String pattern = REFRESH_USER_PREFIX + userId + ":session:*";
        Set<String> userKeys = redisTemplate.keys(pattern);
        if (userKeys != null && !userKeys.isEmpty()) {
            redisTemplate.delete(userKeys);
            log.debug("Все refresh tokens удалены у пользователя");
        } else {
            log.debug("Нет активных сессий для пользователя: {}", userId);
        }
    }


    /**
     * Проверка существует ли Refresh токен в Redis - Для Refresh Token Rotation
     */
    public boolean existsRefreshToken(UUID userId, String sessionId) {
        String userKey = REFRESH_USER_PREFIX + userId + ":session:" + sessionId;
        Boolean exists = redisTemplate.hasKey(userKey);
        if (Boolean.TRUE.equals(exists)) {
            log.debug("Refresh токен найден для пользователя: {}, сессия: {}", userId, sessionId);
            return true;
        } else {
            log.warn("Refresh token не найден для пользователя: {}, сессия: {}", userId, sessionId);
            return false;
        }
    }

    /**
     * Удаление access токена из Redis
     */
    public void deleteAccessToken(UUID userId, String sessionId) {
        String userKey = ACCESS_USER_PREFIX + userId + ":session:" + sessionId;
        redisTemplate.delete(userKey);
        log.debug("Access token удален для пользователя: {}, сессия: {}", userId, sessionId);
    }

    /**
     * Добавление всех accessToken в черный список
     */
    public void addAllAccessTokenInBlackList(UUID userId) {
        String pattern = ACCESS_USER_PREFIX + userId + ":session:*";
        Set<String> accessKeys = redisTemplate.keys(pattern);
        if (accessKeys == null || accessKeys.isEmpty()) {
            log.debug("Нет активных access токенов для пользователя: {}", userId);
            return;
        }
        for (String key : accessKeys) {
            String accessToken = redisTemplate.opsForValue().get(key);
            if (accessToken != null) {
                Long ttl = jwtTokenProvider.getExpirationFromToken(accessToken);
                addAccessTokenInBlackList(accessToken, ttl);
                redisTemplate.delete(key);
            }
        }
        log.debug("Все access токены пользователя: {} добавлены в черный список", userId);
    }

    /**
     * Добавления accessToken в черный список
     */
    public void addAccessTokenInBlackList(String accessToken, long ttl) {
        if (ttl <= 0) {
            return;
        }
        String key = ACCESS_BLACK_LIST_PREFIX + accessToken;
        redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofMillis(ttl));
        log.debug("Access токен добавлен в черный список, ttl: {} мс", ttl);
    }

    /** Проверка наличия accessToken в черном списке
     * */
    public boolean checkAccessTokenBlackList(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(ACCESS_BLACK_LIST_PREFIX + accessToken));
    }

}
