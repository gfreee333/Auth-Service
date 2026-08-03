package ru.bank.auth_service.infrastructure.strategy.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.model.enums.ClientType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientStrategyFactory {

    private final List<ClientStrategy> strategies;
    private final Map<ClientType, ClientStrategy> strategyCache = new ConcurrentHashMap<>();

    /**
     * <p><b>Метод: init</b></p>
     * <p><b>Описание: Инициализация фабрики и регистрация стратегий</b></p>
     *
     * <p><b>Основная логика:</b></p>
     * <ol>
     *   <li>Получение всех бинов, реализующих {@link ClientStrategy}</li>
     *   <li>Для каждой стратегии извлекаем тип клиента через
     *       {@link ClientStrategy#getClientType()}</li>
     *   <li>Сохранение стратегии в {@link #strategyCache} для быстрого доступа</li>
     *   <li>Логирование успешной регистрации</li>
     * </ol>
     *
     * <p><b>Время выполнения:</b> При старте приложения (PostConstruct)</p>
     */
    @PostConstruct
    public void init(){
        for(ClientStrategy strategy : strategies){
            strategyCache.put(strategy.getClientType(), strategy);
            log.debug("Стратегия зарегистрирована для клиента: {}", strategy.getClientType());
        }
    }

    /**
     * <p><b>Метод: getStrategy</b></p>
     * <p><b>Описание: Получение стратегии для указанного типа клиента</b></p>
     *
     * <p><b>Основная логика:</b></p>
     * <ol>
     *   <li>Проверка переданного типа клиента</li>
     *   <li>Если тип клиента не указан (null) — возвращается стратегия для
     *       {@link ClientType#WEB} по умолчанию</li>
     *   <li>Поиск стратегии в кэше по типу клиента</li>
     *   <li>Если стратегия не найдена — возвращается стратегия для
     *       {@link ClientType#WEB} по умолчанию с предупреждением в лог</li>
     *   <li>Возврат найденной стратегии</li>
     * </ol>
     * @param clientType тип клиента (WEB или MOBILE), может быть null
     * @return {@link ClientStrategy} стратегия для работы с клиентом
     */
    public ClientStrategy getStrategy(ClientType clientType){
        if(clientType == null){
            log.warn("Тип клиента не указан, используем Web");
            return strategyCache.get(ClientType.WEB);
        }
        ClientStrategy strategy = strategyCache.get(clientType);
        if(strategy == null){
            log.warn("Не найдена стратегия для клиента: {}, используем Web", clientType);
            return strategyCache.get(ClientType.WEB);
        }
        return strategy;
    }

}
