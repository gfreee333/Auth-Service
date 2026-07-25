package ru.bank.auth_service.infrastructure.strategy.refresh;

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
public class MobileRefreshProcessor implements RefreshResponseProcessorStrategy{

    @Override
    public LoginResponseDto processRefreshResponse(Users user, String accessToken, String refreshToken, HttpServletResponse response) {
        // Заглушка повторного сохранения токенов для Mobile клиента
        // Пока лишь возвращаем refresh + access через тело запроса пользователю
        log.debug("Mobile токены обновлены для пользователя: {}", user.getId());
        return new LoginResponseDto(
                "Токены успешно обновлены",
                user.getId(),
                user.getRole().name(),
                user.getStatus().isFirstLogin(),
                accessToken,
                refreshToken,
                ClientType.MOBILE
        );
    }

    @Override
    public void clearClientTokens(HttpServletResponse response) {
        // Заглушка, в будущем может быть добавлена дополнительная поддержка Mobile клиента
        log.debug("Очистка токенов для Mobile клиента");
    }

    @Override
    public ClientType getClientType() {
        return ClientType.MOBILE;
    }
}
