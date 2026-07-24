package ru.bank.auth_service.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.bank.auth_service.model.dto.request.RegistrationRequestDto;
import ru.bank.auth_service.model.dto.response.RegistrationResponseDto;
import ru.bank.auth_service.model.dto.response.UserInformationDto;
import ru.bank.auth_service.model.dto.response.UserProfile;
import ru.bank.auth_service.model.entity.Users;

@Mapper(componentModel = "spring")
public interface UsersMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Users toUserEntity(RegistrationRequestDto request);

    @Mapping(target = "message", constant = "Пользователь зарегистрирован в системе")
    RegistrationResponseDto toRegistrationResponse(Users users);

    UserInformationDto toUserInformationResponse(Users user);

    UserProfile toMyProfile(Users user);


}
