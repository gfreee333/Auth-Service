package ru.bank.auth_service.infrastructure.strategy.refresh;

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
public class RefreshResponseProcessorFactory {

    private final List<RefreshResponseProcessorStrategy> processors;
    private final Map<ClientType, RefreshResponseProcessorStrategy> processorCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init(){
        for (RefreshResponseProcessorStrategy processor : processors){
            processorCache.put(processor.getClientType(), processor);
            log.info("Успешная регистрация refresh процессора для клиента: {}", processor.getClientType());
        }
    }

    public RefreshResponseProcessorStrategy getProcessor(ClientType clientType){
        if(clientType == null){
            log.warn("Тип клиента не указан, используем WEB");
            return processorCache.get(ClientType.WEB);
        }
        RefreshResponseProcessorStrategy processor = processorCache.get(clientType);
        if(processor == null){
            log.warn("Не найден refresh процессор для клиента: {}, используем web", clientType);
            return processorCache.get(ClientType.WEB);
        }
        return processor;
    }

}
