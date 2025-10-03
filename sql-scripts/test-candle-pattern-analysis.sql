-- Тестовый скрипт для функции анализа паттернов свечей
-- Этот скрипт демонстрирует работу функции analyze_candle_patterns

-- 1. Создание тестовых данных (если нужно для демонстрации)
-- Раскомментируйте блок ниже, если хотите создать тестовые данные

/*
-- Создаем тестовые данные для демонстрации
-- Инструмент 1: 5 дней подряд BULLISH свечи
INSERT INTO daily_candles (figi, time, open, high, low, close, volume, is_complete, candle_type) VALUES
('TEST001', '2024-01-15 00:00:00+03', 100.00, 105.00, 99.00, 104.00, 1000, true, 'BULLISH'),
('TEST001', '2024-01-16 00:00:00+03', 104.00, 108.00, 103.00, 107.00, 1100, true, 'BULLISH'),
('TEST001', '2024-01-17 00:00:00+03', 107.00, 110.00, 106.00, 109.00, 1200, true, 'BULLISH'),
('TEST001', '2024-01-18 00:00:00+03', 109.00, 112.00, 108.00, 111.00, 1300, true, 'BULLISH'),
('TEST001', '2024-01-19 00:00:00+03', 111.00, 115.00, 110.00, 114.00, 1400, true, 'BULLISH');

-- Инструмент 2: 6 дней подряд BEARISH свечи
INSERT INTO daily_candles (figi, time, open, high, low, close, volume, is_complete, candle_type) VALUES
('TEST002', '2024-01-14 00:00:00+03', 200.00, 201.00, 195.00, 196.00, 2000, true, 'BEARISH'),
('TEST002', '2024-01-15 00:00:00+03', 196.00, 197.00, 192.00, 193.00, 2100, true, 'BEARISH'),
('TEST002', '2024-01-16 00:00:00+03', 193.00, 194.00, 189.00, 190.00, 2200, true, 'BEARISH'),
('TEST002', '2024-01-17 00:00:00+03', 190.00, 191.00, 186.00, 187.00, 2300, true, 'BEARISH'),
('TEST002', '2024-01-18 00:00:00+03', 187.00, 188.00, 183.00, 184.00, 2400, true, 'BEARISH'),
('TEST002', '2024-01-19 00:00:00+03', 184.00, 185.00, 180.00, 181.00, 2500, true, 'BEARISH');

-- Инструмент 3: смешанные типы свечей (не должен попасть в результат)
INSERT INTO daily_candles (figi, time, open, high, low, close, volume, is_complete, candle_type) VALUES
('TEST003', '2024-01-15 00:00:00+03', 300.00, 305.00, 299.00, 304.00, 3000, true, 'BULLISH'),
('TEST003', '2024-01-16 00:00:00+03', 304.00, 305.00, 301.00, 302.00, 3100, true, 'BEARISH'),
('TEST003', '2024-01-17 00:00:00+03', 302.00, 307.00, 301.00, 306.00, 3200, true, 'BULLISH'),
('TEST003', '2024-01-18 00:00:00+03', 306.00, 307.00, 303.00, 304.00, 3300, true, 'BEARISH'),
('TEST003', '2024-01-19 00:00:00+03', 304.00, 309.00, 303.00, 308.00, 3400, true, 'BULLISH');
*/

-- 2. Запуск анализа паттернов свечей
-- Анализируем паттерны на дату 2024-01-20 (будет анализировать торговые дни, исключая выходные)
SELECT 'Запуск анализа паттернов свечей (по умолчанию 5 торговых дней)...' as status;

-- Вызываем функцию анализа с параметрами по умолчанию (5 торговых дней)
SELECT analyze_candle_patterns('2024-01-20'::date);

-- Дополнительные тесты с разным количеством торговых дней
SELECT 'Запуск анализа паттернов свечей для 3 торговых дней подряд...' as status;
SELECT analyze_candle_patterns('2024-01-20'::date, 3);

SELECT 'Запуск анализа паттернов свечей для 7 торговых дней подряд...' as status;
SELECT analyze_candle_patterns('2024-01-20'::date, 7);

-- Пример с датой 30.09.2025 (понедельник)
-- Будет анализировать: 27.09 (пт), 26.09 (чт), 25.09 (ср), 24.09 (вт), 23.09 (пн)
-- Пропустит: 28.09 (сб), 29.09 (вс)
SELECT 'Пример анализа на 30.09.2025 (5 торговых дней: 27,26,25,24,23 сентября)...' as status;
-- SELECT analyze_candle_patterns('2025-09-30'::date, 5);

-- 3. Проверяем результаты анализа
SELECT 'Результаты анализа паттернов:' as section;

-- Показываем найденные паттерны
SELECT 
    figi,
    candle_type,
    pattern_start_date,
    pattern_end_date,
    consecutive_days,
    avg_volume,
    round(avg_price_change, 4) as avg_price_change,
    round(total_price_change, 4) as total_price_change,
    strategy_applicable,
    created_at
FROM candle_pattern_analysis 
WHERE analysis_date = '2024-01-20'::date
ORDER BY figi, pattern_start_date;

-- 4. Показываем статистику по найденным паттернам
SELECT 'Статистика по паттернам:' as section;

SELECT * FROM get_candle_pattern_stats('2024-01-20'::date);

-- 5. Проверяем логи выполнения
SELECT 'Логи выполнения функции:' as section;

SELECT 
    task_id,
    endpoint,
    status,
    message,
    start_time,
    end_time,
    duration_ms
FROM system_logs 
WHERE task_id LIKE 'CANDLE_PATTERN_ANALYSIS_2024_01_20%'
ORDER BY start_time;

-- 6. Дополнительные запросы для анализа

-- Показать все паттерны за последние 30 дней
SELECT 'Все паттерны за последние 30 дней:' as section;

SELECT 
    analysis_date,
    count(*) as total_patterns,
    count(distinct figi) as unique_instruments,
    sum(case when candle_type = 'BULLISH' then 1 else 0 end) as bullish_patterns,
    sum(case when candle_type = 'BEARISH' then 1 else 0 end) as bearish_patterns
FROM candle_pattern_analysis 
WHERE analysis_date >= current_date - interval '30 days'
GROUP BY analysis_date
ORDER BY analysis_date DESC;

-- Показать топ инструментов по количеству паттернов
SELECT 'Топ-10 инструментов по количеству паттернов:' as section;

SELECT 
    figi,
    count(*) as total_patterns,
    sum(case when candle_type = 'BULLISH' then 1 else 0 end) as bullish_patterns,
    sum(case when candle_type = 'BEARISH' then 1 else 0 end) as bearish_patterns,
    avg(consecutive_days) as avg_consecutive_days,
    max(consecutive_days) as max_consecutive_days
FROM candle_pattern_analysis 
WHERE analysis_date >= current_date - interval '30 days'
GROUP BY figi
ORDER BY total_patterns DESC
LIMIT 10;

-- 7. Тестирование синонимов в схеме invest
SELECT 'Тестирование синонимов в схеме invest:' as section;

-- Использование синонима функции из схемы invest
SELECT 'Запуск анализа через синоним в схеме invest (5 дней по умолчанию)...' as status;
-- SELECT invest.analyze_candle_patterns('2024-01-20'::date);

SELECT 'Запуск анализа через синоним в схеме invest (3 дня)...' as status;
-- SELECT invest.analyze_candle_patterns('2024-01-20'::date, 3);

-- Использование синонима таблицы из схемы invest
SELECT 'Результаты через синоним таблицы в схеме invest:' as status;
SELECT 
    figi,
    candle_type,
    pattern_start_date,
    pattern_end_date,
    consecutive_days
FROM invest.candle_pattern_analysis 
WHERE analysis_date = '2024-01-20'::date
ORDER BY figi, pattern_start_date
LIMIT 5;

-- Использование синонима функции статистики из схемы invest
SELECT 'Статистика через синоним функции в схеме invest:' as status;
SELECT * FROM invest.get_candle_pattern_stats('2024-01-20'::date);

-- 8. Пример запуска анализа для текущей даты
SELECT 'Пример запуска для текущей даты:' as section;

-- Раскомментируйте строки ниже для запуска анализа на текущую дату
-- SELECT analyze_candle_patterns(current_date);  -- 5 торговых дней по умолчанию
-- SELECT analyze_candle_patterns(current_date, 3);  -- 3 торговых дня подряд
-- SELECT analyze_candle_patterns(current_date, 10); -- 10 торговых дней подряд

-- Или используйте синонимы из схемы invest:
-- SELECT invest.analyze_candle_patterns(current_date);     -- 5 торговых дней по умолчанию
-- SELECT invest.analyze_candle_patterns(current_date, 7);  -- 7 торговых дней подряд

-- 9. Демонстрация работы с выходными днями
SELECT 'Демонстрация исключения выходных дней:' as section;

-- Показываем, какие дни недели исключаются (0=воскресенье, 6=суббота)
SELECT 
    'Дни недели в PostgreSQL: 0=Воскресенье, 1=Понедельник, 2=Вторник, 3=Среда, 4=Четверг, 5=Пятница, 6=Суббота' as info;

-- Пример: если анализируем 02.10.2025 с параметром 6 дней, то ищем паттерны в торговые дни:
SELECT 
    date_val,
    to_char(date_val, 'Day') as day_name,
    extract(dow from date_val) as day_of_week,
    case 
        when extract(dow from date_val) in (0, 6) then 'ВЫХОДНОЙ (исключается)'
        else 'ТОРГОВЫЙ ДЕНЬ (анализируется)'
    end as trading_status,
    case 
        when extract(dow from date_val) not in (0, 6) then 
            row_number() over (order by date_val desc) 
        else null 
    end as trading_day_rank
FROM generate_series('2025-09-23'::date, '2025-10-01'::date, '1 day'::interval) as date_val
ORDER BY date_val DESC;

-- Проверка конкретного инструмента FUTPLT062600
SELECT 'Проверка данных для FUTPLT062600:' as section;

SELECT 
    time::date as candle_date,
    to_char(time::date, 'Day') as day_name,
    candle_type,
    volume,
    price_change,
    row_number() over (order by time::date desc) as day_rank
FROM daily_candles 
WHERE figi = 'FUTPLT062600'
  AND time::date <= '2025-10-01'::date
  AND candle_type in ('BULLISH', 'BEARISH')
  AND is_complete = true
  AND extract(dow from time::date) not in (0, 6)  -- Исключаем выходные
ORDER BY time::date DESC
LIMIT 10;

-- 10. Примеры работы с флагом применимости стратегии
SELECT 'Примеры работы с флагом применимости стратегии:' as section;

-- Пример обновления флага применимости для конкретного паттерна
-- UPDATE candle_pattern_analysis 
-- SET strategy_applicable = 'Y' 
-- WHERE figi = 'FUTPLT062600' AND analysis_date = '2024-01-20' AND candle_type = 'BULLISH';

-- Пример обновления флага для паттерна как неприменимого
-- UPDATE candle_pattern_analysis 
-- SET strategy_applicable = 'N' 
-- WHERE figi = 'FUTPLT062600' AND analysis_date = '2024-01-20' AND candle_type = 'BEARISH';

-- Запросы для анализа по флагу применимости
SELECT 'Статистика по применимости стратегии:' as info;

SELECT 
    strategy_applicable,
    count(*) as total_patterns,
    count(distinct figi) as unique_instruments,
    round(avg(consecutive_days), 2) as avg_consecutive_days
FROM candle_pattern_analysis 
WHERE analysis_date >= current_date - interval '30 days'
GROUP BY strategy_applicable
ORDER BY strategy_applicable;

-- Паттерны, помеченные как применимые для стратегии
SELECT 'Паттерны, применимые для стратегии (Y):' as info;

SELECT 
    figi,
    candle_type,
    pattern_start_date,
    pattern_end_date,
    consecutive_days,
    strategy_applicable
FROM candle_pattern_analysis 
WHERE strategy_applicable = 'Y'
  AND analysis_date >= current_date - interval '7 days'
ORDER BY analysis_date DESC, figi
LIMIT 5;

-- Паттерны, требующие оценки (NULL)
SELECT 'Паттерны, требующие оценки (NULL):' as info;

SELECT 
    count(*) as patterns_to_evaluate,
    count(distinct figi) as unique_instruments,
    min(analysis_date) as earliest_date,
    max(analysis_date) as latest_date
FROM candle_pattern_analysis 
WHERE strategy_applicable IS NULL;

SELECT 'Тестирование завершено!' as status;
