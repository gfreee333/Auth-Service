package ru.bank.auth_service.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.UniqueElements;
import ru.bank.auth_service.model.enumerate.Role;
import ru.bank.auth_service.model.enumerate.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@RequiredArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @NotNull(message = "firstName не может быть пустым")
    @Size(max = 100, message = "Максимально допустимое значение firstName 100")
    private String firstName;
    @NotNull(message = "lastName не может быть пустой")
    @Size(max = 100, message = "Максимально допустимое значение lastName 100")
    private String lastName;
    @NotNull(message = "phoneNumber не может быть пустым")
    @UniqueElements(message = "phoneNumber должен быть уникальным")
    private String phoneNumber;
    @Email(message = "Ошибка в формате Email")
    @NotNull(message = "Email не может быть пустым")
    @UniqueElements(message = "Email должен быть уникальным")
    private String email;
    @NotNull(message = "Password не может быть пустым")
    @Size(min = 10, max = 100, message =
            "Минимальное допустимое значение password 10, " +
                    "Максимальное допустимое значение password 100")
    private String password;
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.PENDING;
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private LocalDateTime activatedAt;
    @NotNull(message = "createdBy не может быть пустым")
    private UUID createdBy;
}
