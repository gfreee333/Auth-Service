package ru.bank.auth_service.infrastructure.strategy.login;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.exception.custom.auth.UnsupportedIdentifierException;
import ru.bank.auth_service.model.dto.request.LoginRequestDto;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginProcessorFactory {

    private final List<LoginProcessorStrategy> strategies;

    /**
     * <p><b>Метод: getStrategy </b></p>
     * <p><b>Описание: Выбор подходящий стратегии аутентификации на основе входных данных</b></p>
     *
     * <p><b>Основная логика:</b></p>
     * <ol>
     *   <li>Перебор всех зарегистрированных стратегий</li>
     *   <li>При помощи фильтра проверяем, какая стратегия подходит</li>
     *   <li>После нахождения первой подходящий стратегии прекращаем поиск</li>
     *   <li>В случае если стратегии не подошли exception</li>
     * </ol>
     *
     * @param request данные для входа в систему
     * @return {@link LoginProcessorFactory} возвращаем подходящую стратегию аутентификации
     * @throws UnsupportedIdentifierException - Неверный формат входного идентификатора
     */
    public LoginProcessorStrategy getStrategy(LoginRequestDto request) {
        return strategies.stream()
                .filter(s -> s.supports(request))
                .findFirst()
                .orElseThrow(() -> new UnsupportedIdentifierException("Неверный формат идентификатора. " +
                        "Используйте email: try@email.com / phone: +7XXXXXXXXXX"));
    }
}
