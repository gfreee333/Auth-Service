package ru.bank.auth_service.infrastructure.strategy.client;

import ru.bank.auth_service.model.enums.ClientType;

public interface ClientBaseStrategy {
    ClientType getClientType();
}
