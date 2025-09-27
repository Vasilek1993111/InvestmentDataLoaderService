# База данных

Схема по умолчанию: `invest`.

## 📊 Основные таблицы

### Инструменты

#### `shares` - Акции
**Первичный ключ:** `figi`
- `figi` (VARCHAR(255), NOT NULL) - Уникальный идентификатор инструмента (Financial Instrument Global Identifier)
- `ticker` (VARCHAR(255)) - Тикер акции
- `name` (VARCHAR(255)) - Название компании
- `currency` (VARCHAR(255)) - Валюта торговли
- `exchange` (VARCHAR(255)) - Биржа, на которой торгуется акция
- `sector` (VARCHAR(255)) - Сектор экономики
- `trading_status` (VARCHAR(255)) - Статус торговли
- `created_at` (TIMESTAMP WITH TIME ZONE) - Дата и время создания записи (московское время)
- `updated_at` (TIMESTAMP WITH TIME ZONE) - Дата и время последнего обновления записи (московское время)

#### `futures` - Фьючерсы
**Первичный ключ:** `figi`
- `figi` (VARCHAR(255), NOT NULL) - Уникальный идентификатор инструмента
- `asset_type` (VARCHAR(255)) - Тип базового актива (TYPE_SECURITY, TYPE_COMMODITY, TYPE_CURRENCY, TYPE_INDEX)
- `basic_asset` (VARCHAR(255)) - Базовый актив фьючерса
- `currency` (VARCHAR(255)) - Валюта инструмента
- `exchange` (VARCHAR(255)) - Биржа, на которой торгуется инструмент
- `ticker` (VARCHAR(255)) - Тикер инструмента
- `created_at` (TIMESTAMP(6), NOT NULL) - Дата и время создания записи
- `updated_at` (TIMESTAMP(6), NOT NULL) - Дата и время последнего обновления записи

#### `indicatives` - Индикативные инструменты
**Первичный ключ:** `figi`
- `figi` (VARCHAR(255), NOT NULL) - FIGI инструмента (уникальный идентификатор)
- `ticker` (VARCHAR(255), NOT NULL) - Тикер инструмента
- `name` (VARCHAR(255), NOT NULL) - Название инструмента
- `currency` (VARCHAR(255), NOT NULL) - Валюта инструмента
- `exchange` (VARCHAR(255), NOT NULL) - Биржа/площадка
- `class_code` (VARCHAR(255)) - Код класса инструмента
- `uid` (VARCHAR(255)) - Уникальный идентификатор
- `sell_available_flag` (BOOLEAN, DEFAULT FALSE) - Флаг доступности для продажи
- `buy_available_flag` (BOOLEAN, DEFAULT FALSE) - Флаг доступности для покупки
- `created_at` (TIMESTAMP WITH TIME ZONE) - Дата создания записи (UTC+3)
- `updated_at` (TIMESTAMP WITH TIME ZONE) - Дата последнего обновления записи (UTC+3)

### Рыночные данные

#### `minute_candles` - Минутные свечи
**Первичный ключ:** `(figi, time)`
**Партиционирование:** По дням (RANGE по time)
- `figi` (VARCHAR(255), NOT NULL) - Уникальный идентификатор инструмента
- `time` (TIMESTAMP(6) WITH TIME ZONE, NOT NULL) - Время начала минутной свечи в московской таймзоне
- `open` (NUMERIC(18,9), NOT NULL) - Цена открытия за минуту
- `high` (NUMERIC(18,9), NOT NULL) - Максимальная цена за минуту
- `low` (NUMERIC(18,9), NOT NULL) - Минимальная цена за минуту
- `close` (NUMERIC(18,9), NOT NULL) - Цена закрытия за минуту
- `volume` (BIGINT, NOT NULL) - Объем торгов за минуту (количество лотов)
- `is_complete` (BOOLEAN, NOT NULL) - Флаг завершенности свечи
- `price_change` (NUMERIC(18,9)) - Изменение цены (close - open)
- `price_change_percent` (NUMERIC(18,4)) - Процентное изменение цены
- `candle_type` (VARCHAR(20)) - Тип свечи: BULLISH, BEARISH, DOJI
- `body_size` (NUMERIC(18,9)) - Размер тела свечи (абсолютное значение изменения цены)
- `upper_shadow` (NUMERIC(18,9)) - Верхняя тень свечи
- `lower_shadow` (NUMERIC(18,9)) - Нижняя тень свечи
- `high_low_range` (NUMERIC(18,9)) - Диапазон цен (high - low)
- `average_price` (NUMERIC(18,2)) - Средняя цена (high + low + open + close) / 4
- `created_at` (TIMESTAMP(6) WITH TIME ZONE) - Время создания записи
- `updated_at` (TIMESTAMP(6) WITH TIME ZONE) - Время последнего обновления записи

#### `daily_candles` - Дневные свечи
**Первичный ключ:** `(figi, time)`
**Партиционирование:** По месяцам (RANGE по time)
- `figi` (VARCHAR(255), NOT NULL) - Уникальный идентификатор инструмента
- `time` (TIMESTAMP(6) WITH TIME ZONE, NOT NULL) - Дата торгов (начало дня)
- `open` (NUMERIC(18,9), NOT NULL) - Цена открытия дня
- `high` (NUMERIC(18,9), NOT NULL) - Максимальная цена дня
- `low` (NUMERIC(18,9), NOT NULL) - Минимальная цена дня
- `close` (NUMERIC(18,9), NOT NULL) - Цена закрытия дня
- `volume` (BIGINT, NOT NULL) - Общий объем торгов за день
- `is_complete` (BOOLEAN, NOT NULL) - Флаг завершенности свечи
- `price_change` (NUMERIC(18,9)) - Изменение цены (close - open)
- `price_change_percent` (NUMERIC(18,4)) - Процентное изменение цены
- `candle_type` (VARCHAR(20)) - Тип свечи: BULLISH, BEARISH, DOJI
- `body_size` (NUMERIC(18,9)) - Размер тела свечи
- `upper_shadow` (NUMERIC(18,9)) - Верхняя тень свечи
- `lower_shadow` (NUMERIC(18,9)) - Нижняя тень свечи
- `high_low_range` (NUMERIC(18,9)) - Диапазон цен
- `average_price` (NUMERIC(18,2)) - Средняя цена
- `created_at` (TIMESTAMP(6) WITH TIME ZONE) - Время создания записи
- `updated_at` (TIMESTAMP(6) WITH TIME ZONE) - Время последнего обновления записи

### Цены сессий

#### `close_prices` - Цены закрытия основной сессии
**Первичный ключ:** `(figi, price_date)`
**Партиционирование:** По месяцам (RANGE по price_date)
- `figi` (VARCHAR(255), NOT NULL) - Уникальный идентификатор инструмента
- `price_date` (DATE, NOT NULL) - Дата торгов
- `close_price` (NUMERIC(18,9), NOT NULL) - Цена закрытия инструмента
- `instrument_type` (VARCHAR(255), NOT NULL) - Тип финансового инструмента
- `currency` (VARCHAR(255), NOT NULL) - Валюта, в которой выражена цена
- `exchange` (VARCHAR(255), NOT NULL) - Биржа, на которой торгуется инструмент
- `created_at` (TIMESTAMP(6) WITH TIME ZONE) - Время создания записи
- `updated_at` (TIMESTAMP(6) WITH TIME ZONE) - Время последнего обновления записи

#### `close_prices_evening_session` - Цены закрытия вечерней сессии
**Первичный ключ:** `(price_date, figi)`
- `price_date` (DATE, NOT NULL) - Дата торгов
- `figi` (VARCHAR(255), NOT NULL) - Идентификатор инструмента
- `close_price` (DECIMAL(18,9), NOT NULL) - Цена закрытия вечерней сессии
- `instrument_type` (VARCHAR) - Тип инструмента (SHARES, FUTURES)
- `currency` (VARCHAR) - Валюта (RUB)
- `exchange` (VARCHAR) - Биржа (MOEX)
- `created_at` (TIMESTAMP) - Время создания записи

#### `open_prices` - Цены открытия утренней сессии
**Первичный ключ:** `(figi, price_date)`
**Партиционирование:** По месяцам (RANGE по price_date)
- `figi` (VARCHAR(255), NOT NULL) - Уникальный идентификатор инструмента
- `price_date` (DATE, NOT NULL) - Дата торгов
- `open_price` (NUMERIC(18,9), NOT NULL) - Цена открытия инструмента
- `instrument_type` (VARCHAR(255), NOT NULL) - Тип финансового инструмента
- `currency` (VARCHAR(255), NOT NULL) - Валюта, в которой выражена цена
- `exchange` (VARCHAR(255), NOT NULL) - Биржа, на которой торгуется инструмент
- `created_at` (TIMESTAMP(6) WITH TIME ZONE) - Время создания записи
- `updated_at` (TIMESTAMP(6) WITH TIME ZONE) - Время последнего обновления записи

### Последние сделки

#### `last_prices` - Последние цены
**Первичный ключ:** `(figi, time)`
**Партиционирование:** По дням (RANGE по time)
- `figi` (VARCHAR(255), NOT NULL) - Уникальный идентификатор инструмента
- `time` (TIMESTAMP(6) WITH TIME ZONE, NOT NULL) - Время последней сделки
- `price` (NUMERIC(18,9), NOT NULL) - Цена последней сделки
- `currency` (VARCHAR(255), NOT NULL) - Валюта сделки
- `exchange` (VARCHAR(255), NOT NULL) - Биржа
- `created_at` (TIMESTAMP(6) WITH TIME ZONE) - Время создания записи
- `updated_at` (TIMESTAMP(6) WITH TIME ZONE) - Время последнего обновления записи

### Сервисные таблицы

#### `system_logs` - Системные логи
**Первичный ключ:** `id`
- `id` (BIGSERIAL) - Уникальный идентификатор записи
- `task_id` (VARCHAR(255), NOT NULL) - ID задачи
- `endpoint` (VARCHAR(255), NOT NULL) - Название эндпоинта
- `method` (VARCHAR(255), NOT NULL) - HTTP метод
- `status` (VARCHAR(255), NOT NULL) - Статус выполнения
- `message` (TEXT, NOT NULL) - Текстовое сообщение о работе
- `start_time` (TIMESTAMP WITH TIME ZONE, NOT NULL) - Время начала
- `end_time` (TIMESTAMP WITH TIME ZONE) - Время завершения
- `duration_ms` (BIGINT) - Длительность в миллисекундах
- `created_at` (TIMESTAMP WITH TIME ZONE) - Время создания записи

#### `index_session_times` - Время закрытия сессий индексов
**Первичный ключ:** `figi`
- `figi` (VARCHAR(50), NOT NULL) - Уникальный идентификатор инструмента (FIGI)
- `ticker` (VARCHAR(20), NOT NULL) - Тикер индекса
- `name` (VARCHAR(255), NOT NULL) - Название индекса
- `session_close_time` (TIME, NOT NULL) - Время закрытия торговой сессии по Московскому времени
- `description` (TEXT) - Описание индекса
- `is_active` (BOOLEAN, DEFAULT TRUE) - Активна ли запись
- `created_at` (TIMESTAMP WITH TIME ZONE) - Дата и время создания записи
- `updated_at` (TIMESTAMP WITH TIME ZONE) - Дата и время последнего обновления записи

## 📈 Материализованные представления

### `daily_volume_aggregation` - Общее агрегирование по figi за все время
**Источник:** `minute_candles` (таймзона `Europe/Moscow`)

**Поля:**
- `figi` (VARCHAR) - Идентификатор инструмента
- `instrument_type` (TEXT) - Тип инструмента (share, future, unknown)
- `total_volume` (BIGINT) - Общий объем торгов за все время
- `total_candles` (BIGINT) - Общее количество свечей за все время
- `avg_volume_per_candle` (NUMERIC) - Средний объем на одну свечу, округлено до 2 знаков
- `morning_session_volume` (BIGINT) - Объем торгов в утренней сессии (06:59-09:59 МСК)
- `morning_session_candles` (BIGINT) - Количество свечей в утренней сессии
- `morning_avg_volume_per_candle` (NUMERIC) - Средний объем на свечу в утренней сессии
- `main_session_volume` (BIGINT) - Объем торгов в основной сессии (10:00-18:45 МСК)
- `main_session_candles` (BIGINT) - Количество свечей в основной сессии
- `main_avg_volume_per_candle` (NUMERIC) - Средний объем на свечу в основной сессии
- `evening_session_volume` (BIGINT) - Объем торгов в вечерней сессии (19:05-23:50 МСК)
- `evening_session_candles` (BIGINT) - Количество свечей в вечерней сессии
- `evening_avg_volume_per_candle` (NUMERIC) - Средний объем на свечу в вечерней сессии
- `weekend_exchange_session_volume` (BIGINT) - Объем торгов в выходные дни (биржевая сессия)
- `weekend_exchange_session_candles` (BIGINT) - Количество свечей в выходные дни (биржевая сессия)
- `weekend_exchange_avg_volume_per_candle` (NUMERIC) - Средний объем на свечу в выходные дни (биржевая сессия)
- `weekend_otc_session_volume` (BIGINT) - Объем торгов в выходные дни (OTC сессия)
- `weekend_otc_session_candles` (BIGINT) - Количество свечей в выходные дни (OTC сессия)
- `weekend_otc_avg_volume_per_candle` (NUMERIC) - Средний объем на свечу в выходные дни (OTC сессия)
- `first_candle_time` (TIMESTAMP) - Время первой свечи
- `last_candle_time` (TIMESTAMP) - Время последней свечи
- `last_updated` (TIMESTAMP) - Время последнего обновления записи

### `today_volume_aggregation` - Агрегирование за текущий день
**Источник:** `minute_candles` (таймзона `Europe/Moscow`)

**Поля:**
- `figi` (VARCHAR) - Идентификатор инструмента
- `instrument_type` (TEXT) - Тип инструмента (share, future, unknown)
- `trade_date` (DATE) - Дата торгов (текущая дата)
- `total_volume` (BIGINT) - Общий объем торгов за день
- `total_candles` (BIGINT) - Общее количество свечей за день
- `avg_volume_per_candle` (NUMERIC) - Средний объем на одну свечу за день
- `morning_session_volume` (BIGINT) - Объем торгов в утренней сессии
- `morning_session_candles` (BIGINT) - Количество свечей в утренней сессии
- `morning_avg_volume_per_candle` (NUMERIC) - Средний объем на свечу в утренней сессии
- `main_session_volume` (BIGINT) - Объем торгов в основной сессии
- `main_session_candles` (BIGINT) - Количество свечей в основной сессии
- `main_avg_volume_per_candle` (NUMERIC) - Средний объем на свечу в основной сессии
- `evening_session_volume` (BIGINT) - Объем торгов в вечерней сессии
- `evening_session_candles` (BIGINT) - Количество свечей в вечерней сессии
- `evening_avg_volume_per_candle` (NUMERIC) - Средний объем на свечу в вечерней сессии
- `weekend_exchange_session_volume` (BIGINT) - Объем торгов в выходные дни (биржевая сессия)
- `weekend_exchange_session_candles` (BIGINT) - Количество свечей в выходные дни (биржевая сессия)
- `weekend_exchange_avg_volume_per_candle` (NUMERIC) - Средний объем на свечу в выходные дни (биржевая сессия)
- `weekend_otc_session_volume` (BIGINT) - Объем торгов в выходные дни (OTC сессия)
- `weekend_otc_session_candles` (BIGINT) - Количество свечей в выходные дни (OTC сессия)
- `weekend_otc_avg_volume_per_candle` (NUMERIC) - Средний объем на свечу в выходные дни (OTC сессия)
- `first_candle_time` (TIMESTAMP) - Время первой свечи за день
- `last_candle_time` (TIMESTAMP) - Время последней свечи за день
- `last_updated` (TIMESTAMP) - Время последнего обновления записи

## 🔍 Обычные представления

### `today_volume_view` - Представление объемов за сегодня
**Источник:** `minute_candles` + `shares` + `futures`

**Поля:** Аналогичны `today_volume_aggregation`

**Особенности:**
- Обновляется в реальном времени
- Включает определение типа инструмента через JOIN с таблицами инструментов
- Показывает данные только за текущий день

## ⚙️ Функции и триггеры (PL/pgSQL)

### Функции вставки данных

#### `bulk_insert_last_prices(p_figi, p_time, p_price, p_currency, p_exchange)`
**Назначение:** Оптимизированная вставка данных с проверкой дневных партиций
**Параметры:**
- `p_figi` (VARCHAR) - FIGI инструмента
- `p_time` (TIMESTAMP) - Время сделки
- `p_price` (NUMERIC) - Цена сделки
- `p_currency` (VARCHAR) - Валюта
- `p_exchange` (VARCHAR) - Биржа

**Функциональность:**
- Автоматически создает дневные партиции для `last_prices`
- Выполняет вставку с обработкой конфликтов
- Использует `ON CONFLICT (figi, time) DO NOTHING`

### Функции обновления материализованных представлений

#### `update_today_volume_aggregation()`
**Назначение:** Обновление материализованного представления `today_volume_aggregation`
**Выполняет:** `REFRESH MATERIALIZED VIEW invest.today_volume_aggregation`

#### `update_daily_volume_aggregation()`
**Назначение:** Обновление материализованного представления `daily_volume_aggregation`
**Выполняет:** `REFRESH MATERIALIZED VIEW invest.daily_volume_aggregation`

### Аналитические функции

#### `get_session_analytics(figi, start_date, end_date)`
**Назначение:** Получение аналитики по сессиям за период
**Параметры:**
- `figi` (VARCHAR) - FIGI инструмента
- `start_date` (DATE) - Начальная дата
- `end_date` (DATE) - Конечная дата

**Возвращает:** Таблицу с полями:
- `figi, trade_date, total_volume, total_candles, avg_volume_per_candle`
- `morning_session_volume, morning_session_candles, morning_avg_volume_per_candle`
- `main_session_volume, main_session_candles, main_avg_volume_per_candle`
- `evening_session_volume, evening_session_candles, evening_avg_volume_per_candle`
- `weekend_exchange_session_volume, weekend_exchange_session_candles, weekend_exchange_avg_volume_per_candle`
- `weekend_otc_session_volume, weekend_otc_session_candles, weekend_otc_avg_volume_per_candle`

#### `get_today_session_analytics(figi)`
**Назначение:** Получение аналитики по сессиям за сегодня
**Параметры:**
- `figi` (VARCHAR) - FIGI инструмента

**Возвращает:** Аналогично `get_session_analytics` для текущего дня

### Триггеры

#### `calculate_minute_candle_statistics()`
**Назначение:** Автоматический расчет статистики для минутных свечей
**Срабатывает:** BEFORE INSERT OR UPDATE на `minute_candles`

**Вычисляет:**
- `price_change` = close - open
- `price_change_percent` = (price_change / open) * 100
- `candle_type` = BULLISH/BEARISH/DOJI
- `body_size` = ABS(price_change)
- `upper_shadow` = high - GREATEST(open, close)
- `lower_shadow` = LEAST(open, close) - low
- `high_low_range` = high - low
- `average_price` = (high + low + close) / 3

#### `calculate_daily_candle_statistics()`
**Назначение:** Автоматический расчет статистики для дневных свечей
**Срабатывает:** BEFORE INSERT OR UPDATE на `daily_candles`

**Вычисляет:** Аналогично `calculate_minute_candle_statistics`

#### `calculate_duration_ms()`
**Назначение:** Автоматический расчет длительности в миллисекундах
**Срабатывает:** BEFORE INSERT OR UPDATE на `system_logs`

**Вычисляет:**
- `duration_ms` = EXTRACT(EPOCH FROM (end_time - start_time)) * 1000

## 🔧 Партиционирование

### Минутные свечи (`minute_candles`)
**Тип партиционирования:** RANGE по `time`
**Партиции:** Ежедневные (по дням)
**Примеры партиций:**
- `minute_candles_2024_06_01` - данные за 1 июня 2024
- `minute_candles_2024_06_02` - данные за 2 июня 2024
- И т.д.

### Дневные свечи (`daily_candles`)
**Тип партиционирования:** RANGE по `time`
**Партиции:** Ежемесячные (по месяцам)
**Примеры партиций:**
- `daily_candles_2024_01` - данные за январь 2024
- `daily_candles_2024_02` - данные за февраль 2024
- И т.д.

### Цены закрытия (`close_prices`)
**Тип партиционирования:** RANGE по `price_date`
**Партиции:** Ежемесячные (по месяцам)
**Примеры партиций:**
- `close_prices_2024_01` - данные за январь 2024
- `close_prices_2024_02` - данные за февраль 2024
- И т.д.

### Цены открытия (`open_prices`)
**Тип партиционирования:** RANGE по `price_date`
**Партиции:** Ежемесячные (по месяцам)

### Последние цены (`last_prices`)
**Тип партиционирования:** RANGE по `time`
**Партиции:** Ежедневные (по дням)
**Автоматическое создание:** Через функцию `bulk_insert_last_prices`

## 📊 Индексы

### Основные индексы
- `minute_candles(figi, time)` - PK
- `daily_candles(figi, time)` - PK
- `close_prices(figi, price_date)` - PK
- `open_prices(figi, price_date)` - PK
- `last_prices(figi, time)` - PK
- `shares(figi)` - PK
- `futures(figi)` - PK
- `indicatives(figi)` - PK

### Дополнительные индексы
- `idx_minute_candles_time` - по времени
- `idx_minute_candles_figi_time` - по figi и времени
- `idx_close_prices_date` - по дате
- `idx_close_prices_figi_date` - по figi и дате
- `idx_system_logs_task_id` - по task_id
- `idx_system_logs_endpoint` - по эндпоинту
- `idx_system_logs_status` - по статусу

### Материализованные представления
- `daily_volume_aggregation(figi)` - UNIQUE
- `today_volume_aggregation(figi)` - UNIQUE

## 🔐 Права доступа

### Роли
- **postgres** - владелец схемы и таблиц
- **admin** - полные права на все таблицы
- **tester** - права только на чтение

### Права для admin
- `SELECT, INSERT, UPDATE, DELETE, REFERENCES, TRIGGER, TRUNCATE` на все таблицы
- `EXECUTE` на все функции
- `USAGE` на все последовательности

### Права для tester
- `SELECT` на все таблицы
- `UPDATE` на `system_logs`
- `USAGE` на последовательности

## 🕐 Таймзоны

**Основная таймзона:** `Europe/Moscow` (UTC+3)

### Использование таймзон
- Все временные метки хранятся в UTC
- При выборке конвертируются в московское время
- Планировщики работают по московскому времени
- Аналитика по сессиям рассчитывается в московском времени

### Временные интервалы сессий (МСК)
- **Утренняя сессия:** 06:59-09:59
- **Основная сессия:** 10:00-18:45
- **Вечерняя сессия:** 19:05-23:50
- **Выходные дни:** Суббота и воскресенье

## 📈 Производительность

### Партиционирование
- **Ежедневное** для `minute_candles` и `last_prices`
- **Ежемесячное** для `daily_candles`, `close_prices`, `open_prices`
- **Автоматическое создание** партиций через функции

### Индексирование
- **Составные индексы** по основным полям запросов
- **Покрывающие индексы** для часто используемых запросов
- **Партициальные индексы** для оптимизации

### Материализованные представления
- **Предварительно вычисленная** аналитика
- **Автоматическое обновление** через функции
- **Оптимизированные запросы** для отчетности

## 🔄 Обслуживание

### Ежедневные задачи
- Обновление `today_volume_aggregation`
- Создание новых партиций
- Очистка старых логов

### Еженедельные задачи
- Обновление `daily_volume_aggregation`
- Анализ производительности
- Проверка целостности данных

### Ежемесячные задачи
- Архивирование старых данных
- Оптимизация индексов
- Анализ использования пространства
