-- Установка таймзоны для сессии
SET timezone = 'Europe/Moscow';

select count(*) from invest.shares s; --Проверка количества акций
select count(*) from invest.futures f; --Проверка количества фьючерсов
select count(*) from invest.indicatives i; --Проверка количества индикативных инструментов

select * from invest.shares s; --Проверка данных акций
select * from invest.futures f; --Проверка данных фьючерсов
select * from invest.indicatives i; --Проверка данных индикативных инструментов

select * from invest.shares s where s.figi = 'BBG004730N88'; --Проверка данных акции по FIGI
select * from invest.futures f where f.figi = 'BBG004730N88'; --Проверка данных фьючерса по FIGI
select * from invest.indicatives i where i.figi = 'BBG004730N88'; --Проверка данных индикативного инструмента по FIGI

select * from invest.shares s where s.ticker = 'AAPL'; --Проверка данных акции по тикеру


select * from invest.close_prices; Проверка цен закрытия основной сессии

select * from invest.close_prices cp 
join invest.shares s on cp.figi = s.figi 
where cp.price_date = '2025-09-19'; --Проверка цен закрытия основной сессии для акций за определенный день


select * from invest.close_prices cp 
join invest.futures s on cp.figi = f.figi; 
where cp.price_date = '2025-09-19'; --Проверка цен закрытия основной сессии для фьючерсов за определенный день


SELECT * FROM invest.shares s
WHERE NOT EXISTS (
    SELECT 1 FROM invest.close_prices cp
    WHERE cp.figi = s.figi
      AND cp.price_date = '2025-09-19'
); --Проверка отсутствия цен закрытия основной сессии для акций за определенный день



SELECT * FROM invest.futures f
WHERE NOT EXISTS (
    SELECT 1 FROM invest.close_prices cp
    WHERE cp.figi = f.figi
      AND cp.price_date = '2025-09-19'
); --Проверка отсутствия цен закрытия основной сессии для фьючерсов за определенный день



select * from invest.close_prices_evening_session cpes 
join invest.shares s on cpes.figi = s.figi 
where cpes.price_date = '2025-09-19'; --Проверка цен закрытия вечерней сессии для акций за определенный день


select * from invest.close_prices_evening_session  cpes 
join invest.futures s on cpes.figi = f.figi; 
where cpes.price_date = '2025-09-19'; --Проверка цен закрытия вечерней сессии для фьючерсов за определенный день


SELECT * FROM invest.shares s
WHERE NOT EXISTS (
    SELECT 1 FROM invest.close_prices_evening_session cpes
    WHERE cpes.figi = s.figi
      AND cpes.price_date = '2025-09-19'
); --Проверка отсутствия цен закрытия вечерней сессии для акций за определенный день



SELECT * FROM invest.futures f
WHERE NOT EXISTS (
    SELECT 1 FROM invest.close_prices cpes
    WHERE cpes.figi = f.figi
      AND cpes.price_date = '2025-09-19'
); --Проверка отсутствия цен закрытия вечерней сессии для фьючерсов за определенный день


--Проверка количества инструментов в БД
SELECT 
    'shares' as instrument_type,
    count(figi) as count
FROM shares
UNION ALL
SELECT 
    'futures' as instrument_type,
    count(figi) as count
FROM futures
UNION ALL
SELECT 
    'indicatives' as instrument_type,
    count(figi) as count
FROM indicatives
UNION ALL
SELECT 
    'TOTAL' as instrument_type,
    (
        (SELECT count(figi) FROM shares) +
        (SELECT count(figi) FROM futures) +
        (SELECT count(figi) FROM indicatives)
    ) as count;

--Создание партиций в цикле для таблицы minute_candles
DO $$
    DECLARE
        d DATE;
    BEGIN
        FOR d IN SELECT generate_series('2024-12-01'::date, '2024-12-31'::date, '1 day'::interval)::date
            LOOP
                PERFORM create_daily_partition_candles('minute_candles', d);
            END LOOP;
    END $$;