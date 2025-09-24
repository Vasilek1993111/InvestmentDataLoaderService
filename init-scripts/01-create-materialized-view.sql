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
    -- Утренняя сессия (6:59:59 - 09:59:59) - рабочие дни
    SUM(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5 -- Понедельник-Пятница
        AND (
            (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 6 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') = 59 AND EXTRACT(SECOND FROM time AT TIME ZONE 'Europe/Moscow') >= 59)
            OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 7 AND 8
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
        )
        THEN volume 
        ELSE 0 
    END) as morning_session_volume,
    COUNT(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
        AND (
            (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 6 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') = 59 AND EXTRACT(SECOND FROM time AT TIME ZONE 'Europe/Moscow') >= 59)
            OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 7 AND 8
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
        )
        THEN 1 
        ELSE NULL 
    END) as morning_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 6 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') = 59 AND EXTRACT(SECOND FROM time AT TIME ZONE 'Europe/Moscow') >= 59)
                OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 7 AND 8
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 6 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') = 59 AND EXTRACT(SECOND FROM time AT TIME ZONE 'Europe/Moscow') >= 59)
                OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 7 AND 8
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 6 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') = 59 AND EXTRACT(SECOND FROM time AT TIME ZONE 'Europe/Moscow') >= 59)
                OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 7 AND 8
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as morning_avg_volume_per_candle,
    
    -- Основная сессия (10:00:00 - 18:59:59) - рабочие дни
    SUM(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
        AND (
            EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
        )
        THEN volume 
        ELSE 0 
    END) as main_session_volume,
    COUNT(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
        AND (
            EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
        )
        THEN 1 
        ELSE NULL 
    END) as main_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as main_avg_volume_per_candle,
    
    -- Вечерняя сессия (19:00:00 - 23:50:00) - рабочие дни
    SUM(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
        AND (
            EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
        )
        THEN volume 
        ELSE 0 
    END) as evening_session_volume,
    COUNT(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
        AND (
            EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
        )
        THEN 1 
        ELSE NULL 
    END) as evening_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            )
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            )
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            )
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as evening_avg_volume_per_candle,
    
    -- Выходной день биржевая сессия (10:00:00 - 18:59:59) - выходные дни
    SUM(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6) -- Воскресенье = 0, Суббота = 6
        AND (
            EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
        )
        THEN volume 
        ELSE 0 
    END) as weekend_exchange_session_volume,
    COUNT(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
        AND (
            EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
        )
        THEN 1 
        ELSE NULL 
    END) as weekend_exchange_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as weekend_exchange_avg_volume_per_candle,
    
    -- Выходной день внебиржевая сессия (02:00:00 - 09:59:59 + 19:00:00 - 23:50:00) - выходные дни
    SUM(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
        AND (
            EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 2 AND 8
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
        )
        THEN volume 
        ELSE 0 
    END) as weekend_otc_session_volume,
    COUNT(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
        AND (
            EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 2 AND 8
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
        )
        THEN 1 
        ELSE NULL 
    END) as weekend_otc_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 2 AND 8
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
                OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            )
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 2 AND 8
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
                OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            )
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 2 AND 8
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
                OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            )
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as weekend_otc_avg_volume_per_candle,
    
    -- Временные метки (конвертируем в московское время)
    MIN(time) AT TIME ZONE 'Europe/Moscow' as first_candle_time,
    MAX(time) AT TIME ZONE 'Europe/Moscow' as last_candle_time,
    NOW() AT TIME ZONE 'Europe/Moscow' as last_updated
FROM invest.minute_candles
GROUP BY figi
ORDER BY figi;

-- Создание индекса для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_daily_volume_agg_figi 
ON invest.daily_volume_aggregation (figi);

-- Обычное дневное представление (нематериализованное) - данные только за сегодня
CREATE OR REPLACE VIEW invest.today_volume_view AS
SELECT 
    figi,
    CURRENT_DATE AT TIME ZONE 'Europe/Moscow' as trade_date,
    
    -- Общие метрики за сегодня
    SUM(volume) as total_volume,
    COUNT(*) as total_candles,
    ROUND(AVG(volume)::NUMERIC, 2) as avg_volume_per_candle,
    
    -- Аналитика по сессиям
    -- Утренняя сессия (6:59:59 - 09:59:59) - рабочие дни
    SUM(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5 -- Понедельник-Пятница
        AND (
            (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 6 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') = 59 AND EXTRACT(SECOND FROM time AT TIME ZONE 'Europe/Moscow') >= 59)
            OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 7 AND 8
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
        )
        THEN volume 
        ELSE 0 
    END) as morning_session_volume,
    COUNT(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
        AND (
            (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 6 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') = 59 AND EXTRACT(SECOND FROM time AT TIME ZONE 'Europe/Moscow') >= 59)
            OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 7 AND 8
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
        )
        THEN 1 
        ELSE NULL 
    END) as morning_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 6 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') = 59 AND EXTRACT(SECOND FROM time AT TIME ZONE 'Europe/Moscow') >= 59)
                OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 7 AND 8
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 6 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') = 59 AND EXTRACT(SECOND FROM time AT TIME ZONE 'Europe/Moscow') >= 59)
                OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 7 AND 8
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 6 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') = 59 AND EXTRACT(SECOND FROM time AT TIME ZONE 'Europe/Moscow') >= 59)
                OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 7 AND 8
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as morning_avg_volume_per_candle,
    
    -- Основная сессия (10:00:00 - 18:59:59) - рабочие дни
    SUM(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
        AND (
            EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
        )
        THEN volume 
        ELSE 0 
    END) as main_session_volume,
    COUNT(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
        AND (
            EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
        )
        THEN 1 
        ELSE NULL 
    END) as main_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as main_avg_volume_per_candle,
    
    -- Вечерняя сессия (19:00:00 - 23:50:00) - рабочие дни
    SUM(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
        AND (
            EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
        )
        THEN volume 
        ELSE 0 
    END) as evening_session_volume,
    COUNT(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
        AND (
            EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
        )
        THEN 1 
        ELSE NULL 
    END) as evening_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            )
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            )
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 1 AND 5
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            )
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as evening_avg_volume_per_candle,
    
    -- Выходной день биржевая сессия (10:00:00 - 18:59:59) - выходные дни
    SUM(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6) -- Воскресенье = 0, Суббота = 6
        AND (
            EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
        )
        THEN volume 
        ELSE 0 
    END) as weekend_exchange_session_volume,
    COUNT(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
        AND (
            EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
        )
        THEN 1 
        ELSE NULL 
    END) as weekend_exchange_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            )
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as weekend_exchange_avg_volume_per_candle,
    
    -- Выходной день внебиржевая сессия (02:00:00 - 09:59:59 + 19:00:00 - 23:50:00) - выходные дни
    SUM(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
        AND (
            EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 2 AND 8
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
        )
        THEN volume 
        ELSE 0 
    END) as weekend_otc_session_volume,
    COUNT(CASE 
        WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
        AND (
            EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 2 AND 8
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
            OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
            OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
        )
        THEN 1 
        ELSE NULL 
    END) as weekend_otc_session_candles,
    CASE 
        WHEN COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 2 AND 8
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
                OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            )
            THEN 1 
            ELSE NULL 
        END) > 0 
        THEN ROUND((SUM(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 2 AND 8
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
                OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            )
            THEN volume 
            ELSE 0 
        END)::NUMERIC / COUNT(CASE 
            WHEN EXTRACT(DOW FROM time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            AND (
                EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 2 AND 8
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 59)
                OR EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
                OR (EXTRACT(HOUR FROM time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM time AT TIME ZONE 'Europe/Moscow') <= 50)
            )
            THEN 1 
            ELSE NULL 
        END)), 2)
        ELSE 0 
    END as weekend_otc_avg_volume_per_candle,
    
    -- Временные метки
    MIN(time) as first_candle_time,
    MAX(time) as last_candle_time,
    NOW() AT TIME ZONE 'Europe/Moscow' as last_updated
FROM invest.minute_candles
WHERE 
    -- Фильтр только за сегодня
    DATE(time AT TIME ZONE 'Europe/Moscow') = CURRENT_DATE AT TIME ZONE 'Europe/Moscow'
GROUP BY figi
ORDER BY figi;

