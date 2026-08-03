package ru.bank.auth_service.exception.custom.user;

public class UserDeleteForbiddenException extends UserException{
    public UserDeleteForbiddenException(String message) {
        super(message);
    }
}
