package ru.bank.auth_service.infrastructure.util;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

@Slf4j
public class PasswordGenerated {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
    private static final int LENGTH = 12;

    /**
     * <p><b>Метод: generatedPassword</b></p>
     * <p><b>Описание: Генерация пароля из 12 символов, на основе шаблона<b/></p>
     *
     * @return сгенерированный пароль состоящий из 12 символов
     */
    public static String generatedPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
