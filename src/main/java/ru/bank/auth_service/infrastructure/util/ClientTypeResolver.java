package ru.bank.auth_service.infrastructure.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.exception.custom.auth.ClientTypeNotSupportedException;
import ru.bank.auth_service.model.enums.ClientType;

@Component
@Slf4j
public class ClientTypeResolver {

    /**
     * <p><b>Метод: resolver</b></p>
     * <p><b>Описание: Определения типа используемого клиента</b></p>
     *
     * <p><b>Основная логика:</b></p>
     * <ol>
     *   <li>Получение типа клиента из заголовка</li>
     *   <li>В случае успеха получения типа <br>
     *   из заголовка возвращаем тип клиента {@code Mobile}</li>
     *   <li>В случае если заголовок пустой <br>
     *   пытаемся получить тип клиента из User-Agent</li>
     *   <li>Если удалось получить тип клиента из User-Agent <br>
     *   возвращаем тип клиента {@code Web}</li>
     *   <li>Если тип клиента не удалось определить <br>
     *   предполагаем, что используется {@code Web} по умолчанию</li>
     * </ol>
     *
     * @return {@link ClientType} - enum тип используемого клиента
     * @throws ClientTypeNotSupportedException данный тип клиента не поддерживается
     */
    public ClientType resolve(HttpServletRequest request) {
        String header = request.getHeader("X-Client-Type");
        if (header != null) {
            try {
                ClientType type = ClientType.valueOf(header.toUpperCase());
                log.debug("Тип клиента определен по заголовку: {}", type);
                return type;
            } catch (IllegalArgumentException ex) {
                log.error("Неизвестный тип клиента в заголовке: {}", header);
                throw new ClientTypeNotSupportedException("Неизвестный тип клиента в заголовке: " + header);
            }
        }
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && isBrowserUserAgent(userAgent)) {
            log.debug("Тип клиента определен как WEB через User-Agent");
            return ClientType.WEB;
        }
        log.debug("Тип клиента не определен, по умолчанию используем WEB");
        return ClientType.WEB;
    }

    /**
     * <p><b>Метод: isBrowserUserAgent</b></p>
     * <p><b>Описание: Проверка использования Web клиента</b></p>
     * <p><b>Основная логика:</b></p>
     * <ol>
     *     <li>Проверяем User-Agent на содержание информации о браузере</li>
     *     <li>Возвращаем {@code true} в случае содержания описания в User-Agent <br>
     *     иначе возвращаем {@code false }</li>
     * </ol>
     * <p>Проверяем User-Agent на содержания информации о браузере</p>
     *
     * @return {@code boolean} - {@code true/false} в зависимости наличия соответсвия
     */

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
