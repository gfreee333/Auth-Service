package ru.bank.auth_service.model.enums;

public enum OutboxEventType {
    PASSWORD_EVENT("auth-password-topic"),
    PASSWORD_CHANGE_EVENT("auth-information-topic"),
    USER_BLOCKED_EVENT("auth-blocked-events-topic"),
    USER_UNBLOCKED_EVENT("auth-unblocked-topic"),
    USER_UPDATE_INFORMATION_EVENT("auth-update-information-topic");

    private final String topic;
    OutboxEventType(String topic){
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }
}
