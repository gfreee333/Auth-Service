package ru.bank.auth_service.infrastructure.storage.cookies;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CookieManager {

    @Value("${jwt.expiration}")
    private Long accessTokenExpiration;
    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

    private static final String ACCESS_COOKIE_NAME = "access_token";
    private static final String REFRESH_COOKIE_NAME = "refresh_token";
    private static final String COOKIES_PATH = "/";
    private static final boolean COOKIES_HTTP_ONLY = true;
    private static final boolean COOKIES_SECURE = true;
    private static final String COOKIES_SAME_SITE = "Strict";

    // todo: Добавление access в Cookie
    public void addAccessTokenCookie(HttpServletResponse response, String token){
        addCookies(response, ACCESS_COOKIE_NAME, token, accessTokenExpiration);
    }

    // todo: Добавление refresh в Cookie
    public void addRefreshTokenCookie(HttpServletResponse response, String token){
        addCookies(response, REFRESH_COOKIE_NAME, token, refreshTokenExpiration);
    }

    // todo: Удаление access токена из Cookies
    public void deleteAccessTokenCookies(HttpServletResponse response){
        deleteCookies(response, ACCESS_COOKIE_NAME);
    }

    // todo: Удаление refresh токена из Cookies
    public void deleteRefreshTokenCookies(HttpServletResponse response){
        deleteCookies(response, REFRESH_COOKIE_NAME);
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


    // todo: Добавление данных в Cookie
    private void addCookies(HttpServletResponse response, String name, String value, Long maxAge){
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(COOKIES_HTTP_ONLY);
        cookie.setSecure(COOKIES_SECURE);
        cookie.setPath(COOKIES_PATH);
        cookie.setMaxAge((int) (maxAge / 1000));
        cookie.setAttribute("SameSite", COOKIES_SAME_SITE);
        response.addCookie(cookie);
        log.debug("Cookies добавлены [ name: {} | path: {} | maxAge: {} ]", name, COOKIES_PATH, maxAge / 1000);
    }

    // todo: Удаление данных Cookie
    private void deleteCookies(HttpServletResponse response, String name){
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(COOKIES_HTTP_ONLY);
        cookie.setSecure(COOKIES_SECURE);
        cookie.setPath(COOKIES_PATH);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        log.debug("Cookie удален name: {}", name);
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
