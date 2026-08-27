package ru.bank.auth_service.model.enums;

public enum ClientType {
    WEB, MOBILE, INTERNAL;
    public boolean isWeb(){
        return this.equals(WEB);
    }
}
