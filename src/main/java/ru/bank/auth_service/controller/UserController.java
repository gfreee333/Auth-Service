package ru.bank.auth_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    // Получение информации о конкретном пользователе
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfile> getMyProfile(@PathVariable("userId") UUID userId){
        UserProfile result = userManagementService.getMyProfile(userId);
        return ResponseEntity.ok().body(result);
    }

    // Смена базовых данных, email, phoneNumber, firstName, lastName
    @PatchMapping("/{userId}/profile")
    public ResponseEntity<Void> updateProfile(
            @PathVariable("userId") UUID userId,
            @RequestBody UpdateUserProfileRequestDto request) {
        userManagementService.updateMyProfile(userId, request);
        return ResponseEntity.noContent().build();
    }

    // Смена пароля
    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable("userId") UUID userId,
            @RequestBody ChangePasswordRequestDto request) {
        userManagementService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

}
