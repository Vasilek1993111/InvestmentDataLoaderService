-- Скрипт сверки цен открытия из open_prices с daily_candles.open
-- Параметры: замените '2025-01-15' на нужную дату
-- Сравнивает только SHARES и FUTURE инструменты

WITH target_date AS (
    SELECT '2025-09-17'::date as check_date
),
     open_prices_data AS (
         SELECT
             figi,
             price_date,
             open_price as table_open_price,
             currency,
             exchange,
             instrument_type,
             created_at as table_created_at,
             updated_at as table_updated_at
         FROM open_prices
         WHERE price_date = (SELECT check_date FROM target_date)
           AND instrument_type IN ('share', 'future')
     ),
     daily_candles_data AS (
         SELECT
             figi,
             DATE(time AT TIME ZONE 'Europe/Moscow') as candle_date,
             open as candle_open_price,
             time as candle_time,
             is_complete,
             created_at as candle_created_at,
             updated_at as candle_updated_at
         FROM daily_candles
         WHERE DATE(time AT TIME ZONE 'Europe/Moscow') = (SELECT check_date FROM target_date)
           AND figi NOT IN (SELECT figi FROM indicatives)
     ),
     comparison_data AS (
         SELECT
             COALESCE(o.figi, d.figi) as figi,
             COALESCE(o.price_date, d.candle_date) as check_date,
             o.table_open_price,
             d.candle_open_price,
             o.currency,
             o.exchange,
             o.instrument_type,
             d.is_complete,
             o.table_created_at,
             o.table_updated_at,
             d.candle_created_at,
             d.candle_updated_at,
             CASE
                 WHEN o.figi IS NULL THEN 'MISSING_OPEN_PRICES'
                 WHEN d.figi IS NULL THEN 'MISSING_DAILY_CANDLES'
                 WHEN ABS(o.table_open_price - d.candle_open_price) > 0.000000001 THEN 'PRICE_MISMATCH'
                 ELSE 'MATCH'
                 END as status,
             CASE
                 WHEN o.figi IS NOT NULL AND d.figi IS NOT NULL
                     THEN ABS(o.table_open_price - d.candle_open_price)
                 ELSE NULL
                 END as price_difference,
             CASE
                 WHEN o.figi IS NOT NULL AND d.figi IS NOT NULL AND d.candle_open_price > 0
                     THEN ROUND(ABS(o.table_open_price - d.candle_open_price) * 100.0 / d.candle_open_price, 6)
                 ELSE NULL
                 END as price_difference_percent
         FROM open_prices_data o
                  FULL OUTER JOIN daily_candles_data d
                                  ON o.figi = d.figi AND o.price_date = d.candle_date
     )
-- Основной результат сверки
SELECT
    figi,
    check_date,
    status,
    table_open_price,
    candle_open_price,
    price_difference,
    price_difference_percent,
    currency,
    exchange,
    instrument_type,
    is_complete,
    table_created_at,
    table_updated_at,
    candle_created_at,
    candle_updated_at
FROM comparison_data
ORDER BY
    instrument_type,
    figi;
