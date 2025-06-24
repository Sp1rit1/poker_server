package com.io.github.Sp1rit1.poker_server.config;

import org.slf4j.Logger; // Рекомендуется для логирования
import org.slf4j.LoggerFactory; // Рекомендуется для логирования
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Опционально, но рекомендуется: добавьте логгер
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Обработчик для ошибок валидации DTO (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        logger.warn("Validation error for request {}: {}", request.getDescription(false), ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request"); // Стандартное сообщение для 400
        body.put("message", "Validation failed. Check 'fieldErrors' for details."); // Общее сообщение

        // Собираем детальные сообщения об ошибках полей
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        body.put("fieldErrors", fieldErrors); // Детальные ошибки по полям

        // Получаем путь запроса
        String path = request.getDescription(false);
        if (path.startsWith("uri=")) {
            path = path.substring(4);
        }
        body.put("path", path);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Обработчик для ваших кастомных RuntimeException, которые вы бросаете в сервисах
    // (например, "User not found", "Already friends", etc.)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleCustomRuntimeExceptions(
            RuntimeException ex, WebRequest request) {

        // Логируем исключение (важно для отладки)
        logger.error("Runtime exception for request {}: {}", request.getDescription(false), ex.getMessage(), ex);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());


        HttpStatus status = HttpStatus.BAD_REQUEST; // По умолчанию для бизнес-логики ошибок, которые являются виной клиента
        String errorType = "Bad Request";



        body.put("status", status.value());
        body.put("error", errorType);
        body.put("message", ex.getMessage()); // Сообщение из вашего RuntimeException

        String path = request.getDescription(false);
        if (path.startsWith("uri=")) {
            path = path.substring(4);
        }
        body.put("path", path);

        return new ResponseEntity<>(body, status);
    }
}
