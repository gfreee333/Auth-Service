package ru.bank.auth_service.model.enums;

import lombok.Getter;

@Getter
public enum OutboxEventType {
    PASSWORD_EVENT("auth-password-topic"),
    PASSWORD_CHANGE_EVENT("auth-information-topic"),
    USER_BLOCKED_EVENT("auth-blocked-topic"),
    USER_UNBLOCKED_EVENT("auth-unblocked-topic"),
    USER_DELETE_EVENT("auth-delete-user-topic");

    private final String topic;
    OutboxEventType(String topic){
        this.topic = topic;
    }

}
