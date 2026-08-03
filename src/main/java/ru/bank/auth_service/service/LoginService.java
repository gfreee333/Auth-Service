package ru.bank.auth_service.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bank.auth_service.exception.custom.auth.ClientInBlackListException;
import ru.bank.auth_service.infrastructure.security.token.TokenGenerator;
import ru.bank.auth_service.infrastructure.strategy.client.ClientStrategy;
import ru.bank.auth_service.infrastructure.strategy.client.ClientStrategyFactory;
import ru.bank.auth_service.infrastructure.strategy.login.LoginProcessorFactory;
import ru.bank.auth_service.infrastructure.strategy.login.LoginProcessorStrategy;
import ru.bank.auth_service.model.dto.request.LoginRequestDto;
import ru.bank.auth_service.model.dto.response.LoginResponseDto;
import ru.bank.auth_service.infrastructure.security.token.TokenPair;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.model.enums.ClientType;
import ru.bank.auth_service.model.enums.UserStatus;
import ru.bank.auth_service.repository.UsersRepository;
import java.time.LocalDateTime;

/**
 * <p><b>Сервис входа в систему</b></p>
 * <p><b>Описание: Обеспечивает аутентификацию пользователя и генерацию токенов</b></p>
 * <p><b>Поддержка клиентов:</b></p>
 * <ol>
 *   <li>WEB</li>
 *   <li>MOBILE</li>
 * </ol>
 *
 * @see LoginProcessorFactory
 * @see UsersRepository
 * @see ClientStrategyFactory
 * @see TokenGenerator
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final LoginProcessorFactory loginProcessorFactory;
    private final ClientStrategyFactory clientProcessorFactory;
    private final UsersRepository usersRepository;
    private final TokenGenerator tokenGenerator;

    /**
     * <p><b>Метод: Login</b></p>
     * <p><b>Описание: Вход пользователя в систему</b></p>
     * <p><b>Основная логика:</b></p>
     * <ol>
     *   <li>Определение стратегии входа (email/phone)</li>
     *   <li>Аутентификация пользователя (проверка пароля)</li>
     *   <li>Проверка статуса (не заблокирован)</li>
     *   <li>При первом входе: PENDING => ACTIVE</li>
     *   <li>Генерация пары токенов (access + refresh) с единым sessionId,<br></li>
     *    сохранение токенов в Redis</li>
     *   <li>Сохранение токенов (Web => cookies, Mobile => JSON)</li>
     * </ol>
     * @param request данные для входа (email/phone + password)
     * @param response HTTP ответ для установки cookies (для Web)
     * @param clientType тип клиента (WEB или MOBILE)
     * @return {@link LoginResponseDto} DTO с токенами и информацией о пользователе
     * @throws ClientInBlackListException если пользователь заблокирован
     */
    @Transactional
    public LoginResponseDto login(LoginRequestDto request,
                                  HttpServletResponse response,
                                  ClientType clientType) {
        LoginProcessorStrategy loginProcessor = loginProcessorFactory.getStrategy(request);
        Users user = loginProcessor.authenticate(request);
        if (user.getStatus().isBlocked()) {
            log.warn("Пользователь: {} заблокирован в системе", user.getId());
            throw new ClientInBlackListException("Пользователь со статусом: " + user.getStatus());
        }
        if(user.getStatus().isFirstLogin()){
            user.setStatus(UserStatus.ACTIVE);
            user.setActivatedAt(LocalDateTime.now());
            usersRepository.save(user);
            log.debug("Пользователь: {} активирован при первом заходе в систему", user.getId());
        }
        TokenPair tokenPair = tokenGenerator.generatedTokens(user);
        ClientStrategy strategy = clientProcessorFactory.getStrategy(clientType);
        return strategy.processLoginResponse(user, tokenPair.accessToken(),
                tokenPair.refreshToken(), response);
    }
}
