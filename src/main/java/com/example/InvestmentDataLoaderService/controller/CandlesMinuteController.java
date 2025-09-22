package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.repository.*;
import com.example.InvestmentDataLoaderService.service.*;
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
 * Контроллер для работы с минутными свечами
 */
@RestController
@RequestMapping("/api/candles/minute")
public class CandlesMinuteController {

    private final MinuteCandleService minuteCandleService;
    private final MarketDataService marketDataService;
    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final IndicativeRepository indicativeRepository;
    private final SystemLogRepository systemLogRepository;

    public CandlesMinuteController(
            MinuteCandleService minuteCandleService,
            MarketDataService marketDataService,
            ShareRepository shareRepository,
            FutureRepository futureRepository,
            IndicativeRepository indicativeRepository,
            SystemLogRepository systemLogRepository
    ) {
        this.minuteCandleService = minuteCandleService;
        this.marketDataService = marketDataService;
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.indicativeRepository = indicativeRepository;
        this.systemLogRepository = systemLogRepository;
    }

    // ==================== ОБЩИЕ МИНУТНЫЕ СВЕЧИ ====================

    /**
     * Загрузка минутных свечей за сегодня
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> loadMinuteCandlesTodayAsync(@RequestBody MinuteCandleRequestDto request) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/minute";
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки минутных свечей за сегодня");
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== АСИНХРОННАЯ ЗАГРУЗКА МИНУТНЫХ СВЕЧЕЙ ЗА СЕГОДНЯ ===");
            System.out.println("Инструменты: " + request.getInstruments());
            System.out.println("Типы активов: " + request.getAssetType());
            System.out.println("Task ID: " + taskId);

            // Запускаем асинхронную загрузку
            minuteCandleService.saveMinuteCandlesAsync(request, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Асинхронная загрузка минутных свечей за сегодня запущена");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("instruments", request.getInstruments());
            response.put("assetTypes", request.getAssetType());
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            System.err.println("Ошибка запуска асинхронной загрузки минутных свечей: " + e.getMessage());
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
     * Асинхронная загрузка минутных свечей за конкретную дату
     */
    @PostMapping("/{date}")
    public ResponseEntity<Map<String, Object>> loadMinuteCandlesForDateAsync(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody MinuteCandleRequestDto request
    ) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/minute/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки минутных свечей за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== АСИНХРОННАЯ ЗАГРУЗКА МИНУТНЫХ СВЕЧЕЙ ЗА ДАТУ ===");
            System.out.println("Дата: " + date);
            System.out.println("Инструменты: " + request.getInstruments());
            System.out.println("Типы активов: " + request.getAssetType());
            System.out.println("Task ID: " + taskId);

            // Запускаем асинхронную загрузку
            minuteCandleService.saveMinuteCandlesAsync(request, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Асинхронная загрузка минутных свечей за " + date + " запущена");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("date", date.toString());
            response.put("instruments", request.getInstruments());
            response.put("assetTypes", request.getAssetType());
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            System.err.println("Ошибка запуска асинхронной загрузки минутных свечей за дату: " + e.getMessage());
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
     * Получение минутных свечей акций за дату без сохранения
     */
    @GetMapping("/shares/{date}")
    public ResponseEntity<Map<String, Object>> getSharesMinuteCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Генерируем уникальный ID задачи для логирования
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/minute/shares/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы метода
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("GET");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало получения минутных свечей акций за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== ПОЛУЧЕНИЕ МИНУТНЫХ СВЕЧЕЙ АКЦИЙ ===");
            System.out.println("Дата: " + date);

            // Получаем все акции из БД
            List<ShareEntity> shares = shareRepository.findAll();
            System.out.println("Найдено акций: " + shares.size());

            List<Map<String, Object>> allCandles = new ArrayList<>();
            int totalCandles = 0;
            int processedInstruments = 0;
            int successfulInstruments = 0;
            int noDataInstruments = 0;
            int errorInstruments = 0;

            for (ShareEntity share : shares) {
                processedInstruments++;
                try {
                    System.out.println("Получаем свечи для акции: " + share.getTicker() + " (" + share.getFigi() + ")");

                    // Получаем минутные свечи из API
                    var candles = marketDataService.getCandles(share.getFigi(), date, "CANDLE_INTERVAL_1_MIN");

                    if (candles != null && !candles.isEmpty()) {
                        System.out.println("Получено " + candles.size() + " свечей для " + share.getTicker());
                        successfulInstruments++;

                        // Логируем успешное получение данных для каждого FIGI
                        SystemLogEntity figiLog = new SystemLogEntity();
                        figiLog.setTaskId(taskId);
                        figiLog.setEndpoint(endpoint);
                        figiLog.setMethod("GET");
                        figiLog.setStatus("SUCCESS");
                        figiLog.setMessage("Успешно получено " + candles.size() + " минутных свечей для акции " + share.getTicker() + " (FIGI: " + share.getFigi() + ") за дату " + date);
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
                            Map<String, Object> candleData = new HashMap<>();
                            candleData.put("figi", share.getFigi());
                            candleData.put("ticker", share.getTicker());
                            candleData.put("name", share.getName());
                            candleData.put("open", candle.open());
                            candleData.put("close", candle.close());
                            candleData.put("high", candle.high());
                            candleData.put("low", candle.low());
                            candleData.put("volume", candle.volume());
                            candleData.put("time", candle.time());
                            candleData.put("isComplete", candle.isComplete());

                            // Добавляем расширенную статистику
                            BigDecimal priceChange = candle.close().subtract(candle.open());
                            BigDecimal priceChangePercent = candle.open().compareTo(BigDecimal.ZERO) > 0 
                                ? priceChange.divide(candle.open(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                                : BigDecimal.ZERO;

                            candleData.put("priceChange", priceChange);
                            candleData.put("priceChangePercent", priceChangePercent);
                            candleData.put("highLowRange", candle.high().subtract(candle.low()));
                            candleData.put("bodySize", candle.close().subtract(candle.open()).abs());
                            candleData.put("upperShadow", candle.high().subtract(candle.open().max(candle.close())));
                            candleData.put("lowerShadow", candle.open().min(candle.close()).subtract(candle.low()));
                            candleData.put("averagePrice", candle.high().add(candle.low()).add(candle.open()).add(candle.close()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP));
                            candleData.put("candleType", candle.close().compareTo(candle.open()) > 0 ? "BULLISH" : 
                                candle.close().compareTo(candle.open()) < 0 ? "BEARISH" : "DOJI");

                            allCandles.add(candleData);
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
                        noDataLog.setMessage("Нет минутных свечей для акции " + share.getTicker() + " (FIGI: " + share.getFigi() + ") за дату " + date);
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
                    errorLog.setMessage("Ошибка получения минутных свечей для акции " + share.getTicker() + " (FIGI: " + share.getFigi() + ") за дату " + date + ": " + e.getMessage());
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
            response.put("date", date);
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
                        Object volume = candle.get("volume");
                        if (volume instanceof BigDecimal) {
                            return (BigDecimal) volume;
                        } else if (volume instanceof Long) {
                            return BigDecimal.valueOf((Long) volume);
                        } else if (volume instanceof Integer) {
                            return BigDecimal.valueOf((Integer) volume);
                        } else {
                            return BigDecimal.ZERO;
                        }
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal avgPrice = allCandles.stream()
                    .map(candle -> (BigDecimal) candle.get("averagePrice"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(allCandles.size()), 2, RoundingMode.HALF_UP);

                response.put("totalVolume", totalVolume);
                response.put("averagePrice", avgPrice);
            }

            System.out.println("=== ЗАВЕРШЕНИЕ ПОЛУЧЕНИЯ МИНУТНЫХ СВЕЧЕЙ АКЦИЙ ===");
            System.out.println("Всего инструментов: " + shares.size());
            System.out.println("Всего свечей: " + totalCandles);

            // Логируем успешное завершение
            SystemLogEntity resultLog = new SystemLogEntity();
            resultLog.setTaskId(taskId);
            resultLog.setEndpoint(endpoint);
            resultLog.setMethod("GET");
            resultLog.setStatus("COMPLETED");
            resultLog.setMessage("Получение минутных свечей акций завершено успешно за " + date + 
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
            System.err.println("Ошибка получения минутных свечей акций: " + e.getMessage());
            e.printStackTrace();

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("GET");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка получения минутных свечей акций за " + date + ": " + e.getMessage());
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
            errorResponse.put("error", "Ошибка получения минутных свечей акций: " + e.getMessage());
            errorResponse.put("date", date);
            errorResponse.put("assetType", "SHARES");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Асинхронная загрузка минутных свечей акций за дату
     */
    @PostMapping("/shares/{date}")
    public ResponseEntity<Map<String, Object>> loadSharesMinuteCandlesForDateAsync(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/minute/shares/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки минутных свечей акций за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== АСИНХРОННАЯ ЗАГРУЗКА МИНУТНЫХ СВЕЧЕЙ АКЦИЙ ЗА ДАТУ ===");
            System.out.println("Дата: " + date);
            System.out.println("Task ID: " + taskId);

            // Получаем все акции из БД
            List<ShareEntity> shares = shareRepository.findAll();
            System.out.println("Найдено акций: " + shares.size());

            // Создаем запрос для загрузки всех минутных свечей акций
            MinuteCandleRequestDto request = new MinuteCandleRequestDto();
            request.setInstruments(shares.stream().map(ShareEntity::getFigi).toList());
            request.setAssetType(List.of("SHARES"));
            request.setDate(date);

            // Запускаем асинхронную загрузку
            minuteCandleService.saveMinuteCandlesAsync(request, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Асинхронная загрузка минутных свечей акций запущена");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("date", date.toString());
            response.put("instrumentsCount", shares.size());
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            System.err.println("Ошибка запуска асинхронной загрузки минутных свечей акций: " + e.getMessage());
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
     * Асинхронная загрузка минутных свечей фьючерсов за дату
     */
    @PostMapping("/futures/{date}")
    public ResponseEntity<Map<String, Object>> loadFuturesMinuteCandlesForDateAsync(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/minute/futures/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки минутных свечей фьючерсов за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== АСИНХРОННАЯ ЗАГРУЗКА МИНУТНЫХ СВЕЧЕЙ ФЬЮЧЕРСОВ ЗА ДАТУ ===");
            System.out.println("Дата: " + date);
            System.out.println("Task ID: " + taskId);

            // Получаем все фьючерсы из БД
            List<FutureEntity> futures = futureRepository.findAll();
            System.out.println("Найдено фьючерсов: " + futures.size());

            // Создаем запрос для загрузки всех минутных свечей фьючерсов
            MinuteCandleRequestDto request = new MinuteCandleRequestDto();
            request.setInstruments(futures.stream().map(FutureEntity::getFigi).toList());
            request.setAssetType(List.of("FUTURES"));
            request.setDate(date);

            // Запускаем асинхронную загрузку
            minuteCandleService.saveMinuteCandlesAsync(request, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Асинхронная загрузка минутных свечей фьючерсов за " + date + " запущена");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("date", date.toString());
            response.put("instrumentsCount", futures.size());
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            System.err.println("Ошибка запуска асинхронной загрузки минутных свечей фьючерсов: " + e.getMessage());
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
     * Получение минутных свечей фьючерсов за дату без сохранения
     */
    @GetMapping("/futures/{date}")
    public ResponseEntity<Map<String, Object>> getFuturesMinuteCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Генерируем уникальный ID задачи для логирования
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/minute/futures/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы метода
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("GET");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало получения минутных свечей фьючерсов за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== ПОЛУЧЕНИЕ МИНУТНЫХ СВЕЧЕЙ ФЬЮЧЕРСОВ ===");
            System.out.println("Дата: " + date);

            // Получаем все фьючерсы из БД
            List<FutureEntity> futures = futureRepository.findAll();
            System.out.println("Найдено фьючерсов: " + futures.size());

            List<Map<String, Object>> allCandles = new ArrayList<>();
            int totalCandles = 0;

            for (FutureEntity future : futures) {
                try {
                    System.out.println("Получаем свечи для фьючерса: " + future.getTicker() + " (" + future.getFigi() + ")");

                    // Получаем минутные свечи из API
                    var candles = marketDataService.getCandles(future.getFigi(), date, "CANDLE_INTERVAL_1_MIN");

                    if (candles != null && !candles.isEmpty()) {
                        System.out.println("Получено " + candles.size() + " свечей для " + future.getTicker());

                        for (var candle : candles) {
                            Map<String, Object> candleData = new HashMap<>();
                            candleData.put("figi", future.getFigi());
                            candleData.put("ticker", future.getTicker());
                            candleData.put("name", future.getTicker());
                            candleData.put("open", candle.open());
                            candleData.put("close", candle.close());
                            candleData.put("high", candle.high());
                            candleData.put("low", candle.low());
                            candleData.put("volume", candle.volume());
                            candleData.put("time", candle.time());
                            candleData.put("isComplete", candle.isComplete());

                            // Добавляем расширенную статистику
                            BigDecimal priceChange = candle.close().subtract(candle.open());
                            BigDecimal priceChangePercent = candle.open().compareTo(BigDecimal.ZERO) > 0 
                                ? priceChange.divide(candle.open(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                                : BigDecimal.ZERO;

                            candleData.put("priceChange", priceChange);
                            candleData.put("priceChangePercent", priceChangePercent);
                            candleData.put("highLowRange", candle.high().subtract(candle.low()));
                            candleData.put("bodySize", candle.close().subtract(candle.open()).abs());
                            candleData.put("upperShadow", candle.high().subtract(candle.open().max(candle.close())));
                            candleData.put("lowerShadow", candle.open().min(candle.close()).subtract(candle.low()));
                            candleData.put("averagePrice", candle.high().add(candle.low()).add(candle.open()).add(candle.close()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP));
                            candleData.put("candleType", candle.close().compareTo(candle.open()) > 0 ? "BULLISH" : 
                                candle.close().compareTo(candle.open()) < 0 ? "BEARISH" : "DOJI");

                            allCandles.add(candleData);
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
            response.put("date", date);
            response.put("assetType", "FUTURES");
            response.put("candles", allCandles);
            response.put("totalCandles", totalCandles);
            response.put("totalInstruments", futures.size());

            // Добавляем статистику по объемам и ценам
            if (!allCandles.isEmpty()) {
                BigDecimal totalVolume = allCandles.stream()
                    .map(candle -> {
                        Object volume = candle.get("volume");
                        if (volume instanceof BigDecimal) {
                            return (BigDecimal) volume;
                        } else if (volume instanceof Long) {
                            return BigDecimal.valueOf((Long) volume);
                        } else if (volume instanceof Integer) {
                            return BigDecimal.valueOf((Integer) volume);
                        } else {
                            return BigDecimal.ZERO;
                        }
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal avgPrice = allCandles.stream()
                    .map(candle -> (BigDecimal) candle.get("averagePrice"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(allCandles.size()), 2, RoundingMode.HALF_UP);

                response.put("totalVolume", totalVolume);
                response.put("averagePrice", avgPrice);
            }

            System.out.println("=== ЗАВЕРШЕНИЕ ПОЛУЧЕНИЯ МИНУТНЫХ СВЕЧЕЙ ФЬЮЧЕРСОВ ===");
            System.out.println("Всего инструментов: " + futures.size());
            System.out.println("Всего свечей: " + totalCandles);

            // Логируем успешное завершение
            SystemLogEntity resultLog = new SystemLogEntity();
            resultLog.setTaskId(taskId);
            resultLog.setEndpoint(endpoint);
            resultLog.setMethod("GET");
            resultLog.setStatus("COMPLETED");
            resultLog.setMessage("Получение минутных свечей фьючерсов завершено успешно за " + date + 
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
            System.err.println("Ошибка получения минутных свечей фьючерсов: " + e.getMessage());
            e.printStackTrace();

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("GET");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка получения минутных свечей фьючерсов за " + date + ": " + e.getMessage());
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
            errorResponse.put("error", "Ошибка получения минутных свечей фьючерсов: " + e.getMessage());
            errorResponse.put("date", date);
            errorResponse.put("assetType", "FUTURES");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== ИНДИКАТИВЫ ====================

    /**
     * Асинхронная загрузка минутных свечей индикативов за дату
     */
    @PostMapping("/indicatives/{date}")
    public ResponseEntity<Map<String, Object>> loadIndicativesMinuteCandlesForDateAsync(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/minute/indicatives/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки минутных свечей индикативов за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== АСИНХРОННАЯ ЗАГРУЗКА МИНУТНЫХ СВЕЧЕЙ ИНДИКАТИВОВ ЗА ДАТУ ===");
            System.out.println("Дата: " + date);
            System.out.println("Task ID: " + taskId);

            // Получаем все индикативы из БД
            List<IndicativeEntity> indicatives = indicativeRepository.findAll();
            System.out.println("Найдено индикативов: " + indicatives.size());

            // Создаем запрос для загрузки всех минутных свечей индикативов
            MinuteCandleRequestDto request = new MinuteCandleRequestDto();
            request.setInstruments(indicatives.stream().map(IndicativeEntity::getFigi).toList());
            request.setAssetType(List.of("INDICATIVES"));
            request.setDate(date);

            // Запускаем асинхронную загрузку
            minuteCandleService.saveMinuteCandlesAsync(request, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Асинхронная загрузка минутных свечей индикативов за " + date + " запущена");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("date", date.toString());
            response.put("instrumentsCount", indicatives.size());
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            System.err.println("Ошибка запуска асинхронной загрузки минутных свечей индикативов: " + e.getMessage());
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
     * Получение минутных свечей индикативов за дату без сохранения
     */
    @GetMapping("/indicatives/{date}")
    public ResponseEntity<Map<String, Object>> getIndicativesMinuteCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Генерируем уникальный ID задачи для логирования
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/candles/minute/indicatives/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы метода
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("GET");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало получения минутных свечей индикативов за " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            System.out.println("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            System.err.println("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            System.out.println("=== ПОЛУЧЕНИЕ МИНУТНЫХ СВЕЧЕЙ ИНДИКАТИВОВ ===");
            System.out.println("Дата: " + date);

            // Получаем все индикативы из БД
            List<IndicativeEntity> indicatives = indicativeRepository.findAll();
            System.out.println("Найдено индикативов: " + indicatives.size());

            List<Map<String, Object>> allCandles = new ArrayList<>();
            int totalCandles = 0;

            for (IndicativeEntity indicative : indicatives) {
                try {
                    System.out.println("Получаем свечи для индикатива: " + indicative.getTicker() + " (" + indicative.getFigi() + ")");

                    // Получаем минутные свечи из API
                    var candles = marketDataService.getCandles(indicative.getFigi(), date, "CANDLE_INTERVAL_1_MIN");

                    if (candles != null && !candles.isEmpty()) {
                        System.out.println("Получено " + candles.size() + " свечей для " + indicative.getTicker());

                        for (var candle : candles) {
                            Map<String, Object> candleData = new HashMap<>();
                            candleData.put("figi", indicative.getFigi());
                            candleData.put("ticker", indicative.getTicker());
                            candleData.put("name", indicative.getName());
                            candleData.put("open", candle.open());
                            candleData.put("close", candle.close());
                            candleData.put("high", candle.high());
                            candleData.put("low", candle.low());
                            candleData.put("volume", candle.volume());
                            candleData.put("time", candle.time());
                            candleData.put("isComplete", candle.isComplete());

                            // Добавляем расширенную статистику
                            BigDecimal priceChange = candle.close().subtract(candle.open());
                            BigDecimal priceChangePercent = candle.open().compareTo(BigDecimal.ZERO) > 0 
                                ? priceChange.divide(candle.open(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                                : BigDecimal.ZERO;

                            candleData.put("priceChange", priceChange);
                            candleData.put("priceChangePercent", priceChangePercent);
                            candleData.put("highLowRange", candle.high().subtract(candle.low()));
                            candleData.put("bodySize", candle.close().subtract(candle.open()).abs());
                            candleData.put("upperShadow", candle.high().subtract(candle.open().max(candle.close())));
                            candleData.put("lowerShadow", candle.open().min(candle.close()).subtract(candle.low()));
                            candleData.put("averagePrice", candle.high().add(candle.low()).add(candle.open()).add(candle.close()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP));
                            candleData.put("candleType", candle.close().compareTo(candle.open()) > 0 ? "BULLISH" : 
                                candle.close().compareTo(candle.open()) < 0 ? "BEARISH" : "DOJI");

                            allCandles.add(candleData);
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
            response.put("date", date);
            response.put("assetType", "INDICATIVES");
            response.put("candles", allCandles);
            response.put("totalCandles", totalCandles);
            response.put("totalInstruments", indicatives.size());

            // Добавляем статистику по объемам и ценам
            if (!allCandles.isEmpty()) {
                BigDecimal totalVolume = allCandles.stream()
                    .map(candle -> {
                        Object volume = candle.get("volume");
                        if (volume instanceof BigDecimal) {
                            return (BigDecimal) volume;
                        } else if (volume instanceof Long) {
                            return BigDecimal.valueOf((Long) volume);
                        } else if (volume instanceof Integer) {
                            return BigDecimal.valueOf((Integer) volume);
                        } else {
                            return BigDecimal.ZERO;
                        }
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal avgPrice = allCandles.stream()
                    .map(candle -> (BigDecimal) candle.get("averagePrice"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(allCandles.size()), 2, RoundingMode.HALF_UP);

                response.put("totalVolume", totalVolume);
                response.put("averagePrice", avgPrice);
            }

            System.out.println("=== ЗАВЕРШЕНИЕ ПОЛУЧЕНИЯ МИНУТНЫХ СВЕЧЕЙ ИНДИКАТИВОВ ===");
            System.out.println("Всего инструментов: " + indicatives.size());
            System.out.println("Всего свечей: " + totalCandles);

            // Логируем успешное завершение
            SystemLogEntity resultLog = new SystemLogEntity();
            resultLog.setTaskId(taskId);
            resultLog.setEndpoint(endpoint);
            resultLog.setMethod("GET");
            resultLog.setStatus("COMPLETED");
            resultLog.setMessage("Получение минутных свечей индикативов завершено успешно за " + date + 
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
            System.err.println("Ошибка получения минутных свечей индикативов: " + e.getMessage());
            e.printStackTrace();

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("GET");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка получения минутных свечей индикативов за " + date + ": " + e.getMessage());
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
            errorResponse.put("error", "Ошибка получения минутных свечей индикативов: " + e.getMessage());
            errorResponse.put("date", date);
            errorResponse.put("assetType", "INDICATIVES");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
