package ru.bank.auth_service.infrastructure.kafka;

import lombok.Builder;
import lombok.Data;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.model.enums.OutboxEventType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class OutboxEvent {

    private OutboxEventType eventType;
    private UUID userId;
    private UUID eventId;
    private String firstName;
    private String lastName;
    private String email;
    private String encryptedPassword;
    private LocalDateTime timestamp;

    public static OutboxEventBuilder baseBuilder(OutboxEventType eventType, Users user) {
        return OutboxEvent.builder()
                .eventType(eventType)
                .userId(user.getId())
                .eventId(UUID.randomUUID())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .timestamp(LocalDateTime.now());
    }


    public static OutboxEvent passwordEvent(Users user, String encryptedPassword) {
        return baseBuilder(OutboxEventType.PASSWORD_EVENT, user)
                .encryptedPassword(encryptedPassword)
                .build();
    }

    public static OutboxEvent passwordChangeEvent(Users user) {
        return baseBuilder(OutboxEventType.PASSWORD_CHANGE_EVENT, user).build();
    }

    public static OutboxEvent blockedEvent(Users user) {
        return baseBuilder(OutboxEventType.USER_BLOCKED_EVENT, user).build();
    }

    public static OutboxEvent unblockedEvent(Users user) {
        return baseBuilder(OutboxEventType.USER_UNBLOCKED_EVENT, user).build();
    }

    public static OutboxEvent deleteUserEvent(Users user) {
        return baseBuilder(OutboxEventType.USER_DELETE_EVENT, user).build();
    }

}
