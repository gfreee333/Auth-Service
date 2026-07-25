package ru.bank.auth_service.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bank.auth_service.exception.custom.auth.ClientInBlackListException;
import ru.bank.auth_service.infrastructure.security.JwtTokenProvider;
import ru.bank.auth_service.infrastructure.storage.redis.RedisTokenStore;
import ru.bank.auth_service.infrastructure.strategy.client.ClientResponseProcessorFactory;
import ru.bank.auth_service.infrastructure.strategy.client.ClientResponseProcessorStrategy;
import ru.bank.auth_service.infrastructure.strategy.login.LoginProcessorFactory;
import ru.bank.auth_service.infrastructure.strategy.login.LoginProcessorStrategy;
import ru.bank.auth_service.model.dto.request.LoginRequestDto;
import ru.bank.auth_service.model.dto.response.LoginResponseDto;
import ru.bank.auth_service.model.dto.response.TokenPair;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.model.enums.ClientType;
import ru.bank.auth_service.model.enums.UserStatus;
import ru.bank.auth_service.repository.UsersRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final LoginProcessorFactory loginProcessorFactory;
    private final ClientResponseProcessorFactory clientResponseProcessorFactory;
    private final UsersRepository usersRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenStore redisTokenStore;

    // todo: login - вход пользователя в систему
    @Transactional
    public LoginResponseDto login(LoginRequestDto request, HttpServletResponse response, ClientType clientType) {
        log.info("Попытка входа в систему для клиента: {}, с идентификатором: {}", clientType, request.getIdentifier());
        LoginProcessorStrategy loginProcessor = loginProcessorFactory.getStrategy(request);
        Users user = loginProcessor.authenticate(request);
        if (user.getStatus().isBlocked()) {
            throw new ClientInBlackListException("Пользователь со статусом: " + user.getStatus());
        }
        if(user.getStatus().isFirstLogin()){
            user.setStatus(UserStatus.ACTIVE);
            user.setActivatedAt(LocalDateTime.now());
            usersRepository.save(user);
            log.info("Пользователь: {} активирован при первом заходе в систему", user.getId());
        }

        TokenPair tokenPair = jwtTokenProvider.generatedTokenPair(user);
        String sessionId = jwtTokenProvider.getSessionIdFromToken(tokenPair.refreshToken());
        Long refreshTtl = jwtTokenProvider.getExpirationFromToken(tokenPair.refreshToken());
        redisTokenStore.saveRefreshToken(user.getId(), sessionId, tokenPair.refreshToken(), refreshTtl);
        log.debug("Создана сессия для пользователя: {}, session: {}", user.getId(), sessionId);
        ClientResponseProcessorStrategy processor = clientResponseProcessorFactory.getProcessor(clientType);
        return processor.processorLoginResponse(user, tokenPair.accessToken(), tokenPair.refreshToken(), response);
    }
}
