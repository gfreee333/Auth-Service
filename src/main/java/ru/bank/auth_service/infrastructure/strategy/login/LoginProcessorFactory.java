package ru.bank.auth_service.infrastructure.strategy.login;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.exception.custom.auth.AuthException;
import ru.bank.auth_service.model.dto.request.LoginRequestDto;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginProcessorFactory {

    private final List<LoginProcessorStrategy> strategies;

    public LoginProcessorStrategy getStrategy(LoginRequestDto request){
        return strategies.stream()
                .filter(s -> s.supports(request))
                .findFirst()
                .orElseThrow(() -> new AuthException("Данный способ аутентификации не поддерживается"));
    }
}
