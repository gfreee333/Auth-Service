package ru.bank.auth_service.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.bank.auth_service.infrastructure.strategy.client.ClientStrategy;
import ru.bank.auth_service.infrastructure.strategy.client.ClientStrategyFactory;
import ru.bank.auth_service.infrastructure.security.token.TokenInvalidation;
import ru.bank.auth_service.infrastructure.security.token.TokenPair;
import ru.bank.auth_service.model.enums.ClientType;


/**
 * <p><b>Сервис выхода из системы</b></p>
 * <p><b>Описание: Обеспечивает выход пользователя из системе</b></p>
 * <p><b>Поддержка клиентов:</b></p>
 * <ol>
 *   <li>WEB</li>
 *   <li>MOBILE</li>
 * </ol>
 *
 * @see ClientStrategyFactory
 * @see TokenInvalidation
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService {

    private final ClientStrategyFactory clientStrategyFactory;
    private final TokenInvalidation tokenInvalidation;

    /**
     * <p><b>Метод: Logout</b></p>
     * <p><b>Описание: Выход пользователя из системы в рамках одной сессии</b></p>
     * <p><b>Основная логика:</b></p>
     * <ol>
     *   <li>Выбор стратегии выхода из системы (Web/Mobile)</li>
     *    <li>Генерация пары JWT токенов</li>
     *    <li>Инвалидация токенов в системе</li>
     *    <li>Очистка токенов на стороне клиента</li>
     *  </ol>
     * @param request    HTTP запрос для извлечения токенов
     * @param response   HTTP ответ для очистки cookies
     * @param clientType тип клиента (WEB или MOBILE)
     */
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       ClientType clientType) {
        ClientStrategy strategy = clientStrategyFactory.getStrategy(clientType);
        TokenPair tokens = strategy.extractTokens(request);
        tokenInvalidation.invalidateTokens(tokens);
        strategy.clearClientTokens(response);
        log.debug("Выход выполнен для клиента: {}", clientType);
    }

}
