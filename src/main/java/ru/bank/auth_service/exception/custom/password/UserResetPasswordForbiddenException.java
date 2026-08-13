package ru.bank.auth_service.exception.custom.password;

public class UserResetPasswordForbiddenException extends PasswordException{
    public UserResetPasswordForbiddenException(String message) {
        super(message);
    }
}
