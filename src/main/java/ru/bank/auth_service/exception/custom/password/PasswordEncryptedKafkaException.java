package ru.bank.auth_service.exception.custom.password;

public class PasswordEncryptedKafkaException extends RuntimeException {
    public PasswordEncryptedKafkaException(String message){
        super(message);
    }
}
