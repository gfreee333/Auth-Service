package ru.bank.auth_service.exception.custom.auth;

public class AuthException extends RuntimeException{
    public AuthException(String message){
        super(message);
    }
}
