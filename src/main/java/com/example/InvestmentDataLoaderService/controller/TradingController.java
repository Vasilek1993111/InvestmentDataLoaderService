package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.exception.ValidationException;
import com.example.InvestmentDataLoaderService.service.TradingService;
import com.example.InvestmentDataLoaderService.util.QueryParamValidator;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Контроллер для работы с торговыми данными
 * 
 * <p>Предоставляет REST API для работы с торговой информацией через Tinkoff Invest API:</p>
 * <ul>
 *   <li><strong>Счета</strong> - получение списка торговых счетов</li>
 *   <li><strong>Торговые расписания</strong> - информация о торговых сессиях и расписании</li>
 *   <li><strong>Торговые статусы</strong> - текущие статусы торговли по инструментам</li>
 *   <li><strong>Торговые дни</strong> - анализ торговых и неторговых дней</li>
 *   <li><strong>Статистика торгов</strong> - аналитика по торговой активности</li>
 * </ul>
 * 
 * <p>Все методы возвращают данные в формате JSON и поддерживают HTTP статус-коды.</p>
 * 
 * <p>Примеры использования:</p>
 * <pre>
 * GET /api/trading/accounts
 * GET /api/trading/schedules?exchange=MOEX&from=2024-01-01T00:00:00Z&to=2024-01-10T00:00:00Z
 * GET /api/trading/statuses?figi=BBG004730N88&instrumentId=BBG004730ZJ9
 * GET /api/trading/trading-days?exchange=MOEX
 * GET /api/trading/stats?from=2024-01-01T00:00:00Z&to=2024-01-31T00:00:00Z
 * </pre>
 * 
 * @author InvestmentDataLoaderService
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/trading")
public class TradingController {

    private final TradingService tradingService;

    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    // ==================== СЧЕТА ====================

    /**
     * Получение списка торговых счетов пользователя
     * 
     * <p>Возвращает все доступные торговые счета, связанные с токеном авторизации.</p>
     * 
     * <p>Примеры использования:</p>
     * <pre>
     * GET /api/trading/accounts
     * </pre>
     * 
     * <p>Ответ (пример):</p>
     * <pre>
     * [
     *   {
     *     "id": "2000123456",
     *     "name": "Брокерский счет",
     *     "type": "ACCOUNT_TYPE_TINKOFF"
     *   }
     * ]
     * </pre>
     * 
     * @return список торговых счетов пользователя
     */
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> getAccounts() {
        return ResponseEntity.ok(tradingService.getAccounts());
    }

    // ==================== ТОРГОВЫЕ РАСПИСАНИЯ ====================

    /**
     * Получение торговых расписаний за указанный период
     * 
     * <p>Возвращает информацию о торговых сессиях для указанной биржи и периода времени.</p>
     * 
     * <p>Параметры запроса:</p>
     * <ul>
     *   <li><strong>exchange</strong> (опционально) - биржа (например: MOEX, SPB)</li>
     *   <li><strong>from</strong> (опционально) - начальная дата в формате ISO 8601 (по умолчанию: текущее время)</li>
     *   <li><strong>to</strong> (опционально) - конечная дата в формате ISO 8601 (по умолчанию: текущее время + 1 день)</li>
     * </ul>
     * 
     * <p>Ограничения:</p>
     * <ul>
     *   <li>Период между from и to: минимум 1 день, максимум 14 дней</li>
     *   <li>Даты не могут быть более чем на 1 год в будущем</li>
     * </ul>
     * 
     * <p>Примеры использования:</p>
     * <pre>
     * GET /api/trading/schedules
     * GET /api/trading/schedules?exchange=MOEX
     * GET /api/trading/schedules?from=2024-01-01T00:00:00Z&to=2024-01-10T00:00:00Z
     * GET /api/trading/schedules?exchange=MOEX&from=2024-01-01T00:00:00Z&to=2024-01-10T00:00:00Z
     * </pre>
     * 
     * <p>Ответ (пример):</p>
     * <pre>
     * [
     *   {
     *     "exchange": "MOEX",
     *     "days": [
     *       {
     *         "date": "2024-01-02",
     *         "isTradingDay": true,
     *         "startTime": "2024-01-02T06:50:00+03:00",
     *         "endTime": "2024-01-02T23:59:59+03:00"
     *       }
     *     ]
     *   }
     * ]
     * </pre>
     * 
     * @param exchange биржа (опционально)
     * @param from начальная дата в формате ISO 8601 (опционально)
     * @param to конечная дата в формате ISO 8601 (опционально)
     * @return список торговых расписаний
     */
    @GetMapping("/schedules")
    public ResponseEntity<List<TradingScheduleDto>> getTradingSchedules(
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) throws ValidationException {
        // Валидация разрешенных параметров
        QueryParamValidator.validateTradingParams();
        
        // Валидация параметров запроса
        TradingRequestParams params = TradingRequestParams.create(exchange, from, to);
        
        // Устанавливаем значения по умолчанию: сегодня и завтра
        Instant fromInstant = params.from() != null ? params.from() : Instant.now();
        Instant toInstant = params.to() != null ? params.to() : Instant.now().plusSeconds(86400); // +1 день
        
        return ResponseEntity.ok(tradingService.getTradingSchedules(
            params.exchange() != null ? params.exchange().getValue() : null, 
            fromInstant, 
            toInstant
        ));
    }


    // ==================== ТОРГОВЫЕ СТАТУСЫ ====================

    /**
     * Получение торговых статусов инструментов с детальной информацией
     * 
     * <p>Возвращает текущие торговые статусы для указанных финансовых инструментов.</p>
     * 
     * <p>Параметры запроса:</p>
     * <ul>
     *   <li><strong>instrumentId</strong> (опционально) - список FIGI идентификаторов инструментов</li>
     *   <li><strong>figi</strong> (опционально) - один FIGI идентификатор инструмента</li>
     * </ul>
     * 
     * <p>Примечания:</p>
     * <ul>
     *   <li>Необходимо указать хотя бы один параметр (instrumentId или figi)</li>
     *   <li>Можно комбинировать оба параметра для запроса нескольких инструментов</li>
     *   <li>Используйте FIGI идентификаторы, а не тикеры (например: BBG004730N88 вместо SBER)</li>
     * </ul>
     * 
     * <p>Примеры использования:</p>
     * <pre>
     * GET /api/trading/statuses?figi=BBG004730N88
     * GET /api/trading/statuses?instrumentId=BBG004730N88&instrumentId=BBG004730ZJ9
     * GET /api/trading/statuses?figi=BBG004730N88&instrumentId=BBG004730ZJ9
     * </pre>
     * 
     * <p>Ответ (пример):</p>
     * <pre>
     * {
     *   "success": true,
     *   "data": [
     *     {
     *       "figi": "BBG004730N88",
     *       "status": "SECURITY_TRADING_STATUS_NORMAL_TRADING"
     *     }
     *   ],
     *   "count": 1,
     *   "requested_instruments": 1,
     *   "instruments": ["BBG004730N88"]
     * }
     * </pre>
     * 
     * @param instrumentId список FIGI идентификаторов (опционально)
     * @param figi один FIGI идентификатор (опционально)
     * @return детальная информация о торговых статусах
     */
    @GetMapping("/statuses")
    public ResponseEntity<Map<String, Object>> getTradingStatuses(
            @RequestParam(required = false) List<String> instrumentId,
            @RequestParam(required = false) String figi
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> instruments = new ArrayList<>();
            
            // Если передан список instrumentId
            if (instrumentId != null && !instrumentId.isEmpty()) {
                instruments.addAll(instrumentId);
            }
            
            // Если передан figi
            if (figi != null && !figi.isEmpty()) {
                instruments.add(figi);
            }
            
            // Если ничего не передано, возвращаем ошибку
            if (instruments.isEmpty()) {
                response.put("success", false);
                response.put("message", "Необходимо указать хотя бы один инструмент через параметр 'instrumentId' или 'figi'");
                response.put("error", "MISSING_PARAMETER");
                return ResponseEntity.badRequest().body(response);
            }
            
            return ResponseEntity.ok(tradingService.getTradingStatusesDetailed(instruments));
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения торговых статусов: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== ТОРГОВЫЕ ДНИ ====================

    /**
     * Получение информации о торговых и неторговых днях за период
     * 
     * <p>Анализирует торговые расписания и группирует дни по типам торговой активности.</p>
     * 
     * <p>Параметры запроса:</p>
     * <ul>
     *   <li><strong>exchange</strong> (опционально) - биржа (например: MOEX, SPB)</li>
     *   <li><strong>from</strong> (опционально) - начальная дата в формате ISO 8601 (по умолчанию: текущее время)</li>
     *   <li><strong>to</strong> (опционально) - конечная дата в формате ISO 8601 (по умолчанию: текущее время + 1 день)</li>
     * </ul>
     * 
     * <p>Примеры использования:</p>
     * <pre>
     * GET /api/trading/trading-days
     * GET /api/trading/trading-days?exchange=MOEX
     * GET /api/trading/trading-days?from=2024-01-01T00:00:00Z&to=2024-01-10T00:00:00Z
     * GET /api/trading/trading-days?exchange=MOEX&from=2024-01-01T00:00:00Z&to=2024-01-10T00:00:00Z
     * </pre>
     * 
     * <p>Ответ (пример):</p>
     * <pre>
     * {
     *   "success": true,
     *   "trading_days": {
     *     "2024-01-02": "trading",
     *     "2024-01-06": "non-trading"
     *   },
     *   "trading_days_count": 6,
     *   "non_trading_days_count": 4,
     *   "total_days": 10,
     *   "from": "2024-01-01T00:00:00Z",
     *   "to": "2024-01-10T00:00:00Z",
     *   "exchange": "MOEX"
     * }
     * </pre>
     * 
     * @param exchange биржа (опционально)
     * @param from начальная дата в формате ISO 8601 (опционально)
     * @param to конечная дата в формате ISO 8601 (опционально)
     * @return детальная информация о торговых днях
     */
    @GetMapping("/trading-days")
    public ResponseEntity<Map<String, Object>> getTradingDays(
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) throws ValidationException {
        // Валидация разрешенных параметров
        QueryParamValidator.validateTradingParams();
        
        // Валидация параметров запроса
        TradingRequestParams params = TradingRequestParams.create(exchange, from, to);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Устанавливаем значения по умолчанию: сегодня и завтра
            Instant fromInstant = params.from() != null ? params.from() : Instant.now();
            Instant toInstant = params.to() != null ? params.to() : Instant.now().plusSeconds(86400); // +1 день
            
            Map<String, Object> result = tradingService.getTradingDays(
                params.exchange() != null ? params.exchange().getValue() : null, 
                fromInstant, 
                toInstant
            );
            
            // Обновляем значения from и to в ответе
            result.put("from", from != null ? from : fromInstant.toString());
            result.put("to", to != null ? to : toInstant.toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения информации о торговых днях: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== СТАТИСТИКА ТОРГОВ ====================

    /**
     * Получение статистики торговой активности за период
     * 
     * <p>Вычисляет процентное соотношение торговых и неторговых дней, а также общую статистику.</p>
     * 
     * <p>Параметры запроса:</p>
     * <ul>
     *   <li><strong>exchange</strong> (опционально) - биржа (например: MOEX, SPB)</li>
     *   <li><strong>from</strong> (опционально) - начальная дата в формате ISO 8601 (по умолчанию: текущее время)</li>
     *   <li><strong>to</strong> (опционально) - конечная дата в формате ISO 8601 (по умолчанию: текущее время + 1 день)</li>
     * </ul>
     * 
     * <p>Примеры использования:</p>
     * <pre>
     * GET /api/trading/stats
     * GET /api/trading/stats?exchange=MOEX
     * GET /api/trading/stats?from=2024-01-01T00:00:00Z&to=2024-01-31T00:00:00Z
     * GET /api/trading/stats?exchange=MOEX&from=2024-01-01T00:00:00Z&to=2024-01-31T00:00:00Z
     * </pre>
     * 
     * <p>Ответ (пример):</p>
     * <pre>
     * {
     *   "success": true,
     *   "period": {
     *     "from": "2024-01-01T00:00:00Z",
     *     "to": "2024-01-31T00:00:00Z",
     *     "exchange": "MOEX"
     *   },
     *   "trading_days": 20,
     *   "non_trading_days": 11,
     *   "total_days": 31,
     *   "trading_percentage": 64.52
     * }
     * </pre>
     * 
     * @param exchange биржа (опционально)
     * @param from начальная дата в формате ISO 8601 (опционально)
     * @param to конечная дата в формате ISO 8601 (опционально)
     * @return статистика торговой активности
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTradingStats(
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) throws ValidationException {
        // Валидация разрешенных параметров
        QueryParamValidator.validateTradingParams();
        
        // Валидация параметров запроса
        TradingRequestParams params = TradingRequestParams.create(exchange, from, to);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Устанавливаем значения по умолчанию: сегодня и завтра
            Instant fromInstant = params.from() != null ? params.from() : Instant.now();
            Instant toInstant = params.to() != null ? params.to() : Instant.now().plusSeconds(86400); // +1 день
            
            Map<String, Object> result = tradingService.getTradingStats(
                params.exchange() != null ? params.exchange().getValue() : null, 
                fromInstant, 
                toInstant
            );
            
            // Обновляем значения from и to в ответе
            @SuppressWarnings("unchecked")
            Map<String, Object> period = (Map<String, Object>) result.get("period");
            period.put("from", from != null ? from : fromInstant.toString());
            period.put("to", to != null ? to : toInstant.toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения статистики торгов: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

}
