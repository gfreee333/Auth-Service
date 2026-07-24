package ru.bank.auth_service.exception.custom.auth;

public class ClientInBlackListException extends AuthException{
    public ClientInBlackListException(String message) {
        super(message);
    }
}
