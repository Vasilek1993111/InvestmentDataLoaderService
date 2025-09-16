-- Обновленное создание материализованных представлений для агрегации объемов торгов
-- с аналитикой по торговым сессиям и средним объемом на свечу

-- Общее материализованное представление (обновляется ежедневно в 2:20)
-- Агрегирует данные по figi за все время
CREATE MATERIALIZED VIEW IF NOT EXISTS invest.daily_volume_aggregation AS
SELECT 
    figi,
    
    -- Общие метрики за все время
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
        WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
        OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 45)
        THEN volume 
        ELSE 0 
    END) as main_session_volume,
    COUNT(CASE 
        WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
        OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 45)
        THEN 1 
        ELSE NULL 
    END) as main_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 45)
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 45)
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 45)
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as main_avg_volume_per_candle,
    
    -- Вечерняя сессия (19:05-23:50)
    SUM(CASE 
        WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
        OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
        THEN volume 
        ELSE 0 
    END) as evening_session_volume,
    COUNT(CASE 
        WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
        OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
        THEN 1 
        ELSE NULL 
    END) as evening_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as evening_avg_volume_per_candle,
    
    -- Сессия выходного дня (если есть данные в выходные)
    SUM(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6) -- Воскресенье = 0, Суббота = 6
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
    NOW() as last_updated
FROM invest.candles
GROUP BY figi
ORDER BY figi;

-- Создание индекса для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_daily_volume_agg_figi 
ON invest.daily_volume_aggregation (figi);

-- Дневное материализованное представление (обновляется каждую минуту)
CREATE MATERIALIZED VIEW IF NOT EXISTS invest.today_volume_aggregation AS
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
        WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
        OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 45)
        THEN volume 
        ELSE 0 
    END) as main_session_volume,
    COUNT(CASE 
        WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
        OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 45)
        THEN 1 
        ELSE NULL 
    END) as main_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 45)
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 45)
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 45)
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as main_avg_volume_per_candle,
    
    -- Вечерняя сессия (19:05-23:50)
    SUM(CASE 
        WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
        OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
        THEN volume 
        ELSE 0 
    END) as evening_session_volume,
    COUNT(CASE 
        WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
        OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
        THEN 1 
        ELSE NULL 
    END) as evening_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as evening_avg_volume_per_candle,
    
    -- Сессия выходного дня (если есть данные в выходные)
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
    NOW() as last_updated
FROM invest.candles
WHERE DATE(time AT TIME ZONE 'Europe/Moscow') = CURRENT_DATE AT TIME ZONE 'Europe/Moscow'
GROUP BY figi
ORDER BY figi;

-- Создание индекса для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_today_volume_agg_figi 
ON invest.today_volume_aggregation (figi);
