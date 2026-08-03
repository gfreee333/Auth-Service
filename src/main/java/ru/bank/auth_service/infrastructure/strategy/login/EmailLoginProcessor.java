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
public class EmailLoginProcessor implements LoginProcessorStrategy {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * <p><b>Метод: authenticate</b></p>
     * <p><b>Описание: Аутентификация пользователя по Email + Password</b></p>
     * <p><b>Основная логика:</b></p>
     * <ol>
     *   <li>Проверка наличия пользователя в системе</li>
     *   <li>Проверка hash значения пароля в системе</li>
     *   <li>Возвращаем данные пользователя при успехе</li>
     * </ol>
     *
     * @param request данные для входа (email + password)
     * @return {@link Users} Аутентифицированный пользователь
     * @throws AuthException В случае если пользователь не найден или password неверный
     */
    @Override
    public Users authenticate(LoginRequestDto request) {
        Users user = usersRepository.findByEmail(request.getIdentifier())
                .orElseThrow(() -> new AuthException("Неверные учетные данные: email"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Неверные учетные данные: password");
        }
        return user;
    }

    /**
     * <p><b>Метод: supports </b></p>
     * <p><b>Описание: Проверяет, является ли идентификатор валидным email-адресом</b></p>
     *
     * <p><b>Основная логика:</b></p>
     * <ol>
     *   <li>Подсчет количества символов '@' в строке</li>
     *   <li>Если '@' не ровно один → возвращаем {@code false}</li>
     *   <li>Проверка позиции '@' (не должен быть первым символом)</li>
     *   <li>Проверка наличия домена (должна быть точка после '@')</li>
     *   <li>Проверка доменной зоны (после точки должны быть символы)</li>
     * </ol>
     *
     * @return {@code true/false} в зависимости от входных данных
     */
    @Override
    public boolean supports(LoginRequestDto request) {
        int atIndex = request.getIdentifier().indexOf("@");
        int dotIndex = request.getIdentifier().lastIndexOf(".");
        long atCount = request.getIdentifier().chars().filter(ch -> ch == '@').count();
        if (atCount != 1) {
            return false;
        }
        return atIndex > 0 &&
                dotIndex > atIndex + 1 &&
                dotIndex < request.getIdentifier().length() - 1;
    }
}
