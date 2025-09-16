-- Обновленные функции для работы с сессионной аналитикой
-- daily_volume_aggregation теперь агрегирует данные по figi за все время
-- today_volume_aggregation остается с группировкой по дням

-- Функция обновления дневного представления (каждую минуту)
CREATE OR REPLACE FUNCTION invest.update_today_volume_aggregation()
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    today_date DATE;
    refreshed_count INTEGER := 0;
BEGIN
    -- Получаем текущую дату в московском времени
    today_date := CURRENT_DATE AT TIME ZONE 'Europe/Moscow';
    
    -- Обновляем дневное материализованное представление
    REFRESH MATERIALIZED VIEW invest.today_volume_aggregation;
    
    -- Получаем количество записей за сегодня для статистики
    SELECT COUNT(*) INTO refreshed_count
    FROM invest.today_volume_aggregation;
    
    RAISE NOTICE 'Обновлено дневное представление. Записей за %: %', today_date, refreshed_count;
END;
$$;

-- Функция обновления общего представления (ежедневно в 2:20)
CREATE OR REPLACE FUNCTION invest.update_daily_volume_aggregation()
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    refreshed_count INTEGER := 0;
BEGIN
    -- Обновляем общее материализованное представление
    REFRESH MATERIALIZED VIEW invest.daily_volume_aggregation;
    
    -- Получаем общее количество записей для статистики
    SELECT COUNT(*) INTO refreshed_count
    FROM invest.daily_volume_aggregation;
    
    RAISE NOTICE 'Обновлено общее представление. Всего записей: %', refreshed_count;
END;
$$;

-- Функция для получения общей статистики по сессиям (за все время)
CREATE OR REPLACE FUNCTION invest.get_session_analytics(
    p_figi TEXT DEFAULT NULL
)
RETURNS TABLE (
    figi TEXT,
    total_volume BIGINT,
    total_candles BIGINT,
    avg_volume_per_candle NUMERIC,
    morning_session_volume BIGINT,
    morning_session_candles BIGINT,
    morning_avg_volume_per_candle NUMERIC,
    main_session_volume BIGINT,
    main_session_candles BIGINT,
    main_avg_volume_per_candle NUMERIC,
    evening_session_volume BIGINT,
    evening_session_candles BIGINT,
    evening_avg_volume_per_candle NUMERIC,
    weekend_session_volume BIGINT,
    weekend_session_candles BIGINT,
    weekend_avg_volume_per_candle NUMERIC
)
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        dva.figi,
        dva.total_volume,
        dva.total_candles,
        dva.avg_volume_per_candle,
        dva.morning_session_volume,
        dva.morning_session_candles,
        dva.morning_avg_volume_per_candle,
        dva.main_session_volume,
        dva.main_session_candles,
        dva.main_avg_volume_per_candle,
        dva.evening_session_volume,
        dva.evening_session_candles,
        dva.evening_avg_volume_per_candle,
        dva.weekend_session_volume,
        dva.weekend_session_candles,
        dva.weekend_avg_volume_per_candle
    FROM invest.daily_volume_aggregation dva
    WHERE (p_figi IS NULL OR dva.figi = p_figi)
    ORDER BY dva.figi;
END;
$$;

-- Функция для получения статистики по сессиям за период (из таблицы candles)
CREATE OR REPLACE FUNCTION invest.get_session_analytics_by_period(
    p_figi TEXT DEFAULT NULL,
    p_start_date DATE DEFAULT NULL,
    p_end_date DATE DEFAULT NULL
)
RETURNS TABLE (
    figi TEXT,
    trade_date DATE,
    total_volume BIGINT,
    total_candles BIGINT,
    avg_volume_per_candle NUMERIC,
    morning_session_volume BIGINT,
    morning_session_candles BIGINT,
    morning_avg_volume_per_candle NUMERIC,
    main_session_volume BIGINT,
    main_session_candles BIGINT,
    main_avg_volume_per_candle NUMERIC,
    evening_session_volume BIGINT,
    evening_session_candles BIGINT,
    evening_avg_volume_per_candle NUMERIC,
    weekend_session_volume BIGINT,
    weekend_session_candles BIGINT,
    weekend_avg_volume_per_candle NUMERIC
)
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.figi,
        DATE(c.time AT TIME ZONE 'Europe/Moscow') as trade_date,
        SUM(c.volume) as total_volume,
        COUNT(*) as total_candles,
        ROUND(AVG(c.volume)::NUMERIC, 2) as avg_volume_per_candle,
        SUM(CASE 
            WHEN EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') = 9 
            THEN c.volume 
            ELSE 0 
        END) as morning_session_volume,
        COUNT(CASE 
            WHEN EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') = 9 
            THEN 1 
            ELSE NULL 
        END) as morning_session_candles,
        CASE 
            WHEN COUNT(CASE 
                WHEN EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') = 9 
                THEN 1 
                ELSE NULL 
            END) > 0 
            THEN ROUND((SUM(CASE 
                WHEN EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') = 9 
                THEN c.volume 
                ELSE 0 
            END)::NUMERIC / COUNT(CASE 
                WHEN EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') = 9 
                THEN 1 
                ELSE NULL 
            END)), 2)
            ELSE 0 
        END as morning_avg_volume_per_candle,
        SUM(CASE 
            WHEN EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
            OR (EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM c.time AT TIME ZONE 'Europe/Moscow') <= 45)
            THEN c.volume 
            ELSE 0 
        END) as main_session_volume,
        COUNT(CASE 
            WHEN EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
            OR (EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM c.time AT TIME ZONE 'Europe/Moscow') <= 45)
            THEN 1 
            ELSE NULL 
        END) as main_session_candles,
        CASE 
            WHEN COUNT(CASE 
                WHEN EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
                OR (EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM c.time AT TIME ZONE 'Europe/Moscow') <= 45)
                THEN 1 
                ELSE NULL 
            END) > 0 
            THEN ROUND((SUM(CASE 
                WHEN EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
                OR (EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM c.time AT TIME ZONE 'Europe/Moscow') <= 45)
                THEN c.volume 
                ELSE 0 
            END)::NUMERIC / COUNT(CASE 
                WHEN EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') BETWEEN 10 AND 17
                OR (EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM c.time AT TIME ZONE 'Europe/Moscow') <= 45)
                THEN 1 
                ELSE NULL 
            END)), 2)
            ELSE 0 
        END as main_avg_volume_per_candle,
        SUM(CASE 
            WHEN EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
            OR (EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM c.time AT TIME ZONE 'Europe/Moscow') <= 50)
            THEN c.volume 
            ELSE 0 
        END) as evening_session_volume,
        COUNT(CASE 
            WHEN EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
            OR (EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM c.time AT TIME ZONE 'Europe/Moscow') <= 50)
            THEN 1 
            ELSE NULL 
        END) as evening_session_candles,
        CASE 
            WHEN COUNT(CASE 
                WHEN EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
                OR (EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM c.time AT TIME ZONE 'Europe/Moscow') <= 50)
                THEN 1 
                ELSE NULL 
            END) > 0 
            THEN ROUND((SUM(CASE 
                WHEN EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
                OR (EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM c.time AT TIME ZONE 'Europe/Moscow') <= 50)
                THEN c.volume 
                ELSE 0 
            END)::NUMERIC / COUNT(CASE 
                WHEN EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') BETWEEN 19 AND 22
                OR (EXTRACT(HOUR FROM c.time AT TIME ZONE 'Europe/Moscow') = 23 AND EXTRACT(MINUTE FROM c.time AT TIME ZONE 'Europe/Moscow') <= 50)
                THEN 1 
                ELSE NULL 
            END)), 2)
            ELSE 0 
        END as evening_avg_volume_per_candle,
        SUM(CASE 
            WHEN EXTRACT(DOW FROM c.time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            THEN c.volume 
            ELSE 0 
        END) as weekend_session_volume,
        COUNT(CASE 
            WHEN EXTRACT(DOW FROM c.time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
            THEN 1 
            ELSE NULL 
        END) as weekend_session_candles,
        CASE 
            WHEN COUNT(CASE 
                WHEN EXTRACT(DOW FROM c.time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
                THEN 1 
                ELSE NULL 
            END) > 0 
            THEN ROUND((SUM(CASE 
                WHEN EXTRACT(DOW FROM c.time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
                THEN c.volume 
                ELSE 0 
            END)::NUMERIC / COUNT(CASE 
                WHEN EXTRACT(DOW FROM c.time AT TIME ZONE 'Europe/Moscow') IN (0, 6)
                THEN 1 
                ELSE NULL 
            END)), 2)
            ELSE 0 
        END as weekend_avg_volume_per_candle
    FROM invest.candles c
    WHERE 
        (p_figi IS NULL OR c.figi = p_figi)
        AND (p_start_date IS NULL OR DATE(c.time AT TIME ZONE 'Europe/Moscow') >= p_start_date)
        AND (p_end_date IS NULL OR DATE(c.time AT TIME ZONE 'Europe/Moscow') <= p_end_date)
    GROUP BY c.figi, DATE(c.time AT TIME ZONE 'Europe/Moscow')
    ORDER BY c.figi, DATE(c.time AT TIME ZONE 'Europe/Moscow');
END;
$$;

-- Функция для получения текущей статистики по сессиям (сегодня)
CREATE OR REPLACE FUNCTION invest.get_today_session_analytics(
    p_figi TEXT DEFAULT NULL
)
RETURNS TABLE (
    figi TEXT,
    trade_date DATE,
    total_volume BIGINT,
    total_candles BIGINT,
    avg_volume_per_candle NUMERIC,
    morning_session_volume BIGINT,
    morning_session_candles BIGINT,
    morning_avg_volume_per_candle NUMERIC,
    main_session_volume BIGINT,
    main_session_candles BIGINT,
    main_avg_volume_per_candle NUMERIC,
    evening_session_volume BIGINT,
    evening_session_candles BIGINT,
    evening_avg_volume_per_candle NUMERIC,
    weekend_session_volume BIGINT,
    weekend_session_candles BIGINT,
    weekend_avg_volume_per_candle NUMERIC
)
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        tva.figi,
        tva.trade_date,
        tva.total_volume,
        tva.total_candles,
        tva.avg_volume_per_candle,
        tva.morning_session_volume,
        tva.morning_session_candles,
        tva.morning_avg_volume_per_candle,
        tva.main_session_volume,
        tva.main_session_candles,
        tva.main_avg_volume_per_candle,
        tva.evening_session_volume,
        tva.evening_session_candles,
        tva.evening_avg_volume_per_candle,
        tva.weekend_session_volume,
        tva.weekend_session_candles,
        tva.weekend_avg_volume_per_candle
    FROM invest.today_volume_aggregation tva
    WHERE (p_figi IS NULL OR tva.figi = p_figi)
    ORDER BY tva.figi;
END;
$$;
