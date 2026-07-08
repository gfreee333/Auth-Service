package ru.bank.auth_service.model.enums;

public enum ClientType {
    WEB, MOBILE;
    public boolean isWeb(){
        return this.equals(WEB);
    }
    public boolean isMobile(){
        return this.equals(MOBILE);
    }

}
