package ru.bank.auth_service.infrastructure.strategy.login;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.bank.auth_service.exception.custom.auth.AuthException;
import ru.bank.auth_service.model.dto.request.LoginRequestDto;
import ru.bank.auth_service.model.entity.Users;
import ru.bank.auth_service.repository.UsersRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class PhoneLoginProcessor implements LoginProcessorStrategy {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Users authenticate(LoginRequestDto request) {
        Users user = usersRepository.findByPhoneNumber(request.getIdentifier())
                .orElseThrow(() -> new AuthException("Неверные учетные данные"));
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new AuthException("Неверные учетные данные");
        }
        return user;
    }

    @Override
    public boolean supports(LoginRequestDto request) {
        return request.getIdentifier().matches("^\\+?[0-9]{10,15}$");
    }
}
