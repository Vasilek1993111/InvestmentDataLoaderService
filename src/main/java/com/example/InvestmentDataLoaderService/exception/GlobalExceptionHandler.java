package com.example.InvestmentDataLoaderService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для контроллеров
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 400 BAD REQUEST ОШИБКИ ====================

    /**
     * Обработка ошибок валидации данных (400)
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Ошибка валидации: " + ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", "ValidationException");
        response.put("field", ex.getField());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Обработка некорректного JSON в теле запроса (400)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Некорректный формат JSON в теле запроса: " + ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", "BadRequest");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Обработка отсутствующих обязательных параметров (400)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Отсутствует обязательный параметр: " + ex.getParameterName() + " (" + ex.getParameterType() + ")");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", "BadRequest");
        response.put("parameter", ex.getParameterName());
        response.put("type", ex.getParameterType());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Обработка неверного типа параметра (400)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Неверный тип параметра '" + ex.getName() + "': ожидается " + ex.getRequiredType().getSimpleName() + ", получен " + ex.getValue());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", "BadRequest");
        response.put("parameter", ex.getName());
        response.put("expectedType", ex.getRequiredType().getSimpleName());
        response.put("actualValue", ex.getValue());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Обработка ошибок валидации @Valid (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Ошибка валидации полей: " + ex.getBindingResult().getFieldErrors().toString());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", "ValidationException");
        response.put("fieldErrors", ex.getBindingResult().getFieldErrors());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Обработка неподдерживаемого HTTP метода (405 -> 400)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Неподдерживаемый HTTP метод: " + ex.getMethod() + ". Поддерживаемые методы: " + String.join(", ", ex.getSupportedMethods()));
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", "BadRequest");
        response.put("method", ex.getMethod());
        response.put("supportedMethods", ex.getSupportedMethods());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    

    /**
     * Обработка некорректных URL (400)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        // Проверяем, является ли это ошибкой URL
        if (ex.getMessage() != null && ex.getMessage().contains("No static resource")) {
            response.put("success", false);
            response.put("message", "Некорректный URL: " + request.getDescription(false));
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("error", "BadRequest");
            response.put("path", request.getDescription(false));
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        // Для других IllegalArgumentException возвращаем 500
        response.put("success", false);
        response.put("message", "Некорректный аргумент: " + ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", "IllegalArgumentException");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ==================== 404 NOT FOUND ОШИБКИ ====================

    /**
     * Обработка ошибок 404 - ресурс не найден
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(NoHandlerFoundException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Эндпоинт не найден: " + ex.getRequestURL());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", "NotFound");
        response.put("path", ex.getRequestURL());
        response.put("method", ex.getHttpMethod());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Обработка ошибок 404 - статический ресурс не найден
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Статический ресурс не найден: " + ex.getResourcePath());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", "NotFound");
        response.put("path", ex.getResourcePath());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Обработка ошибок 404 - инструменты не найдены
     */
    @ExceptionHandler(com.example.InvestmentDataLoaderService.exception.InstrumentsNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleInstrumentsNotFoundException(com.example.InvestmentDataLoaderService.exception.InstrumentsNotFoundException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", "InstrumentsNotFound");
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ==================== 500 INTERNAL SERVER ERROR ====================

    /**
     * Обработка исключений загрузки данных (500)
     */
    @ExceptionHandler(DataLoadException.class)
    public ResponseEntity<Map<String, Object>> handleDataLoadException(DataLoadException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Ошибка загрузки данных: " + ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", "DataLoadException");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Обработка исключений планировщика (500)
     */
    @ExceptionHandler(SchedulerException.class)
    public ResponseEntity<Map<String, Object>> handleSchedulerException(SchedulerException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Ошибка планировщика: " + ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", "SchedulerException");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Обработка общих API исключений (500)
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "API ошибка: " + ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", "ApiException");
        
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    /**
     * Обработка общих RuntimeException (500)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Внутренняя ошибка сервера: " + ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", "RuntimeException");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Обработка всех остальных исключений (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Неожиданная ошибка: " + ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", "Exception");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
