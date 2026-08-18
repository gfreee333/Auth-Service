package ru.bank.auth_service.infrastructure.security;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.infrastructure.security.token.TokenPair;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.model.enums.Role;
import ru.bank.auth_service.model.enums.UserStatus;
import ru.bank.jwt.JwtValidationFactory;
import ru.bank.jwt.JwtValidator;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.expiration}")
    private Long accessTokenExpiration;
    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpiration;
    @Value("${jwt.private-key-path}")
    private String privateKeyPath;
    @Value("${jwt.public-key-path}")
    private String publicKeyPath;
    private PrivateKey privateKey;
    private JwtValidator jwtValidator;

    @PostConstruct
    public void init() throws Exception {
        this.privateKey = loadPrivateKey();
        this.jwtValidator = JwtValidationFactory.fromClasspath(publicKeyPath);
    }

    /** Загружаем закрытый ключ для подписи токена
     * */
    public PrivateKey loadPrivateKey() throws Exception {
        String keyContent = new String(
                new ClassPathResource(privateKeyPath).getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );
        String privateKeyPem = keyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(privateKeyPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory;
        keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /** Создание builder для jwt токенов
     * */
    private String buildToken(Users user, Long expiration, String sessionId) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("sessionId", sessionId)
                .claim("role", user.getRole())
                .claim("status", user.getStatus())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /** Генерация пары токенов access + refresh
     * */
    public TokenPair generatedTokenPair(Users user) {
        String sessionId = UUID.randomUUID().toString();
        String accessToken = buildToken(user, accessTokenExpiration, sessionId);
        String refreshToken = buildToken(user, refreshTokenExpiration, sessionId);
        log.debug("Создана пара токенов с session: {}", sessionId);
        return new TokenPair(accessToken, refreshToken);
    }

    /** Метод для проверки является ли токен невалидным
     * */
    public boolean isInvalidToken(String token) {
        return jwtValidator.isInvalidToken(token);
    }

    /** Получение остатка времени жизни токена
     * */
    public Long getExpirationFromToken(String token) {
        return jwtValidator.getExpirationFromToken(token);
    }

    /** Извлечение sessionId из токена
     * */
    public String getSessionIdFromToken(String token) {
        return jwtValidator.getSessionIdFromToken(token);
    }

    /** Извлечение email из токена
     * */
    public UUID getUserIdFromToken(String token) {
        return jwtValidator.getUserId(token);
    }

    /** Извлечение email из токена
     * */
    public String getEmailFromToken(String token) {
       return jwtValidator.getEmailFromToken(token);
    }

    /** Извлечение статуса
     * */
    public UserStatus getUserStatusFromToken(String token) {
        return UserStatus.valueOf(jwtValidator.getUserStatusFromToken(token));
    }

    /** Извлечение role из токена
     * */
    public Role getRoleFromToken(String token) {
        return Role.valueOf(jwtValidator.getRoleFromToken(token));
    }

}
