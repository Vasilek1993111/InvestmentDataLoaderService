-- Скрипт для создания таблицы indicatives в схеме invest
-- Структура таблицы основана на данных из T-Invest API

CREATE TABLE invest.indicatives (
    figi VARCHAR(50) NOT NULL PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    exchange VARCHAR(50) NOT NULL,
    class_code VARCHAR(20),
    uid VARCHAR(50),
    sell_available_flag BOOLEAN DEFAULT false,
    buy_available_flag BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow'),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow')
);

-- Создание индексов для оптимизации запросов
CREATE INDEX IF NOT EXISTS idx_indicatives_ticker ON invest.indicatives(ticker);
CREATE INDEX IF NOT EXISTS idx_indicatives_currency ON invest.indicatives(currency);
CREATE INDEX IF NOT EXISTS idx_indicatives_exchange ON invest.indicatives(exchange);
CREATE INDEX IF NOT EXISTS idx_indicatives_class_code ON invest.indicatives(class_code);
CREATE INDEX IF NOT EXISTS idx_indicatives_uid ON invest.indicatives(uid);

-- Комментарии к таблице и столбцам
COMMENT ON TABLE invest.indicatives IS 'Индикативные инструменты (индексы, товары и другие)';
COMMENT ON COLUMN invest.indicatives.figi IS 'FIGI инструмента (уникальный идентификатор)';
COMMENT ON COLUMN invest.indicatives.ticker IS 'Тикер инструмента';
COMMENT ON COLUMN invest.indicatives.name IS 'Название инструмента';
COMMENT ON COLUMN invest.indicatives.currency IS 'Валюта инструмента';
COMMENT ON COLUMN invest.indicatives.exchange IS 'Биржа/площадка';
COMMENT ON COLUMN invest.indicatives.class_code IS 'Код класса инструмента';
COMMENT ON COLUMN invest.indicatives.uid IS 'Уникальный идентификатор';
COMMENT ON COLUMN invest.indicatives.sell_available_flag IS 'Флаг доступности для продажи';
COMMENT ON COLUMN invest.indicatives.buy_available_flag IS 'Флаг доступности для покупки';
COMMENT ON COLUMN invest.indicatives.created_at IS 'Дата создания записи (UTC+3)';
COMMENT ON COLUMN invest.indicatives.updated_at IS 'Дата последнего обновления записи (UTC+3)';
