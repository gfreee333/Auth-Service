package ru.bank.auth_service.infrastructure.storage.cookies;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieManager {

    @Value("${jwt.expiration}")
    private Long accessTokenExpiration;
    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpiration;
    private final static String ACCESS_COOKIE_NAME = "access_token";
    private final static String REFRESH_COOKIE_NAME = "refresh_token";

    // todo: Добавление access в Cookie
    public void addAccessTokenCookie(HttpServletResponse response, String token){
        Cookie cookie = new Cookie(ACCESS_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (accessTokenExpiration / 1000));
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    // todo: Добавление refresh в Cookie
    public void addRefreshTokenCookie(HttpServletResponse response, String token){
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge((int) (refreshTokenExpiration / 1000));
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    // todo: Удаление access токена из Cookies
    public void deleteAccessTokenCookies(HttpServletResponse response){
        Cookie cookie = new Cookie(ACCESS_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    // todo: Удаление refresh токена из Cookies
    public void deleteRefreshTokenCookies(HttpServletResponse response){
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    // todo: Общий метод для очистки всех cookies
    public void clearAllCookies(HttpServletResponse response){
        deleteAccessTokenCookies(response);
        deleteRefreshTokenCookies(response);
    }

    // todo: Чтение access из Cookies
    public String getAccessTokenFromCookie(HttpServletRequest request){
        return getCookieValue(request, ACCESS_COOKIE_NAME);
    }

    // todo: Чтение refresh из Cookies
    public String getRefreshTokenFromCookie(HttpServletRequest request){
        return getCookieValue(request, REFRESH_COOKIE_NAME);
    }

    // todo: Вспомогательный метод для получения данных
    private String getCookieValue(HttpServletRequest request, String cookieName){
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for (Cookie cookie : cookies){
                if (cookieName.equals(cookie.getName())){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
