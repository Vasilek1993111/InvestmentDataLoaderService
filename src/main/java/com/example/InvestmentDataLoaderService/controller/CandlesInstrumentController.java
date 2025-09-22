package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.repository.*;
import com.example.InvestmentDataLoaderService.service.*;
import com.example.InvestmentDataLoaderService.util.MinuteCandleMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * Контроллер для работы с свечами конкретных инструментов
 */
@RestController
@RequestMapping("/api/candles/instrument")
public class CandlesInstrumentController {

    private final MinuteCandleService minuteCandleService;
    private final DailyCandleService dailyCandleService;
    private final MarketDataService marketDataService;
    private final SystemLogRepository systemLogRepository;

    public CandlesInstrumentController(
            MinuteCandleService minuteCandleService,
            DailyCandleService dailyCandleService,
            MarketDataService marketDataService,
            SystemLogRepository systemLogRepository
    ) {
        this.minuteCandleService = minuteCandleService;
        this.dailyCandleService = dailyCandleService;
        this.marketDataService = marketDataService;
        this.systemLogRepository = systemLogRepository;
    }

    // ==================== МИНУТНЫЕ СВЕЧИ ====================

    /**
     * Получение минутных свечей конкретного инструмента за дату без сохранения
     */
    @GetMapping("/minute/{figi}/{date}")
    public ResponseEntity<Map<String, Object>> getInstrumentMinuteCandlesForDate(
            @PathVariable String figi,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Генерируем уникальный ID задачи для логирования
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/instrument/minute/" + figi + "/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы метода
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("GET");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало получения минутных свечей инструмента " + figi + " за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== ПОЛУЧЕНИЕ МИНУТНЫХ СВЕЧЕЙ ИНСТРУМЕНТА ===");
            System.out.println("FIGI: " + figi);
            System.out.println("Дата: " + date);

            // Получаем минутные свечи из API
            var candles = marketDataService.getCandles(figi, date, "CANDLE_INTERVAL_1_MIN");

            if (candles == null || candles.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("figi", figi);
                response.put("date", date);
                response.put("candles", new ArrayList<>());
                response.put("totalCandles", 0);
                response.put("message", "Нет данных для инструмента " + figi + " за дату " + date);

                return ResponseEntity.ok(response);
            }

            List<MinuteCandleExtendedDto> allCandles = new ArrayList<>();
            int totalCandles = 0;

            for (var candle : candles) {
                // Используем маппер для создания расширенного DTO
                MinuteCandleExtendedDto extendedCandle = MinuteCandleMapper.toExtendedDto(candle);
                allCandles.add(extendedCandle);
                totalCandles++;
            }

            // Вычисляем общую статистику
            Map<String, Object> response = new HashMap<>();
            response.put("figi", figi);
            response.put("date", date);
            response.put("candles", allCandles);
            response.put("totalCandles", totalCandles);

            // Добавляем статистику по объемам и ценам
            if (!allCandles.isEmpty()) {
                BigDecimal totalVolume = allCandles.stream()
                    .map(candle -> {
                        Long volume = candle.volume();
                        if (volume != null) {
                            return BigDecimal.valueOf(volume);
                        } else {
                            return BigDecimal.ZERO;
                        }
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal avgPrice = allCandles.stream()
                    .map(candle -> candle.averagePrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(allCandles.size()), 2, RoundingMode.HALF_UP);

                response.put("totalVolume", totalVolume);
                response.put("averagePrice", avgPrice);
            }

            System.out.println("=== ЗАВЕРШЕНИЕ ПОЛУЧЕНИЯ МИНУТНЫХ СВЕЧЕЙ ИНСТРУМЕНТА ===");
            System.out.println("FIGI: " + figi);
            System.out.println("Всего свечей: " + totalCandles);

            // Логируем успешное завершение
            SystemLogEntity resultLog = new SystemLogEntity();
            resultLog.setTaskId(taskId);
            resultLog.setEndpoint(endpoint);
            resultLog.setMethod("GET");
            resultLog.setStatus("COMPLETED");
            resultLog.setMessage("Получение минутных свечей инструмента " + figi + " завершено успешно за " + date + 
                ": Всего свечей=" + totalCandles);
            resultLog.setStartTime(startTime);
            resultLog.setEndTime(Instant.now());
            resultLog.setDurationMs(Instant.now().toEpochMilli() - startTime.toEpochMilli());

            try {
                systemLogRepository.save(resultLog);
                System.out.println("Лог завершения работы сохранен для taskId: " + taskId);
            } catch (Exception logException) {
                System.err.println("Ошибка сохранения лога завершения работы: " + logException.getMessage());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Ошибка получения минутных свечей инструмента: " + e.getMessage());
            e.printStackTrace();

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("GET");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка получения минутных свечей инструмента " + figi + " за " + date + ": " + e.getMessage());
            errorLog.setStartTime(startTime);
            errorLog.setEndTime(Instant.now());
            errorLog.setDurationMs(Instant.now().toEpochMilli() - startTime.toEpochMilli());

            try {
                systemLogRepository.save(errorLog);
                System.out.println("Лог ошибки сохранен для taskId: " + taskId);
            } catch (Exception logException) {
                System.err.println("Ошибка сохранения лога ошибки: " + logException.getMessage());
            }

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка получения минутных свечей инструмента: " + e.getMessage());
            errorResponse.put("figi", figi);
            errorResponse.put("date", date);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Асинхронное сохранение минутных свечей конкретного инструмента за дату
     */
    @PostMapping("/minute/{figi}/{date}")
    public ResponseEntity<Map<String, Object>> saveInstrumentMinuteCandlesForDateAsync(
            @PathVariable String figi,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/instrument/minute/" + figi + "/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронного сохранения минутных свечей инструмента " + figi + " за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== АСИНХРОННОЕ СОХРАНЕНИЕ МИНУТНЫХ СВЕЧЕЙ ИНСТРУМЕНТА ===");
            System.out.println("FIGI: " + figi);
            System.out.println("Дата: " + date);
            System.out.println("Task ID: " + taskId);

            // Создаем запрос для загрузки минутных свечей
            MinuteCandleRequestDto request = new MinuteCandleRequestDto();
            request.setInstruments(List.of(figi));
            request.setAssetType(List.of("INSTRUMENT"));
            request.setDate(date);

            // Запускаем параллельную загрузку
            minuteCandleService.saveMinuteCandlesAsync(request, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Сохранение минутных свечей инструмента " + figi + " за " + date + " запущено");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("figi", figi);
            response.put("date", date.toString());
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            System.err.println("Ошибка запуска асинхронного сохранения минутных свечей инструмента: " + e.getMessage());
            e.printStackTrace();

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("ERROR");
            errorLog.setMessage("Ошибка запуска асинхронного сохранения: " + e.getMessage());
            errorLog.setStartTime(startTime);
            errorLog.setEndTime(Instant.now());
            errorLog.setDurationMs(Instant.now().toEpochMilli() - startTime.toEpochMilli());

            try {
                systemLogRepository.save(errorLog);
            } catch (Exception logException) {
                System.err.println("Ошибка сохранения лога ошибки: " + logException.getMessage());
            }

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка запуска асинхронного сохранения: " + e.getMessage());
            errorResponse.put("taskId", taskId);
            errorResponse.put("figi", figi);
            errorResponse.put("date", date.toString());
            errorResponse.put("status", "ERROR");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== ДНЕВНЫЕ СВЕЧИ ====================

    /**
     * Получение дневных свечей конкретного инструмента за дату без сохранения
     */
    @GetMapping("/daily/{figi}/{date}")
    public ResponseEntity<Map<String, Object>> getInstrumentDailyCandlesForDate(
            @PathVariable String figi,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Генерируем уникальный ID задачи для логирования
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/instrument/daily/" + figi + "/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы метода
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("GET");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало получения дневных свечей инструмента " + figi + " за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== ПОЛУЧЕНИЕ ДНЕВНЫХ СВЕЧЕЙ ИНСТРУМЕНТА ===");
            System.out.println("FIGI: " + figi);
            System.out.println("Дата: " + date);

            // Получаем дневные свечи из API
            var candles = marketDataService.getCandles(figi, date, "CANDLE_INTERVAL_DAY");

            if (candles == null || candles.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("figi", figi);
                response.put("date", date);
                response.put("candles", new ArrayList<>());
                response.put("totalCandles", 0);
                response.put("message", "Нет данных для инструмента " + figi + " за дату " + date);

                return ResponseEntity.ok(response);
            }

            List<DailyCandleExtendedDto> allCandles = new ArrayList<>();
            int totalCandles = 0;

            for (var candle : candles) {
                // Создаем расширенный DTO для каждой свечи
                DailyCandleExtendedDto extendedCandle = DailyCandleExtendedDto.fromBasicData(
                    figi,
                    null, // ticker - будет определен по FIGI
                    null, // name - будет определен по FIGI
                    candle.time(),
                    candle.open(),
                    candle.close(),
                    candle.high(),
                    candle.low(),
                    candle.volume(),
                    candle.isComplete()
                );
                
                allCandles.add(extendedCandle);
                totalCandles++;
            }

            // Вычисляем общую статистику
            Map<String, Object> response = new HashMap<>();
            response.put("figi", figi);
            response.put("date", date);
            response.put("candles", allCandles);
            response.put("totalCandles", totalCandles);

            // Добавляем статистику по объемам и ценам
            if (!allCandles.isEmpty()) {
                BigDecimal totalVolume = allCandles.stream()
                    .map(candle -> {
                        Long volume = candle.volume();
                        if (volume != null) {
                            return BigDecimal.valueOf(volume);
                        } else {
                            return BigDecimal.ZERO;
                        }
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal avgPrice = allCandles.stream()
                    .map(candle -> candle.averagePrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(allCandles.size()), 2, RoundingMode.HALF_UP);

                response.put("totalVolume", totalVolume);
                response.put("averagePrice", avgPrice);
            }

            System.out.println("=== ЗАВЕРШЕНИЕ ПОЛУЧЕНИЯ ДНЕВНЫХ СВЕЧЕЙ ИНСТРУМЕНТА ===");
            System.out.println("FIGI: " + figi);
            System.out.println("Всего свечей: " + totalCandles);

            // Логируем успешное завершение
            SystemLogEntity resultLog = new SystemLogEntity();
            resultLog.setTaskId(taskId);
            resultLog.setEndpoint(endpoint);
            resultLog.setMethod("GET");
            resultLog.setStatus("COMPLETED");
            resultLog.setMessage("Получение дневных свечей инструмента " + figi + " завершено успешно за " + date + 
                ": Всего свечей=" + totalCandles);
            resultLog.setStartTime(startTime);
            resultLog.setEndTime(Instant.now());
            resultLog.setDurationMs(Instant.now().toEpochMilli() - startTime.toEpochMilli());

            try {
                systemLogRepository.save(resultLog);
                System.out.println("Лог завершения работы сохранен для taskId: " + taskId);
            } catch (Exception logException) {
                System.err.println("Ошибка сохранения лога завершения работы: " + logException.getMessage());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Ошибка получения дневных свечей инструмента: " + e.getMessage());
            e.printStackTrace();

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("GET");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка получения дневных свечей инструмента " + figi + " за " + date + ": " + e.getMessage());
            errorLog.setStartTime(startTime);
            errorLog.setEndTime(Instant.now());
            errorLog.setDurationMs(Instant.now().toEpochMilli() - startTime.toEpochMilli());

            try {
                systemLogRepository.save(errorLog);
                System.out.println("Лог ошибки сохранен для taskId: " + taskId);
            } catch (Exception logException) {
                System.err.println("Ошибка сохранения лога ошибки: " + logException.getMessage());
            }

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка получения дневных свечей инструмента: " + e.getMessage());
            errorResponse.put("figi", figi);
            errorResponse.put("date", date);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Асинхронное сохранение дневных свечей конкретного инструмента за дату
     */
    @PostMapping("/daily/{figi}/{date}")
    public ResponseEntity<Map<String, Object>> saveInstrumentDailyCandlesForDateAsync(
            @PathVariable String figi,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/instrument/daily/" + figi + "/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронного сохранения дневных свечей инструмента " + figi + " за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== АСИНХРОННОЕ СОХРАНЕНИЕ ДНЕВНЫХ СВЕЧЕЙ ИНСТРУМЕНТА ===");
            System.out.println("FIGI: " + figi);
            System.out.println("Дата: " + date);
            System.out.println("Task ID: " + taskId);

            // Создаем запрос для загрузки дневных свечей
            DailyCandleRequestDto request = new DailyCandleRequestDto();
            request.setInstruments(List.of(figi));
            request.setAssetType(List.of("INSTRUMENT"));
            request.setDate(date);

            // Запускаем загрузку
            dailyCandleService.saveDailyCandlesAsync(request, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Сохранение дневных свечей инструмента " + figi + " за " + date + " запущено");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("figi", figi);
            response.put("date", date.toString());
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            System.err.println("Ошибка запуска асинхронного сохранения дневных свечей инструмента: " + e.getMessage());
            e.printStackTrace();

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("ERROR");
            errorLog.setMessage("Ошибка запуска асинхронного сохранения: " + e.getMessage());
            errorLog.setStartTime(startTime);
            errorLog.setEndTime(Instant.now());
            errorLog.setDurationMs(Instant.now().toEpochMilli() - startTime.toEpochMilli());

            try {
                systemLogRepository.save(errorLog);
            } catch (Exception logException) {
                System.err.println("Ошибка сохранения лога ошибки: " + logException.getMessage());
            }

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка запуска асинхронного сохранения: " + e.getMessage());
            errorResponse.put("taskId", taskId);
            errorResponse.put("figi", figi);
            errorResponse.put("date", date.toString());
            errorResponse.put("status", "ERROR");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
