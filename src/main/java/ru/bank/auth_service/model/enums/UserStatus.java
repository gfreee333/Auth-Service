package ru.bank.auth_service.model.enums;

public enum UserStatus {
    PENDING, BLOCKED, ACTIVE;

    public boolean isFirstLogin() {
        return this.equals(PENDING);
    }

    public boolean isBlocked() {
        return this.equals(BLOCKED);
    }
}
