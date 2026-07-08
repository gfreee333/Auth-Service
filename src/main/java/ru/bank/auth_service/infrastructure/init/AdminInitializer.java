package ru.bank.auth_service.infrastructure.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.model.enums.Role;
import ru.bank.auth_service.model.enums.UserStatus;
import ru.bank.auth_service.repository.UsersRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void initAdmin(){
        if(usersRepository.count() == 0){
            Users admin = Users.builder()
                    .firstName("System")
                    .lastName("Administration")
                    .email("admin@bank.com")
                    .phoneNumber("+70000000000")
                    .password(passwordEncoder.encode("Admin123"))
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .createdBy(UUID.randomUUID())
                    .build();
            usersRepository.save(admin);
            log.info("Админ успешно создан");
            log.info("Email: admin@bank.com");
            log.info("Phone: +70000000000");
            log.info("Password: Admin123");
        }
    }

}
