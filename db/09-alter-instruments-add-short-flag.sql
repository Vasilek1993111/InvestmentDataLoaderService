-- Добавление флага шортовости инструмента в таблицы shares и futures
-- Схема по умолчанию: invest

-- Акции
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'invest' AND table_name = 'shares' AND column_name = 'short_enabled'
    ) THEN
        ALTER TABLE invest.shares ADD COLUMN short_enabled boolean;
        COMMENT ON COLUMN invest.shares.short_enabled IS 'Флаг: инструмент доступен для коротких продаж (short)';
    END IF;
END $$;

-- Фьючерсы
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'invest' AND table_name = 'futures' AND column_name = 'short_enabled'
    ) THEN
        ALTER TABLE invest.futures ADD COLUMN short_enabled boolean;
        COMMENT ON COLUMN invest.futures.short_enabled IS 'Флаг: инструмент доступен для коротких продаж (short)';
    END IF;
END $$;


