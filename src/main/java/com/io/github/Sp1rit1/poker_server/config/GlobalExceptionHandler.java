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

@ControllerAdvice // Помечает класс как глобальный обработчик исключений для контроллеров
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

        // Определяем HTTP статус в зависимости от типа вашего исключения или сообщения
        // Для простоты, сейчас все RuntimeException будут 400 или 500.
        // В реальном приложении вы бы создали свои кастомные исключения и маппили их на разные статусы.
        HttpStatus status = HttpStatus.BAD_REQUEST; // По умолчанию для бизнес-логики ошибок, которые являются виной клиента
        String errorType = "Bad Request";

        // Пример: если это ошибка, которую вы не ожидали (не бизнес-логика)
        // if (!(ex instanceof YourBusinessLogicException)) { // Замените YourBusinessLogicException на ваш базовый класс бизнес-исключений
        //     status = HttpStatus.INTERNAL_SERVER_ERROR;
        //     errorType = "Internal Server Error";
        // }

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
