-- Добавление расширенной статистики в таблицу minute_candles
-- Скрипт для добавления колонок с расширенной статистикой для минутных свечей

-- Добавляем колонки для расширенной статистики
ALTER TABLE invest.minute_candles 
ADD COLUMN IF NOT EXISTS price_change DECIMAL(18,9),
ADD COLUMN IF NOT EXISTS price_change_percent DECIMAL(18,4),
ADD COLUMN IF NOT EXISTS candle_type VARCHAR(20),
ADD COLUMN IF NOT EXISTS body_size DECIMAL(18,9),
ADD COLUMN IF NOT EXISTS upper_shadow DECIMAL(18,9),
ADD COLUMN IF NOT EXISTS lower_shadow DECIMAL(18,9),
ADD COLUMN IF NOT EXISTS high_low_range DECIMAL(18,9),
ADD COLUMN IF NOT EXISTS average_price DECIMAL(18,2);

-- Создаем индексы для улучшения производительности запросов
CREATE INDEX IF NOT EXISTS idx_minute_candles_candle_type ON invest.minute_candles(candle_type);
CREATE INDEX IF NOT EXISTS idx_minute_candles_price_change ON invest.minute_candles(price_change);
CREATE INDEX IF NOT EXISTS idx_minute_candles_average_price ON invest.minute_candles(average_price);

-- Обновляем существующие записи, вычисляя расширенную статистику
UPDATE invest.minute_candles 
SET 
    price_change = close - open,
    price_change_percent = CASE 
        WHEN open > 0 THEN ((close - open) / open) * 100
        ELSE 0
    END,
    candle_type = CASE 
        WHEN close > open THEN 'BULLISH'
        WHEN close < open THEN 'BEARISH'
        ELSE 'DOJI'
    END,
    body_size = ABS(close - open),
    upper_shadow = high - GREATEST(close, open),
    lower_shadow = LEAST(open, close) - low,
    high_low_range = high - low,
    average_price = (high + low + open + close) / 4
WHERE price_change IS NULL;

-- Добавляем комментарии к колонкам
COMMENT ON COLUMN invest.minute_candles.price_change IS 'Изменение цены (close - open)';
COMMENT ON COLUMN invest.minute_candles.price_change_percent IS 'Процентное изменение цены';
COMMENT ON COLUMN invest.minute_candles.candle_type IS 'Тип свечи: BULLISH, BEARISH, DOJI';
COMMENT ON COLUMN invest.minute_candles.body_size IS 'Размер тела свечи (абсолютное значение изменения цены)';
COMMENT ON COLUMN invest.minute_candles.upper_shadow IS 'Верхняя тень свечи';
COMMENT ON COLUMN invest.minute_candles.lower_shadow IS 'Нижняя тень свечи';
COMMENT ON COLUMN invest.minute_candles.high_low_range IS 'Диапазон цен (high - low)';
COMMENT ON COLUMN invest.minute_candles.average_price IS 'Средняя цена (high + low + open + close) / 4';

-- Создаем функцию для автоматического вычисления расширенной статистики
CREATE OR REPLACE FUNCTION invest.calculate_minute_candle_statistics()
RETURNS TRIGGER AS $$
BEGIN
    -- Вычисляем расширенную статистику
    NEW.price_change := NEW.close - NEW.open;
    NEW.price_change_percent := CASE 
        WHEN NEW.open > 0 THEN ((NEW.close - NEW.open) / NEW.open) * 100
        ELSE 0
    END;
    NEW.candle_type := CASE 
        WHEN NEW.close > NEW.open THEN 'BULLISH'
        WHEN NEW.close < NEW.open THEN 'BEARISH'
        ELSE 'DOJI'
    END;
    NEW.body_size := ABS(NEW.close - NEW.open);
    NEW.upper_shadow := NEW.high - GREATEST(NEW.close, NEW.open);
    NEW.lower_shadow := LEAST(NEW.open, NEW.close) - NEW.low;
    NEW.high_low_range := NEW.high - NEW.low;
    NEW.average_price := (NEW.high + NEW.low + NEW.open + NEW.close) / 4;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Создаем триггер для автоматического вычисления статистики при вставке/обновлении
DROP TRIGGER IF EXISTS trigger_calculate_minute_candle_statistics ON invest.minute_candles;
CREATE TRIGGER trigger_calculate_minute_candle_statistics
    BEFORE INSERT OR UPDATE ON invest.minute_candles
    FOR EACH ROW
    EXECUTE FUNCTION invest.calculate_minute_candle_statistics();

-- Выводим информацию о выполнении
DO $$
BEGIN
    RAISE NOTICE 'Расширенная статистика для минутных свечей успешно добавлена';
    RAISE NOTICE 'Добавлены колонки: price_change, price_change_percent, candle_type, body_size, upper_shadow, lower_shadow, high_low_range, average_price';
    RAISE NOTICE 'Созданы индексы для улучшения производительности';
    RAISE NOTICE 'Создан триггер для автоматического вычисления статистики';
END $$;
