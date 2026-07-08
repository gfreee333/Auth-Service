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
import ru.bank.auth_service.infrastructure.storage.cookies.CookieManager;
import ru.bank.auth_service.infrastructure.storage.redis.RedisTokenStore;
import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class  JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenStore redisTokenStore;
    private final CookieManager cookieManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        // todo: Если токена нет -> пользователь не авторизован в системе
        if (token == null) {
            log.debug("Токен не найден в запросе");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (redisTokenStore.checkAccessTokenBlackList(token)) {
                log.warn("Ошибка аутентификации, token пользователя находиться в черном списке");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        "Token находиться в черном списке");
                return;
            }

            if(!jwtTokenProvider.isValidToken(token)){
                log.warn("Невалидный токен");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        "Невалидный токен");
                return;
            }

            String email = jwtTokenProvider.getEmailFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);
            String status = jwtTokenProvider.getUserStatusFromToken(token);
            // todo: Проверка статуса пользователя
            if ("BLOCKED".equals(status) || "DELETED".equals(status)) {
                log.warn("Пользователь со статусом: {}", status);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Пользователь: " + status);
                return;
            }
            // todo: Установка аутентификации в SecurityContext
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(email, null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Пользователь аутентифицирован: {} с ролью: {}", email, role);
            }
        } catch (Exception ex) {
            log.warn("Ошибка валидации JWT: {}", ex.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Некорректный token");
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request){
        // todo: Получение токена для Mobile
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            String token = authHeader.substring(7);
            log.debug("Токен извлечен из Authorization header");
            return token;
        }
        // todo: Получения токена для Web
        String token = cookieManager.getAccessTokenFromCookie(request);
        if(token != null){
            log.debug("Токен извлечен из Cookies");
        }
        return token;
    }

}
