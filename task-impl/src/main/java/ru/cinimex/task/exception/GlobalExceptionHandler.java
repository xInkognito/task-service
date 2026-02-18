package ru.cinimex.task.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // Вспомогательный метод для создания JSON-ответа
    private ResponseEntity<Map<String, String>> createErrorResponse(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(Map.of("message", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleInternal(Exception ex) {
        log.error("Unhandled exception: ", ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера: " + ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        String msg = String.format("Метод %s не поддерживается для этого адреса", ex.getMethod());
        return createErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, msg);
    }

    // Обработка ошибок доступа (@PreAuthorize)
    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<Map<String, String>> handleAccessDenied(Exception ex) {
        return createErrorResponse(HttpStatus.FORBIDDEN, "Недостаточно прав для выполнения этой операции");
    }

    // Ошибка несоответствия типов (например, неверный формат даты в параметрах)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String msg = String.format("Неверный формат параметра '%s'", ex.getName());
        return createErrorResponse(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(Exception ex) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Ошибка аутентификации: пользователь не авторизован");
    }
}