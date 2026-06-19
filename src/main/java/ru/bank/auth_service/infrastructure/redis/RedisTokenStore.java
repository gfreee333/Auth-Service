package ru.bank.auth_service.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisTokenStore {

    private final StringRedisTemplate redisTemplate;
    private static final String REFRESH_USER_PREFIX = "refresh:user:";
    private static final String REFRESH_PREFIX = "refresh:";
    private static final String ACCESS_BLACK_LIST_PREFIX = "accessBlackList:";

    // todo: Сохранение в Redis Refresh токена
    public void saveRefreshToken(UUID userId, String refreshToken, long ttl) {
        String userKey = REFRESH_USER_PREFIX + userId;
        String tokenKey = REFRESH_PREFIX + refreshToken;
        redisTemplate.opsForSet().add(userKey, refreshToken);
        redisTemplate.expire(userKey, Duration.ofMillis(ttl));
        redisTemplate.opsForValue().set(tokenKey, userId.toString(), Duration.ofMillis(ttl));
        log.debug("Refresh token сохранен для пользователя: {}", userId);
    }

    // todo: Удаление долгоживущего токена
    public void deleteRefreshToken(UUID userId, String refreshToken) {
        String userKey = REFRESH_USER_PREFIX + userId;
        String tokenKey = REFRESH_PREFIX + refreshToken;
        redisTemplate.opsForSet().remove(userKey, refreshToken);
        redisTemplate.delete(tokenKey);
        log.debug("Refresh token удален для пользователя: {}", userId);
    }

    // todo: Удаление всех Refresh token пользователя
    public void deleteAllRefreshToken(UUID userId) {
        String userKey = REFRESH_USER_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(userKey);
        if (tokens != null && !tokens.isEmpty()) {
            tokens.forEach(token -> redisTemplate.delete(REFRESH_PREFIX + token));
            redisTemplate.delete(userKey);
            log.info("Все refresh tokens удалены у пользователя");
        }
    }

    // todo: Проверка принадлежности токена пользователю
    public boolean validateRefreshToken(UUID userId, String refreshToken) {
        String userKey = REFRESH_USER_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(userKey, refreshToken));
    }

    // todo: Добавления accessToken в черный список
    public void addAccessTokenInBlackList(String accessToken, long ttl) {
        if (ttl <= 0) {
            return;
        }
        String key = ACCESS_BLACK_LIST_PREFIX + accessToken;
        redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofMillis(ttl));
        log.debug("Access токен добавлен в черный список, ttl: {} мс", ttl);
    }

    // todo: Проверка наличия accessToken в черном списке
    public boolean checkAccessTokenBlackList(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(ACCESS_BLACK_LIST_PREFIX + accessToken));
    }

}
