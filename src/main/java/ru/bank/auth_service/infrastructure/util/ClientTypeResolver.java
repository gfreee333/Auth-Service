package ru.bank.auth_service.infrastructure.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.exception.custom.auth.ClientTypeNotSupportedException;
import ru.bank.auth_service.model.enums.ClientType;

@Component
@Slf4j
public class ClientTypeResolver {

    public ClientType resolve(HttpServletRequest request) {
        // Получения типа из заголовка
        String header = request.getHeader("X-Client-Type");
        if (header != null) {
            try {
                ClientType type = ClientType.valueOf(header.toUpperCase());
                log.debug("Тип клиента определен по заголовку: {}", type);
                return type;
            } catch (IllegalArgumentException ex) {
                log.warn("Неизвестный тип клиента в заголовке: {}", header);
                throw new ClientTypeNotSupportedException("Неизвестный тип клиента в заголовке: " + header);
            }
        }
        // Получение типа из User-Agent ---> (Браузерная версия по умолчанию)
        String userAgent = request.getHeader("User-Agent");
        if(userAgent != null && isBrowserUserAgent(userAgent)){
            log.debug("Тип клиента определен как WEB через User-Agent");
            return ClientType.WEB;
        }
        log.debug("Тип клиента не определен, по умолчанию используем WEB");
        return ClientType.WEB;
    }

    private boolean isBrowserUserAgent(String userAgent) {
        String ua = userAgent.toLowerCase();
        return ua.contains("mozilla/")
                || ua.contains("chrome/")
                || ua.contains("firefox/")
                || ua.contains("edge/")
                || ua.contains("opera/")
                || ua.contains("YaBrowser/")
                || ua.contains("safari/");
    }

}
