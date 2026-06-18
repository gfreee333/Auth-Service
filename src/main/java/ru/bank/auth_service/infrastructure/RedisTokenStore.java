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

    // Сохранение в Redis Refresh токена
    public void saveRefreshToken(UUID userId, String refreshToken, Long ttl){
        String key = REFRESH_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofMillis(ttl));
    }


}
