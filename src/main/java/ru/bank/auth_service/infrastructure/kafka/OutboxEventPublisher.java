package ru.bank.auth_service.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.bank.auth_service.model.entity.Outbox;
import ru.bank.auth_service.repository.OutboxRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Long generate(MessageEvent event){
        try {
            String payload = objectMapper.writeValueAsString(event);
            Outbox outbox = Outbox.builder()
                    .userId(event.getUserId())
                    .payload(payload)
                    .createdAt(LocalDateTime.now())
                    .build();
            Outbox save = outboxRepository.save(outbox);
            log.debug("Событие: {} сохранено в Outbox", event.getEventType());
            return save.getId();
        } catch (Exception ex){ // Сделать более оптимальный способ обработки
            log.error("Ошибка сохранения Outbox: {}", ex.getMessage());
            throw new RuntimeException("Ошибка сохранения Outbox");
        }
    }

}
