package com.example.InvestmentDataLoaderService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для контроллеров
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработка общих RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Внутренняя ошибка сервера: " + ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        response.put("error", "RuntimeException");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Обработка исключений валидации данных
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Ошибка валидации: " + ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        response.put("error", "ValidationException");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Обработка исключений загрузки данных
     */
    @ExceptionHandler(DataLoadException.class)
    public ResponseEntity<Map<String, Object>> handleDataLoadException(DataLoadException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Ошибка загрузки данных: " + ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        response.put("error", "DataLoadException");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Обработка исключений планировщика
     */
    @ExceptionHandler(SchedulerException.class)
    public ResponseEntity<Map<String, Object>> handleSchedulerException(SchedulerException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Ошибка планировщика: " + ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        response.put("error", "SchedulerException");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Обработка общих API исключений
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "API ошибка: " + ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        response.put("error", "ApiException");
        
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    /**
     * Обработка всех остальных исключений
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Неожиданная ошибка: " + ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        response.put("error", "Exception");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
