package ru.bank.auth_service.infrastructure.strategy.client;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.model.dto.response.LoginResponseDto;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.model.enums.ClientType;

@Component
@RequiredArgsConstructor
@Slf4j
public class MobileClientResponseProcessor implements ClientResponseProcessorStrategy {

    @Override
    public LoginResponseDto processorLoginResponse(Users user, String accessToken, String refreshToken, HttpServletResponse response) {
        log.info("Mobile пользователь {} успешно вошел в систему", user.getId());
        return new LoginResponseDto(
                "Успешный вход в систему",
                user.getId(),
                user.getRole().name(),
                user.getStatus().isFirstLogin(),
                accessToken,
                refreshToken,
                ClientType.MOBILE
        );
    }

    @Override
    public ClientType getClientType() {
        return ClientType.MOBILE;
    }
}
