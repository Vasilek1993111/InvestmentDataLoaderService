-- =====================================================
-- Оптимизированная таблица last_prices с дневным партиционированием
-- =====================================================

-- Удаляем существующую таблицу (если есть)
DROP TABLE IF EXISTS invest.last_prices CASCADE;

-- Создаем основную таблицу с партиционированием по дням
CREATE TABLE invest.last_prices (
    figi     VARCHAR(255)   NOT NULL, -- Уникальный идентификатор финансового инструмента (FIGI)
    time     TIMESTAMP(6)   NOT NULL, -- Время совершения сделки (в московском времени)
    currency VARCHAR(255)   NULL,     -- Валюта инструмента (RUB, USD, EUR и т.д.)
    exchange VARCHAR(255)   NULL,     -- Биржа, на которой совершена сделка (MOEX, FORTS и т.д.)
    price    NUMERIC(18, 9) NOT NULL, -- Цена сделки с точностью до 9 знаков после запятой
    
    -- Составной первичный ключ
    PRIMARY KEY (figi, time)
) 
PARTITION BY RANGE (time);

-- Добавляем комментарии к таблице и столбцам
COMMENT ON TABLE invest.last_prices IS 'Таблица обезличенных сделок с дневным партиционированием для оптимизации производительности';
COMMENT ON COLUMN invest.last_prices.figi IS 'Уникальный идентификатор финансового инструмента (FIGI)';
COMMENT ON COLUMN invest.last_prices.time IS 'Время совершения сделки (в московском времени)';
COMMENT ON COLUMN invest.last_prices.currency IS 'Валюта инструмента (RUB, USD, EUR и т.д.)';
COMMENT ON COLUMN invest.last_prices.exchange IS 'Биржа, на которой совершена сделка (MOEX, FORTS и т.д.)';
COMMENT ON COLUMN invest.last_prices.price IS 'Цена сделки с точностью до 9 знаков после запятой';

-- =====================================================
-- Создание дневных партиций на текущий месяц
-- =====================================================

-- Создаем партиции для текущего месяца (пример для января 2024)
CREATE TABLE invest.last_prices_2024_01_01 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-01 00:00:00') TO ('2024-01-02 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_01 IS 'Партиция сделок за 01.01.2024';

CREATE TABLE invest.last_prices_2024_01_02 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-02 00:00:00') TO ('2024-01-03 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_02 IS 'Партиция сделок за 02.01.2024';

CREATE TABLE invest.last_prices_2024_01_03 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-03 00:00:00') TO ('2024-01-04 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_03 IS 'Партиция сделок за 03.01.2024';

CREATE TABLE invest.last_prices_2024_01_04 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-04 00:00:00') TO ('2024-01-05 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_04 IS 'Партиция сделок за 04.01.2024';

CREATE TABLE invest.last_prices_2024_01_05 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-05 00:00:00') TO ('2024-01-06 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_05 IS 'Партиция сделок за 05.01.2024';

-- Создаем партиции для остальных дней января 2024
CREATE TABLE invest.last_prices_2024_01_06 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-06 00:00:00') TO ('2024-01-07 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_06 IS 'Партиция сделок за 06.01.2024';

CREATE TABLE invest.last_prices_2024_01_07 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-07 00:00:00') TO ('2024-01-08 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_07 IS 'Партиция сделок за 07.01.2024';

CREATE TABLE invest.last_prices_2024_01_08 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-08 00:00:00') TO ('2024-01-09 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_08 IS 'Партиция сделок за 08.01.2024';

CREATE TABLE invest.last_prices_2024_01_09 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-09 00:00:00') TO ('2024-01-10 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_09 IS 'Партиция сделок за 09.01.2024';

CREATE TABLE invest.last_prices_2024_01_10 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-10 00:00:00') TO ('2024-01-11 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_10 IS 'Партиция сделок за 10.01.2024';

-- Продолжаем для остальных дней...
CREATE TABLE invest.last_prices_2024_01_11 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-11 00:00:00') TO ('2024-01-12 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_11 IS 'Партиция сделок за 11.01.2024';

CREATE TABLE invest.last_prices_2024_01_12 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-12 00:00:00') TO ('2024-01-13 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_12 IS 'Партиция сделок за 12.01.2024';

CREATE TABLE invest.last_prices_2024_01_13 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-13 00:00:00') TO ('2024-01-14 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_13 IS 'Партиция сделок за 13.01.2024';

CREATE TABLE invest.last_prices_2024_01_14 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-14 00:00:00') TO ('2024-01-15 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_14 IS 'Партиция сделок за 14.01.2024';

CREATE TABLE invest.last_prices_2024_01_15 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-15 00:00:00') TO ('2024-01-16 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_15 IS 'Партиция сделок за 15.01.2024';

-- Создаем партиции для остальных дней января
CREATE TABLE invest.last_prices_2024_01_16 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-16 00:00:00') TO ('2024-01-17 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_16 IS 'Партиция сделок за 16.01.2024';

CREATE TABLE invest.last_prices_2024_01_17 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-17 00:00:00') TO ('2024-01-18 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_17 IS 'Партиция сделок за 17.01.2024';

CREATE TABLE invest.last_prices_2024_01_18 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-18 00:00:00') TO ('2024-01-19 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_18 IS 'Партиция сделок за 18.01.2024';

CREATE TABLE invest.last_prices_2024_01_19 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-19 00:00:00') TO ('2024-01-20 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_19 IS 'Партиция сделок за 19.01.2024';

CREATE TABLE invest.last_prices_2024_01_20 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-20 00:00:00') TO ('2024-01-21 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_20 IS 'Партиция сделок за 20.01.2024';

-- Создаем партиции для остальных дней января
CREATE TABLE invest.last_prices_2024_01_21 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-21 00:00:00') TO ('2024-01-22 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_21 IS 'Партиция сделок за 21.01.2024';

CREATE TABLE invest.last_prices_2024_01_22 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-22 00:00:00') TO ('2024-01-23 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_22 IS 'Партиция сделок за 22.01.2024';

CREATE TABLE invest.last_prices_2024_01_23 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-23 00:00:00') TO ('2024-01-24 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_23 IS 'Партиция сделок за 23.01.2024';

CREATE TABLE invest.last_prices_2024_01_24 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-24 00:00:00') TO ('2024-01-25 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_24 IS 'Партиция сделок за 24.01.2024';

CREATE TABLE invest.last_prices_2024_01_25 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-25 00:00:00') TO ('2024-01-26 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_25 IS 'Партиция сделок за 25.01.2024';

-- Создаем партиции для остальных дней января
CREATE TABLE invest.last_prices_2024_01_26 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-26 00:00:00') TO ('2024-01-27 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_26 IS 'Партиция сделок за 26.01.2024';

CREATE TABLE invest.last_prices_2024_01_27 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-27 00:00:00') TO ('2024-01-28 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_27 IS 'Партиция сделок за 27.01.2024';

CREATE TABLE invest.last_prices_2024_01_28 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-28 00:00:00') TO ('2024-01-29 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_28 IS 'Партиция сделок за 28.01.2024';

CREATE TABLE invest.last_prices_2024_01_29 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-29 00:00:00') TO ('2024-01-30 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_29 IS 'Партиция сделок за 29.01.2024';

CREATE TABLE invest.last_prices_2024_01_30 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-30 00:00:00') TO ('2024-01-31 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_30 IS 'Партиция сделок за 30.01.2024';

CREATE TABLE invest.last_prices_2024_01_31 PARTITION OF invest.last_prices
    FOR VALUES FROM ('2024-01-31 00:00:00') TO ('2024-02-01 00:00:00');
COMMENT ON TABLE invest.last_prices_2024_01_31 IS 'Партиция сделок за 31.01.2024';

-- =====================================================
-- Создание индексов для оптимизации запросов
-- =====================================================

-- Индекс по времени для быстрого поиска по дате
CREATE INDEX idx_last_prices_time ON invest.last_prices (time);
COMMENT ON INDEX idx_last_prices_time IS 'Индекс по времени для оптимизации запросов по дате';

-- Индекс по FIGI для быстрого поиска по инструменту
CREATE INDEX idx_last_prices_figi ON invest.last_prices (figi);
COMMENT ON INDEX idx_last_prices_figi IS 'Индекс по FIGI для оптимизации запросов по инструменту';

-- Составной индекс для запросов по FIGI и времени
CREATE INDEX idx_last_prices_figi_time ON invest.last_prices (figi, time);
COMMENT ON INDEX idx_last_prices_figi_time IS 'Составной индекс для оптимизации запросов по инструменту и времени';

-- Индекс по бирже для фильтрации по биржам
CREATE INDEX idx_last_prices_exchange ON invest.last_prices (exchange);
COMMENT ON INDEX idx_last_prices_exchange IS 'Индекс по бирже для фильтрации сделок по биржам';

-- =====================================================
-- Настройка прав доступа
-- =====================================================

-- Владелец таблицы
ALTER TABLE invest.last_prices OWNER TO postgres;

-- Права для тестировщика (только чтение)
GRANT SELECT ON invest.last_prices TO tester;

-- Права для администратора (полный доступ)
GRANT DELETE, INSERT, REFERENCES, SELECT, TRIGGER, TRUNCATE, UPDATE ON invest.last_prices TO admin;

-- =====================================================
-- Создание функции для автоматического создания дневных партиций
-- =====================================================

CREATE OR REPLACE FUNCTION invest.create_last_prices_daily_partition(target_date DATE)
RETURNS VOID AS $$
DECLARE
    partition_name TEXT;
    start_date DATE;
    end_date DATE;
BEGIN
    -- Определяем имя партиции (YYYY_MM_DD)
    partition_name := 'last_prices_' || TO_CHAR(target_date, 'YYYY_MM_DD');
    
    -- Определяем диапазон дат (день)
    start_date := target_date;
    end_date := target_date + INTERVAL '1 day';
    
    -- Проверяем, существует ли уже партиция
    IF NOT EXISTS (
        SELECT 1 FROM pg_tables 
        WHERE schemaname = 'invest' 
        AND tablename = partition_name
    ) THEN
        -- Создаем партицию
        EXECUTE format('CREATE TABLE invest.%I PARTITION OF invest.last_prices
            FOR VALUES FROM (%L) TO (%L)',
            partition_name,
            start_date,
            end_date
        );
        
        -- Добавляем комментарий
        EXECUTE format('COMMENT ON TABLE invest.%I IS %L',
            partition_name,
            'Партиция сделок за ' || TO_CHAR(target_date, 'DD.MM.YYYY')
        );
        
        RAISE NOTICE 'Создана дневная партиция % для даты %', partition_name, target_date;
    ELSE
        RAISE NOTICE 'Дневная партиция % уже существует', partition_name;
    END IF;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION invest.create_last_prices_daily_partition(DATE) IS 'Функция для автоматического создания дневных партиций';

-- =====================================================
-- Создание функции для массового создания дневных партиций
-- =====================================================

CREATE OR REPLACE FUNCTION invest.create_daily_partitions_for_period(
    start_date DATE,
    end_date DATE
)
RETURNS VOID AS $$
DECLARE
    current_day DATE;  -- Переименовал переменную чтобы избежать конфликта
    partition_name TEXT;
    next_day DATE;
BEGIN
    current_day := start_date;
    
    WHILE current_day <= end_date LOOP
        partition_name := 'last_prices_' || TO_CHAR(current_day, 'YYYY_MM_DD');
        
        -- Проверяем, существует ли партиция
        IF NOT EXISTS (
            SELECT 1 FROM pg_tables 
            WHERE schemaname = 'invest' 
            AND tablename = partition_name
        ) THEN
            next_day := current_day + INTERVAL '1 day';
            
            -- Создаем партицию
            EXECUTE format('CREATE TABLE invest.%I PARTITION OF invest.last_prices
                FOR VALUES FROM (%L) TO (%L)',
                partition_name,
                current_day,
                next_day
            );
            
            -- Добавляем комментарий
            EXECUTE format('COMMENT ON TABLE invest.%I IS %L',
                partition_name,
                'Партиция сделок за ' || TO_CHAR(current_day, 'DD.MM.YYYY')
            );
            
            RAISE NOTICE 'Создана дневная партиция %', partition_name;
        END IF;
        
        current_day := current_day + INTERVAL '1 day';
    END LOOP;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION invest.create_daily_partitions_for_period(DATE, DATE) IS 'Функция для массового создания дневных партиций за период';

-- =====================================================
-- Создание триггера для автоматического создания дневных партиций
-- =====================================================

CREATE OR REPLACE FUNCTION invest.last_prices_daily_partition_trigger()
RETURNS TRIGGER AS $$
DECLARE
    partition_name TEXT;
    target_date DATE;
BEGIN
    -- Определяем дату и имя партиции
    target_date := NEW.time::DATE;
    partition_name := 'last_prices_' || TO_CHAR(target_date, 'YYYY_MM_DD');
    
    -- Проверяем, существует ли партиция
    IF NOT EXISTS (
        SELECT 1 FROM pg_tables 
        WHERE schemaname = 'invest' 
        AND tablename = partition_name
    ) THEN
        -- Создаем партицию автоматически
        PERFORM invest.create_last_prices_daily_partition(target_date);
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION invest.last_prices_daily_partition_trigger() IS 'Триггер для автоматического создания дневных партиций при вставке данных';

-- Создаем триггер
CREATE TRIGGER trg_last_prices_daily_partition
    BEFORE INSERT ON invest.last_prices
    FOR EACH ROW
    EXECUTE FUNCTION invest.last_prices_daily_partition_trigger();

-- =====================================================
-- Создание функции для очистки старых дневных партиций
-- =====================================================

CREATE OR REPLACE FUNCTION invest.cleanup_old_daily_partitions(retention_days INTEGER DEFAULT 30)
RETURNS VOID AS $$
DECLARE
    partition_record RECORD;
    cutoff_date DATE;
BEGIN
    -- Определяем дату отсечения
    cutoff_date := CURRENT_DATE - (retention_days * INTERVAL '1 day');
    
    -- Находим партиции старше указанного периода
    FOR partition_record IN
        SELECT tablename 
        FROM pg_tables 
        WHERE schemaname = 'invest' 
        AND tablename LIKE 'last_prices_%'
        AND tablename ~ '^last_prices_\d{4}_\d{2}_\d{2}$'
        AND TO_DATE(SUBSTRING(tablename FROM 'last_prices_(\d{4}_\d{2}_\d{2})'), 'YYYY_MM_DD') < cutoff_date
    LOOP
        -- Удаляем старую партицию
        EXECUTE 'DROP TABLE IF EXISTS invest.' || partition_record.tablename || ' CASCADE';
        RAISE NOTICE 'Удалена старая дневная партиция %', partition_record.tablename;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION invest.cleanup_old_daily_partitions(INTEGER) IS 'Функция для очистки старых дневных партиций (по умолчанию старше 30 дней)';

-- =====================================================
-- Создание представления для статистики дневных партиций
-- =====================================================

CREATE OR REPLACE VIEW invest.last_prices_daily_partition_stats AS
SELECT 
    schemaname,
    tablename as partition_name,
    TO_DATE(SUBSTRING(tablename FROM 'last_prices_(\d{4}_\d{2}_\d{2})'), 'YYYY_MM_DD') as partition_date,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
    pg_total_relation_size(schemaname||'.'||tablename) as size_bytes
FROM pg_tables 
WHERE schemaname = 'invest' 
AND tablename LIKE 'last_prices_%'
AND tablename ~ '^last_prices_\d{4}_\d{2}_\d{2}$'
ORDER BY partition_date DESC;

COMMENT ON VIEW invest.last_prices_daily_partition_stats IS 'Статистика по дневным партициям таблицы last_prices';

-- =====================================================
-- Создание функции для получения статистики по дневным партициям
-- =====================================================

CREATE OR REPLACE FUNCTION invest.get_daily_partition_info()
RETURNS TABLE (
    partition_name TEXT,
    partition_date DATE,
    row_count BIGINT,
    size_pretty TEXT,
    size_bytes BIGINT,
    min_time TIMESTAMP,
    max_time TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        p.tablename::TEXT as partition_name,
        TO_DATE(SUBSTRING(p.tablename FROM 'last_prices_(\d{4}_\d{2}_\d{2})'), 'YYYY_MM_DD') as partition_date,
        COALESCE(s.n_tup_ins - s.n_tup_del, 0) as row_count,
        pg_size_pretty(pg_total_relation_size('invest.'||p.tablename)) as size_pretty,
        pg_total_relation_size('invest.'||p.tablename) as size_bytes,
        (SELECT MIN(time) FROM invest.last_prices WHERE 
         time::DATE = TO_DATE(SUBSTRING(p.tablename FROM 'last_prices_(\d{4}_\d{2}_\d{2})'), 'YYYY_MM_DD')) as min_time,
        (SELECT MAX(time) FROM invest.last_prices WHERE 
         time::DATE = TO_DATE(SUBSTRING(p.tablename FROM 'last_prices_(\d{4}_\d{2}_\d{2})'), 'YYYY_MM_DD')) as max_time
    FROM pg_tables p
    LEFT JOIN pg_stat_user_tables s ON s.relname = p.tablename AND s.schemaname = p.schemaname
    WHERE p.schemaname = 'invest' 
    AND p.tablename LIKE 'last_prices_%'
    AND p.tablename ~ '^last_prices_\d{4}_\d{2}_\d{2}$'
    ORDER BY partition_date DESC;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION invest.get_daily_partition_info() IS 'Функция для получения детальной информации о дневных партициях';

-- =====================================================
-- Создание функции для массовой вставки с оптимизацией
-- =====================================================

CREATE OR REPLACE FUNCTION invest.bulk_insert_last_prices(
    p_figi VARCHAR(255),
    p_time TIMESTAMP(6),
    p_price NUMERIC(18, 9),
    p_currency VARCHAR(255),
    p_exchange VARCHAR(255)
)
RETURNS VOID AS $$
DECLARE
    partition_name TEXT;
    target_date DATE;
BEGIN
    -- Определяем дату и партицию
    target_date := p_time::DATE;
    partition_name := 'last_prices_' || TO_CHAR(target_date, 'YYYY_MM_DD');
    
    -- Проверяем существование партиции
    IF NOT EXISTS (
        SELECT 1 FROM pg_tables 
        WHERE schemaname = 'invest' 
        AND tablename = partition_name
    ) THEN
        PERFORM invest.create_last_prices_daily_partition(target_date);
    END IF;
    
    -- Вставляем данные
    INSERT INTO invest.last_prices (figi, time, price, currency, exchange)
    VALUES (p_figi, p_time, p_price, p_currency, p_exchange)
    ON CONFLICT (figi, time) DO NOTHING;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION invest.bulk_insert_last_prices IS 'Функция для оптимизированной вставки данных с проверкой дневных партиций';

-- =====================================================
-- Создание функции для получения статистики по сделкам
-- =====================================================

CREATE OR REPLACE FUNCTION invest.get_last_prices_daily_stats(
    p_start_date DATE DEFAULT NULL,
    p_end_date DATE DEFAULT NULL,
    p_figi VARCHAR(255) DEFAULT NULL
)
RETURNS TABLE (
    trade_date DATE,
    total_trades BIGINT,
    unique_instruments BIGINT,
    avg_price NUMERIC(18, 9),
    min_price NUMERIC(18, 9),
    max_price NUMERIC(18, 9)
) AS $$
DECLARE
    start_date DATE;
    end_date DATE;
BEGIN
    -- Устанавливаем даты по умолчанию
    start_date := COALESCE(p_start_date, CURRENT_DATE - INTERVAL '7 days');
    end_date := COALESCE(p_end_date, CURRENT_DATE);
    
    RETURN QUERY
    SELECT 
        time::DATE as trade_date,
        COUNT(*) as total_trades,
        COUNT(DISTINCT figi) as unique_instruments,
        AVG(price) as avg_price,
        MIN(price) as min_price,
        MAX(price) as max_price
    FROM invest.last_prices
    WHERE time >= start_date 
    AND time < end_date + INTERVAL '1 day'
    AND (p_figi IS NULL OR figi = p_figi)
    GROUP BY time::DATE
    ORDER BY trade_date DESC;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION invest.get_last_prices_daily_stats IS 'Функция для получения дневной статистики по сделкам';

-- =====================================================
-- Примеры использования
-- =====================================================

/*
-- Создание дневной партиции для конкретной даты
SELECT invest.create_last_prices_daily_partition('2024-02-01'::DATE);

-- Создание дневных партиций за период
SELECT invest.create_daily_partitions_for_period('2024-02-01'::DATE, '2024-02-29'::DATE);

-- Получение статистики по дневным партициям
SELECT * FROM invest.last_prices_daily_partition_stats;

-- Получение детальной информации о дневных партициях
SELECT * FROM invest.get_daily_partition_info();

-- Очистка старых дневных партиций (старше 30 дней)
SELECT invest.cleanup_old_daily_partitions(30);

-- Вставка данных (партиция создастся автоматически)
INSERT INTO invest.last_prices (figi, time, price, currency, exchange) 
VALUES ('BBG004730N88', '2024-01-15 10:30:00', 123.45, 'RUB', 'MOEX');

-- Использование функции массовой вставки
SELECT invest.bulk_insert_last_prices(
    'BBG004730N88', 
    '2024-01-15 10:30:00'::TIMESTAMP, 
    123.45, 
    'RUB', 
    'MOEX'
);

-- Получение дневной статистики за последние 7 дней
SELECT * FROM invest.get_last_prices_daily_stats();

-- Получение дневной статистики за конкретный период
SELECT * FROM invest.get_last_prices_daily_stats('2024-01-01', '2024-01-31');

-- Получение дневной статистики по конкретному инструменту
SELECT * FROM invest.get_last_prices_daily_stats(NULL, NULL, 'BBG004730N88');

-- Оптимизированные запросы (всегда включайте условие по времени)
SELECT * FROM invest.last_prices 
WHERE time >= '2024-01-15 00:00:00' 
AND time < '2024-01-16 00:00:00'
AND figi = 'BBG004730N88';

-- Запрос по конкретной дневной партиции
SELECT * FROM invest.last_prices_2024_01_15 
WHERE figi = 'BBG004730N88'
ORDER BY time DESC;

-- Создание партиций на будущие дни (например, на неделю вперед)
SELECT invest.create_daily_partitions_for_period(
    CURRENT_DATE, 
    CURRENT_DATE + INTERVAL '7 days'
);
*/