package ru.bank.auth_service.exception.custom.user;

public class UserBlockedException extends RuntimeException{
    public UserBlockedException(String message){
        super(message);
    }
}
