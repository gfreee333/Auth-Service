package ru.bank.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableCaching
@EnableDiscoveryClient
@EntityScan(basePackages = {
        "ru.bank.auth_service.model.entity",
        "ru.bank.outbox_library.model.entity"

})
@EnableJpaRepositories(basePackages = {
        "ru.bank.auth_service.repository",
        "ru.bank.outbox_library.repository"
})
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
