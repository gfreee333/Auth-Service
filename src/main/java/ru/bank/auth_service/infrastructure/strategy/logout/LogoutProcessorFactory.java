package ru.bank.auth_service.infrastructure.strategy.logout;

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
public class LogoutProcessorFactory {

    private final List<LogoutProcessorStrategy> processors;
    private final Map<ClientType, LogoutProcessorStrategy> processorCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init(){
        for (LogoutProcessorStrategy processor : processors){
            processorCache.put(processor.getClientType(), processor);
            log.info("Зарегистрирован logout процессор для клиента: {}", processor.getClientType());
        }
    }

    public LogoutProcessorStrategy getProcessor(ClientType clientType){
        if(clientType == null){
            log.warn("Тип клиента не указан, используем Web");
            return processorCache.get(ClientType.WEB);
        }
        LogoutProcessorStrategy processor = processorCache.get(clientType);
        if(processor == null){
            log.warn("Не найден logout процессор для клиента: {}, используем Web", clientType);
            return processorCache.get(ClientType.WEB);
        }
        return processor;
    }

}
