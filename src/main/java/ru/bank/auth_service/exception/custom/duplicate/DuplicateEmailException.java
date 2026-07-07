package ru.bank.auth_service.exception.custom.duplicate;

public class DuplicateEmailException extends DuplicateException {
    public DuplicateEmailException(String email) {
        super(String.format("Email %s уже зарегистрирован в системе", email));
    }
}
