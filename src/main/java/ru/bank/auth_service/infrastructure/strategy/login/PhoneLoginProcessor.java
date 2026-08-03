package ru.bank.auth_service.infrastructure.strategy.login;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.exception.custom.auth.AuthException;
import ru.bank.auth_service.model.dto.request.LoginRequestDto;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.repository.UsersRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class PhoneLoginProcessor implements LoginProcessorStrategy {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * <p><b>Метод: authenticate</b></p>
     * <p><b>Описание: Аутентификация пользователя по Phone + Password</b></p>
     * <p>Основные шаги:</p>
     * <ol>
     *   <li>Поиск пользователя в БД по номеру телефона</li>
     *   <li>Проверка hash значение пароля в системе</li>
     *   <li>Возвращаем данные пользователя при успехе</li>
     * </ol>
     *
     * @param request данные для входа (phone + password)
     * @return {@link Users} аутентифицированный пользователь
     * @throws AuthException если пользователь не найден или password неверный
     */
    @Override
    public Users authenticate(LoginRequestDto request) {
        Users user = usersRepository.findByPhoneNumber(request.getIdentifier())
                .orElseThrow(() -> new AuthException("Неверные учетные данные: phoneNumber"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Неверные учетные данные: password");
        }
        return user;
    }

    /**
     * <p><b>Метод: supports </b></p>
     * <p><b>Описание: Проверяет, является ли идентификатор валидным phoneNumber</b></p>
     *
     * <p><b>Основная логика:</b></p>
     * <ol>
     *   <li>Проверка номера телефона по паттерну</li>
     *   <li>Если номер соблюдает паттерн +7XXX... {@code true}</li>
     *   <li>Если номер не соблюдает паттерн {@code false}</li>
     * </ol>
     *
     * @return {@code true/false} в зависимости от входных данных
     */
    @Override
    public boolean supports(LoginRequestDto request) {
        return request.getIdentifier().matches("^\\+[0-9]\\d{1,14}$");
    }
}
