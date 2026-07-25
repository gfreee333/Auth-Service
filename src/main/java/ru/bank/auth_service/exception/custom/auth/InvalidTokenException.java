package ru.bank.auth_service.exception.custom.auth;

public class InvalidTokenException extends AuthException{
    public InvalidTokenException(String message) {
        super(message);
    }
}
