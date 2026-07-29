package ru.bank.auth_service.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.bank.auth_service.exception.custom.auth.ClientInBlackListException;
import ru.bank.auth_service.exception.custom.auth.InvalidTokenException;
import ru.bank.auth_service.exception.custom.auth.TokenInBlackListException;
import ru.bank.auth_service.infrastructure.storage.cookies.CookieManager;
import ru.bank.auth_service.infrastructure.storage.redis.RedisTokenStore;
import ru.bank.auth_service.infrastructure.util.ClientTypeResolver;
import ru.bank.auth_service.model.enums.ClientType;
import ru.bank.auth_service.model.enums.Role;
import ru.bank.auth_service.model.enums.UserStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenStore redisTokenStore;
    private final ClientTypeResolver clientTypeResolver;
    private final CookieManager cookieManager;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/auth/refresh/tokens"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String url = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(url::equals);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }
            validateToken(token);
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                setAuthentication(request, token);
            }
            filterChain.doFilter(request, response);
        } catch (TokenInBlackListException | InvalidTokenException ex) {
            log.warn("Ошибка аутентификации: {}", ex.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
        } catch (ClientInBlackListException ex) {
            log.warn("Доступ запрещен: {}", ex.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        }
    }

    // todo: Получение access + refresh токенов
    private String extractToken(HttpServletRequest request) {
        ClientType clientType = clientTypeResolver.resolve(request);
        if(clientType.isWeb()){
            log.debug("Access токен извлечен из cookies");
            return cookieManager.getAccessTokenFromCookie(request);
        } else {
            String authHeader = request.getHeader("Authorization");
            if(authHeader != null && authHeader.startsWith("Bearer ")){
                log.debug("Access токен извлечен из Authorization header");
                return authHeader.substring(7);
            }
        }
        return null;
    }

    // todo: Проверка валидности токенов
    private void validateToken(String token) {
        if (redisTokenStore.checkAccessTokenBlackList(token)) {
            log.warn("Токен находиться в черном списке");
            throw new TokenInBlackListException("Токен находиться в черном списке");
        }
        if (jwtTokenProvider.isInvalidToken(token)) {
            log.warn("Невалидный токен");
            throw new InvalidTokenException("Невалидный токен");
        }
        UserStatus status = jwtTokenProvider.getUserStatusFromToken(token);
        if (status.isBlocked()) {
            log.warn("Пользователь заблокирован в системе");
            throw new ClientInBlackListException("Пользователь заблокирован в системе");
        }
    }

    // todo: Установка аутентификации
    private void setAuthentication(HttpServletRequest request, String token) {
        String email = jwtTokenProvider.getEmailFromToken(token);
        Role role = jwtTokenProvider.getRoleFromToken(token);
        UUID userId = jwtTokenProvider.getUserIdFromToken(token);
        CustomUserDetails details = new CustomUserDetails(email, role, userId);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                details,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.debug("Пользователь: {} аутентифицирован с email: {} и ролью: {}", userId, email, role);
    }


}
