package ru.bank.auth_service.infrastructure.security.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.infrastructure.security.JwtTokenProvider;
import ru.bank.auth_service.infrastructure.storage.redis.RedisTokenStore;
import ru.bank.auth_service.model.entity.Users;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenGenerator {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenStore redisTokenStore;

    public TokenPair generatedTokens(Users user){
        TokenPair tokenPair = jwtTokenProvider.generatedTokenPair(user);
        String newSessionId = jwtTokenProvider.getSessionIdFromToken(tokenPair.refreshToken());
        Long accessTtl = jwtTokenProvider.getExpirationFromToken(tokenPair.accessToken());
        Long refreshTtl = jwtTokenProvider.getExpirationFromToken(tokenPair.refreshToken());
        redisTokenStore.saveAccessToken(user.getId(), newSessionId, tokenPair.accessToken(), accessTtl);
        redisTokenStore.saveRefreshToken(user.getId(), newSessionId, tokenPair.refreshToken(), refreshTtl);
        log.debug("Пара токенов сгенерирована для пользователя: {}, сессия: {}", user.getId(), newSessionId);
        return tokenPair;
    }

}
