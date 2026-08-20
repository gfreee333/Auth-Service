package ru.bank.auth_service.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bank.outbox_library.processor.TopicResolver;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthTopicResolver implements TopicResolver {

    private final ObjectMapper objectMapper;

    @Override
    public String resolver(String payload) {
        try {
            OutboxEvent event = objectMapper.readValue(payload, OutboxEvent.class);
            return event.getEventType().getTopic();
        } catch (Exception ex){
            log.warn("Не удалось определить топик, ошибка: {}", ex.getMessage());
            throw new RuntimeException("Не удалось определить Topic для записи в Kafka", ex);
        }
    }
}
