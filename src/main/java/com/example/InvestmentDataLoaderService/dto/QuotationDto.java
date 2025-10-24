package com.example.InvestmentDataLoaderService.dto;

import java.math.BigDecimal;

/**
 * DTO для котировки - денежная сумма без указания валюты.
 * Представляет цену в виде целой части (units) и дробной части (nano).
 * 
 * @param units целая часть суммы, может быть отрицательным числом
 * @param nano  дробная часть суммы, может быть отрицательным числом
 */
public record QuotationDto(
        long units,
        int nano) {

    /**
     * Создает QuotationDTO из BigDecimal цены.
     * 
     * @param price цена в виде BigDecimal
     * @return QuotationDTO объект
     */
    public static QuotationDto fromBigDecimal(BigDecimal price) {
        if (price == null) {
            return new QuotationDto(0L, 0);
        }
        // Нормализуем до 9 знаков после запятой для полной точности nano
        BigDecimal normalized = price.setScale(9, java.math.RoundingMode.HALF_UP);

        long units = normalized.longValue();
        BigDecimal fractionalPart = normalized.subtract(BigDecimal.valueOf(units));
        // Храним nano в 9-значном формате (стандартный Quotation формат)
        BigDecimal nanoDecimal = fractionalPart.multiply(BigDecimal.valueOf(1_000_000_000L));
        int nano = nanoDecimal.intValue();

        return new QuotationDto(units, nano);
    }

    /**
     * Преобразует QuotationDTO обратно в BigDecimal.
     * 
     * @return BigDecimal цена
     */
    public BigDecimal toBigDecimal() {
        // Стандартный Quotation формат: nano в масштабе 10^9
        BigDecimal value = BigDecimal.valueOf(units)
                .add(BigDecimal.valueOf(nano).divide(BigDecimal.valueOf(1_000_000_000L), 9,
                        java.math.RoundingMode.HALF_UP));
        // Убираем лишние нули в конце, сохраняя полную точность для minPriceIncrement
        return value.stripTrailingZeros();
    }

    /**
     * Проверяет, является ли котировка нулевой.
     * 
     * @return true если units и nano равны нулю
     */
    public boolean isZero() {
        return units == 0L && nano == 0;
    }

    /**
     * Проверяет, является ли котировка положительной.
     * 
     * @return true если котировка больше нуля
     */
    public boolean isPositive() {
        return units > 0L || (units == 0L && nano > 0);
    }

    /**
     * Создает QuotationDto из JSON объекта с полями units и nano.
     * Используется для преобразования данных из Tinkoff API.
     * 
     * @param jsonNode JSON узел с полями units (long) и nano (int)
     * @return QuotationDto объект
     */
    public static QuotationDto fromJsonNode(com.fasterxml.jackson.databind.JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return new QuotationDto(0L, 0);
        }
        
        long units = jsonNode.has("units") ? jsonNode.get("units").asLong() : 0L;
        int nano = jsonNode.has("nano") ? jsonNode.get("nano").asInt() : 0;
        
        return new QuotationDto(units, nano);
    }

    /**
     * Преобразует JSON объект minPriceIncrement в BigDecimal.
     * Удобный метод для работы с данными из Tinkoff API.
     * 
     * @param jsonNode JSON узел с полями units и nano
     * @return BigDecimal значение минимального шага цены
     */
    public static BigDecimal minPriceIncrementToBigDecimal(com.fasterxml.jackson.databind.JsonNode jsonNode) {
        QuotationDto quotation = fromJsonNode(jsonNode);
        return quotation.toBigDecimal();
    }
}