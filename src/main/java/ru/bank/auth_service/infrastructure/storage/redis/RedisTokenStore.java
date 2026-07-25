package ru.bank.auth_service.infrastructure.storage.redis;

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
    private static final String ACCESS_BLACK_LIST_PREFIX = "accessBlackList:";

    // todo: Сохранение в Redis Refresh токена
    public void saveRefreshToken(UUID userId, String sessionId, String refreshToken, long ttl) {
        String userKey = REFRESH_USER_PREFIX + userId + ":session:" + sessionId;
        redisTemplate.opsForValue().set(userKey, refreshToken, Duration.ofMillis(ttl));
        log.debug("Refresh token сохранен для пользователя: {}, сессия: {}", userId, sessionId);
    }

    // todo: Удаление долгоживущего токена
    public void deleteRefreshToken(UUID userId, String sessionId) {
        String userKey = REFRESH_USER_PREFIX + userId + ":session:" + sessionId;
        redisTemplate.delete(userKey);
        log.debug("Refresh token удален для пользователя: {}, сессия: {}", userId, sessionId);
    }

    // todo: Удаление всех Refresh token пользователя
    public void deleteAllRefreshToken(UUID userId) {
        String pattern = REFRESH_USER_PREFIX + userId + ":session:*";
        log.info("ПОИСК КЛЮЧА ПО ПАТТЕРНУ: {}", pattern);
        Set<String> userKeys = redisTemplate.keys(pattern);
        log.info("Найдено ключей : {}", userKeys);
        if (userKeys != null && !userKeys.isEmpty()) {
            log.info("Удаляем ключе: {}", userKeys);
            redisTemplate.delete(userKeys);
            log.info("Все refresh tokens удалены у пользователя");
        } else {
            log.debug("Нет активных сессий для пользователя: {}", userId);
        }
    }

    // todo: Проверка существует ли Refresh токен в Redis - Для Refresh Token Rotation
    public boolean existsRefreshToken(UUID userId, String sessionId){
        String userKey = REFRESH_USER_PREFIX + userId + ":session:" + sessionId;
        Boolean exists = redisTemplate.hasKey(userKey);
        if(Boolean.TRUE.equals(exists)){
            log.debug("Refresh токен найден для пользователя: {}, сессия: {}", userId, sessionId);
            return true;
        } else {
            log.warn("Refresh token не найден для пользователя: {}, сессия: {}", userId, sessionId);
            return false;
        }
    }

    // todo: Получение кол-ва активных сессий у пользователя
    public Integer getActiveSessionCount(UUID userId){
        String pattern = REFRESH_USER_PREFIX + userId + ":";
        Set<String> keys = redisTemplate.keys(pattern);
        return keys != null ? keys.size() : 0;
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
