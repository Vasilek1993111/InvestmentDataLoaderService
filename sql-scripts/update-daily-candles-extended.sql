-- Обновление таблицы daily_candles для поддержки расширенной статистики
-- Добавляем новые колонки для расширенной статистики свечей

-- Добавляем колонки для расширенной статистики
ALTER TABLE daily_candles 
ADD COLUMN IF NOT EXISTS price_change NUMERIC(18, 9),
ADD COLUMN IF NOT EXISTS price_change_percent NUMERIC(18, 9),
ADD COLUMN IF NOT EXISTS candle_type VARCHAR(20),
ADD COLUMN IF NOT EXISTS body_size NUMERIC(18, 9),
ADD COLUMN IF NOT EXISTS upper_shadow NUMERIC(18, 9),
ADD COLUMN IF NOT EXISTS lower_shadow NUMERIC(18, 9),
ADD COLUMN IF NOT EXISTS high_low_range NUMERIC(18, 9),
ADD COLUMN IF NOT EXISTS average_price NUMERIC(18, 9);

-- Добавляем комментарии к новым колонкам
COMMENT ON COLUMN daily_candles.price_change IS 'Изменение цены за день (close - open) с точностью до 9 знаков после запятой';
COMMENT ON COLUMN daily_candles.price_change_percent IS 'Процентное изменение цены за день с точностью до 4 знаков после запятой';
COMMENT ON COLUMN daily_candles.candle_type IS 'Тип свечи: BULLISH (бычья), BEARISH (медвежья), DOJI (доджи)';
COMMENT ON COLUMN daily_candles.body_size IS 'Размер тела свечи (абсолютное значение изменения цены) с точностью до 9 знаков после запятой';
COMMENT ON COLUMN daily_candles.upper_shadow IS 'Верхняя тень свечи (high - max(open, close)) с точностью до 9 знаков после запятой';
COMMENT ON COLUMN daily_candles.lower_shadow IS 'Нижняя тень свечи (min(open, close) - low) с точностью до 9 знаков после запятой';
COMMENT ON COLUMN daily_candles.high_low_range IS 'Диапазон цен за день (high - low) с точностью до 9 знаков после запятой';
COMMENT ON COLUMN daily_candles.average_price IS 'Средняя цена за день ((high + low + close) / 3) с точностью до 2 знаков после запятой';

-- Создаем индексы для новых колонок для улучшения производительности запросов
CREATE INDEX IF NOT EXISTS idx_daily_candles_candle_type ON daily_candles(candle_type);
CREATE INDEX IF NOT EXISTS idx_daily_candles_price_change ON daily_candles(price_change);
CREATE INDEX IF NOT EXISTS idx_daily_candles_price_change_percent ON daily_candles(price_change_percent);

-- Создаем функцию для автоматического вычисления расширенной статистики
CREATE OR REPLACE FUNCTION calculate_daily_candle_statistics()
RETURNS TRIGGER AS $$
BEGIN
    -- Вычисляем расширенную статистику
    NEW.price_change := NEW.close - NEW.open;
    
    IF NEW.open > 0 THEN
        NEW.price_change_percent := (NEW.price_change / NEW.open) * 100;
    ELSE
        NEW.price_change_percent := 0;
    END IF;
    
    -- Определяем тип свечи
    IF NEW.close > NEW.open THEN
        NEW.candle_type := 'BULLISH';
    ELSIF NEW.close < NEW.open THEN
        NEW.candle_type := 'BEARISH';
    ELSE
        NEW.candle_type := 'DOJI';
    END IF;
    
    -- Вычисляем размер тела свечи
    NEW.body_size := ABS(NEW.price_change);
    
    -- Вычисляем тени
    NEW.upper_shadow := NEW.high - GREATEST(NEW.open, NEW.close);
    NEW.lower_shadow := LEAST(NEW.open, NEW.close) - NEW.low;
    
    -- Вычисляем диапазон
    NEW.high_low_range := NEW.high - NEW.low;
    
    -- Вычисляем среднюю цену
    NEW.average_price := (NEW.high + NEW.low + NEW.close) / 3;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Создаем триггер для автоматического вычисления статистики при вставке/обновлении
DROP TRIGGER IF EXISTS trigger_calculate_daily_candle_statistics ON daily_candles;
CREATE TRIGGER trigger_calculate_daily_candle_statistics
    BEFORE INSERT OR UPDATE ON daily_candles
    FOR EACH ROW
    EXECUTE FUNCTION calculate_daily_candle_statistics();

-- Обновляем существующие записи, вычисляя для них расширенную статистику
UPDATE daily_candles 
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
    upper_shadow = high - GREATEST(open, close),
    lower_shadow = LEAST(open, close) - low,
    high_low_range = high - low,
    average_price = (high + low + close) / 3
WHERE price_change IS NULL;

-- Выводим информацию о завершении обновления
SELECT 
    'Таблица daily_candles успешно обновлена для поддержки расширенной статистики' as message,
    COUNT(*) as total_records,
    COUNT(CASE WHEN price_change IS NOT NULL THEN 1 END) as records_with_statistics
FROM daily_candles;
