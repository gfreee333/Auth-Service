package ru.bank.auth_service.exception.custom.duplicate;

public class DuplicateException extends RuntimeException{
    public DuplicateException(String message){
        super(message);
    }
}
