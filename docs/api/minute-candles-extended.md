# Расширенные минутные свечи API

## Обзор

API для работы с минутными свечами был расширен для включения дополнительной статистики и анализа. Теперь каждая минутная свеча содержит расширенную информацию о ценовом движении, типах свечей и технических индикаторах.

## Новые поля в ответах

### MinuteCandleExtendedDto

Каждая минутная свеча теперь включает следующие дополнительные поля:

| Поле | Тип | Описание |
|------|-----|----------|
| `priceChange` | BigDecimal | Изменение цены (close - open) |
| `priceChangePercent` | BigDecimal | Процентное изменение цены |
| `candleType` | String | Тип свечи: BULLISH, BEARISH, DOJI |
| `bodySize` | BigDecimal | Размер тела свечи (абсолютное значение) |
| `upperShadow` | BigDecimal | Верхняя тень свечи |
| `lowerShadow` | BigDecimal | Нижняя тень свечи |
| `highLowRange` | BigDecimal | Диапазон цен (high - low) |
| `averagePrice` | BigDecimal | Средняя цена (high + low + open + close) / 4 |

## Эндпоинты

### GET /api/candles/instrument/minute/{figi}/{date}

Получение минутных свечей конкретного инструмента с расширенной статистикой.

**Параметры:**
- `figi` (String) - FIGI инструмента
- `date` (LocalDate) - Дата в формате YYYY-MM-DD

**Пример запроса:**
```http
GET /api/candles/instrument/minute/BBG004730N88/2025-09-18
```

**Пример ответа:**
```json
{
  "figi": "BBG004730N88",
  "date": "2025-09-18",
  "candles": [
    {
      "figi": "BBG004730N88",
      "ticker": "SBER",
      "name": "Сбербанк",
      "time": "2025-09-18T10:00:00Z",
      "open": 119.830000000,
      "close": 117.050000000,
      "high": 119.950000000,
      "low": 117.050000000,
      "volume": 1424143,
      "isComplete": true,
      "priceChange": -2.780000000,
      "priceChangePercent": -2.3200,
      "candleType": "BEARISH",
      "bodySize": 2.780000000,
      "upperShadow": 0.120000000,
      "lowerShadow": 0.000000000,
      "highLowRange": 2.900000000,
      "averagePrice": 118.02
    }
  ],
  "totalCandles": 1,
  "totalVolume": 1424143,
  "averagePrice": 118.02
}
```

### POST /api/candles/instrument/minute/{figi}/{date}

Асинхронное сохранение минутных свечей конкретного инструмента с расширенной статистикой.

**Параметры:**
- `figi` (String) - FIGI инструмента
- `date` (LocalDate) - Дата в формате YYYY-MM-DD

**Пример запроса:**
```http
POST /api/candles/instrument/minute/BBG004730N88/2025-09-18
```

**Пример ответа:**
```json
{
  "success": true,
  "message": "Асинхронное сохранение минутных свечей инструмента BBG004730N88 за 2025-09-18 запущено",
  "taskId": "550e8400-e29b-41d4-a716-446655440000",
  "endpoint": "/api/candles/instrument/minute/BBG004730N88/2025-09-18",
  "figi": "BBG004730N88",
  "date": "2025-09-18",
  "status": "STARTED",
  "startTime": "2025-09-18T10:00:00Z"
}
```

## Типы свечей

### BULLISH (Бычья)
- `close > open` - цена закрытия выше цены открытия
- Положительное изменение цены
- Зеленый цвет в графиках

### BEARISH (Медвежья)
- `close < open` - цена закрытия ниже цены открытия
- Отрицательное изменение цены
- Красный цвет в графиках

### DOJI (Доджи)
- `close = open` - цена закрытия равна цене открытия
- Нулевое изменение цены
- Нейтральный сигнал

## Технические индикаторы

### Body Size (Размер тела)
- Абсолютное значение изменения цены
- Показывает силу движения цены
- `bodySize = |close - open|`

### Upper Shadow (Верхняя тень)
- Расстояние от максимума до верхней границы тела
- `upperShadow = high - max(close, open)`

### Lower Shadow (Нижняя тень)
- Расстояние от минимума до нижней границы тела
- `lowerShadow = min(open, close) - low`

### High-Low Range (Диапазон)
- Общий диапазон цен за период
- `highLowRange = high - low`

### Average Price (Средняя цена)
- Среднее арифметическое всех цен
- `averagePrice = (high + low + open + close) / 4`

## База данных

### Новая структура таблицы minute_candles

```sql
-- Основные поля (существующие)
figi VARCHAR NOT NULL,
volume BIGINT NOT NULL,
high DECIMAL(18,9) NOT NULL,
low DECIMAL(18,9) NOT NULL,
time TIMESTAMP NOT NULL,
close DECIMAL(18,9) NOT NULL,
open DECIMAL(18,9) NOT NULL,
is_complete BOOLEAN NOT NULL,

-- Расширенная статистика (новые поля)
price_change DECIMAL(18,9),
price_change_percent DECIMAL(18,4),
candle_type VARCHAR(20),
body_size DECIMAL(18,9),
upper_shadow DECIMAL(18,9),
lower_shadow DECIMAL(18,9),
high_low_range DECIMAL(18,9),
average_price DECIMAL(18,2),

-- Служебные поля
created_at TIMESTAMP NOT NULL,
updated_at TIMESTAMP NOT NULL
```

### Автоматическое вычисление

Расширенная статистика автоматически вычисляется при:
- Создании новых записей
- Обновлении существующих записей
- Использовании триггера `trigger_calculate_minute_candle_statistics`

### Индексы

Для улучшения производительности созданы индексы:
- `idx_minute_candles_candle_type` - по типу свечи
- `idx_minute_candles_price_change` - по изменению цены
- `idx_minute_candles_average_price` - по средней цене

## Примеры использования

### Фильтрация по типу свечи
```sql
SELECT * FROM invest.minute_candles 
WHERE candle_type = 'BULLISH' 
AND figi = 'BBG004730N88';
```

### Поиск свечей с большим изменением цены
```sql
SELECT * FROM invest.minute_candles 
WHERE ABS(price_change_percent) > 5.0 
AND figi = 'BBG004730N88';
```

### Анализ волатильности
```sql
SELECT 
    figi,
    AVG(high_low_range) as avg_volatility,
    MAX(high_low_range) as max_volatility
FROM invest.minute_candles 
WHERE date(time) = '2025-09-18'
GROUP BY figi;
```

## Обратная совместимость

- Все существующие API эндпоинты продолжают работать
- Старые клиенты получат базовые поля свечей
- Новые клиенты получат расширенную статистику
- База данных обновляется автоматически при применении миграции

## Миграция

Для применения изменений выполните SQL скрипт:
```bash
psql -d your_database -f init-scripts/08-add-minute-candles-extended-statistics.sql
```

Скрипт:
1. Добавляет новые колонки в таблицу
2. Создает индексы для производительности
3. Обновляет существующие записи
4. Создает триггер для автоматического вычисления
5. Добавляет комментарии к колонкам
