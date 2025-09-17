-- Создание таблицы futures в схеме invest
-- Структура соответствует FutureDto без поля stockTicker

CREATE TABLE IF NOT EXISTS invest.futures (
    figi VARCHAR(50) NOT NULL PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL,
    asset_type VARCHAR(50) NOT NULL,
    basic_asset VARCHAR(50) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    exchange VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow'),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow')
);

-- Создание индексов для оптимизации запросов
CREATE INDEX IF NOT EXISTS idx_futures_ticker ON invest.futures(ticker);
CREATE INDEX IF NOT EXISTS idx_futures_asset_type ON invest.futures(asset_type);
CREATE INDEX IF NOT EXISTS idx_futures_currency ON invest.futures(currency);
CREATE INDEX IF NOT EXISTS idx_futures_exchange ON invest.futures(exchange);
CREATE INDEX IF NOT EXISTS idx_futures_basic_asset ON invest.futures(basic_asset);

-- Комментарии к таблице и полям
COMMENT ON TABLE invest.futures IS 'Таблица фьючерсов из T-Invest API';
COMMENT ON COLUMN invest.futures.figi IS 'Уникальный идентификатор инструмента';
COMMENT ON COLUMN invest.futures.ticker IS 'Тикер инструмента';
COMMENT ON COLUMN invest.futures.asset_type IS 'Тип базового актива (TYPE_SECURITY, TYPE_COMMODITY, TYPE_CURRENCY, TYPE_INDEX)';
COMMENT ON COLUMN invest.futures.basic_asset IS 'Базовый актив фьючерса';
COMMENT ON COLUMN invest.futures.currency IS 'Валюта инструмента';
COMMENT ON COLUMN invest.futures.exchange IS 'Биржа, на которой торгуется инструмент';
COMMENT ON COLUMN invest.futures.created_at IS 'Дата и время создания записи';
COMMENT ON COLUMN invest.futures.updated_at IS 'Дата и время последнего обновления записи';

-- Функция для автоматического обновления updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Триггер для автоматического обновления updated_at
CREATE TRIGGER update_futures_updated_at 
    BEFORE UPDATE ON invest.futures 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
