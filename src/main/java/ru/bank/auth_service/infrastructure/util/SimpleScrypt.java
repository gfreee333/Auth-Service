package ru.bank.auth_service.infrastructure.util;

import lombok.extern.slf4j.Slf4j;
import ru.bank.auth_service.exception.custom.password.PasswordEncryptedKafkaException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class SimpleScrypt {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final byte[] SECRET_KEY = "1234567890123456".getBytes(StandardCharsets.UTF_8);

    private SimpleScrypt(){}

    public static String encrypt(String password){
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex){
            log.warn("Не удалось применить шифрование пароля для Kafka");
            throw new PasswordEncryptedKafkaException("Ошибка в формирования зашифрованного" +
                    " пароля, для ивента в Kafka");
        }
    }
}
