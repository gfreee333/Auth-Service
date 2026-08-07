package ru.bank.auth_service.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.bank.auth_service.model.entity.Outbox;
import ru.bank.auth_service.model.enums.OutboxStatus;
import ru.bank.auth_service.repository.OutboxRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxStatusManager {

    private final OutboxRepository outboxRepository;
    private static final long INITIAL_DELAY_SEC = 1;
    private static final int MAX_RETRY = 10;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsSent(Outbox event){
        Outbox managed = outboxRepository.findById(event.getId()).orElseThrow();
        managed.setStatus(OutboxStatus.SENT);
        managed.setLastAttemptAt(LocalDateTime.now());
        managed.setNextAttemptAt(null);
        outboxRepository.save(managed);
        log.debug("Событие: {} помечено как отправленное", managed.getId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementRetry(Outbox event) {
        Outbox managed = outboxRepository.findById(event.getId()).orElseThrow();
        managed.setRetryCount(managed.getRetryCount() + 1);
        managed.setLastAttemptAt(LocalDateTime.now());
        if (managed.getRetryCount() >= MAX_RETRY) {
            managed.setStatus(OutboxStatus.DEAD);
            managed.setNextAttemptAt(null);
            log.error("Событие: {} достигло лимитов retry, статус DEAD", managed.getId());
        } else {
            managed.setStatus(OutboxStatus.PENDING);
            long delaySeconds = Math.min(3600, (long) Math.pow(2, managed.getRetryCount()) * INITIAL_DELAY_SEC);
            managed.setNextAttemptAt(LocalDateTime.now().plusSeconds(delaySeconds));
            log.warn("Событие {}: попытка {}, следующая через {} сек",
                    managed.getId(), managed.getRetryCount(), delaySeconds);
        }
        outboxRepository.save(managed);
    }

}
