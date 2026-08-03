package ru.bank.auth_service.exception.custom.password;

public class InvalidPasswordException extends PasswordException{
    public InvalidPasswordException(String message) {
        super(message);
    }
}
