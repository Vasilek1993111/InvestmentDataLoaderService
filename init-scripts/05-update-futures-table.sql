-- Обновление существующей таблицы futures
-- Удаление поля stockTicker и добавление полей created_at, updated_at

-- Сначала удаляем поле stockTicker, если оно существует
ALTER TABLE invest.futures DROP COLUMN IF EXISTS stock_ticker;

-- Добавляем поля created_at и updated_at, если они не существуют
ALTER TABLE invest.futures 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow'),
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow');

-- Обновляем существующие записи, устанавливая значения по умолчанию
UPDATE invest.futures 
SET created_at = (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow')
WHERE created_at IS NULL;

UPDATE invest.futures 
SET updated_at = (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow')
WHERE updated_at IS NULL;

-- Делаем поля NOT NULL
ALTER TABLE invest.futures 
ALTER COLUMN created_at SET NOT NULL,
ALTER COLUMN updated_at SET NOT NULL;

-- Создаем индексы, если они не существуют
CREATE INDEX IF NOT EXISTS idx_futures_ticker ON invest.futures(ticker);
CREATE INDEX IF NOT EXISTS idx_futures_asset_type ON invest.futures(asset_type);
CREATE INDEX IF NOT EXISTS idx_futures_currency ON invest.futures(currency);
CREATE INDEX IF NOT EXISTS idx_futures_exchange ON invest.futures(exchange);
CREATE INDEX IF NOT EXISTS idx_futures_basic_asset ON invest.futures(basic_asset);

-- Добавляем комментарии
COMMENT ON TABLE invest.futures IS 'Таблица фьючерсов из T-Invest API';
COMMENT ON COLUMN invest.futures.figi IS 'Уникальный идентификатор инструмента';
COMMENT ON COLUMN invest.futures.ticker IS 'Тикер инструмента';
COMMENT ON COLUMN invest.futures.asset_type IS 'Тип базового актива (TYPE_SECURITY, TYPE_COMMODITY, TYPE_CURRENCY, TYPE_INDEX)';
COMMENT ON COLUMN invest.futures.basic_asset IS 'Базовый актив фьючерса';
COMMENT ON COLUMN invest.futures.currency IS 'Валюта инструмента';
COMMENT ON COLUMN invest.futures.exchange IS 'Биржа, на которой торгуется инструмент';
COMMENT ON COLUMN invest.futures.created_at IS 'Дата и время создания записи';
COMMENT ON COLUMN invest.futures.updated_at IS 'Дата и время последнего обновления записи';

-- Создаем функцию для автоматического обновления updated_at, если она не существует
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Создаем триггер для автоматического обновления updated_at, если он не существует
DROP TRIGGER IF EXISTS update_futures_updated_at ON invest.futures;
CREATE TRIGGER update_futures_updated_at 
    BEFORE UPDATE ON invest.futures 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
