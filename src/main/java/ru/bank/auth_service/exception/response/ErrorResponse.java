package ru.bank.auth_service.exception.response;

public record ErrorResponse(
        String message,
        long httpStatus
){}
