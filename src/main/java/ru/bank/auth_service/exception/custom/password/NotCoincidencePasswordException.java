package ru.bank.auth_service.exception.custom.password;

public class NotCoincidencePasswordException extends PasswordException{
    public NotCoincidencePasswordException(String message) {
        super(message);
    }
}
