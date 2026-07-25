package ru.bank.auth_service.exception.custom.auth;

public class UnsupportedIdentifierException extends AuthException{
    public UnsupportedIdentifierException(String message) {
        super(message);
    }
}
