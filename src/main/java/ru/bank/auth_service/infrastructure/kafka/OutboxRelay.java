package ru.bank.auth_service.infrastructure.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.model.entity.Outbox;
import ru.bank.auth_service.repository.OutboxRepository;
import java.util.List;

@Component
@Slf4j
public class OutboxRelay {


    private final TaskExecutor outboxTaskExecutor;
    private final OutboxRepository outboxRepository;
    private final OutboxMessageSender outboxMessageSender;

    public OutboxRelay(
            @Qualifier("outboxExecutor") TaskExecutor outboxTaskExecutor,
            OutboxRepository outboxRepository,
            OutboxMessageSender outboxMessageSender
    ){
        this.outboxTaskExecutor = outboxTaskExecutor;
        this.outboxRepository = outboxRepository;
        this.outboxMessageSender = outboxMessageSender;
    }

    private static final int BATCH_SIZE = 100;

    @Scheduled(fixedDelay = 5000) // каждые 5 сек
    public void relayOutbox(){
        List<Outbox> events = outboxRepository.findAndLockPendingEvents(BATCH_SIZE);
        if(events.isEmpty()) return;
        for(Outbox event : events){
            outboxTaskExecutor.execute(() -> outboxMessageSender.sendProcess(event));
        }
    }

}
