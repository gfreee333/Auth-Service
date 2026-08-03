package ru.bank.auth_service.exception.custom.user;

public class UserException extends RuntimeException{
    public UserException(String message){
        super(message);
    }
}
