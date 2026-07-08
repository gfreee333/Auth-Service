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
public class ClientResponseProcessorFactory {

    private final List<ClientResponseProcessorStrategy> processors;
    private final Map<ClientType, ClientResponseProcessorStrategy> processorCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init(){
        for (ClientResponseProcessorStrategy processor : processors){
            processorCache.put(processor.getClientType(), processor);
            log.info("Зарегистрирован процессор для клиента: {}", processor.getClientType());
        }
    }

    public ClientResponseProcessorStrategy getProcessor(ClientType clientType){
        if(clientType == null){
            log.warn("Тип клиента не указан (null), использовать Web по умолчанию");
            return processorCache.get(ClientType.WEB);
        }
        ClientResponseProcessorStrategy processor = processorCache.get(clientType);
        if(processor == null){
            log.warn("Не найден процессор для типа клиента: {}, использовать Web по умолчанию", clientType);
            return processorCache.get(ClientType.WEB);
        }
        return processor;
    }

}
