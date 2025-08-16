-- Инициализация базы данных для Ingestion Service
-- Этот скрипт выполняется при первом запуске PostgreSQL контейнера

-- Создание схемы для рыночных данных
CREATE SCHEMA IF NOT EXISTS invest;

-- Создание таблицы акций (если не существует)
CREATE TABLE IF NOT EXISTS shares (
    figi VARCHAR PRIMARY KEY,
    ticker VARCHAR NOT NULL,
    name VARCHAR NOT NULL,
    currency VARCHAR NOT NULL,
    exchange VARCHAR NOT NULL
);

-- Создание таблицы фьючерсов (если не существует)
CREATE TABLE IF NOT EXISTS futures (
    figi VARCHAR PRIMARY KEY,
    ticker VARCHAR NOT NULL,
    asset_type VARCHAR NOT NULL,
    basic_asset VARCHAR NOT NULL,
    currency VARCHAR NOT NULL,
    exchange VARCHAR NOT NULL,
    stock_ticker VARCHAR
);

-- Создание таблицы цен закрытия (если не существует)
CREATE TABLE IF NOT EXISTS invest.close_prices (
    price_date DATE,
    figi VARCHAR,
    close_price DECIMAL(18,9) NOT NULL,
    instrument_type VARCHAR NOT NULL,
    currency VARCHAR NOT NULL,
    exchange VARCHAR NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (price_date, figi)
);

-- Создание таблицы последних цен (если не существует)
CREATE TABLE IF NOT EXISTS invest.last_prices (
    figi VARCHAR,
    time TIMESTAMP,
    price DECIMAL(18,9) NOT NULL,
    currency VARCHAR NOT NULL,
    exchange VARCHAR NOT NULL,
    PRIMARY KEY (figi, time)
);

-- Создание индексов для оптимизации запросов
CREATE INDEX IF NOT EXISTS idx_shares_ticker ON shares(ticker);
CREATE INDEX IF NOT EXISTS idx_shares_exchange ON shares(exchange);
CREATE INDEX IF NOT EXISTS idx_shares_currency ON shares(currency);

CREATE INDEX IF NOT EXISTS idx_futures_ticker ON futures(ticker);
CREATE INDEX IF NOT EXISTS idx_futures_exchange ON futures(exchange);
CREATE INDEX IF NOT EXISTS idx_futures_basic_asset ON futures(basic_asset);

CREATE INDEX IF NOT EXISTS idx_close_prices_date ON invest.close_prices(price_date);
CREATE INDEX IF NOT EXISTS idx_close_prices_figi ON invest.close_prices(figi);
CREATE INDEX IF NOT EXISTS idx_close_prices_instrument_type ON invest.close_prices(instrument_type);

CREATE INDEX IF NOT EXISTS idx_last_prices_figi ON invest.last_prices(figi);
CREATE INDEX IF NOT EXISTS idx_last_prices_time ON invest.last_prices(time);

-- Создание представлений для удобства работы с данными
CREATE OR REPLACE VIEW invest.shares_with_prices AS
SELECT 
    s.figi,
    s.ticker,
    s.name,
    s.currency,
    s.exchange,
    cp.close_price,
    cp.price_date,
    cp.created_at
FROM shares s
LEFT JOIN invest.close_prices cp ON s.figi = cp.figi
WHERE cp.instrument_type = 'SHARE';

CREATE OR REPLACE VIEW invest.futures_with_prices AS
SELECT 
    f.figi,
    f.ticker,
    f.asset_type,
    f.basic_asset,
    f.currency,
    f.exchange,
    cp.close_price,
    cp.price_date,
    cp.created_at
FROM futures f
LEFT JOIN invest.close_prices cp ON f.figi = cp.figi
WHERE cp.instrument_type = 'FUTURE';

-- Создание функции для получения последних цен
CREATE OR REPLACE FUNCTION invest.get_latest_prices(p_instrument_type VARCHAR DEFAULT NULL)
RETURNS TABLE (
    figi VARCHAR,
    ticker VARCHAR,
    name VARCHAR,
    close_price DECIMAL(18,9),
    price_date DATE,
    currency VARCHAR,
    exchange VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT DISTINCT ON (cp.figi)
        cp.figi,
        CASE 
            WHEN cp.instrument_type = 'SHARE' THEN s.ticker
            WHEN cp.instrument_type = 'FUTURE' THEN f.ticker
        END as ticker,
        CASE 
            WHEN cp.instrument_type = 'SHARE' THEN s.name
            WHEN cp.instrument_type = 'FUTURE' THEN f.basic_asset
        END as name,
        cp.close_price,
        cp.price_date,
        cp.currency,
        cp.exchange
    FROM invest.close_prices cp
    LEFT JOIN shares s ON cp.figi = s.figi AND cp.instrument_type = 'SHARE'
    LEFT JOIN futures f ON cp.figi = f.figi AND cp.instrument_type = 'FUTURE'
    WHERE (p_instrument_type IS NULL OR cp.instrument_type = p_instrument_type)
    ORDER BY cp.figi, cp.price_date DESC;
END;
$$ LANGUAGE plpgsql;

-- Создание функции для получения статистики
CREATE OR REPLACE FUNCTION invest.get_market_stats()
RETURNS TABLE (
    total_shares INTEGER,
    total_futures INTEGER,
    total_prices INTEGER,
    latest_price_date DATE
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        (SELECT COUNT(*) FROM shares) as total_shares,
        (SELECT COUNT(*) FROM futures) as total_futures,
        (SELECT COUNT(*) FROM invest.close_prices) as total_prices,
        (SELECT MAX(price_date) FROM invest.close_prices) as latest_price_date;
END;
$$ LANGUAGE plpgsql;

-- Предоставление прав на схему invest
GRANT USAGE ON SCHEMA invest TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA invest TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA invest TO postgres;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA invest TO postgres;

-- Создание пользователя для приложения (если нужно)
-- CREATE USER IF NOT EXISTS ingestion_user WITH PASSWORD 'ingestion_password';
-- GRANT CONNECT ON DATABASE postgres TO ingestion_user;
-- GRANT USAGE ON SCHEMA invest TO ingestion_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA invest TO ingestion_user;

-- Логирование успешной инициализации
DO $$
BEGIN
    RAISE NOTICE 'Database initialization completed successfully';
    RAISE NOTICE 'Schema: invest created';
    RAISE NOTICE 'Tables: shares, futures, close_prices, last_prices created';
    RAISE NOTICE 'Indexes: created for optimization';
    RAISE NOTICE 'Views: shares_with_prices, futures_with_prices created';
    RAISE NOTICE 'Functions: get_latest_prices, get_market_stats created';
END $$;