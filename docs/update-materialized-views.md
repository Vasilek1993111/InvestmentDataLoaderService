# Обновление материализованных представлений

## Добавление полей для среднего объема на свечу по сессиям

### Новые поля в материализованных представлениях:

1. **`morning_avg_volume_per_candle`** — средний объем на свечу в утренней сессии (09:00-10:00)
2. **`main_avg_volume_per_candle`** — средний объем на свечу в основной сессии (10:00-18:45)
3. **`evening_avg_volume_per_candle`** — средний объем на свечу в вечерней сессии (19:05-23:50)
4. **`weekend_avg_volume_per_candle`** — средний объем на свечу в выходные дни

### Порядок обновления:

#### Вариант 1: Полное пересоздание (рекомендуется)
```sql
-- Выполните этот скрипт для полного пересоздания представлений
\i init-scripts/04-update-existing-views.sql
```

#### Вариант 2: Поэтапное обновление
```sql
-- 1. Сначала создайте новые представления
\i init-scripts/01-create-materialized-view-updated.sql

-- 2. Затем создайте функции
\i init-scripts/02-create-session-analytics-functions.sql

-- 3. Удалите старые представления (если нужно)
DROP MATERIALIZED VIEW IF EXISTS invest.daily_volume_aggregation CASCADE;
DROP MATERIALIZED VIEW IF EXISTS invest.today_volume_aggregation CASCADE;

-- 4. Переименуйте новые представления
ALTER MATERIALIZED VIEW invest.daily_volume_aggregation_new RENAME TO daily_volume_aggregation;
ALTER MATERIALIZED VIEW invest.today_volume_aggregation_new RENAME TO today_volume_aggregation;
```

### Проверка обновления:

```sql
-- Проверяем структуру обновленных представлений
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_schema = 'invest' 
AND table_name = 'daily_volume_aggregation'
AND column_name LIKE '%avg_volume_per_candle%'
ORDER BY ordinal_position;

-- Тестируем новые функции
SELECT * FROM invest.get_session_analytics() LIMIT 5;
SELECT * FROM invest.get_today_session_analytics() LIMIT 5;
```

### Структура обновленных представлений:

#### daily_volume_aggregation и today_volume_aggregation теперь содержат:

**Общие поля:**
- `figi` — идентификатор инструмента
- `trade_date` — дата торгов
- `total_volume` — общий объем торгов за день
- `total_candles` — общее количество свечей за день
- `avg_volume_per_candle` — средний объем на одну свечу за весь день

**Утренняя сессия (09:00-10:00):**
- `morning_session_volume` — объем торгов в утренней сессии
- `morning_session_candles` — количество свечей в утренней сессии
- `morning_avg_volume_per_candle` — средний объем на свечу в утренней сессии

**Основная сессия (10:00-18:45):**
- `main_session_volume` — объем торгов в основной сессии
- `main_session_candles` — количество свечей в основной сессии
- `main_avg_volume_per_candle` — средний объем на свечу в основной сессии

**Вечерняя сессия (19:05-23:50):**
- `evening_session_volume` — объем торгов в вечерней сессии
- `evening_session_candles` — количество свечей в вечерней сессии
- `evening_avg_volume_per_candle` — средний объем на свечу в вечерней сессии

**Выходные дни:**
- `weekend_session_volume` — объем торгов в выходные дни
- `weekend_session_candles` — количество свечей в выходные дни
- `weekend_avg_volume_per_candle` — средний объем на свечу в выходные дни

**Временные метки:**
- `first_candle_time` — время первой свечи за день
- `last_candle_time` — время последней свечи за день
- `last_updated` — время последнего обновления записи

### API эндпоинты теперь будут возвращать:

- `/api/analytics/session-analytics` — полную аналитику по сессиям
- `/api/analytics/session-analytics/today` — дневную аналитику по сессиям
- `/api/analytics/session-analytics/summary` — сводную статистику по сессиям

Все эндпоинты включают поля `*_avg_volume_per_candle` для каждой сессии.
