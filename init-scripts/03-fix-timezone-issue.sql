-- Исправление проблемы с таймзоной и логикой дневного представления
-- Обновляет все временные поля и логику фильтрации для корректного отображения

-- 1. Пересоздаем дневное материализованное представление с правильной логикой
DROP MATERIALIZED VIEW IF EXISTS invest.today_volume_aggregation;

CREATE MATERIALIZED VIEW invest.today_volume_aggregation AS
SELECT 
    figi,
    CURRENT_DATE AT TIME ZONE 'Europe/Moscow' as trade_date,
    
    -- Общие метрики
    SUM(volume) as total_volume,
    COUNT(*) as total_candles,
    ROUND(AVG(volume)::NUMERIC, 2) as avg_volume_per_candle,
    
    -- Аналитика по сессиям
    -- Утренняя сессия (09:00-10:00)
    SUM(CASE 
        WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 
        THEN volume 
        ELSE 0 
    END) as morning_session_volume,
    COUNT(CASE 
        WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 
        THEN 1 
        ELSE NULL 
    END) as morning_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as morning_avg_volume_per_candle,
    
    -- Основная сессия (10:00-18:45)
    SUM(CASE 
        WHEN (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') >= 10 AND 
              EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') <= 17) OR
             (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND 
              EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 45)
        THEN volume 
        ELSE 0 
    END) as main_session_volume,
    COUNT(CASE 
        WHEN (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') >= 10 AND 
              EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') <= 17) OR
             (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND 
              EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 45)
        THEN 1 
        ELSE NULL 
    END) as main_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') >= 10 AND 
                  EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') <= 17) OR
                 (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND 
                  EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 45)
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') >= 10 AND 
                  EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') <= 17) OR
                 (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND 
                  EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 45)
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') >= 10 AND 
                  EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') <= 17) OR
                 (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND 
                  EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 45)
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as main_avg_volume_per_candle,
    
    -- Вечерняя сессия (19:00-23:50)
    SUM(CASE 
        WHEN (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') >= 19 AND 
              EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') <= 22) OR
             (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND 
              EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
        THEN volume 
        ELSE 0 
    END) as evening_session_volume,
    COUNT(CASE 
        WHEN (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') >= 19 AND 
              EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') <= 22) OR
             (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND 
              EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
        THEN 1 
        ELSE NULL 
    END) as evening_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') >= 19 AND 
                  EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') <= 22) OR
                 (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND 
                  EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') >= 19 AND 
                  EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') <= 22) OR
                 (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND 
                  EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') >= 19 AND 
                  EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') <= 22) OR
                 (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND 
                  EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as evening_avg_volume_per_candle,
    
    -- Выходные сессии (суббота и воскресенье)
    SUM(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
        THEN volume 
        ELSE 0 
    END) as weekend_session_volume,
    COUNT(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
        THEN 1 
        ELSE NULL 
    END) as weekend_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as weekend_avg_volume_per_candle,
    
    -- Временные метки
    MIN(time) as first_candle_time,
    MAX(time) as last_candle_time,
    NOW() AT TIME ZONE 'Europe/Moscow' as last_updated
FROM invest.candles
WHERE 
  -- Если сейчас время показа данных (07:00-23:59), показываем данные за сегодня
  EXTRACT(HOUR FROM NOW() AT TIME ZONE 'Europe/Moscow') >= 7 
  AND DATE(time AT TIME ZONE 'Europe/Moscow') = CURRENT_DATE AT TIME ZONE 'Europe/Moscow'
GROUP BY figi
ORDER BY figi;

-- 2. Создаем индексы для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_today_volume_agg_figi 
ON invest.today_volume_aggregation (figi);

-- 3. Обновляем представление
REFRESH MATERIALIZED VIEW invest.today_volume_aggregation;

-- 4. Проверяем результат
SELECT 
    'today_volume_aggregation' as view_name,
    COUNT(*) as records,
    MIN(first_candle_time) as earliest_candle,
    MAX(last_candle_time) as latest_candle,
    MAX(last_updated) as last_updated,
    'Current Moscow time: ' || NOW() AT TIME ZONE 'Europe/Moscow' as current_time
FROM invest.today_volume_aggregation;