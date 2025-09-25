WITH target_date AS (
    SELECT '2025-09-23'::date as check_date
),
     evening_session_data AS (
         SELECT
             figi,
             price_date,
             close_price as evening_close_price,
             currency,
             exchange,
             instrument_type,
             created_at as evening_created_at
         FROM close_prices_evening_session
         WHERE price_date = (SELECT check_date FROM target_date)

     ),
     daily_candles_data AS (
         SELECT
             figi,
             DATE(time AT TIME ZONE 'Europe/Moscow') as candle_date,
             close as daily_close_price,
             time as candle_time,
             is_complete,
             created_at as candle_created_at
         FROM daily_candles
         WHERE DATE(time AT TIME ZONE 'Europe/Moscow') = (SELECT check_date FROM target_date)
           and figi NOT IN (SELECT figi FROM indicatives)
     ),
     comparison_data AS (
         SELECT
             COALESCE(e.figi, d.figi) as figi,
             COALESCE(e.price_date, d.candle_date) as check_date,
             e.evening_close_price,
             d.daily_close_price,
             e.currency,
             e.exchange,
             e.instrument_type,
             d.is_complete,
             e.evening_created_at,
             d.candle_created_at,
             CASE
                 WHEN e.figi IS NULL THEN 'MISSING_EVENING'
                 WHEN d.figi IS NULL THEN 'MISSING_DAILY'
                 WHEN ABS(e.evening_close_price - d.daily_close_price) > 0.000000001 THEN 'PRICE_MISMATCH'
                 ELSE 'MATCH'
                 END as status,
             CASE
                 WHEN e.figi IS NOT NULL AND d.figi IS NOT NULL
                     THEN ABS(e.evening_close_price - d.daily_close_price)
                 ELSE NULL
                 END as price_difference
         FROM evening_session_data e
                  FULL OUTER JOIN daily_candles_data d
                                  ON e.figi = d.figi AND e.price_date = d.candle_date
     )
-- Основной результат сверки
SELECT
    figi,
    check_date,
    status,
    evening_close_price,
    daily_close_price,
    price_difference,
    currency,
    exchange,
    instrument_type,
    is_complete,
    evening_created_at,
    candle_created_at
FROM comparison_data
--where instrument_type='FUTURE' --Для запуска в выходные
ORDER BY
    figi;