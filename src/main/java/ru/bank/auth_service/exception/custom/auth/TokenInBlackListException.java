package ru.bank.auth_service.exception.custom.auth;

public class TokenInBlackListException extends AuthException{
    public TokenInBlackListException(String message) {
        super(message);
    }
}
