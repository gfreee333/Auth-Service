package ru.bank.auth_service.exception.custom.user;

public class InvalidUserFirstNameException extends UserException{
    public InvalidUserFirstNameException(String message) {
        super(message);
    }
}
