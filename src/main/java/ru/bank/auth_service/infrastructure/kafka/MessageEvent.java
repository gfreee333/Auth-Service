package ru.bank.auth_service.infrastructure.kafka;

import lombok.Builder;
import lombok.Data;
import ru.bank.auth_service.model.enums.OutboxEventType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MessageEvent {
    private OutboxEventType eventType;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String encryptedPassword;
    private LocalDateTime timestamp;

    // Решил попробовать использовать фабричный способ решения
    public static MessageEventBuilder baseBuilder(OutboxEventType eventType,
                                                  UUID userId,
                                                  String firstName,
                                                  String lastName,
                                                  String email
    ) {
        return MessageEvent.builder()
                .eventType(eventType)
                .userId(userId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .timestamp(LocalDateTime.now());
    }


    public static MessageEvent passwordEvent(UUID userId,
                                             String firstName,
                                             String lastName,
                                             String email,
                                             String encryptedPassword
    ) {
        return baseBuilder(OutboxEventType.PASSWORD_EVENT,
                userId, firstName, lastName, email)
                .encryptedPassword(encryptedPassword)
                .build();
    }

    public static MessageEvent passwordChangeEvent(UUID userId,
                                                   String firstName,
                                                   String lastName,
                                                   String email
    ) {
        return baseBuilder(OutboxEventType.PASSWORD_CHANGE_EVENT,
                userId, firstName, lastName, email)
                .build();
    }

    public static MessageEvent blockedEvent(UUID userId,
                                            String firstName,
                                            String lastName,
                                            String email
    ) {
        return baseBuilder(OutboxEventType.USER_BLOCKED_EVENT,
                userId, firstName, lastName, email).build();
    }

    public static MessageEvent unblockedEvent(UUID userId,
                                              String firstName,
                                              String lastName,
                                              String email
    ) {
        return baseBuilder(OutboxEventType.USER_UNBLOCKED_EVENT,
                userId, firstName, lastName, email).build();
    }

    public static MessageEvent userInformationUpdateEvent(UUID userId,
                                                          String firstName,
                                                          String lastName,
                                                          String email
    ) {
        return baseBuilder(OutboxEventType.USER_UPDATE_INFORMATION_EVENT,
                userId, firstName, lastName, email).build();
    }

}
