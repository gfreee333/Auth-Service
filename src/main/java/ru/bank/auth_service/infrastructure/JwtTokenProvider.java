package ru.bank.auth_service.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.model.enumerate.Role;
import ru.bank.auth_service.model.enumerate.UserStatus;

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
public class JwtTokenProvider {
    @Value("${jwt.access-expiration}")
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
    public void init() throws Exception{
        this.privateKey = loadPrivateKey();
        this.publicKey = loadPublicKey();
    }
    // todo: Загружаем закрытый ключ для подписи токена
    public PrivateKey loadPrivateKey() throws Exception{
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
    private String buildToken(String email, UUID userId, Role role, UserStatus status, String phoneNumber, Long expiration){
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .claim("status", status)
                .claim("phoneNumber", phoneNumber)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }
    // todo: Генерация долгоживущего токена
    public String generatedRefreshToken(String email, UUID userId, Role role, UserStatus status, String phoneNumber){
        return buildToken(email, userId, role, status, phoneNumber, accessTokenExpiration);
    }
    // todo: Генерация временного токена
    public String generatedAccessToken(String email, UUID userId, Role role, UserStatus status, String phoneNumber){
        return buildToken(email, userId, role, status, phoneNumber, refreshTokenExpiration);
    }
    // todo: Проверка валидности токена
    public Claims validateToken(String token){
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    // todo: Получение остатка времени жизни токена
    public Long getExpirationFromToken(String token){
        Date expiration = validateToken(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}
