package ru.bank.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bank.auth_service.exception.custom.auth.AuthException;
import ru.bank.auth_service.infrastructure.mapper.UsersMapper;
import ru.bank.auth_service.infrastructure.storage.redis.RedisTokenStore;
import ru.bank.auth_service.infrastructure.util.PasswordGenerated;
import ru.bank.auth_service.infrastructure.util.UserSecurityContext;
import ru.bank.auth_service.model.dto.request.ChangePasswordRequestDto;
import ru.bank.auth_service.model.dto.request.UpdateUserProfileRequestDto;
import ru.bank.auth_service.model.dto.response.UserInformationDto;
import ru.bank.auth_service.model.dto.response.UserProfile;
import ru.bank.auth_service.model.entity.Users;
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

    // todo: Базовый функционал по взаимодействию с БД

    // todo: Информация о всех пользователях
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<UserInformationDto> findAllUsers(){
        List<Users> users = usersRepository.findAll();
        return users.stream()
                    .map(usersMapper::toUserInformationResponse)
                    .toList();
    }

    // todo: Получение детальной информации о пользователе через email
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public UserInformationDto findUserByEmail(String email){
        Users user = usersRepository.findByEmail(email).orElseThrow(); // Добавить обработку
        // Пытаемся найти пользователя по email, если не нашли выбросили ошибку, пользователя с таким email
        // не существует, в противном случае вернем информацию о конкретном пользователи
        return usersMapper.toUserInformationResponse(user);
    }

    // todo: Получение детальной информации о пользователе через id
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public UserInformationDto findUserById(UUID userId){
        Users user = usersRepository.findById(userId).orElseThrow();
        return usersMapper.toUserInformationResponse(user);
    }

    // todo: Получение детальной информации о пользователе через phoneNumber
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public UserInformationDto findUserByPhoneNumber(String phoneNumber){
        Users user = usersRepository.findByPhoneNumber(phoneNumber).orElseThrow();
        return usersMapper.toUserInformationResponse(user);
    }

    // todo: Удалить пользователя из системы
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Transactional
    public void deleteUserById(UUID userId){
        Users user = usersRepository.findById(userId).orElseThrow(); // Реализовать ошибку
        usersRepository.delete(user);
        // После удаления пользователя стоит удалить его токены access для всех сессий добавить в blackList
        // refresh по всюду удалить
    }


    // todo: Блокировка пользователя в системе
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Transactional
    public void blockedUserInSystem(UUID userId){
        Users user = usersRepository.findById(userId).orElseThrow();
        if(!user.getStatus().isBlocked()){
            user.setStatus(UserStatus.BLOCKED);
        }
        // После блокировки пользователя, нужно будет сделать для него logout для всех сессий
        // Чтобы пользователь не смог пользоваться приложением
    }

    // todo: Разблокирование пользователя в системе
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Transactional
    public void unblockedUserInSystem(UUID userId){
        Users user = usersRepository.findById(userId).orElseThrow();
        if(!user.getStatus().isBlocked()){
            throw new AuthException(""); // ДОБАВИТЬ КАСТОМНУЕ ИСКЛЮЧЕНИЕ
        }
        user.setStatus(UserStatus.ACTIVE);
        usersRepository.save(user);
        log.info("Пользователь: {} разблокирован", userId);
    }

    // todo: Получение информации о себе пользователем
    @PreAuthorize("hasAnyRole('USER')")
    public UserProfile getMyProfile(UUID userId){
        Users user = usersRepository.findById(userId).orElseThrow();
        return usersMapper.toMyProfile(user);
    }

    // todo: Изменение данных о пользователе в системе (ВОПРОС ОБ УРОВНЕ ДОСТУПНОСТИ, СКОРЕЕ ВСЕГО MANAGER)
    // Но тут нужно все таки обдумать момент, кому это фактически будет доступно как в банковской системе
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Transactional
    public void updateMyProfile(UUID userId, UpdateUserProfileRequestDto request){
        Users user = usersRepository.findById(userId).orElseThrow();
        if(request.getFirstName() != null){
            user.setFirstName(request.getFirstName());
        }
        if(request.getLastName() != null){
            user.setLastName(request.getLastName());
        }
        if(request.getEmail() != null){
            user.setEmail(request.getEmail());
        }
        if(request.getPhoneNumber() != null){
            user.setPhoneNumber(request.getPhoneNumber());
        }
    }

    // todo: Смена пароля пользователя с проверкой старого пароля
    @PreAuthorize("hasAnyRole('USER')")
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequestDto request){
        Users user = usersRepository.findById(userId).orElseThrow();
        if(!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())){
            log.warn("Неверный текущей пароль для пользователя: {}", userId);
            throw new AuthException("Неверный текущей пароль"); // Добавить кастомное исключение
        }
        if(!request.getNewPassword().equals(request.getConfirmPassword())){
            throw new AuthException("Пароль для подтверждения не верный, отмена операции"); // Добавить кастомное исключение
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepository.save(user);
        // Тут должны удалиться абсолютно все refresh токенов для прошлых session
        // + добавиться в черный список все остальные accessTokens
        log.info("Пароль успешно изменен для пользователя: {}", userId);
    }

    // todo: Сгенерировать новый временный пароль для пользователя
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Transactional
    public void resetPassword(UUID userId){
        Users user = usersRepository.findById(userId).orElseThrow();
        String newPassword = PasswordGenerated.generatedPassword();
        log.info("Пароль был обновлен для пользователя: {}", userId);
        // Тут будет логика отправки пользователю PUSH уведомления с новыми данными, например на почту
    }

}
