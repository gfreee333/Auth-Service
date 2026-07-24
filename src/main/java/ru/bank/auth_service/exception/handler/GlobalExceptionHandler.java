package ru.bank.auth_service.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.bank.auth_service.exception.custom.auth.ClientInBlackListException;
import ru.bank.auth_service.exception.custom.auth.ClientTypeNotSupportedException;
import ru.bank.auth_service.exception.response.ErrorResponse;
import ru.bank.auth_service.exception.custom.auth.AuthException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> authExceptionHandler(AuthException ex){
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(ClientInBlackListException.class)
    public ResponseEntity<ErrorResponse> clientInBlackListHandler(ClientInBlackListException ex){
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(ClientTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> clientTypeNotSupportedHandler(ClientTypeNotSupportedException ex){
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

}
