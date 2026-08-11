package ru.bank.auth_service.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.model.entity.Outbox;
import ru.bank.auth_service.model.enums.OutboxStatus;

@Component
@Slf4j
public class OutboxEventProcessor {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OutboxStatusManager statusManager;

    public OutboxEventProcessor(
            @Qualifier("criticalKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            OutboxStatusManager statusManager
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.statusManager = statusManager;
    }

    public void sendProcess(Outbox event) {
        if (event.getStatus().isTerminal()) {
            return;
        }
        if (event.getStatus() != OutboxStatus.PROCESSING) {
            log.warn("Событие: {} уже в процессе обработки", event.getId());
            return;
        }
        try {
            OutboxEvent outboxEvent = objectMapper.readValue(
                    event.getPayload(),
                    OutboxEvent.class
            );
            String topic = outboxEvent.getEventType().getTopic();
            kafkaTemplate.send(topic, String.valueOf(event.getUserId()), event.getPayload());
            statusManager.markAsSent(event);
        } catch (Exception ex) {
            log.warn("Ошибка отправки события: {} message: {}", event.getId(), ex.getMessage());
            statusManager.incrementRetry(event);
        }
    }
}
