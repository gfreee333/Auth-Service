package ru.bank.auth_service.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bank.auth_service.model.enums.OutboxStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Outbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID userId;
    private String payload;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.PENDING;
    private LocalDateTime createdAt;
    private int retryCount = 0;
    private LocalDateTime lastAttemptAt = null;
    private LocalDateTime nextAttemptAt = null;
    @Version
    private Long version;
}
