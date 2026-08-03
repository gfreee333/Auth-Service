package ru.bank.auth_service.infrastructure.storage.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TempPasswordStore {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "generated:password:";

    /** Сохранение временного пароля в Redis
     * */
    public void saveInRedis(UUID userId, String tempPassword, Duration ttl){
        redisTemplate.opsForValue().set(KEY_PREFIX + userId, tempPassword, ttl);
    }

    /** Получение временного пароля из Redis
     * */
    public String getInRedis(UUID userId){
        return redisTemplate.opsForValue().get(KEY_PREFIX + userId);
    }

    /** Удаление пароля из Redis
     * */
    public void deleteFromRedis(UUID userId){
        redisTemplate.delete(KEY_PREFIX + userId);
    }

    /** Проверка валидности временного пароля
     * */
    public boolean isValid(UUID userId, String rawPassword){
        String stored = getInRedis(userId);
        return stored != null && stored.equals(rawPassword);
    }

}
