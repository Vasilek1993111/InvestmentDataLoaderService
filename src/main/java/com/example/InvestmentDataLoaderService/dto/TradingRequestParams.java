package com.example.InvestmentDataLoaderService.dto;

import com.example.InvestmentDataLoaderService.enums.ExchangeType;
import com.example.InvestmentDataLoaderService.exception.ValidationException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Параметры запроса для торговых операций с валидацией
 */
public record TradingRequestParams(
    ExchangeType exchange,
    Instant from,
    Instant to
) {
    
    /**
     * Создает параметры запроса с валидацией
     * 
     * @param exchangeStr строка биржи
     * @param fromStr строка начальной даты в формате ISO 8601
     * @param toStr строка конечной даты в формате ISO 8601
     * @return валидные параметры запроса
     * @throws ValidationException если параметры невалидны
     */
    public static TradingRequestParams create(
            String exchangeStr,
            String fromStr,
            String toStr
    ) throws ValidationException {
        
        // Валидация биржи
        ExchangeType exchange = null;
        if (exchangeStr != null && !exchangeStr.trim().isEmpty()) {
            exchange = ExchangeType.fromString(exchangeStr);
            if (exchange == null) {
                throw new ValidationException("Невалидная биржа: " + exchangeStr + 
                    ". Допустимые значения: " + java.util.Arrays.toString(ExchangeType.getAllValues()), "exchange");
            }
        }
        
        Instant now = Instant.now();
        // Получаем начало текущего дня (00:00:00) в UTC
        Instant startOfToday = LocalDate.now(ZoneId.of("UTC")).atStartOfDay(ZoneId.of("UTC")).toInstant();
        // Получаем максимально допустимую дату (начало текущего дня + 14 дней)
        Instant maxAllowedDate = startOfToday.plus(14, ChronoUnit.DAYS);
        
        // Валидация дат
        Instant from = null;
        if (fromStr != null && !fromStr.trim().isEmpty()) {
            try {
                from = Instant.parse(fromStr);
                
                // Проверяем, что from не может быть раньше начала текущего дня
                if (from.isBefore(startOfToday)) {
                    throw new ValidationException("Начальная дата не может быть раньше начала текущего дня. " +
                        "Начало текущего дня: " + startOfToday.toString() + ", указанная дата: " + from.toString(), "from");
                }
                
                // Проверяем, что from не более чем на 14 дней в будущем (с учетом часов, минут, секунд)
                if (from.isAfter(maxAllowedDate)) {
                    long daysFromToday = ChronoUnit.DAYS.between(startOfToday, from);
                    long hoursFromToday = ChronoUnit.HOURS.between(startOfToday, from);
                    throw new ValidationException("Начальная дата не может быть более чем на 14 дней в будущем. " +
                        "Начало текущего дня: " + startOfToday.toString() + ", указанная дата: " + from.toString() + 
                        ", разница: " + daysFromToday + " дней (" + hoursFromToday + " часов)", "from");
                }
                
            } catch (DateTimeParseException e) {
                throw new ValidationException("Невалидный формат начальной даты: " + fromStr + 
                    ". Ожидается формат ISO 8601 (например: 2024-01-01T00:00:00Z). " +
                    "Ошибка парсинга: " + e.getMessage(), "from");
            }
        }
        
        Instant to = null;
        if (toStr != null && !toStr.trim().isEmpty()) {
            try {
                to = Instant.parse(toStr);
                
                // Проверяем, что to не может быть раньше начала текущего дня
                if (to.isBefore(startOfToday)) {
                    throw new ValidationException("Конечная дата не может быть раньше начала текущего дня. " +
                        "Начало текущего дня: " + startOfToday.toString() + ", указанная дата: " + to.toString(), "to");
                }
                
                // Проверяем, что to не более чем на 14 дней в будущем (с учетом часов, минут, секунд)
                if (to.isAfter(maxAllowedDate)) {
                    long daysFromToday = ChronoUnit.DAYS.between(startOfToday, to);
                    long hoursFromToday = ChronoUnit.HOURS.between(startOfToday, to);
                    throw new ValidationException("Конечная дата не может быть более чем на 14 дней в будущем. " +
                        "Начало текущего дня: " + startOfToday.toString() + ", указанная дата: " + to.toString() + 
                        ", разница: " + daysFromToday + " дней (" + hoursFromToday + " часов)", "to");
                }
                
            } catch (DateTimeParseException e) {
                throw new ValidationException("Невалидный формат конечной даты: " + toStr + 
                    ". Ожидается формат ISO 8601 (например: 2024-01-01T00:00:00Z). " +
                    "Ошибка парсинга: " + e.getMessage(), "to");
            }
        }
        
        // Валидация периода (если обе даты указаны)
        if (from != null && to != null) {
            if (from.isAfter(to)) {
                throw new ValidationException("Начальная дата не может быть позже конечной даты. " +
                    "Начальная дата: " + from.toString() + ", конечная дата: " + to.toString(), "dateRange");
            }
            
            // Проверяем, что период не превышает 14 дней (с учетом часов, минут, секунд)
            Instant maxToDate = from.plus(14, ChronoUnit.DAYS);
            if (to.isAfter(maxToDate)) {
                long daysBetween = ChronoUnit.DAYS.between(from, to);
                long hoursBetween = ChronoUnit.HOURS.between(from, to);
                throw new ValidationException("Период между датами не может превышать 14 дней. " +
                    "Начальная дата: " + from.toString() + ", конечная дата: " + to.toString() + 
                    ", получен период: " + daysBetween + " дней (" + hoursBetween + " часов)", "dateRange");
            }
        }
        
        return new TradingRequestParams(exchange, from, to);
    }
}