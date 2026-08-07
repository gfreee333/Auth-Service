package ru.bank.auth_service.model.enums;

public enum OutboxStatus {
    PENDING, SENT, DEAD;

    public boolean isTerminal(){
        return this.equals(SENT) || this.equals(DEAD);
    }

}
