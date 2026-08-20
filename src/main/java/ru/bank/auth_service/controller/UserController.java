package ru.bank.auth_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bank.auth_service.infrastructure.util.UserSecurityContext;
import ru.bank.auth_service.model.dto.request.ChangePasswordRequestDto;
import ru.bank.auth_service.model.dto.request.ChangeRoleRequestDto;
import ru.bank.auth_service.model.dto.request.RegistrationRequestDto;
import ru.bank.auth_service.model.dto.request.UpdateUserProfileRequestDto;
import ru.bank.auth_service.model.dto.response.RegistrationResponseDto;
import ru.bank.auth_service.model.dto.response.UserInformationDto;
import ru.bank.auth_service.model.dto.response.UserProfileDto;
import ru.bank.auth_service.service.RegistrationService;
import ru.bank.auth_service.service.UserManagementService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserManagementService userManagementService;
    private final RegistrationService registrationService;
    private final UserSecurityContext userSecurityContext;

    @GetMapping
    public ResponseEntity<List<UserInformationDto>> findAllUsers() {
        List<UserInformationDto> results = userManagementService.findAllUsers();
        return ResponseEntity.ok().body(results);
    }

    @GetMapping("/email")
    public ResponseEntity<UserInformationDto> findUserByEmail(
            @RequestParam String email) {
        UserInformationDto result = userManagementService.findUserByEmail(email);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/phone")
    public ResponseEntity<UserInformationDto> findUserByPhoneNumber(
            @RequestParam String phone) {
        UserInformationDto result = userManagementService.findUserByPhoneNumber(phone);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/{targetId}")
    public ResponseEntity<UserInformationDto> findUserById(
            @PathVariable("targetId") UUID targetId) {
        UserInformationDto result = userManagementService.findUserById(targetId);
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("/delete/{targetId}")
    public ResponseEntity<Void> deleteUserById(
            @PathVariable("targetId") UUID targetId) {
        UUID currentId = userSecurityContext.getCurrentUserId();
        userManagementService.deleteUserById(targetId, currentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/blocked/{targetId}")
    public ResponseEntity<Void> blockedUserInSystem(
            @PathVariable("targetId") UUID targetId) {
        UUID currentId = userSecurityContext.getCurrentUserId();
        userManagementService.blockedUserInSystem(targetId, currentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/unblocked/{targetId}")
    public ResponseEntity<Void> unblockedUserInSystem(
            @PathVariable("targetId") UUID targetId) {
        userManagementService.unblockedUserInSystem(targetId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/registration")
    public ResponseEntity<RegistrationResponseDto> registrationUser(
            @RequestBody @Valid RegistrationRequestDto request) {
        UUID createdBy = userSecurityContext.getCurrentUserId();
        RegistrationResponseDto result = registrationService.registrationUser(request, createdBy);
        return ResponseEntity.ok().body(result);
    }

    @PatchMapping("/{targetId}/role")
    public ResponseEntity<Void> changeRole(
            @PathVariable("targetId") UUID targetId,
            @RequestBody @Valid ChangeRoleRequestDto request) {
        UUID currentId = userSecurityContext.getCurrentUserId();
        userManagementService.changeUserRole(targetId, request.getRole(), currentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getMyProfile(){
        UUID myUserId = userSecurityContext.getCurrentUserId();
        UserProfileDto result = userManagementService.getMyProfile(myUserId);
        return ResponseEntity.ok().body(result);
    }

    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @RequestBody UpdateUserProfileRequestDto request) {
        UUID myUserId = userSecurityContext.getCurrentUserId();
        userManagementService.updateMyProfile(myUserId, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordRequestDto request) {
        UUID myUserId = userSecurityContext.getCurrentUserId();
        userManagementService.changePassword(myUserId, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/password/reset/{targetId}")
    public ResponseEntity<Void> resetPassword(
            @PathVariable("targetId") UUID targetId) {
        userManagementService.resetPassword(targetId);
        return ResponseEntity.noContent().build();
    }

}
