package ru.bank.auth_service.model.enums;

public enum UserStatus {
    PENDING, BLOCKED, DELETE, ACTIVE;
    public boolean isFirstLogin(){
        return this.equals(PENDING);
    }
    public boolean isBlocked(){
        return this.equals(BLOCKED);
    }
    public boolean isDeleted(){
        return this.equals(DELETE);
    }
    public boolean isActive(){
        return this.equals(ACTIVE);
    }
}
