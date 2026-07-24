package ru.bank.auth_service.exception.custom.auth;

public class ClientTypeNotSupportedException extends AuthException {

    public ClientTypeNotSupportedException(String message) {
        super(message);
    }

}
