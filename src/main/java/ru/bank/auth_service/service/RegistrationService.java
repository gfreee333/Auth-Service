package ru.bank.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bank.auth_service.exception.custom.duplicate.DuplicateEmailException;
import ru.bank.auth_service.exception.custom.duplicate.DuplicatePhoneException;
import ru.bank.auth_service.exception.custom.user.InvalidUserFirstNameException;
import ru.bank.auth_service.infrastructure.kafka.OutboxEvent;
import ru.bank.auth_service.infrastructure.kafka.OutboxEventStore;
import ru.bank.auth_service.infrastructure.util.PasswordGenerated;
import ru.bank.auth_service.infrastructure.mapper.UsersMapper;
import ru.bank.auth_service.infrastructure.util.SimpleScrypt;
import ru.bank.auth_service.model.dto.request.RegistrationRequestDto;
import ru.bank.auth_service.model.dto.response.RegistrationResponseDto;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.repository.UsersRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final OutboxEventStore eventStore;
    private final UsersMapper usersMapper;

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
        if(request.getFirstName().equals("System")){
            log.warn("Пользователь не может иметь имя System");
            throw new InvalidUserFirstNameException("Имя пользователя не может быть System");
        }
        String tempPassword = PasswordGenerated.generatedPassword();
        Users user = usersMapper.toUserEntity(request);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setCreatedBy(createdBy);
        Users savedUser = usersRepository.save(user);
        String encryptedPassword = SimpleScrypt.encrypt(tempPassword);
        OutboxEvent event = OutboxEvent.passwordEvent(savedUser, encryptedPassword);
        eventStore.generate(event);
        log.info("Пользователь: {} успешно создан пользователем: {} ", savedUser.getId(), createdBy);
        return usersMapper.toRegistrationResponse(user);
    }

}
