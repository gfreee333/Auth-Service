package ru.bank.auth_service.exception.custom.password;

public class PasswordException extends RuntimeException{
    public PasswordException(String message){
        super(message);
    }
}
