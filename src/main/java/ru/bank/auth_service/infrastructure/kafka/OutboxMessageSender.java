package ru.bank.auth_service.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.model.entity.Outbox;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OutboxMessageSender {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OutboxStatusManager statusManager;

    public OutboxMessageSender(@Qualifier("criticalKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper,
                               OutboxStatusManager statusManager
    ){
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.statusManager = statusManager;
    }

    private static final int TIMEOUT_SECONDS = 5;

    public void sendProcess(Outbox event) {

        if(event.getStatus().isTerminal()){
            return;
        }
        if (event.getNextAttemptAt() != null && event.getNextAttemptAt().isAfter(LocalDateTime.now())) {
            return;
        }
        try {
            MessageEvent messageEvent = objectMapper.readValue(
                    event.getPayload(),
                    MessageEvent.class
            );
            String topic = messageEvent.getEventType().getTopic();
            kafkaTemplate.send(topic, String.valueOf(event.getId()), event.getPayload())
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            statusManager.markAsSent(event);
        } catch (Exception ex) {
            log.warn("Ошибка отправки события: {} message: {}", event.getId(), ex.getMessage());
            statusManager.incrementRetry(event);
        }

    }

}
