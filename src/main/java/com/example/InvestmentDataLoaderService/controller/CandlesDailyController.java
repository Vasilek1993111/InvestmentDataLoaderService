package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.repository.*;
import com.example.InvestmentDataLoaderService.service.DailyCandleService;
import com.example.InvestmentDataLoaderService.client.TinkoffApiClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;


/**
 * Контроллер для работы с дневными свечами
 */
@RestController
@RequestMapping("/api/candles/daily")
public class CandlesDailyController {

    private final DailyCandleService dailyCandleService;
    private final TinkoffApiClient tinkoffApiClient;
    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final IndicativeRepository indicativeRepository;
    private final SystemLogRepository systemLogRepository;

    public CandlesDailyController(
            DailyCandleService dailyCandleService,
            TinkoffApiClient tinkoffApiClient,
            ShareRepository shareRepository,
            FutureRepository futureRepository,
            IndicativeRepository indicativeRepository,
            SystemLogRepository systemLogRepository
    ) {
        this.dailyCandleService = dailyCandleService;
        this.tinkoffApiClient = tinkoffApiClient;
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.indicativeRepository = indicativeRepository;
        this.systemLogRepository = systemLogRepository;
    }

    // ==================== ОБЩИЕ ДНЕВНЫЕ СВЕЧИ ====================

    /**
     * Асинхронная загрузка дневных свечей за сегодня
     */
    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> loadDailyCandlesTodayAsync(@RequestBody(required = false) DailyCandleRequestDto request) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/daily";
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки дневных свечей за сегодня");
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== АСИНХРОННАЯ ЗАГРУЗКА ДНЕВНЫХ СВЕЧЕЙ ЗА СЕГОДНЯ ===");
            System.out.println("Инструменты: " + request.getInstruments());
            System.out.println("Типы активов: " + request.getAssetType());
            System.out.println("Task ID: " + taskId);

            // Запускаем загрузку
            dailyCandleService.saveDailyCandlesAsync(request, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Загрузка дневных свечей за сегодня запущена");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("instruments", request.getInstruments());
            response.put("assetTypes", request.getAssetType());
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            System.err.println("Ошибка запуска асинхронной загрузки дневных свечей: " + e.getMessage());
            e.printStackTrace();

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("ERROR");
            errorLog.setMessage("Ошибка запуска асинхронной загрузки: " + e.getMessage());
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
            errorResponse.put("message", "Ошибка запуска асинхронной загрузки: " + e.getMessage());
            errorResponse.put("taskId", taskId);
            errorResponse.put("status", "ERROR");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Асинхронная загрузка дневных свечей за конкретную дату
     */
    @PostMapping("/{date}")
    @Transactional
    public ResponseEntity<Map<String, Object>> loadDailyCandlesForDateAsync(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody(required = false) DailyCandleRequestDto request
    ) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/daily/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки дневных свечей за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== АСИНХРОННАЯ ЗАГРУЗКА ДНЕВНЫХ СВЕЧЕЙ ЗА ДАТУ ===");
            System.out.println("Дата: " + date);
            System.out.println("Инструменты: " + request.getInstruments());
            System.out.println("Типы активов: " + request.getAssetType());
            System.out.println("Task ID: " + taskId);

            // Запускаем загрузку
            dailyCandleService.saveDailyCandlesAsync(request, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Загрузка дневных свечей за " + date + " запущена");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("date", date.toString());
            response.put("instruments", request.getInstruments());
            response.put("assetTypes", request.getAssetType());
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            System.err.println("Ошибка запуска асинхронной загрузки дневных свечей за дату: " + e.getMessage());
            e.printStackTrace();

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("ERROR");
            errorLog.setMessage("Ошибка запуска асинхронной загрузки: " + e.getMessage());
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
            errorResponse.put("message", "Ошибка запуска асинхронной загрузки: " + e.getMessage());
            errorResponse.put("taskId", taskId);
            errorResponse.put("status", "ERROR");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== АКЦИИ ====================

    /**
     * Получение дневных свечей акций за дату без сохранения
     */
    @GetMapping("/shares/{date}")
    public ResponseEntity<Map<String, Object>> getSharesDailyCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Генерируем уникальный ID задачи для логирования
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/daily/shares/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы метода
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("GET");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало получения дневных свечей акций за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== ПОЛУЧЕНИЕ ДНЕВНЫХ СВЕЧЕЙ АКЦИЙ ===");
            System.out.println("Дата: " + date);

            // Получаем все акции из БД
            List<ShareEntity> shares = shareRepository.findAll();
            System.out.println("Найдено акций: " + shares.size());

            List<DailyCandleExtendedDto> allCandles = new ArrayList<>();
            int totalCandles = 0;
            int processedInstruments = 0;
            int successfulInstruments = 0;
            int noDataInstruments = 0;
            int errorInstruments = 0;

            for (ShareEntity share : shares) {
                processedInstruments++;
                try {
                    System.out.println("Получаем свечи для акции: " + share.getTicker() + " (" + share.getFigi() + ")");

                    // Получаем дневные свечи из API
                    var candles = tinkoffApiClient.getCandles(share.getFigi(), date, "CANDLE_INTERVAL_DAY");

                    if (candles != null && !candles.isEmpty()) {
                        System.out.println("Получено " + candles.size() + " свечей для " + share.getTicker());
                        successfulInstruments++;

                        // Логируем успешное получение данных для каждого FIGI
                        SystemLogEntity figiLog = new SystemLogEntity();
                        figiLog.setTaskId(taskId);
                        figiLog.setEndpoint(endpoint);
                        figiLog.setMethod("GET");
                        figiLog.setStatus("SUCCESS");
                        figiLog.setMessage("Успешно получено " + candles.size() + " дневных свечей для акции " + share.getTicker() + " (FIGI: " + share.getFigi() + ") за дату " + date);
                        figiLog.setStartTime(Instant.now());
                        figiLog.setEndTime(Instant.now());
                        figiLog.setDurationMs(0L);

                        try {
                            systemLogRepository.save(figiLog);
                            System.out.println("Лог успешного получения данных для FIGI " + share.getFigi() + " сохранен");
                        } catch (Exception logException) {
                            System.err.println("Ошибка сохранения лога для FIGI " + share.getFigi() + ": " + logException.getMessage());
                        }

                        for (var candle : candles) {
                            // Создаем расширенный DTO для каждой свечи
                            DailyCandleExtendedDto extendedCandle = DailyCandleExtendedDto.fromBasicData(
                                share.getFigi(),
                                share.getTicker(),
                                share.getName(),
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
                    } else {
                        System.out.println("Нет данных для акции: " + share.getTicker());
                        noDataInstruments++;

                        // Логируем отсутствие данных для FIGI
                        SystemLogEntity noDataLog = new SystemLogEntity();
                        noDataLog.setTaskId(taskId);
                        noDataLog.setEndpoint(endpoint);
                        noDataLog.setMethod("GET");
                        noDataLog.setStatus("NO_DATA");
                        noDataLog.setMessage("Нет дневных свечей для акции " + share.getTicker() + " (FIGI: " + share.getFigi() + ") за дату " + date);
                        noDataLog.setStartTime(Instant.now());
                        noDataLog.setEndTime(Instant.now());
                        noDataLog.setDurationMs(0L);

                        try {
                            systemLogRepository.save(noDataLog);
                            System.out.println("Лог отсутствия данных для FIGI " + share.getFigi() + " сохранен");
                        } catch (Exception logException) {
                            System.err.println("Ошибка сохранения лога отсутствия данных для FIGI " + share.getFigi() + ": " + logException.getMessage());
                        }
                    }

                } catch (Exception e) {
                    System.err.println("Ошибка получения свечей для акции " + share.getTicker() + ": " + e.getMessage());
                    errorInstruments++;

                    // Логируем ошибку для FIGI
                    SystemLogEntity errorLog = new SystemLogEntity();
                    errorLog.setTaskId(taskId);
                    errorLog.setEndpoint(endpoint);
                    errorLog.setMethod("GET");
                    errorLog.setStatus("ERROR");
                    errorLog.setMessage("Ошибка получения дневных свечей для акции " + share.getTicker() + " (FIGI: " + share.getFigi() + ") за дату " + date + ": " + e.getMessage());
                    errorLog.setStartTime(Instant.now());
                    errorLog.setEndTime(Instant.now());
                    errorLog.setDurationMs(0L);

                    try {
                        systemLogRepository.save(errorLog);
                        System.out.println("Лог ошибки для FIGI " + share.getFigi() + " сохранен");
                    } catch (Exception logException) {
                        System.err.println("Ошибка сохранения лога ошибки для FIGI " + share.getFigi() + ": " + logException.getMessage());
                    }
                }
            }

            // Вычисляем общую статистику
            Map<String, Object> response = new HashMap<>();
            response.put("date", date.toString());
            response.put("assetType", "SHARES");
            response.put("candles", allCandles);
            response.put("totalCandles", totalCandles);
            response.put("totalInstruments", shares.size());
            response.put("processedInstruments", processedInstruments);
            response.put("successfulInstruments", successfulInstruments);
            response.put("noDataInstruments", noDataInstruments);
            response.put("errorInstruments", errorInstruments);

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

            System.out.println("=== ЗАВЕРШЕНИЕ ПОЛУЧЕНИЯ ДНЕВНЫХ СВЕЧЕЙ АКЦИЙ ===");
            System.out.println("Всего инструментов: " + shares.size());
            System.out.println("Всего свечей: " + totalCandles);

            // Логируем успешное завершение
            SystemLogEntity resultLog = new SystemLogEntity();
            resultLog.setTaskId(taskId);
            resultLog.setEndpoint(endpoint);
            resultLog.setMethod("GET");
            resultLog.setStatus("COMPLETED");
            resultLog.setMessage("Получение дневных свечей акций завершено успешно за " + date + 
                ": Всего инструментов=" + shares.size() + 
                ", Обработано=" + processedInstruments + 
                ", Успешно=" + successfulInstruments + 
                ", Без данных=" + noDataInstruments + 
                ", С ошибками=" + errorInstruments + 
                ", Всего свечей=" + totalCandles);
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
            System.err.println("Ошибка получения дневных свечей акций: " + e.getMessage());
            e.printStackTrace();

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("GET");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка получения дневных свечей акций за " + date + ": " + e.getMessage());
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
            errorResponse.put("error", "Ошибка получения дневных свечей акций: " + e.getMessage());
            errorResponse.put("date", date.toString());
            errorResponse.put("assetType", "SHARES");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Асинхронная загрузка дневных свечей акций за дату
     */
    @PostMapping("/shares/{date}")
    @Transactional
    public ResponseEntity<Map<String, Object>> loadSharesDailyCandlesForDateAsync(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/daily/shares/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки дневных свечей акций за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== АСИНХРОННАЯ ЗАГРУЗКА ДНЕВНЫХ СВЕЧЕЙ АКЦИЙ ЗА ДАТУ ===");
            System.out.println("Дата: " + date);
            System.out.println("Task ID: " + taskId);

            // Получаем все акции из БД
            List<ShareEntity> shares = shareRepository.findAll();
            System.out.println("Найдено акций: " + shares.size());

            // Создаем запрос для загрузки всех дневных свечей акций
            DailyCandleRequestDto request = new DailyCandleRequestDto();
            request.setInstruments(shares.stream().map(ShareEntity::getFigi).toList());
            request.setAssetType(List.of("SHARES"));
            request.setDate(date);

            // Запускаем загрузку
            dailyCandleService.saveDailyCandlesAsync(request, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Загрузка дневных свечей акций запущена");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("date", date.toString());
            response.put("instrumentsCount", shares.size());
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            System.err.println("Ошибка запуска асинхронной загрузки дневных свечей акций: " + e.getMessage());
            e.printStackTrace();

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("ERROR");
            errorLog.setMessage("Ошибка запуска асинхронной загрузки: " + e.getMessage());
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
            errorResponse.put("message", "Ошибка запуска асинхронной загрузки: " + e.getMessage());
            errorResponse.put("taskId", taskId);
            errorResponse.put("status", "ERROR");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== ФЬЮЧЕРСЫ ====================

    /**
     * Асинхронная загрузка дневных свечей фьючерсов за дату
     */
    @PostMapping("/futures/{date}")
    @Transactional
    public ResponseEntity<Map<String, Object>> loadFuturesDailyCandlesForDateAsync(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/daily/futures/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки дневных свечей фьючерсов за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== АСИНХРОННАЯ ЗАГРУЗКА ДНЕВНЫХ СВЕЧЕЙ ФЬЮЧЕРСОВ ЗА ДАТУ ===");
            System.out.println("Дата: " + date);
            System.out.println("Task ID: " + taskId);

            // Получаем все фьючерсы из БД
            List<FutureEntity> futures = futureRepository.findAll();
            System.out.println("Найдено фьючерсов: " + futures.size());

            // Создаем запрос для загрузки всех дневных свечей фьючерсов
            DailyCandleRequestDto request = new DailyCandleRequestDto();
            request.setInstruments(futures.stream().map(FutureEntity::getFigi).toList());
            request.setAssetType(List.of("FUTURES"));
            request.setDate(date);

            // Запускаем загрузку
            dailyCandleService.saveDailyCandlesAsync(request, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Загрузка дневных свечей фьючерсов за " + date + " запущена");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("date", date.toString());
            response.put("instrumentsCount", futures.size());
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            System.err.println("Ошибка запуска асинхронной загрузки дневных свечей фьючерсов: " + e.getMessage());
            e.printStackTrace();

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("ERROR");
            errorLog.setMessage("Ошибка запуска асинхронной загрузки: " + e.getMessage());
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
            errorResponse.put("message", "Ошибка запуска асинхронной загрузки: " + e.getMessage());
            errorResponse.put("taskId", taskId);
            errorResponse.put("status", "ERROR");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Получение дневных свечей фьючерсов за дату без сохранения
     */
    @GetMapping("/futures/{date}")
    public ResponseEntity<Map<String, Object>> getFuturesDailyCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Генерируем уникальный ID задачи для логирования
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/daily/futures/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы метода
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("GET");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало получения дневных свечей фьючерсов за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== ПОЛУЧЕНИЕ ДНЕВНЫХ СВЕЧЕЙ ФЬЮЧЕРСОВ ===");
            System.out.println("Дата: " + date);

            // Получаем все фьючерсы из БД
            List<FutureEntity> futures = futureRepository.findAll();
            System.out.println("Найдено фьючерсов: " + futures.size());

            List<DailyCandleExtendedDto> allCandles = new ArrayList<>();
            int totalCandles = 0;

            for (FutureEntity future : futures) {
                try {
                    System.out.println("Получаем свечи для фьючерса: " + future.getTicker() + " (" + future.getFigi() + ")");

                    // Получаем дневные свечи из API
                    var candles = tinkoffApiClient.getCandles(future.getFigi(), date, "CANDLE_INTERVAL_DAY");

                    if (candles != null && !candles.isEmpty()) {
                        System.out.println("Получено " + candles.size() + " свечей для " + future.getTicker());

                        for (var candle : candles) {
                            // Создаем расширенный DTO для каждой свечи
                            DailyCandleExtendedDto extendedCandle = DailyCandleExtendedDto.fromBasicData(
                                future.getFigi(),
                                future.getTicker(),
                                future.getTicker(), // У фьючерсов нет отдельного поля name
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
                    } else {
                        System.out.println("Нет данных для фьючерса: " + future.getTicker());
                    }

                } catch (Exception e) {
                    System.err.println("Ошибка получения свечей для фьючерса " + future.getTicker() + ": " + e.getMessage());
                }
            }

            // Вычисляем общую статистику
            Map<String, Object> response = new HashMap<>();
            response.put("date", date.toString());
            response.put("assetType", "FUTURES");
            response.put("candles", allCandles);
            response.put("totalCandles", totalCandles);
            response.put("totalInstruments", futures.size());

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

            System.out.println("=== ЗАВЕРШЕНИЕ ПОЛУЧЕНИЯ ДНЕВНЫХ СВЕЧЕЙ ФЬЮЧЕРСОВ ===");
            System.out.println("Всего инструментов: " + futures.size());
            System.out.println("Всего свечей: " + totalCandles);

            // Логируем успешное завершение
            SystemLogEntity resultLog = new SystemLogEntity();
            resultLog.setTaskId(taskId);
            resultLog.setEndpoint(endpoint);
            resultLog.setMethod("GET");
            resultLog.setStatus("COMPLETED");
            resultLog.setMessage("Получение дневных свечей фьючерсов завершено успешно за " + date + 
                ": Всего инструментов=" + futures.size() + 
                ", Всего свечей=" + totalCandles);
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
            System.err.println("Ошибка получения дневных свечей фьючерсов: " + e.getMessage());
            e.printStackTrace();

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("GET");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка получения дневных свечей фьючерсов за " + date + ": " + e.getMessage());
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
            errorResponse.put("error", "Ошибка получения дневных свечей фьючерсов: " + e.getMessage());
            errorResponse.put("date", date.toString());
            errorResponse.put("assetType", "FUTURES");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== ИНДИКАТИВЫ ====================

    /**
     * Асинхронная загрузка дневных свечей индикативов за дату
     */
    @PostMapping("/indicatives/{date}")
    @Transactional
    public ResponseEntity<Map<String, Object>> loadIndicativesDailyCandlesForDateAsync(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/daily/indicatives/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки дневных свечей индикативов за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== АСИНХРОННАЯ ЗАГРУЗКА ДНЕВНЫХ СВЕЧЕЙ ИНДИКАТИВОВ ЗА ДАТУ ===");
            System.out.println("Дата: " + date);
            System.out.println("Task ID: " + taskId);

            // Получаем все индикативы из БД
            List<IndicativeEntity> indicatives = indicativeRepository.findAll();
            System.out.println("Найдено индикативов: " + indicatives.size());

            // Создаем запрос для загрузки всех дневных свечей индикативов
            DailyCandleRequestDto request = new DailyCandleRequestDto();
            request.setInstruments(indicatives.stream().map(IndicativeEntity::getFigi).toList());
            request.setAssetType(List.of("INDICATIVES"));
            request.setDate(date);

            // Запускаем загрузку
            dailyCandleService.saveDailyCandlesAsync(request, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Загрузка дневных свечей индикативов за " + date + " запущена");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("date", date.toString());
            response.put("instrumentsCount", indicatives.size());
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            System.err.println("Ошибка запуска асинхронной загрузки дневных свечей индикативов: " + e.getMessage());
            e.printStackTrace();

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("ERROR");
            errorLog.setMessage("Ошибка запуска асинхронной загрузки: " + e.getMessage());
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
            errorResponse.put("message", "Ошибка запуска асинхронной загрузки: " + e.getMessage());
            errorResponse.put("taskId", taskId);
            errorResponse.put("status", "ERROR");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Получение дневных свечей индикативов за дату без сохранения
     */
    @GetMapping("/indicatives/{date}")
    public ResponseEntity<Map<String, Object>> getIndicativesDailyCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Генерируем уникальный ID задачи для логирования
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/daily/indicatives/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы метода
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("GET");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало получения дневных свечей индикативов за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== ПОЛУЧЕНИЕ ДНЕВНЫХ СВЕЧЕЙ ИНДИКАТИВОВ ===");
            System.out.println("Дата: " + date);

            // Получаем все индикативы из БД
            List<IndicativeEntity> indicatives = indicativeRepository.findAll();
            System.out.println("Найдено индикативов: " + indicatives.size());

            List<DailyCandleExtendedDto> allCandles = new ArrayList<>();
            int totalCandles = 0;

            for (IndicativeEntity indicative : indicatives) {
                try {
                    System.out.println("Получаем свечи для индикатива: " + indicative.getTicker() + " (" + indicative.getFigi() + ")");

                    // Получаем дневные свечи из API
                    var candles = tinkoffApiClient.getCandles(indicative.getFigi(), date, "CANDLE_INTERVAL_DAY");

                    if (candles != null && !candles.isEmpty()) {
                        System.out.println("Получено " + candles.size() + " свечей для " + indicative.getTicker());

                        for (var candle : candles) {
                            // Создаем расширенный DTO для каждой свечи
                            DailyCandleExtendedDto extendedCandle = DailyCandleExtendedDto.fromBasicData(
                                indicative.getFigi(),
                                indicative.getTicker(),
                                indicative.getName(),
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
                    } else {
                        System.out.println("Нет данных для индикатива: " + indicative.getTicker());
                    }

                } catch (Exception e) {
                    System.err.println("Ошибка получения свечей для индикатива " + indicative.getTicker() + ": " + e.getMessage());
                }
            }

            // Вычисляем общую статистику
            Map<String, Object> response = new HashMap<>();
            response.put("date", date.toString());
            response.put("assetType", "INDICATIVES");
            response.put("candles", allCandles);
            response.put("totalCandles", totalCandles);
            response.put("totalInstruments", indicatives.size());

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

            System.out.println("=== ЗАВЕРШЕНИЕ ПОЛУЧЕНИЯ ДНЕВНЫХ СВЕЧЕЙ ИНДИКАТИВОВ ===");
            System.out.println("Всего инструментов: " + indicatives.size());
            System.out.println("Всего свечей: " + totalCandles);

            // Логируем успешное завершение
            SystemLogEntity resultLog = new SystemLogEntity();
            resultLog.setTaskId(taskId);
            resultLog.setEndpoint(endpoint);
            resultLog.setMethod("GET");
            resultLog.setStatus("COMPLETED");
            resultLog.setMessage("Получение дневных свечей индикативов завершено успешно за " + date + 
                ": Всего инструментов=" + indicatives.size() + 
                ", Всего свечей=" + totalCandles);
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
            System.err.println("Ошибка получения дневных свечей индикативов: " + e.getMessage());
            e.printStackTrace();

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("GET");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка получения дневных свечей индикативов за " + date + ": " + e.getMessage());
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
            errorResponse.put("error", "Ошибка получения дневных свечей индикативов: " + e.getMessage());
            errorResponse.put("date", date.toString());
            errorResponse.put("assetType", "INDICATIVES");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
