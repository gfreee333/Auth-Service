package ru.bank.auth_service.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenStore redisTokenStore;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            String token = authHeader.substring(7);
            if (redisTokenStore.checkAccessTokenBlackList(token)){
                log.error("Ошибка аутентификации, токен пользователя находиться в черном списке");
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Токен находиться в черном списке");
                return;
            }
        }
        try {}
        catch (Exception ex){

        }
        filterChain.doFilter(request, response);
    }

}
