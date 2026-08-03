package ru.bank.auth_service.infrastructure.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.exception.custom.auth.AuthException;
import ru.bank.auth_service.infrastructure.security.CustomUserDetails;

import java.util.UUID;

@Component
@Slf4j
public class UserSecurityContext {


    /**
     * <p><b>Метод: getCurrentUserId</b></p>
     * <p><b>Описание: Получение userId текущего <br>
     * аутентифицированного пользователя</b></p>
     *
     * <p><b>Основная логика:</b></p>
     * <ol>
     *   <li>Получение Authentication из SecurityContextHolder</li>
     *   <li>Проверка наличия аутентификации</li>
     *   <li>Извлечение CustomUserDetails из principal</li>
     *   <li>Возврат userId из CustomUserDetails</li>
     * </ol>
     *
     * @return {@link UUID} userId текущего пользователя
     * @throws AuthException если пользователь не аутентифицирован <br>
     *                       или не удалось извлечь userId
     */
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Попытка получить userId без аутентификации");
            throw new AuthException("Пользователь не аутентифицирован");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }
        log.warn("Не удалось извлечь userId из SecurityContext");
        throw new AuthException("Не удалось определить пользователя");
    }

}