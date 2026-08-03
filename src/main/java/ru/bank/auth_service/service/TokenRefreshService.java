package ru.bank.auth_service.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.bank.auth_service.exception.custom.auth.InvalidTokenException;
import ru.bank.auth_service.exception.custom.auth.OldTokenUseException;
import ru.bank.auth_service.exception.custom.user.UserNotFoundException;
import ru.bank.auth_service.infrastructure.security.JwtTokenProvider;
import ru.bank.auth_service.infrastructure.security.token.TokenGenerator;
import ru.bank.auth_service.infrastructure.storage.cookies.CookieManager;
import ru.bank.auth_service.infrastructure.storage.redis.RedisTokenStore;
import ru.bank.auth_service.infrastructure.strategy.client.ClientStrategy;
import ru.bank.auth_service.infrastructure.strategy.client.ClientStrategyFactory;
import ru.bank.auth_service.infrastructure.security.token.TokenInvalidation;
import ru.bank.auth_service.infrastructure.security.token.TokenInvalidationResult;
import ru.bank.auth_service.infrastructure.security.token.TokenPair;
import ru.bank.auth_service.model.dto.response.LoginResponseDto;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.model.enums.ClientType;
import ru.bank.auth_service.repository.UsersRepository;

import java.util.UUID;


/**
 * <p><b>Сервис обновление токенов пользователя в системе(RefreshTokenService)</b></p>
 * <p><b>Описание: Обеспечивает получение новой пары refresh + access токенов</b></p>
 * <p><b>Поддержка клиентов:</b></p>
 * <ul>
 *   <li>WEB</li>
 *   <li>MOBILE</li>
 * </ul>
 *
 * @see RedisTokenStore
 * @see UsersRepository
 * @see ClientStrategyFactory
 * @see TokenInvalidation
 * @see TokenGenerator
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenRefreshService {

    private final RedisTokenStore redisTokenStore;
    private final UsersRepository usersRepository;
    private final ClientStrategyFactory clientStrategyFactory;
    private final TokenInvalidation tokenInvalidation;
    private final TokenGenerator tokenGenerator;

    /**
     * <p><b>Метод: refreshToken</b></p>
     * <p><b>Описание: Обновление токенов на основе refresh токена</b></p>
     *
     * <p><b>Основные шаги:</b></p>
     * <ol>
     *   <li>Определение стратегии генерации</li>
     *   <li>Получение пары токенов из (Cookies - Web / Header - Mobile)</li>
     *   <li>Инвалидация access токена</li>
     *   <li>Проверка повторно использования устаревшего Refresh токена</li>
     *   <li>Если проверка {@code false} инвалидация refresh токена</li>
     *   <li>Получение пользователя из системы для будущей генерации JWT</li>
     *   <li>Генерируем новую пару access/refresh токенов</li>
     *   <li>Возвращаем ответ в зависимости от типа клиента</li>
     * </ol>
     *
     * @param clientType тип клиента (Web/Mobile)
     * @param request Http запрос для извлечения токенов
     * @param response для обновления refresh/access токенов
     * @return accessToken (Если тип клиента определен)/(null в противном случае)
     * @throws OldTokenUseException попытка использовать, валидный устаревший токен
     * @throws UserNotFoundException пользователь не найден в системе
     */
    public LoginResponseDto refreshToken(HttpServletRequest request,
                                         HttpServletResponse response,
                                         ClientType clientType) {
        log.info("Попытка получения токенов для клиента: {}", clientType);
        ClientStrategy strategy = clientStrategyFactory.getStrategy(clientType);
        TokenPair oldTokenPair = strategy.extractTokens(request);
        TokenInvalidationResult result = tokenInvalidation.invalidateAccessTokenOnly(oldTokenPair);
        UUID oldUserId = result.userId();
        String oldSessionId = result.sessionId();
        if(!redisTokenStore.existsRefreshToken(oldUserId, oldSessionId)){
            log.warn("Refresh токен устарел");
            strategy.clearClientTokens(response);
            throw new OldTokenUseException("Токен устарел");
        }
        tokenInvalidation.invalidateRefreshTokenOnly(oldUserId, oldSessionId);
        Users user = usersRepository.findById(oldUserId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id: {}" + oldUserId + " не найден"));
        TokenPair newTokenPair = tokenGenerator.generatedTokens(user);
        return strategy.processRefreshResponse(user, newTokenPair.accessToken(),
                        newTokenPair.refreshToken(), response);
    }

}
