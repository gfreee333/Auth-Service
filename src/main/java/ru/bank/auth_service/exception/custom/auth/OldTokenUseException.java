package ru.bank.auth_service.exception.custom.auth;

public class OldTokenUseException extends AuthException {
    public OldTokenUseException(String message){
        super(message);
    }
}
