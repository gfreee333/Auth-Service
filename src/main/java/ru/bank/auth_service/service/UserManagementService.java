package ru.bank.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bank.auth_service.exception.custom.password.InvalidPasswordException;
import ru.bank.auth_service.exception.custom.password.NotCoincidencePasswordException;
import ru.bank.auth_service.exception.custom.password.UserResetPasswordForbiddenException;
import ru.bank.auth_service.exception.custom.user.UserBlockedForbiddenException;
import ru.bank.auth_service.exception.custom.user.UserChangeRoleException;
import ru.bank.auth_service.exception.custom.user.UserDeleteForbiddenException;
import ru.bank.auth_service.exception.custom.user.UserNotFoundException;
import ru.bank.auth_service.infrastructure.kafka.OutboxEvent;
import ru.bank.auth_service.infrastructure.kafka.OutboxEventStore;
import ru.bank.auth_service.infrastructure.mapper.UsersMapper;
import ru.bank.auth_service.infrastructure.storage.redis.RedisTokenStore;
import ru.bank.auth_service.infrastructure.util.PasswordGenerated;
import ru.bank.auth_service.infrastructure.util.SimpleScrypt;
import ru.bank.auth_service.model.dto.request.ChangePasswordRequestDto;
import ru.bank.auth_service.model.dto.request.UpdateUserProfileRequestDto;
import ru.bank.auth_service.model.dto.response.UserInformationDto;
import ru.bank.auth_service.model.dto.response.UserIternalResponseDto;
import ru.bank.auth_service.model.dto.response.UserProfileDto;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.model.enums.Role;
import ru.bank.auth_service.model.enums.UserStatus;
import ru.bank.auth_service.repository.UsersRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {

    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisTokenStore redisTokenStore;
    private final OutboxEventStore outboxStore;


    public UserIternalResponseDto getIternalUserInformation(UUID targetId){
        Users user = usersRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id: " + targetId + " не найден"));
        return new UserIternalResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus(),
                user.getEmail()
        );
    }

    /**
     * Получение детальной информации о всех пользователях в системе
     *
     * @return список пользователь в системе
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<UserInformationDto> findAllUsers() {
        List<Users> users = usersRepository.findAll();
        return users.stream()
                .map(usersMapper::toUserInformationResponse)
                .toList();
    }

    /**
     * Получение детальной информации о пользователе по email
     *
     * @return информация о конкретном пользователе
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public UserInformationDto findUserByEmail(String email) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с данным email не был найден"));
        return usersMapper.toUserInformationResponse(user);
    }

    /**
     * Получение детальной информации о пользователе по id
     *
     * @return информация о конкретном пользователе
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public UserInformationDto findUserById(UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь по id не найден"));
        return usersMapper.toUserInformationResponse(user);
    }

    /**
     * Получение детальной информации о пользователе по phoneNumber
     *
     * @return информация о конкретном пользователе
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public UserInformationDto findUserByPhoneNumber(String phoneNumber) {
        Users user = usersRepository.findByPhoneNumber("+" + phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        return usersMapper.toUserInformationResponse(user);
    }

    /**
     * Удаления пользователя из системы по id <br>
     * с выходом пользователя из всех активных сессий
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Transactional
    public void deleteUserById(UUID targetUserId, UUID currentUserId) {
        Users user = usersRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        UUID createdId = usersRepository.findCreatedBy(currentUserId);
        if (user.getId().equals(createdId) || user.getFirstName().equals("System")) {
            log.warn("Попытка удалить, аккаунт создателя, либо system админа: {}", targetUserId);
            throw new UserDeleteForbiddenException("Попытка удаления, создателя либо system админа");
        }
        redisTokenStore.deleteAllRefreshToken(targetUserId);
        redisTokenStore.addAllAccessTokenInBlackList(targetUserId);
        usersRepository.delete(user);
        OutboxEvent event = OutboxEvent.deleteUserEvent(user);
        outboxStore.generate(event);
    }


    /**
     * Блокировка пользователя в системе <br>
     * с выходом пользователя из всех активных сессий
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Transactional
    public void blockedUserInSystem(UUID targetId, UUID currentId) {
        Users user = usersRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        UUID createdBy = usersRepository.findCreatedBy(currentId);
        if (user.getId().equals(createdBy) || user.getFirstName().equals("System")) {
            log.warn("Попытка заблокировать создателя/system админа: {}", targetId);
            throw new UserBlockedForbiddenException("Попытка заблокировать создателя, либо system админа");
        }
        if (!user.getStatus().isBlocked()) {
            user.setStatus(UserStatus.BLOCKED);
        }
        Users saved = usersRepository.save(user);
        OutboxEvent event = OutboxEvent.blockedEvent(saved);
        outboxStore.generate(event);
        redisTokenStore.addAllAccessTokenInBlackList(targetId);
        redisTokenStore.deleteAllRefreshToken(targetId);

    }

    /**
     * Разблокировать пользователя в системе
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Transactional
    public void unblockedUserInSystem(UUID targetId) {
        Users user = usersRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        if (!user.getStatus().isBlocked()) {
            return;
        }
        user.setStatus(UserStatus.PENDING);
        Users saved = usersRepository.save(user);
        OutboxEvent event = OutboxEvent.unblockedEvent(saved);
        outboxStore.generate(event);
    }

    /**
     * Получение краткой информации о своем профили в системе
     *
     * @return краткая информация о профиле
     */
    public UserProfileDto getMyProfile(UUID myId) {
        Users user = usersRepository.findById(myId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        return usersMapper.toMyProfile(user);
    }

    /**
     * Частичное изменения данных о профиле в системе
     */
    @Transactional
    public void updateMyProfile(UUID myId, UpdateUserProfileRequestDto request) {
        Users user = usersRepository.findById(myId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isEmpty()) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
    }

    /**
     * Смена пароля пользователя с проверкой текущего password в системе, <br>
     * при удачной смене, пользователь выходит со всех активных сессий
     */
    @Transactional
    public void changePassword(UUID myId, ChangePasswordRequestDto request) {
        Users user = usersRepository.findById(myId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Текущей пароль пользователя: {}, неверный ", myId);
            throw new InvalidPasswordException("Старый пароль неверный");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("Новая пара паролей не совпадает");
            throw new NotCoincidencePasswordException("Новая пара паролей не совпадает");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        redisTokenStore.deleteAllRefreshToken(myId);
        redisTokenStore.addAllAccessTokenInBlackList(myId);
        Users saved = usersRepository.save(user);
        OutboxEvent event = OutboxEvent.passwordChangeEvent(saved);
        outboxStore.generate(event);
    }

    /**
     * Сменя роли пользователя в системе, <br>
     * с проверкой является ли пользователь создателем
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Transactional
    public void changeUserRole(UUID targetId, Role newRole, UUID currentUserId) {
        Users user = usersRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        UUID createdId = usersRepository.findCreatedBy(currentUserId);
        if (user.getId().equals(createdId) || user.getFirstName().equals("SYSTEM")) {
            log.warn("Пользователь: {}, является создателем", targetId);
            throw new UserChangeRoleException("Попытка сменить роль создателю, либо system админу");
        }
        user.setRole(newRole);
        usersRepository.save(user);
    }

    /**
     * Смена пароля пользователя на автоматически сгенерированный системы,
     * при успешном входе пользователь выходит из всех активных сессий
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Transactional
    public void resetPassword(UUID targetId) {
        Users user = usersRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        UUID createdBy = user.getCreatedBy();
        if (user.getId().equals(createdBy) || user.getFirstName().equals("System")) {
            log.warn("Попытка сбросить пароль для создателя/system админа: {}", targetId);
            throw new UserResetPasswordForbiddenException("Попытка заблокировать создателя, либо system админа");
        }
        String newPassword = PasswordGenerated.generatedPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        Users saved = usersRepository.save(user);
        String encryptedPassword = SimpleScrypt.encrypt(newPassword);
        OutboxEvent event = OutboxEvent.passwordEvent(saved, encryptedPassword);
        outboxStore.generate(event);
        redisTokenStore.addAllAccessTokenInBlackList(targetId);
        redisTokenStore.deleteAllRefreshToken(targetId);
    }

}
