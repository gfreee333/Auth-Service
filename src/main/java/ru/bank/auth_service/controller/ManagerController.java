package ru.bank.auth_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bank.auth_service.infrastructure.util.UserSecurityContext;
import ru.bank.auth_service.model.dto.request.RegistrationRequestDto;
import ru.bank.auth_service.model.dto.request.UpdateUserProfileRequestDto;
import ru.bank.auth_service.model.dto.response.RegistrationResponseDto;
import ru.bank.auth_service.model.dto.response.UserInformationDto;
import ru.bank.auth_service.service.RegistrationService;
import ru.bank.auth_service.service.UserManagementService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/manager")
public class ManagerController {

    private final UserManagementService userManagementService;
    private final RegistrationService registrationService;
    private final UserSecurityContext userSecurityContext;

    // Получение детальной информации о всех пользователях
    @GetMapping
    public ResponseEntity<List<UserInformationDto>> findAllUsers(){
        List<UserInformationDto> results = userManagementService.findAllUsers();
        return ResponseEntity.ok().body(results);
    }

    // Получение детальной информации о пользователе по Email
    @GetMapping("/email")
    public ResponseEntity<UserInformationDto> findUserByEmail(
            @RequestParam String email) {
        UserInformationDto result = userManagementService.findUserByEmail(email);
        return ResponseEntity.ok().body(result);
    }

    // Получение детальной информации о пользователе по PhoneNumber
    @GetMapping("/phone")
    public ResponseEntity<UserInformationDto> findUserByPhoneNumber(
            @RequestParam String phone) {
        UserInformationDto result = userManagementService.findUserByPhoneNumber(phone);
        return ResponseEntity.ok().body(result);
    }

    // Получение информации о пользователе по Id
    @GetMapping("/{userId}")
    public ResponseEntity<UserInformationDto> findUserById(
            @PathVariable("userId") UUID userId ) {
        UserInformationDto result = userManagementService.findUserById(userId);
        return ResponseEntity.ok().body(result);
    }

    // Регистрация пользователя в системе
    @PostMapping("/registration")
    public ResponseEntity<RegistrationResponseDto> registrationUser(@RequestBody @Valid RegistrationRequestDto request) {
        UUID userId = userSecurityContext.getCurrentUserId();
        RegistrationResponseDto result = registrationService.registrationUser(request, userId);
        return ResponseEntity.ok().body(result);
    }

    // Изменение данных о пользователе в системе
    @PatchMapping("/{userId}/profile")
    public ResponseEntity<Void> updateProfile(
            @PathVariable("userId") UUID userId,
            @RequestBody UpdateUserProfileRequestDto request) {
        userManagementService.updateMyProfile(userId, request);
        return ResponseEntity.noContent().build();
    }

    // Генерация нового пароля для пользователя
    @GetMapping("/password/reset/{userId}")
    public ResponseEntity<Void> resetPassword(
            @PathVariable("userId") UUID userId) {
        userManagementService.resetPassword(userId);
        return ResponseEntity.noContent().build();
    }

}
