-- Добавление колонки min_price_increment для shares и futures
-- Используем NUMERIC(18, 9) для поддержки полной точности Quotation (10^-9)

-- Добавление колонки для shares, если её ещё нет
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'invest' 
                   AND table_name = 'shares' 
                   AND column_name = 'min_price_increment') THEN
        ALTER TABLE invest.shares ADD COLUMN min_price_increment NUMERIC(18, 9);
        COMMENT ON COLUMN invest.shares.min_price_increment IS 'Минимальный шаг цены инструмента';
    END IF;
END $$;

-- Добавление колонки для futures, если её ещё нет
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'invest' 
                   AND table_name = 'futures' 
                   AND column_name = 'min_price_increment') THEN
        ALTER TABLE invest.futures ADD COLUMN min_price_increment NUMERIC(18, 9);
        COMMENT ON COLUMN invest.futures.min_price_increment IS 'Минимальный шаг цены инструмента';
    END IF;
END $$;

-- Добавление колонки lot для shares, если её ещё нет
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'invest' 
                   AND table_name = 'shares' 
                   AND column_name = 'lot') THEN
        ALTER TABLE invest.shares ADD COLUMN lot INTEGER;
        COMMENT ON COLUMN invest.shares.lot IS 'Лотность инструмента';
    END IF;
END $$;

-- Добавление колонки lot для futures, если её ещё нет
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'invest' 
                   AND table_name = 'futures' 
                   AND column_name = 'lot') THEN
        ALTER TABLE invest.futures ADD COLUMN lot INTEGER;
        COMMENT ON COLUMN invest.futures.lot IS 'Лотность инструмента';
    END IF;
END $$;

-- Добавление колонки asset_uid для shares, если её ещё нет
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'invest' 
                   AND table_name = 'shares' 
                   AND column_name = 'asset_uid') THEN
        ALTER TABLE invest.shares ADD COLUMN asset_uid VARCHAR(255);
        COMMENT ON COLUMN invest.shares.asset_uid IS 'Уникальный идентификатор актива';
    END IF;
END $$;

-- Добавление колонки expiration_date для futures, если её ещё нет
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'invest' 
                   AND table_name = 'futures' 
                   AND column_name = 'expiration_date') THEN
        ALTER TABLE invest.futures ADD COLUMN expiration_date TIMESTAMP;
        COMMENT ON COLUMN invest.futures.expiration_date IS 'Дата экспирации фьючерса';
    END IF;
END $$;

