package ru.bank.auth_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bank.auth_service.infrastructure.util.ClientTypeResolver;
import ru.bank.auth_service.model.dto.request.LoginRequestDto;
import ru.bank.auth_service.model.dto.response.LoginResponseDto;
import ru.bank.auth_service.model.enums.ClientType;
import ru.bank.auth_service.service.AuthenticationService;
import ru.bank.auth_service.service.LogoutService;
import ru.bank.auth_service.service.TokenRefreshService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authService;
    private final LogoutService logoutService;
    private final TokenRefreshService tokenRefreshService;
    private final ClientTypeResolver clientTypeResolver;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody @Valid LoginRequestDto request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        ClientType clientType = clientTypeResolver.resolve(servletRequest);
        LoginResponseDto result = authService.login(request, servletResponse, clientType);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ){
        ClientType clientType = clientTypeResolver.resolve(servletRequest);
        logoutService.logout(servletRequest, servletResponse, clientType);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/refresh/tokens")
    public ResponseEntity<LoginResponseDto> refreshToken(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        ClientType clientType = clientTypeResolver.resolve(servletRequest);
        LoginResponseDto result = tokenRefreshService.refreshToken(servletRequest, servletResponse, clientType);
        return ResponseEntity.ok().body(result);
    }

}
