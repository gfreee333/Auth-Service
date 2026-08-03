package ru.bank.auth_service.exception.custom.user;

public class UserChangeRoleException extends UserException{
    public UserChangeRoleException(String message) {
        super(message);
    }
}
