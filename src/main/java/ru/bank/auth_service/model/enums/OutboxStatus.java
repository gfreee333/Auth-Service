package ru.bank.auth_service.model.enums;

public enum OutboxStatus {
    PENDING, SENT, PROCESSING, DEAD;

    public boolean isTerminal(){
        return this.equals(SENT) || this.equals(DEAD);
    }

}
