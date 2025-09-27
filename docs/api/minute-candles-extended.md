# API — Расширенные минутные свечи (`/api/candles/instrument/minute`)

## Обзор

API для работы с минутными свечами конкретных инструментов с расширенной статистикой и анализом.

Предоставляет возможности для:
- **Расширенной статистики** - дополнительная информация о ценовом движении
- **Анализа свечей** - типы свечей и технические индикаторы
- **Асинхронной загрузки** - сохранение данных в базу
- **Синхронного получения** - получение данных без сохранения

**Базовый URL:** `http://localhost:8083/api/candles/instrument/minute` (PROD) / `http://localhost:8087/api/candles/instrument/minute` (TEST)

**Особенности:**
- **Расширенные поля** - дополнительная статистика для каждой свечи
- **Автоматические вычисления** - статистика рассчитывается автоматически
- **Типы свечей** - BULLISH, BEARISH, DOJI
- **Технические индикаторы** - размер тела, тени, диапазон цен

---

## 📊 Расширенные поля данных

### MinuteCandleExtendedDto

Каждая минутная свеча включает следующие дополнительные поля:

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

### Базовые поля

| Поле | Тип | Описание |
|------|-----|----------|
| `figi` | String | FIGI инструмента |
| `ticker` | String | Тикер инструмента |
| `name` | String | Название инструмента |
| `time` | Instant | Время свечи |
| `open` | BigDecimal | Цена открытия |
| `close` | BigDecimal | Цена закрытия |
| `high` | BigDecimal | Максимальная цена |
| `low` | BigDecimal | Минимальная цена |
| `volume` | Long | Объем торгов |
| `isComplete` | Boolean | Завершенность свечи |

---

## GET /api/candles/instrument/minute/{figi}/{date}

Получение минутных свечей конкретного инструмента с расширенной статистикой.

**Параметры запроса:**
- `figi` (path) - FIGI инструмента
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Получение свечей конкретного инструмента
curl "http://localhost:8083/api/candles/instrument/minute/BBG004730N88/2024-01-15"
```

**Ответ (пример):**
```json
{
  "figi": "BBG004730N88",
  "date": "2024-01-15",
  "candles": [
    {
      "figi": "BBG004730N88",
      "ticker": "SBER",
      "name": "Сбербанк",
      "time": "2024-01-15T10:00:00Z",
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

---

## POST /api/candles/instrument/minute/{figi}/{date}

Асинхронное сохранение минутных свечей конкретного инструмента с расширенной статистикой.

**Параметры запроса:**
- `figi` (path) - FIGI инструмента
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Сохранение свечей конкретного инструмента
curl -X POST "http://localhost:8083/api/candles/instrument/minute/BBG004730N88/2024-01-15"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Асинхронное сохранение минутных свечей инструмента BBG004730N88 за 2024-01-15 запущено",
  "taskId": "550e8400-e29b-41d4-a716-446655440000",
  "endpoint": "/api/candles/instrument/minute/BBG004730N88/2024-01-15",
  "figi": "BBG004730N88",
  "date": "2024-01-15",
  "status": "STARTED",
  "startTime": "2024-01-15T10:00:00Z"
}
```

---

## 🕯️ Типы свечей

### BULLISH (Бычья)
- **Условие:** `close > open` - цена закрытия выше цены открытия
- **Изменение:** Положительное изменение цены
- **Цвет:** Зеленый в графиках
- **Сигнал:** Рост цены

### BEARISH (Медвежья)
- **Условие:** `close < open` - цена закрытия ниже цены открытия
- **Изменение:** Отрицательное изменение цены
- **Цвет:** Красный в графиках
- **Сигнал:** Падение цены

### DOJI (Доджи)
- **Условие:** `close = open` - цена закрытия равна цене открытия
- **Изменение:** Нулевое изменение цены
- **Цвет:** Нейтральный
- **Сигнал:** Неопределенность

---

## 📈 Технические индикаторы

### Body Size (Размер тела)
- **Описание:** Абсолютное значение изменения цены
- **Назначение:** Показывает силу движения цены
- **Формула:** `bodySize = |close - open|`
- **Интерпретация:** Больший размер = сильнее движение

### Upper Shadow (Верхняя тень)
- **Описание:** Расстояние от максимума до верхней границы тела
- **Формула:** `upperShadow = high - max(close, open)`
- **Интерпретация:** Показывает сопротивление на максимуме

### Lower Shadow (Нижняя тень)
- **Описание:** Расстояние от минимума до нижней границы тела
- **Формула:** `lowerShadow = min(open, close) - low`
- **Интерпретация:** Показывает поддержку на минимуме

### High-Low Range (Диапазон)
- **Описание:** Общий диапазон цен за период
- **Формула:** `highLowRange = high - low`
- **Интерпретация:** Показывает волатильность

### Average Price (Средняя цена)
- **Описание:** Среднее арифметическое всех цен
- **Формула:** `averagePrice = (high + low + open + close) / 4`
- **Интерпретация:** Средняя цена за период

---

## 🗄️ База данных

### Структура таблицы minute_candles

```sql
-- Основные поля
figi VARCHAR NOT NULL,
volume BIGINT NOT NULL,
high DECIMAL(18,9) NOT NULL,
low DECIMAL(18,9) NOT NULL,
time TIMESTAMP NOT NULL,
close DECIMAL(18,9) NOT NULL,
open DECIMAL(18,9) NOT NULL,
is_complete BOOLEAN NOT NULL,

-- Расширенная статистика
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
- **Создании** новых записей
- **Обновлении** существующих записей
- **Триггере** `trigger_calculate_minute_candle_statistics`

### Индексы для производительности

- `idx_minute_candles_candle_type` - по типу свечи
- `idx_minute_candles_price_change` - по изменению цены
- `idx_minute_candles_average_price` - по средней цене
- `idx_minute_candles_figi_time` - по инструменту и времени
- `idx_minute_candles_date` - по дате

---

## 💡 Примеры использования

### Получение данных
```bash
# Получение свечей конкретного инструмента
curl "http://localhost:8083/api/candles/instrument/minute/BBG004730N88/2024-01-15"

# Сохранение свечей конкретного инструмента
curl -X POST "http://localhost:8083/api/candles/instrument/minute/BBG004730N88/2024-01-15"
```

### SQL запросы для анализа

#### Фильтрация по типу свечи
```sql
SELECT * FROM invest.minute_candles 
WHERE candle_type = 'BULLISH' 
AND figi = 'BBG004730N88';
```

#### Поиск свечей с большим изменением цены
```sql
SELECT * FROM invest.minute_candles 
WHERE ABS(price_change_percent) > 5.0 
AND figi = 'BBG004730N88';
```

#### Анализ волатильности
```sql
SELECT 
    figi,
    AVG(high_low_range) as avg_volatility,
    MAX(high_low_range) as max_volatility
FROM invest.minute_candles 
WHERE date(time) = '2024-01-15'
GROUP BY figi;
```

#### Статистика по типам свечей
```sql
SELECT 
    candle_type,
    COUNT(*) as count,
    AVG(price_change_percent) as avg_change
FROM invest.minute_candles 
WHERE date(time) = '2024-01-15'
GROUP BY candle_type;
```

---

## 🔧 Технические детали

### Архитектура
- **CandlesInstrumentController** - контроллер для инструментов
- **MinuteCandleService** - сервис для обработки свечей
- **Автоматические вычисления** - статистика рассчитывается автоматически
- **Триггеры БД** - обновление статистики при изменении данных

### Обратная совместимость
- **Существующие API** - продолжают работать без изменений
- **Старые клиенты** - получают базовые поля свечей
- **Новые клиенты** - получают расширенную статистику
- **Автоматическая миграция** - обновление БД при применении скрипта

### Миграция

Для применения изменений выполните SQL скрипт:
```bash
psql -d your_database -f init-scripts/08-add-minute-candles-extended-statistics.sql
```

**Что делает скрипт:**
1. **Добавляет колонки** - новые поля в таблицу
2. **Создает индексы** - для улучшения производительности
3. **Обновляет записи** - существующие данные
4. **Создает триггер** - для автоматического вычисления
5. **Добавляет комментарии** - описание колонок

---

## ⚠️ Коды ответов

### Успешные ответы
- **200 OK** - данные получены успешно
- **202 Accepted** - асинхронная операция запущена

### Ошибки
- **400 Bad Request** - некорректные параметры запроса
- **500 Internal Server Error** - внутренняя ошибка сервера

---

## 🔄 Жизненный цикл обработки

### Получение данных
1. **Запрос** - GET запрос с FIGI и датой
2. **Обработка** - получение данных из API
3. **Вычисление** - расчет расширенной статистики
4. **Форматирование** - преобразование в DTO
5. **Ответ** - возврат данных клиенту

### Сохранение данных
1. **Запрос** - POST запрос с FIGI и датой
2. **Генерация taskId** - уникальный идентификатор
3. **Запуск сервиса** - асинхронная обработка
4. **Вычисление** - расчет расширенной статистики
5. **Сохранение** - запись в базу данных
6. **Ответ** - возврат taskId клиенту
