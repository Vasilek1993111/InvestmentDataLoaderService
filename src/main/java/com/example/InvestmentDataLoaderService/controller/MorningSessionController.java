package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
import com.example.InvestmentDataLoaderService.repository.SystemLogRepository;
import com.example.InvestmentDataLoaderService.service.MorningSessionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Контроллер для работы с ценами утренней сессии
 * Управляет загрузкой цен утренней торговой сессии
 */
@RestController
@RequestMapping("/api/morning-session")
public class MorningSessionController {

    private final MorningSessionService morningSessionService;
    private final SystemLogRepository systemLogRepository;

    public MorningSessionController(MorningSessionService morningSessionService, SystemLogRepository systemLogRepository) {
        this.morningSessionService = morningSessionService;
        this.systemLogRepository = systemLogRepository;
    }

    /**
     * Асинхронная загрузка цен утренней сессии за сегодня
     */
    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> loadMorningSessionPricesToday() {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/morning-session";
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки цен утренней сессии за сегодня");
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== АСИНХРОННАЯ ЗАГРУЗКА ЦЕН УТРЕННЕЙ СЕССИИ ЗА СЕГОДНЯ ===");
            System.out.println("Task ID: " + taskId);

            // Запускаем асинхронное сохранение
            morningSessionService.fetchAndStoreMorningSessionPricesTodayAsync(taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Асинхронная загрузка цен утренней сессии за сегодня запущена");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            System.err.println("Ошибка запуска асинхронной загрузки цен утренней сессии за сегодня: " + e.getMessage());
            e.printStackTrace();

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка запуска асинхронной загрузки цен утренней сессии за сегодня: " + e.getMessage());
            errorLog.setStartTime(startTime);
            errorLog.setEndTime(Instant.now());

            try {
                systemLogRepository.save(errorLog);
            } catch (Exception logException) {
                System.err.println("Ошибка сохранения лога ошибки: " + logException.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Ошибка запуска асинхронной загрузки цен утренней сессии за сегодня: " + e.getMessage(),
                "taskId", taskId,
                "error", "InternalServerError"
            ));
        }
    }

    /**
     * Предпросмотр цен открытия утренней сессии за сегодня (без сохранения в БД)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> previewMorningSessionPricesToday() {
        Map<String, Object> response = new HashMap<>();

        try {
            LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));

            SaveResponseDto result = morningSessionService.previewMorningSessionPricesForDate(today);

            // Статистика
            int totalRequested = result.getTotalRequested();
            int found = result.getSavedItems() != null ? result.getSavedItems().size() : 0;
            int notFound = totalRequested - found;

            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("dateUsed", today.toString());
            response.put("statistics", Map.of(
                "totalRequested", totalRequested,
                "found", found,
                "notFound", notFound
            ));
            response.put("items", result.getSavedItems());
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка предпросмотра цен утренней сессии: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Загрузка цен открытия по акциям и фьючерсам за дату
     */
    @PostMapping("/by-date/{date}")
    @Transactional
    public ResponseEntity<Map<String, Object>> loadOpenPricesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            SaveResponseDto result = morningSessionService.fetchAndStoreMorningSessionPricesForDate(date);

            // Статистика
            int totalRequested = result.getTotalRequested();
            int found = result.getNewItemsSaved() + result.getExistingItemsSkipped();
            int notFound = totalRequested - found;

            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("date", date.toString());
            response.put("statistics", Map.of(
                "totalRequested", totalRequested,
                "found", found,
                "notFound", notFound,
                "saved", result.getNewItemsSaved(),
                "skippedExisting", result.getExistingItemsSkipped()
            ));
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки цен открытия за дату: " + e.getMessage());
            response.put("date", date.toString());
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Поиск свечей за указанный день
     */
    @GetMapping("/by-date/{date}")
    public ResponseEntity<Map<String, Object>> getCandlesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            SaveResponseDto result = morningSessionService.previewMorningSessionPricesForDate(date);

            // Статистика
            int totalRequested = result.getTotalRequested();
            int found = result.getSavedItems() != null ? result.getSavedItems().size() : 0;
            int notFound = totalRequested - found;

            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("date", date.toString());
            response.put("statistics", Map.of(
                "totalRequested", totalRequested,
                "found", found,
                "notFound", notFound
            ));
            response.put("items", result.getSavedItems());
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка поиска свечей за дату: " + e.getMessage());
            response.put("date", date.toString());
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(500).body(response);
        }
    }

    // ========== МЕТОДЫ ДЛЯ АКЦИЙ ==========

    /**
     * Загрузка цен открытия по акциям за дату
     */
    @PostMapping("/shares/{date}")
    @Transactional
    public ResponseEntity<Map<String, Object>> loadSharesPricesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            SaveResponseDto result = morningSessionService.fetchAndStoreSharesPricesForDate(date);

            // Статистика
            int totalRequested = result.getTotalRequested();
            int found = result.getNewItemsSaved() + result.getExistingItemsSkipped();
            int notFound = totalRequested - found;

            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("date", date.toString());
            response.put("instrumentType", "shares");
            response.put("statistics", Map.of(
                "totalRequested", totalRequested,
                "found", found,
                "notFound", notFound,
                "saved", result.getNewItemsSaved(),
                "skippedExisting", result.getExistingItemsSkipped()
            ));
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки цен акций за дату: " + e.getMessage());
            response.put("date", date.toString());
            response.put("instrumentType", "shares");
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Поиск свечей акций за указанный день
     */
    @GetMapping("/shares/{date}")
    public ResponseEntity<Map<String, Object>> getSharesCandlesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            SaveResponseDto result = morningSessionService.previewSharesPricesForDate(date);

            // Статистика
            int totalRequested = result.getTotalRequested();
            int found = result.getSavedItems() != null ? result.getSavedItems().size() : 0;
            int notFound = totalRequested - found;

            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("date", date.toString());
            response.put("instrumentType", "shares");
            response.put("statistics", Map.of(
                "totalRequested", totalRequested,
                "found", found,
                "notFound", notFound
            ));
            response.put("items", result.getSavedItems());
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка поиска свечей акций за дату: " + e.getMessage());
            response.put("date", date.toString());
            response.put("instrumentType", "shares");
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(500).body(response);
        }
    }

    // ========== МЕТОДЫ ДЛЯ ФЬЮЧЕРСОВ ==========

    /**
     * Загрузка цен открытия по фьючерсам за дату
     */
    @PostMapping("/futures/{date}")
    @Transactional
    public ResponseEntity<Map<String, Object>> loadFuturesPricesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            SaveResponseDto result = morningSessionService.fetchAndStoreFuturesPricesForDate(date);

            // Статистика
            int totalRequested = result.getTotalRequested();
            int found = result.getNewItemsSaved() + result.getExistingItemsSkipped();
            int notFound = totalRequested - found;

            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("date", date.toString());
            response.put("instrumentType", "futures");
            response.put("statistics", Map.of(
                "totalRequested", totalRequested,
                "found", found,
                "notFound", notFound,
                "saved", result.getNewItemsSaved(),
                "skippedExisting", result.getExistingItemsSkipped()
            ));
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки цен фьючерсов за дату: " + e.getMessage());
            response.put("date", date.toString());
            response.put("instrumentType", "futures");
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Поиск свечей фьючерсов за указанный день
     */
    @GetMapping("/futures/{date}")
    public ResponseEntity<Map<String, Object>> getFuturesCandlesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            SaveResponseDto result = morningSessionService.previewFuturesPricesForDate(date);

            // Статистика
            int totalRequested = result.getTotalRequested();
            int found = result.getSavedItems() != null ? result.getSavedItems().size() : 0;
            int notFound = totalRequested - found;

            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("date", date.toString());
            response.put("instrumentType", "futures");
            response.put("statistics", Map.of(
                "totalRequested", totalRequested,
                "found", found,
                "notFound", notFound
            ));
            response.put("items", result.getSavedItems());
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка поиска свечей фьючерсов за дату: " + e.getMessage());
            response.put("date", date.toString());
            response.put("instrumentType", "futures");
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(500).body(response);
        }
    }

    // ========== МЕТОДЫ ПО FIGI ==========

    /**
     * Загрузка цены открытия по FIGI за дату
     */
    @PostMapping("/by-figi-date/{figi}/{date}")
    @Transactional
    public ResponseEntity<Map<String, Object>> loadPriceByFigiForDate(
            @PathVariable String figi,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Валидация FIGI
            if (figi == null || figi.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "FIGI не может быть пустым");
                response.put("figi", figi);
                response.put("date", date.toString());
                response.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.badRequest().body(response);
            }

            SaveResponseDto result = morningSessionService.fetchAndStorePriceByFigiForDate(figi.trim(), date);

            // Статистика
            int totalRequested = result.getTotalRequested();
            int found = result.getNewItemsSaved() + result.getExistingItemsSkipped();
            int notFound = totalRequested - found;

            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("figi", figi.trim());
            response.put("date", date.toString());
            response.put("statistics", Map.of(
                "totalRequested", totalRequested,
                "found", found,
                "notFound", notFound,
                "saved", result.getNewItemsSaved(),
                "skippedExisting", result.getExistingItemsSkipped()
            ));
            response.put("items", result.getSavedItems());
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки цены открытия для FIGI " + figi + ": " + e.getMessage());
            response.put("figi", figi);
            response.put("date", date.toString());
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Поиск цены открытия по FIGI за дату
     */
    @GetMapping("/by-figi-date/{figi}/{date}")
    public ResponseEntity<Map<String, Object>> getPriceByFigiForDate(
            @PathVariable String figi,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Валидация FIGI
            if (figi == null || figi.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "FIGI не может быть пустым");
                response.put("figi", figi);
                response.put("date", date.toString());
                response.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.badRequest().body(response);
            }

            SaveResponseDto result = morningSessionService.previewPriceByFigiForDate(figi.trim(), date);

            // Статистика
            int totalRequested = result.getTotalRequested();
            int found = result.getSavedItems() != null ? result.getSavedItems().size() : 0;
            int notFound = totalRequested - found;

            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("figi", figi.trim());
            response.put("date", date.toString());
            response.put("statistics", Map.of(
                "totalRequested", totalRequested,
                "found", found,
                "notFound", notFound
            ));
            response.put("items", result.getSavedItems());
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка поиска цены открытия для FIGI " + figi + ": " + e.getMessage());
            response.put("figi", figi);
            response.put("date", date.toString());
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(500).body(response);
        }
    }


}
