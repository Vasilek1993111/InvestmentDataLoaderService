--Сравнение объемов дневных свечей и объемов минутных свечей по акциям
-- Проверка соответствия объемов дневных и минутных свечей для АКЦИЙ
-- Дата: 2025-09-19


--Рабочие дни c 7:00 до 23:50
--Выходные дни с 09:59 до 18:59
WITH shares_figi AS (
    -- Получаем список FIGI акций
    SELECT figi
    FROM invest.shares
),
     daily_candles_data AS (
         -- Получаем данные дневных свечей для акций
         SELECT
             dc.figi,
             DATE(dc.time AT TIME ZONE 'Europe/Moscow') as trade_date,
             dc.volume as daily_volume,
             dc.high as daily_high,
             dc.low as daily_low,
             dc.open as daily_open,
             dc.close as daily_close
         FROM daily_candles dc
                  INNER JOIN shares_figi sf ON dc.figi = sf.figi
         WHERE dc.is_complete = true  -- Только завершенные свечи
           AND DATE(dc.time AT TIME ZONE 'Europe/Moscow') >= '2025-09-11'
           AND DATE(dc.time AT TIME ZONE 'Europe/Moscow') < '2025-09-12'
     ),
     minute_candles_data AS (
         -- Агрегируем данные минутных свечей за каждый день для акций
         -- Для выходных дней учитываем только период с 10:00 до 19:00 по МСК
         SELECT
             mc.figi,
             DATE(mc.time AT TIME ZONE 'Europe/Moscow') as trade_date,
             SUM(mc.volume) as minute_volume_sum,
             MAX(mc.high) as minute_high,
             MIN(mc.low) as minute_low,
             -- Берем open первой свечи и close последней свечи
             (ARRAY_AGG(mc.open ORDER BY mc.time))[1] as minute_open,
             (ARRAY_AGG(mc.close ORDER BY mc.time DESC))[1] as minute_close
         FROM minute_candles mc
                  INNER JOIN shares_figi sf ON mc.figi = sf.figi
         WHERE mc.is_complete = true  -- Только завершенные свечи
           AND DATE(mc.time AT TIME ZONE 'Europe/Moscow') >= '2025-09-11'
           AND DATE(mc.time AT TIME ZONE 'Europe/Moscow') < '2025-09-12'
           -- Для выходных дней (суббота=6, воскресенье=0) фильтруем время с 10:00 до 19:00 по МСК
           AND (
             EXTRACT(DOW FROM mc.time AT TIME ZONE 'Europe/Moscow') NOT IN (0, 6) -- Рабочие дни
                 OR (
                 EXTRACT(DOW FROM mc.time AT TIME ZONE 'Europe/Moscow') IN (0, 6) -- Выходные дни
                     AND (
                     EXTRACT(HOUR FROM mc.time AT TIME ZONE 'Europe/Moscow') > 9
                         OR (EXTRACT(HOUR FROM mc.time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM mc.time AT TIME ZONE 'Europe/Moscow') >= 59)
                     )
                     AND (
                     EXTRACT(HOUR FROM mc.time AT TIME ZONE 'Europe/Moscow') < 18
                         OR (EXTRACT(HOUR FROM mc.time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM mc.time AT TIME ZONE 'Europe/Moscow') <= 59)
                     )
                 )
             )
         GROUP BY mc.figi, DATE(mc.time AT TIME ZONE 'Europe/Moscow')
     ),
     comparison AS (
         -- Сравниваем данные дневных и минутных свечей
         SELECT
             COALESCE(d.figi, m.figi) as figi,
             COALESCE(d.trade_date, m.trade_date) as trade_date,

             -- Объемы
             COALESCE(d.daily_volume, 0) as daily_volume,
             COALESCE(m.minute_volume_sum, 0) as minute_volume_sum,

             -- High цены
             d.daily_high,
             m.minute_high,

             -- Low цены
             d.daily_low,
             m.minute_low,

             -- Open и Close цены
             d.daily_open,
             m.minute_open,
             d.daily_close,
             m.minute_close,

             -- Статус наличия данных
             CASE
                 WHEN d.daily_volume IS NULL THEN 'MISSING_DAILY'
                 WHEN m.minute_volume_sum IS NULL THEN 'MISSING_MINUTE'
                 WHEN d.daily_volume = m.minute_volume_sum THEN 'MATCH'
                 ELSE 'MISMATCH'
                 END as volume_status,

             -- Статус сравнения High цен
             CASE
                 WHEN d.daily_high IS NULL OR m.minute_high IS NULL THEN 'MISSING_DATA'
                 WHEN ABS(d.daily_high - m.minute_high) < 0.01 THEN 'HIGH_MATCH'  -- Допуск 0.01
                 ELSE 'HIGH_MISMATCH'
                 END as high_status,

             -- Статус сравнения Low цен
             CASE
                 WHEN d.daily_low IS NULL OR m.minute_low IS NULL THEN 'MISSING_DATA'
                 WHEN ABS(d.daily_low - m.minute_low) < 0.01 THEN 'LOW_MATCH'  -- Допуск 0.01
                 ELSE 'LOW_MISMATCH'
                 END as low_status,

             -- Статус сравнения Open цен
             CASE
                 WHEN d.daily_open IS NULL OR m.minute_open IS NULL THEN 'MISSING_DATA'
                 WHEN ABS(d.daily_open - m.minute_open) < 0.01 THEN 'OPEN_MATCH'
                 ELSE 'OPEN_MISMATCH'
                 END as open_status,

             -- Статус сравнения Close цен
             CASE
                 WHEN d.daily_close IS NULL OR m.minute_close IS NULL THEN 'MISSING_DATA'
                 WHEN ABS(d.daily_close - m.minute_close) < 0.01 THEN 'CLOSE_MATCH'
                 ELSE 'CLOSE_MISMATCH'
                 END as close_status,

             -- Разности
             ABS(COALESCE(d.daily_volume, 0) - COALESCE(m.minute_volume_sum, 0)) as volume_difference,
             ABS(COALESCE(d.daily_high, 0) - COALESCE(m.minute_high, 0)) as high_difference,
             ABS(COALESCE(d.daily_low, 0) - COALESCE(m.minute_low, 0)) as low_difference,
             ABS(COALESCE(d.daily_open, 0) - COALESCE(m.minute_open, 0)) as open_difference,
             ABS(COALESCE(d.daily_close, 0) - COALESCE(m.minute_close, 0)) as close_difference

         FROM daily_candles_data d
                  FULL OUTER JOIN minute_candles_data m
                                  ON d.figi = m.figi AND d.trade_date = m.trade_date
     )
-- Основной результат проверки для АКЦИЙ
SELECT
    'АКЦИИ' as instrument_type,
    figi,
    trade_date,

    -- Объемы
    daily_volume,
    minute_volume_sum,
    volume_status,
    volume_difference,
    CASE
        WHEN daily_volume > 0 THEN
            ROUND((volume_difference::numeric / daily_volume::numeric) * 100, 2)
        ELSE 0
        END as volume_percentage_difference,

    -- High цены
    daily_high,
    minute_high,
    high_status,
    high_difference,
    CASE
        WHEN daily_high > 0 THEN
            ROUND((high_difference::numeric / daily_high::numeric) * 100, 4)
        ELSE 0
        END as high_percentage_difference,

    -- Low цены
    daily_low,
    minute_low,
    low_status,
    low_difference,
    CASE
        WHEN daily_low > 0 THEN
            ROUND((low_difference::numeric / daily_low::numeric) * 100, 4)
        ELSE 0
        END as low_percentage_difference,

    -- Open цены
    daily_open,
    minute_open,
    open_status,
    open_difference,
    CASE
        WHEN daily_open > 0 THEN
            ROUND((open_difference::numeric / daily_open::numeric) * 100, 4)
        ELSE 0
        END as open_percentage_difference,

    -- Close цены
    daily_close,
    minute_close,
    close_status,
    close_difference,
    CASE
        WHEN daily_close > 0 THEN
            ROUND((close_difference::numeric / daily_close::numeric) * 100, 4)
        ELSE 0
        END as close_percentage_difference,

    -- Общий статус дня
    CASE
        WHEN volume_status = 'MATCH'
            AND high_status = 'HIGH_MATCH'
            AND low_status = 'LOW_MATCH'
            AND open_status = 'OPEN_MATCH'
            AND close_status = 'CLOSE_MATCH' THEN 'PERFECT_MATCH'
        WHEN volume_status = 'MATCH'
            AND high_status = 'HIGH_MATCH'
            AND low_status = 'LOW_MATCH' THEN 'PRICE_MATCH'
        WHEN volume_status = 'MATCH' THEN 'VOLUME_MATCH'
        ELSE 'MISMATCH'
        END as overall_status

FROM comparison
--where figi IN ('TCS00A107J11')
ORDER BY
    overall_status,
    figi,
    trade_date;


-- Проверка соответствия объемов дневных и минутных свечей для ФЬЮЧЕРСОВ
-- Дата: 2025-09-19

WITH futures_figi AS (
    -- Получаем список FIGI фьючерсов
    SELECT figi
    FROM invest.futures
),
daily_candles_data AS (
    -- Получаем данные дневных свечей для фьючерсов
    SELECT
        dc.figi,
        DATE(dc.time AT TIME ZONE 'Europe/Moscow') as trade_date,
        dc.volume as daily_volume,
        dc.high as daily_high,
        dc.low as daily_low,
        dc.open as daily_open,
        dc.close as daily_close
    FROM daily_candles dc
    INNER JOIN futures_figi ff ON dc.figi = ff.figi
    WHERE dc.is_complete = true  -- Только завершенные свечи
      AND DATE(dc.time AT TIME ZONE 'Europe/Moscow') >= '2025-09-19'
      AND DATE(dc.time AT TIME ZONE 'Europe/Moscow') < '2025-09-20'
),
minute_candles_data AS (
    -- Агрегируем данные минутных свечей за каждый день для фьючерсов
    SELECT
        mc.figi,
        DATE(mc.time AT TIME ZONE 'Europe/Moscow') as trade_date,
        SUM(mc.volume) as minute_volume_sum,
        MAX(mc.high) as minute_high,
        MIN(mc.low) as minute_low,
        -- Берем open первой свечи и close последней свечи
        (ARRAY_AGG(mc.open ORDER BY mc.time))[1] as minute_open,
        (ARRAY_AGG(mc.close ORDER BY mc.time DESC))[1] as minute_close
    FROM minute_candles mc
    INNER JOIN futures_figi ff ON mc.figi = ff.figi
    WHERE mc.is_complete = true  -- Только завершенные свечи
      AND DATE(mc.time AT TIME ZONE 'Europe/Moscow') >= '2025-09-19'
      AND DATE(mc.time AT TIME ZONE 'Europe/Moscow') < '2025-09-20'
          -- Для выходных дней (суббота=6, воскресенье=0) фильтруем время с 09:59 до 18:59 по МСК
      AND (
          EXTRACT(DOW FROM mc.time AT TIME ZONE 'Europe/Moscow') NOT IN (0, 6) -- Рабочие дни
          OR (
              EXTRACT(DOW FROM mc.time AT TIME ZONE 'Europe/Moscow') IN (0, 6) -- Выходные дни
              AND (
                  EXTRACT(HOUR FROM mc.time AT TIME ZONE 'Europe/Moscow') > 9
                  OR (EXTRACT(HOUR FROM mc.time AT TIME ZONE 'Europe/Moscow') = 9 AND EXTRACT(MINUTE FROM mc.time AT TIME ZONE 'Europe/Moscow') >= 59)
              )
              AND (
                  EXTRACT(HOUR FROM mc.time AT TIME ZONE 'Europe/Moscow') < 18
                  OR (EXTRACT(HOUR FROM mc.time AT TIME ZONE 'Europe/Moscow') = 18 AND EXTRACT(MINUTE FROM mc.time AT TIME ZONE 'Europe/Moscow') <= 59)
              )
          )
      )
    GROUP BY mc.figi, DATE(mc.time AT TIME ZONE 'Europe/Moscow')
),
comparison AS (
    -- Сравниваем данные дневных и минутных свечей
    SELECT
        COALESCE(d.figi, m.figi) as figi,
        COALESCE(d.trade_date, m.trade_date) as trade_date,
        
        -- Объемы
        COALESCE(d.daily_volume, 0) as daily_volume,
        COALESCE(m.minute_volume_sum, 0) as minute_volume_sum,
        
        -- High цены
        d.daily_high,
        m.minute_high,
        
        -- Low цены
        d.daily_low,
        m.minute_low,
        
        -- Open и Close цены
        d.daily_open,
        m.minute_open,
        d.daily_close,
        m.minute_close,
        
        -- Статус наличия данных
        CASE
            WHEN d.daily_volume IS NULL THEN 'MISSING_DAILY'
            WHEN m.minute_volume_sum IS NULL THEN 'MISSING_MINUTE'
            WHEN d.daily_volume = m.minute_volume_sum THEN 'MATCH'
            ELSE 'MISMATCH'
            END as volume_status,
            
        -- Статус сравнения High цен
        CASE
            WHEN d.daily_high IS NULL OR m.minute_high IS NULL THEN 'MISSING_DATA'
            WHEN ABS(d.daily_high - m.minute_high) < 0.01 THEN 'HIGH_MATCH'  -- Допуск 0.01
            ELSE 'HIGH_MISMATCH'
            END as high_status,
            
        -- Статус сравнения Low цен
        CASE
            WHEN d.daily_low IS NULL OR m.minute_low IS NULL THEN 'MISSING_DATA'
            WHEN ABS(d.daily_low - m.minute_low) < 0.01 THEN 'LOW_MATCH'  -- Допуск 0.01
            ELSE 'LOW_MISMATCH'
            END as low_status,
            
        -- Статус сравнения Open цен
        CASE
            WHEN d.daily_open IS NULL OR m.minute_open IS NULL THEN 'MISSING_DATA'
            WHEN ABS(d.daily_open - m.minute_open) < 0.01 THEN 'OPEN_MATCH'
            ELSE 'OPEN_MISMATCH'
            END as open_status,
            
        -- Статус сравнения Close цен
        CASE
            WHEN d.daily_close IS NULL OR m.minute_close IS NULL THEN 'MISSING_DATA'
            WHEN ABS(d.daily_close - m.minute_close) < 0.01 THEN 'CLOSE_MATCH'
            ELSE 'CLOSE_MISMATCH'
            END as close_status,
        
        -- Разности
        ABS(COALESCE(d.daily_volume, 0) - COALESCE(m.minute_volume_sum, 0)) as volume_difference,
        ABS(COALESCE(d.daily_high, 0) - COALESCE(m.minute_high, 0)) as high_difference,
        ABS(COALESCE(d.daily_low, 0) - COALESCE(m.minute_low, 0)) as low_difference,
        ABS(COALESCE(d.daily_open, 0) - COALESCE(m.minute_open, 0)) as open_difference,
        ABS(COALESCE(d.daily_close, 0) - COALESCE(m.minute_close, 0)) as close_difference
        
    FROM daily_candles_data d
    FULL OUTER JOIN minute_candles_data m
        ON d.figi = m.figi AND d.trade_date = m.trade_date
)
-- Основной результат проверки для ФЬЮЧЕРСОВ
SELECT
    'ФЬЮЧЕРСЫ' as instrument_type,
    figi,
    trade_date,
    
    -- Объемы
    daily_volume,
    minute_volume_sum,
    volume_status,
    volume_difference,
    CASE
        WHEN daily_volume > 0 THEN
            ROUND((volume_difference::numeric / daily_volume::numeric) * 100, 2)
        ELSE 0
        END as volume_percentage_difference,
    
    -- High цены
    daily_high,
    minute_high,
    high_status,
    high_difference,
    CASE
        WHEN daily_high > 0 THEN
            ROUND((high_difference::numeric / daily_high::numeric) * 100, 4)
        ELSE 0
        END as high_percentage_difference,
    
    -- Low цены
    daily_low,
    minute_low,
    low_status,
    low_difference,
    CASE
        WHEN daily_low > 0 THEN
            ROUND((low_difference::numeric / daily_low::numeric) * 100, 4)
        ELSE 0
        END as low_percentage_difference,
    
    -- Open цены
    daily_open,
    minute_open,
    open_status,
    open_difference,
    CASE
        WHEN daily_open > 0 THEN
            ROUND((open_difference::numeric / daily_open::numeric) * 100, 4)
        ELSE 0
        END as open_percentage_difference,
    
    -- Close цены
    daily_close,
    minute_close,
    close_status,
    close_difference,
    CASE
        WHEN daily_close > 0 THEN
            ROUND((close_difference::numeric / daily_close::numeric) * 100, 4)
        ELSE 0
        END as close_percentage_difference,
    
    -- Общий статус дня
    CASE
        WHEN volume_status = 'MATCH' 
             AND high_status = 'HIGH_MATCH' 
             AND low_status = 'LOW_MATCH'
             AND open_status = 'OPEN_MATCH'
             AND close_status = 'CLOSE_MATCH' THEN 'PERFECT_MATCH'
        WHEN volume_status = 'MATCH' 
             AND high_status = 'HIGH_MATCH' 
             AND low_status = 'LOW_MATCH' THEN 'PRICE_MATCH'
        WHEN volume_status = 'MATCH' THEN 'VOLUME_MATCH'
        ELSE 'MISMATCH'
        END as overall_status

FROM comparison
ORDER BY
    overall_status,
    figi,
    trade_date;


--Проверка логов по задаче
select * from invest.system_logs sl
where sl.task_id='e2f1b351-a859-430b-b8fc-75f35db649c8'
--and sl.message LIKE '%FUTGOLD06260%'