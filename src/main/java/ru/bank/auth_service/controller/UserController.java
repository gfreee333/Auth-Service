package ru.bank.auth_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bank.auth_service.infrastructure.util.UserSecurityContext;
import ru.bank.auth_service.model.dto.request.ChangePasswordRequestDto;
import ru.bank.auth_service.model.dto.request.UpdateUserProfileRequestDto;
import ru.bank.auth_service.model.dto.response.UserProfile;
import ru.bank.auth_service.service.UserManagementService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserManagementService userManagementService;
    private final UserSecurityContext userSecurityContext;

    // Тут можно брать id пользователя из Context т.к мы явным понимаем, что указанное ID и будет
    // ID пользователя из JWT

    // Получение информации о своем профиле
    @GetMapping
    public ResponseEntity<UserProfile> getMyProfile(){
        UUID myUserId = userSecurityContext.getCurrentUserId();
        UserProfile result = userManagementService.getMyProfile(myUserId);
        return ResponseEntity.ok().body(result);
    }

    // Смена базовых данных, email, phoneNumber, firstName, lastName
    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @RequestBody UpdateUserProfileRequestDto request) {
        UUID myUserId = userSecurityContext.getCurrentUserId();
        userManagementService.updateMyProfile(myUserId, request);
        return ResponseEntity.noContent().build();
    }

    // Смена пароля
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @RequestBody ChangePasswordRequestDto request) {
        UUID myUserId = userSecurityContext.getCurrentUserId();
        userManagementService.changePassword(myUserId, request);
        return ResponseEntity.noContent().build();
    }

}
