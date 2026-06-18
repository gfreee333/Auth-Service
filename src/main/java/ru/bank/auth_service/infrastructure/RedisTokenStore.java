package ru.bank.auth_service.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisTokenStore {
    private final StringRedisTemplate redisTemplate;
    private static final String REFRESH_PREFIX = "refresh:";
    private static final String ACCESS_BLACK_LIST_PREFIX = "accessBlackList:";

    // todo: Сохранение в Redis Refresh токена
    public void saveRefreshToken(UUID userId, String refreshToken, long ttl){
        String key = REFRESH_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofMillis(ttl));
    }
    // todo: Удаление долгоживущего токена
    public void deleteRefreshToken(UUID userId){
        String key = REFRESH_PREFIX + userId;
        redisTemplate.delete(key);
    }
    // todo: Проверка принадлежности токена пользователю
    public boolean validateRefreshToken(UUID userId, String refreshToken){
        String key = REFRESH_PREFIX + userId;
        String stored = redisTemplate.opsForValue().get(key);
        return refreshToken.equals(stored);
    }
    // todo: Добавления accessToken в черный список
    public void addAccessTokenInBlackList(String accessToken, long ttl){
        if(ttl < 0) return;
        String key = ACCESS_BLACK_LIST_PREFIX + accessToken;
        redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofMillis(ttl));
    }
    // todo: Проверка наличия accessToken в черном списке
    public boolean checkAccessTokenBlackList(String accessToken){
        return Boolean.TRUE.equals(redisTemplate.hasKey(ACCESS_BLACK_LIST_PREFIX + accessToken));
    }

}
