package ru.bank.auth_service.exception.handler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.bank.auth_service.exception.custom.auth.*;
import ru.bank.auth_service.exception.custom.duplicate.DuplicateEmailException;
import ru.bank.auth_service.exception.custom.duplicate.DuplicatePhoneException;
import ru.bank.auth_service.exception.custom.password.InvalidPasswordException;
import ru.bank.auth_service.exception.custom.password.NotCoincidencePasswordException;
import ru.bank.auth_service.exception.custom.password.PasswordEncryptedKafkaException;
import ru.bank.auth_service.exception.custom.user.UserChangeRoleException;
import ru.bank.auth_service.exception.custom.user.UserDeleteForbiddenException;
import ru.bank.auth_service.exception.custom.user.UserNotFoundException;
import ru.bank.auth_service.exception.response.ErrorResponse;
import ru.bank.auth_service.exception.response.ValidationErrorResponse;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * <p><b>Ошибка: AuthException</b></p>
     * <p><b>Описание: Обработка общих ошибок аутентификации <br>
     * Возвращает HTTP 401 Unauthorized</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 401
     */

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> authExceptionHandler(AuthException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * <p><b>Ошибка: ClientInBlackListException</b></p>
     * <p><b>Описание: Пользователь находиться в черном списке системы<br>
     * Возвращает HTTP 403 Forbidden</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 403
     */

    @ExceptionHandler(ClientInBlackListException.class)
    public ResponseEntity<ErrorResponse> clientInBlackListHandler(ClientInBlackListException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * <p><b>Ошибка: ClientTypeNotSupportedException</b></p>
     * <p><b>Описание: Данный тип клиента не поддерживается системой<br>
     * Возвращает HTTP 401 Unauthorized с сообщение об ошибке</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 401
     */

    @ExceptionHandler(ClientTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> clientTypeNotSupportedHandler(ClientTypeNotSupportedException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * <p><b>Ошибка: UnsupportedIdentifierException</b></p>
     * <p><b>Описание: Неверный формат идентификатора для входа в систему<br>
     * Возвращает HTTP 401 Unauthorized</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 401
     */

    @ExceptionHandler(UnsupportedIdentifierException.class)
    public ResponseEntity<ErrorResponse> unsupportedIdentifierHandler(UnsupportedIdentifierException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * <p><b>Ошибка: TokenInBlackListException</b></p>
     * <p><b>Описание: Ошибка входа, токен находиться в черном списке системы<br>
     * Возвращает HTTP 403 Unauthorized</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 403
     */

    @ExceptionHandler(TokenInBlackListException.class)
    public ResponseEntity<ErrorResponse> tokenInBlackListHandler(TokenInBlackListException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * <p><b>Ошибка: InvalidateTokenException</b></p>
     * <p><b>Описание: Используемый JWT токен не является валидным<br>
     * Возвращает HTTP 401 Unauthorized</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 401
     */

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> invalidTokenHandler(InvalidTokenException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * <p><b>Ошибка: UserNotFoundException</b></p>
     * <p><b>Описание: Пользователь не найден в системе<br>
     * Возвращает HTTP 404 NotFound</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 404
     */

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> userNotFoundHandler(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * <p><b>Ошибка: OldTokeUseException</b></p>
     * <p><b>Описание: Повторное использование устаревшего refresh токена<br>
     * Возвращает HTTP 401 Unauthorized</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 401
     */

    @ExceptionHandler(OldTokenUseException.class)
    public ResponseEntity<ErrorResponse> tokenReuseAttemptHandler(OldTokenUseException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * <p><b>Ошибка: MethodArgumentNotValidException</b></p>
     * <p><b>Описание: Ошибка валидации входных данных<br>
     * Возвращает HTTP 400 Bad Request</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 401
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> methodArgumentNotValidHandler(MethodArgumentNotValidException ex) {
        log.warn("Ошибка валидации: {}", ex.getMessage());
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Неверное значение"
                ));
        ValidationErrorResponse response = new ValidationErrorResponse(
                "Ошибка входных данных",
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * <p><b>Ошибка: UserChangeRoleException </b></p>
     * <p><b>Описание: Недостаточно прав для смены роли пользователя <br>
     * Возвращает HTTP 403 Forbidden</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 403
     */

    @ExceptionHandler(UserChangeRoleException.class)
    public ResponseEntity<ErrorResponse> userChangeRoleHandler(UserChangeRoleException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * <p><b>Ошибка: UserDeleteForbiddenException</b></p>
     * <p><b>Описание: Не достаточно прав для удаления пользователя из системы<br>
     * Возвращает HTTP 403 Forbidden</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 403
     */

    @ExceptionHandler(UserDeleteForbiddenException.class)
    public ResponseEntity<ErrorResponse> userDeleteForbiddenHandler(UserDeleteForbiddenException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * <p><b>Ошибка: ExpiredJwtException</b></p>
     * <p><b>Описание: У JWT истек срок жизни<br>
     * Возвращает HTTP 401 Unauthorized</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 401
     */

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> expireJwtHandler(ExpiredJwtException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * <p><b>Ошибка: SignatureException</b></p>
     * <p><b>Описание: Ошибка в цифровой подписи JWT<br>
     * Возвращает HTTP 401 Unauthorized</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 401
     */

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ErrorResponse> signatureHandler(SignatureException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * <p><b>Ошибка: IllegalArgumentException</b></p>
     * <p><b>Описание: В данном случае возникает если JWT {@code null/isEmpty} <br>
     * Возвращает HTTP 401 Unauthorized</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 401
     */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> illegalArgumentHandler(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * <p><b>Ошибка: MalformedJwtException</b></p>
     * <p><b>Описание: Возникает в результате использования сломанного JWT<br>
     * Возвращает HTTP 401 Unauthorized</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 401
     */

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ErrorResponse> malformedJwtHandler(MalformedJwtException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * <p><b>Ошибка: NotCoincidencePasswordException</b></p>
     * <p><b>Описание: Возникает в результате не совпадение пары паролей<br>
     * Возвращает HTTP 400 Bad request</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 400
     */

    @ExceptionHandler(NotCoincidencePasswordException.class)
    public ResponseEntity<ErrorResponse> notCoincidencePasswordHandler(NotCoincidencePasswordException ex){
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * <p><b>Ошибка: InvalidPasswordException</b></p>
     * <p><b>Описание: Возникает в результате неверного введенного пароля<br>
     * Возвращает HTTP 400 Bad request</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 400
     */

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> invalidPasswordHandler(InvalidPasswordException ex){
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * <p><b>Ошибка: PasswordEncryptedKafkaException</b></p>
     * <p><b>Описание: Возникает в результате неудачного<br>
     * шифрования пароля для записи его в payload kafka <br>
     * Возвращает HTTP 500 Internal_server_error</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 500
     */
    @ExceptionHandler(PasswordEncryptedKafkaException.class)
    public ResponseEntity<ErrorResponse> passwordEncryptedKafkaHandler(PasswordEncryptedKafkaException ex){
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * <p><b>Ошибка: DuplicatePhoneException</b></p>
     * <p><b>Описание: Возникает в результате использования <br>
     * дубликата Email при регистрации<br>
     * Возвращает HTTP 409 Conflict</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 409
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> duplicateEmailHandler(DuplicateEmailException ex){
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT.value()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * <p><b>Ошибка: DuplicatePhoneException</b></p>
     * <p><b>Описание: Возникает в результате использования <br>
     * дубликата PhoneNumber при регистрации<br>
     * Возвращает HTTP 409 Conflict</b></p>
     *
     * @param ex информация об ошибке
     * @return ответ с кодом 409
     */
    @ExceptionHandler(DuplicatePhoneException.class)
    public ResponseEntity<ErrorResponse> duplicatePhoneHandler(DuplicatePhoneException ex){
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT.value()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

}
