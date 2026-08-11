package ru.bank.auth_service.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.model.entity.Outbox;
import ru.bank.auth_service.repository.OutboxRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventStore {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void generate(OutboxEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            Outbox outbox = Outbox.builder()
                    .userId(event.getUserId())
                    .payload(payload)
                    .createdAt(LocalDateTime.now())
                    .build();
            outboxRepository.save(outbox);
            log.debug("Событие: {} сохранено в Outbox", event.getEventType());
        } catch (Exception ex) {
            log.error("Ошибка сохранения Outbox: {}", ex.getMessage());
            throw new RuntimeException("Ошибка сохранения Outbox");
        }
    }

}
