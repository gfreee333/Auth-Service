package ru.bank.auth_service.exception.custom.duplicate;

public class DuplicatePhoneException extends DuplicateException {
    public DuplicatePhoneException(String phone) {
        super(String.format("Phone %s уже зарегистрирован в системе", phone));
    }
}
