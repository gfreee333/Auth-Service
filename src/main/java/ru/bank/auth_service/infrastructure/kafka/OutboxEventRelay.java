package ru.bank.auth_service.infrastructure.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.model.entity.Outbox;
import ru.bank.auth_service.model.enums.OutboxStatus;
import ru.bank.auth_service.repository.OutboxRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OutboxEventRelay {


    private final TaskExecutor outboxTaskExecutor;
    private final OutboxRepository outboxRepository;
    private final OutboxEventProcessor outboxEventProcessor;

    public OutboxEventRelay(
            @Qualifier("outboxExecutor") TaskExecutor outboxTaskExecutor,
            OutboxRepository outboxRepository,
            OutboxEventProcessor outboxEventProcessor
    ) {
        this.outboxTaskExecutor = outboxTaskExecutor;
        this.outboxRepository = outboxRepository;
        this.outboxEventProcessor = outboxEventProcessor;
    }

    private static final int BATCH_SIZE = 100;

    @Scheduled(fixedDelay = 5000) // каждые 5 сек
    public void relayOutbox() {
        List<Outbox> events = outboxRepository.findAndLockPendingEvents(BATCH_SIZE);
        if (events.isEmpty()) return;
        for (Outbox event : events) {
            event.setStatus(OutboxStatus.PROCESSING);
            event.setLastAttemptAt(LocalDateTime.now());
        }
        List<Outbox> savedEvents = outboxRepository.saveAll(events);
        for (Outbox event : savedEvents) {
            outboxTaskExecutor.execute(() -> outboxEventProcessor.sendProcess(event));
        }
    }

}
