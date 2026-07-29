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
        log.error("Не удалось извлечь userId из SecurityContext");
        throw new AuthException("Не удалось определить пользователя");
    }


}