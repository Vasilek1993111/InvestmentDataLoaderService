-- Примеры работы с столбцом strategy_applicable в таблице candle_pattern_analysis

-- 1. Проверка структуры таблицы после добавления столбца
SELECT 'Проверка структуры таблицы:' as section;

SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_schema = 'invest_candles' 
  AND table_name = 'candle_pattern_analysis'
  AND column_name = 'strategy_applicable';

-- 2. Примеры установки флагов применимости стратегии
SELECT 'Примеры установки флагов:' as section;

-- Отметить конкретный паттерн как применимый для стратегии
-- UPDATE invest_candles.candle_pattern_analysis 
-- SET strategy_applicable = 'Y' 
-- WHERE figi = 'FUTPLT062600' 
--   AND analysis_date = '2025-10-02' 
--   AND candle_type = 'BULLISH';

-- Отметить паттерн как неприменимый
-- UPDATE invest_candles.candle_pattern_analysis 
-- SET strategy_applicable = 'N' 
-- WHERE id = 123;

-- Массовое обновление для всех BULLISH паттернов определенного инструмента
-- UPDATE invest_candles.candle_pattern_analysis 
-- SET strategy_applicable = 'Y' 
-- WHERE figi = 'FUTPLT062600' 
--   AND candle_type = 'BULLISH' 
--   AND consecutive_days >= 5;

-- 3. Аналитические запросы по применимости стратегии
SELECT 'Статистика по применимости стратегии:' as section;

-- Общая статистика по флагам
SELECT 
    CASE 
        WHEN strategy_applicable = 'Y' THEN 'Применима'
        WHEN strategy_applicable = 'N' THEN 'Не применима'
        WHEN strategy_applicable IS NULL THEN 'Не оценена'
    END as status,
    strategy_applicable,
    count(*) as total_patterns,
    count(distinct figi) as unique_instruments,
    round(avg(consecutive_days), 2) as avg_consecutive_days,
    round(avg(total_price_change), 4) as avg_total_price_change
FROM invest_candles.candle_pattern_analysis 
WHERE analysis_date >= current_date - interval '30 days'
GROUP BY strategy_applicable
ORDER BY strategy_applicable NULLS LAST;

-- 4. Паттерны, применимые для стратегии
SELECT 'Паттерны, применимые для стратегии (Y):' as section;

SELECT 
    figi,
    candle_type,
    pattern_start_date,
    pattern_end_date,
    consecutive_days,
    round(total_price_change, 4) as total_price_change,
    strategy_applicable,
    analysis_date
FROM invest_candles.candle_pattern_analysis 
WHERE strategy_applicable = 'Y'
  AND analysis_date >= current_date - interval '7 days'
ORDER BY analysis_date DESC, total_price_change DESC
LIMIT 10;

-- 5. Паттерны, требующие оценки
SELECT 'Паттерны, требующие оценки (NULL):' as section;

SELECT 
    count(*) as patterns_to_evaluate,
    count(distinct figi) as unique_instruments,
    count(distinct analysis_date) as analysis_dates,
    min(analysis_date) as earliest_date,
    max(analysis_date) as latest_date
FROM invest_candles.candle_pattern_analysis 
WHERE strategy_applicable IS NULL;

-- Показать первые 5 паттернов, требующих оценки
SELECT 
    id,
    figi,
    candle_type,
    pattern_start_date,
    pattern_end_date,
    consecutive_days,
    round(total_price_change, 4) as total_price_change,
    analysis_date
FROM invest_candles.candle_pattern_analysis 
WHERE strategy_applicable IS NULL
ORDER BY analysis_date DESC, total_price_change DESC
LIMIT 5;

-- 6. Анализ эффективности оценки
SELECT 'Анализ эффективности оценки:' as section;

-- Соотношение применимых к неприменимым паттернам
SELECT 
    candle_type,
    sum(case when strategy_applicable = 'Y' then 1 else 0 end) as applicable_count,
    sum(case when strategy_applicable = 'N' then 1 else 0 end) as not_applicable_count,
    sum(case when strategy_applicable is null then 1 else 0 end) as not_evaluated_count,
    round(
        sum(case when strategy_applicable = 'Y' then 1 else 0 end) * 100.0 / 
        nullif(sum(case when strategy_applicable in ('Y', 'N') then 1 else 0 end), 0), 
        2
    ) as applicable_percentage
FROM invest_candles.candle_pattern_analysis 
WHERE analysis_date >= current_date - interval '30 days'
GROUP BY candle_type
ORDER BY candle_type;

-- 7. Топ инструментов с применимыми паттернами
SELECT 'Топ-10 инструментов с применимыми паттернами:' as section;

SELECT 
    figi,
    count(*) as applicable_patterns,
    count(distinct analysis_date) as analysis_dates,
    avg(consecutive_days) as avg_consecutive_days,
    sum(case when candle_type = 'BULLISH' then 1 else 0 end) as bullish_patterns,
    sum(case when candle_type = 'BEARISH' then 1 else 0 end) as bearish_patterns,
    round(avg(total_price_change), 4) as avg_total_price_change
FROM invest_candles.candle_pattern_analysis 
WHERE strategy_applicable = 'Y'
  AND analysis_date >= current_date - interval '30 days'
GROUP BY figi
ORDER BY applicable_patterns DESC, avg_total_price_change DESC
LIMIT 10;

-- 8. Проверка работы через представление в схеме invest
SELECT 'Проверка работы через представление invest:' as section;

SELECT 
    strategy_applicable,
    count(*) as total_patterns
FROM invest.candle_pattern_analysis 
WHERE analysis_date >= current_date - interval '7 days'
GROUP BY strategy_applicable
ORDER BY strategy_applicable NULLS LAST;

-- 9. Пример пакетного обновления для оценки паттернов
SELECT 'Пример пакетного обновления:' as section;

-- Автоматически отметить как применимые все BULLISH паттерны с 7+ днями и положительным изменением цены
-- UPDATE invest_candles.candle_pattern_analysis 
-- SET strategy_applicable = 'Y' 
-- WHERE strategy_applicable IS NULL
--   AND candle_type = 'BULLISH' 
--   AND consecutive_days >= 7
--   AND total_price_change > 0;

-- Автоматически отметить как неприменимые паттерны с очень малым изменением цены
-- UPDATE invest_candles.candle_pattern_analysis 
-- SET strategy_applicable = 'N' 
-- WHERE strategy_applicable IS NULL
--   AND abs(total_price_change) < 0.01;

SELECT 'Примеры завершены!' as status;
