package ru.bank.auth_service.exception.custom.user;

public class UserBlockedForbiddenException extends UserException{
    public UserBlockedForbiddenException(String message) {
        super(message);
    }
}
