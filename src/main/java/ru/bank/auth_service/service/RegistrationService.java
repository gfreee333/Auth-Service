package ru.bank.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bank.auth_service.exception.custom.duplicate.DuplicateEmailException;
import ru.bank.auth_service.exception.custom.duplicate.DuplicatePhoneException;
import ru.bank.auth_service.infrastructure.util.PasswordGenerated;
import ru.bank.auth_service.infrastructure.mapper.UsersMapper;
import ru.bank.auth_service.model.dto.request.RegistrationRequestDto;
import ru.bank.auth_service.model.dto.response.RegistrationResponseDto;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.repository.UsersRepository;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersMapper usersMapper;
    private final TempPasswordService tempPasswordService;

    // todo: Регистрация пользователя в системе
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Transactional
    public RegistrationResponseDto registrationUser(RegistrationRequestDto request, UUID createdBy) {
        if (usersRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        if (usersRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicatePhoneException(request.getPhoneNumber());
        }
        String tempPassword = PasswordGenerated.generatedPassword();
        Users user = usersMapper.toUserEntity(request);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setCreatedBy(createdBy);
        Users savedUser = usersRepository.save(user);
        tempPasswordService.saveInRedis(savedUser.getId(), tempPassword, Duration.ofHours(24));
        log.info("Пользователь {} успешно создан пользователей {} ", savedUser.getId(), createdBy);
        return usersMapper.toRegistrationResponse(user);
    }

}
