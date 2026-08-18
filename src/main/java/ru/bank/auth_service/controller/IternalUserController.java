package ru.bank.auth_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.bank.auth_service.model.dto.response.UserIternalResponseDto;
import ru.bank.auth_service.service.UserManagementService;

import java.util.UUID;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class IternalUserController {

    private final UserManagementService managementService;

    @GetMapping("/{targetId}")
    public ResponseEntity<UserIternalResponseDto> getUserById(@PathVariable("targetId") UUID targetId){
        UserIternalResponseDto response = managementService.getIternalUserInformation(targetId);
        return ResponseEntity.ok().body(response);
    }

}
