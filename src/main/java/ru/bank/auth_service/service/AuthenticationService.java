package ru.bank.auth_service.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.bank.auth_service.exception.custom.auth.AuthException;
import ru.bank.auth_service.infrastructure.security.JwtTokenProvider;
import ru.bank.auth_service.infrastructure.storage.redis.RedisTokenStore;
import ru.bank.auth_service.infrastructure.strategy.client.ClientResponseProcessorFactory;
import ru.bank.auth_service.infrastructure.strategy.client.ClientResponseProcessorStrategy;
import ru.bank.auth_service.infrastructure.strategy.login.LoginProcessorFactory;
import ru.bank.auth_service.infrastructure.strategy.login.LoginProcessorStrategy;
import ru.bank.auth_service.model.dto.request.LoginRequestDto;
import ru.bank.auth_service.model.dto.response.LoginResponseDto;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.model.enums.ClientType;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final LoginProcessorFactory loginProcessorFactory;
    private final ClientResponseProcessorFactory clientResponseProcessorFactory;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenStore redisTokenStore;

    // todo: login - вход пользователя в систему
    public LoginResponseDto login(LoginRequestDto request, HttpServletResponse response, ClientType clientType) {
        log.info("Попытка входа в систему для клиента: {}, с идентификатором: {}", clientType, request.getIdentifier());
        LoginProcessorStrategy loginProcessor = loginProcessorFactory.getStrategy(request);
        Users user = loginProcessor.authenticate(request);
        if (user.getStatus().isBlocked() || user.getStatus().isDeleted()) {
            throw new AuthException("Пользователь со статусом: " + user.getStatus());
        }
        String accessToken = jwtTokenProvider.generatedAccessToken(user);
        String refreshToken = jwtTokenProvider.generatedRefreshToken(user);
        String sessionId = jwtTokenProvider.getSessionIdFromToken(refreshToken);
        Long refreshTtl = jwtTokenProvider.getExpirationFromToken(refreshToken);
        redisTokenStore.saveRefreshToken(user.getId(), sessionId, refreshToken, refreshTtl);
        log.debug("Создана сессия для пользователя: {}, session: {}", user.getId(), sessionId);

        ClientResponseProcessorStrategy processor = clientResponseProcessorFactory.getProcessor(clientType);
        return processor.processorLoginResponse(user, accessToken, refreshToken, response);
    }
}
