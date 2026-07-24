package ru.bank.auth_service.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.exception.custom.auth.AuthException;
import ru.bank.auth_service.model.dto.response.TokenPair;
import ru.bank.auth_service.model.entity.Users;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
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
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        this.privateKey = loadPrivateKey();
        this.publicKey = loadPublicKey();
    }

    // todo: Загружаем закрытый ключ для подписи токена
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

    // todo: Загружаем публичный ключ для проверки валидности токена
    private PublicKey loadPublicKey() throws Exception {
        String keyContext = new String(
                new ClassPathResource(publicKeyPath).getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );
        String publicKeyPem = keyContext
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(publicKeyPem);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    // todo: Создание builder для jwt токенов
    private String buildToken(Users user, Long expiration, String sessionId) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("sessionId", sessionId)
                .claim("role", user.getRole())
                .claim("status", user.getStatus())
                .claim("phoneNumber", user.getPhoneNumber())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    // todo: Генерация пары токенов access + refresh
    public TokenPair generatedTokenPair(Users user){
        String sessionId = UUID.randomUUID().toString();
        String accessToken = buildToken(user, accessTokenExpiration, sessionId);
        String refreshToken = buildToken(user, refreshTokenExpiration, sessionId);
        log.debug("Создана пара токенов с session: {}", sessionId);
        return new TokenPair(accessToken, refreshToken);
    }

    // todo: Проверка валидности токена
    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // todo: Метод для проверки валидности токена
    public boolean isValidToken(String token){
        try {
            validateToken(token);
            return true;
        }catch (Exception ex){
            log.debug("Невалидный токен: {}", ex.getMessage());
            return false;
        }
    }

    // todo: Получение остатка времени жизни токена
    public Long getExpirationFromToken(String token) {
            Date expiration = validateToken(token).getExpiration();
            return expiration.getTime() - System.currentTimeMillis();
    }

    // todo: Извлечение sessionId из токена
    public String getSessionIdFromToken(String token){
        String sessionId = validateToken(token).get("sessionId", String.class);
        if(sessionId == null){
            log.error("Ошибка извлечения sessionId из токена");
            throw new AuthException("sessionId не найден в токене");
        }
        return sessionId;
    }

    // todo: Извлечение userId из токена
    public UUID getUserIdFromToken(String token){
        String userIdStr = validateToken(token).get("userId", String.class);
        if(userIdStr == null){
            log.error("Ошибка извлечения userId из токена");
            throw new AuthException("userId не найден в токене");
        }
        try{
            return UUID.fromString(userIdStr);
        } catch (IllegalArgumentException ex){
            log.error("Ошибка извлечения userId из токена: {}", ex.getMessage());
            throw new AuthException("Неверный формат userId в токене");
        }
    }

    // todo: Извлечение role из токена
    public String getRoleFromToken(String token){
        String role = validateToken(token).get("role", String.class);
        if(role == null){
             log.error("Ошибка извлечения role из токена");
             throw new AuthException("Role не найдена в токене");
        }
        return role;
    }

    // todo: Извлечение email из токена
    public String getEmailFromToken(String token){
        String email = validateToken(token).getSubject();
        if(email == null){
            log.error("Ошибка извлечения Email из токена");
            throw new AuthException("Email не найден в токене");
        }
        return email;
    }

    // todo: Извлечение статуса
    public String getUserStatusFromToken(String token){
        String status = validateToken(token).get("status", String.class);
        if(status == null){
            log.error("Ошибка извлечения status из токена");
            throw new AuthException("Status не найден в токене");
        }
        return status;
    }

}
