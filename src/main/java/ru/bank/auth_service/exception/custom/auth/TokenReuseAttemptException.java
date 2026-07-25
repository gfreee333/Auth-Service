package ru.bank.auth_service.exception.custom.auth;

public class TokenReuseAttemptException extends AuthException {
    public TokenReuseAttemptException(String message){
        super(message);
    }
}
