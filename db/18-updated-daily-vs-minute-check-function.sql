-- Обновленная функция сверки дневных и минутных свечей с поддержкой специальных торговых часов
-- Заменяет существующую функцию run_daily_vs_minute_check

-- Удаляем старую функцию, если она существует
drop function if exists run_daily_vs_minute_check(date);
drop function if exists invest.run_daily_vs_minute_check(date);

-- Создаем обновленную функцию
create function run_daily_vs_minute_check(p_date date)
    returns TABLE(task_id text, total_rows bigint, mismatches bigint)
    language plpgsql
as
$$
DECLARE
    v_task_id TEXT;
    v_total_rows BIGINT := 0;
    v_mismatches BIGINT := 0;
    v_daily_record RECORD;
    v_minute_record RECORD;
    v_instrument_type TEXT;
    v_diff_volume BIGINT;
    v_diff_high NUMERIC;
    v_diff_low NUMERIC;
    v_diff_open NUMERIC;
    v_diff_close NUMERIC;
    v_is_weekend BOOLEAN;
    v_trading_start_hour INTEGER;
    v_trading_start_minute INTEGER;
    v_trading_end_hour INTEGER;
    v_trading_end_minute INTEGER;
    v_log_message TEXT;
    v_log_status TEXT;
    v_start_time TIMESTAMP WITH TIME ZONE;
    v_trading_hours RECORD;
    v_time_condition TEXT;
BEGIN
    -- Засекаем время начала
    v_start_time := NOW();
    
    -- Генерируем уникальный task_id
    v_task_id := 'daily_vs_minute_check_' || TO_CHAR(p_date, 'YYYY_MM_DD') || '_' || EXTRACT(EPOCH FROM NOW())::TEXT;
    
    -- Определяем, выходной ли день (суббота = 6, воскресенье = 0)
    v_is_weekend := EXTRACT(dow FROM p_date) IN (0, 6);
    
    -- Получаем только дневные свечи для акций и фьючерсов за указанную дату
    FOR v_daily_record IN
        SELECT 
            dc.figi,
            dc.volume as daily_volume,
            dc.high as daily_high,
            dc.low as daily_low,
            dc.open as daily_open,
            dc.close as daily_close
        FROM invest.daily_candles dc
        WHERE DATE(dc.time AT TIME ZONE 'Europe/Moscow') = p_date
        AND (
            EXISTS (SELECT 1 FROM invest.shares WHERE figi = dc.figi) OR
            EXISTS (SELECT 1 FROM invest.futures WHERE figi = dc.figi)
        )
    LOOP
        v_total_rows := v_total_rows + 1;
        
        -- Определяем тип инструмента
        v_instrument_type := 'unknown';
        
        -- Проверяем, есть ли в таблице shares
        IF EXISTS (SELECT 1 FROM invest.shares WHERE figi = v_daily_record.figi) THEN
            v_instrument_type := 'shares';
        -- Проверяем, есть ли в таблице futures
        ELSIF EXISTS (SELECT 1 FROM invest.futures WHERE figi = v_daily_record.figi) THEN
            v_instrument_type := 'futures';
        END IF;
        
        -- Получаем торговые часы для данного инструмента
        SELECT * INTO v_trading_hours
        FROM invest_views.get_trading_hours(v_daily_record.figi)
        WHERE (day_type = CASE WHEN v_is_weekend THEN 'weekend' ELSE 'weekday' END OR day_type = 'all')
        ORDER BY day_type DESC, created_at DESC
        LIMIT 1;
        
        -- Если специальных часов нет, используем стандартные
        IF v_trading_hours.start_hour IS NULL THEN
            IF v_is_weekend THEN
                -- Выходные дни: с 09:59 до 18:59
                v_trading_hours.start_hour := 9;
                v_trading_hours.start_minute := 59;
                v_trading_hours.end_hour := 18;
                v_trading_hours.end_minute := 59;
                v_trading_hours.is_special := false;
            ELSE
                -- Рабочие дни: без ограничений по времени
                v_trading_hours.start_hour := 0;
                v_trading_hours.start_minute := 0;
                v_trading_hours.end_hour := 23;
                v_trading_hours.end_minute := 59;
                v_trading_hours.is_special := false;
            END IF;
        END IF;
        
        -- Формируем условие для фильтрации по времени
        v_time_condition := format(
            'EXTRACT(hour FROM mc.time AT TIME ZONE ''Europe/Moscow'') >= %s AND ' ||
            '(EXTRACT(hour FROM mc.time AT TIME ZONE ''Europe/Moscow'') > %s OR ' ||
            ' EXTRACT(minute FROM mc.time AT TIME ZONE ''Europe/Moscow'') >= %s) AND ' ||
            'EXTRACT(hour FROM mc.time AT TIME ZONE ''Europe/Moscow'') <= %s AND ' ||
            '(EXTRACT(hour FROM mc.time AT TIME ZONE ''Europe/Moscow'') < %s OR ' ||
            ' EXTRACT(minute FROM mc.time AT TIME ZONE ''Europe/Moscow'') <= %s)',
            v_trading_hours.start_hour::text, v_trading_hours.start_hour::text, v_trading_hours.start_minute::text,
            v_trading_hours.end_hour::text, v_trading_hours.end_hour::text, v_trading_hours.end_minute::text
        );
        
        -- Получаем агрегированные данные из минутных свечей для того же инструмента и даты
        -- с учетом специальных торговых часов
        EXECUTE format('
            WITH filtered_candles AS (
                SELECT mc.volume, mc.high, mc.low, mc.open, mc.close, mc.time
                FROM invest.minute_candles mc
                WHERE mc.figi = %L
                AND DATE(mc.time AT TIME ZONE ''Europe/Moscow'') = %L
                AND (%s)
            )
            SELECT 
                SUM(volume) as minute_volume,
                MAX(high) as minute_high,
                MIN(low) as minute_low,
                (SELECT open FROM filtered_candles ORDER BY time ASC LIMIT 1) as minute_open,
                (SELECT close FROM filtered_candles ORDER BY time DESC LIMIT 1) as minute_close
            FROM filtered_candles',
            v_daily_record.figi, p_date, v_time_condition
        ) INTO v_minute_record;
        
        -- Проверяем наличие данных из минутных свечей
        IF v_minute_record.minute_volume IS NULL THEN
            -- Нет данных из минутных свечей - записываем проблему
            INSERT INTO invest.data_quality_issues (
                task_id, check_name, entity_type, entity_id, trade_date,
                metric, status, message, details
            ) VALUES (
                v_task_id, 'daily_vs_minute_check', v_instrument_type, v_daily_record.figi, p_date,
                'missing_minute_data', 'ERROR', 
                'No minute candles found for daily candle within trading hours',
                jsonb_build_object(
                    'instrument_type', v_instrument_type,
                    'is_weekend', v_is_weekend,
                    'is_special_hours', v_trading_hours.is_special,
                    'trading_hours', format('%02s:%02s-%02s:%02s', 
                        v_trading_hours.start_hour::text, v_trading_hours.start_minute::text,
                        v_trading_hours.end_hour::text, v_trading_hours.end_minute::text),
                    'daily_volume', v_daily_record.daily_volume,
                    'daily_high', v_daily_record.daily_high,
                    'daily_low', v_daily_record.daily_low,
                    'daily_open', v_daily_record.daily_open,
                    'daily_close', v_daily_record.daily_close
                )
            );
            v_mismatches := v_mismatches + 1;
        ELSE
            -- Вычисляем разности
            v_diff_volume := COALESCE(v_daily_record.daily_volume, 0) - COALESCE(v_minute_record.minute_volume, 0);
            v_diff_high := COALESCE(v_daily_record.daily_high, 0) - COALESCE(v_minute_record.minute_high, 0);
            v_diff_low := COALESCE(v_daily_record.daily_low, 0) - COALESCE(v_minute_record.minute_low, 0);
            v_diff_open := COALESCE(v_daily_record.daily_open, 0) - COALESCE(v_minute_record.minute_open, 0);
            v_diff_close := COALESCE(v_daily_record.daily_close, 0) - COALESCE(v_minute_record.minute_close, 0);
            
            -- Проверяем на несоответствия (допускаем небольшие погрешности)
            IF ABS(v_diff_volume) > 1 OR 
               ABS(v_diff_high) > 0.01 OR 
               ABS(v_diff_low) > 0.01 OR 
               ABS(v_diff_open) > 0.01 OR 
               ABS(v_diff_close) > 0.01 THEN
                
                -- Записываем несоответствие
                INSERT INTO invest.data_quality_issues (
                    task_id, check_name, entity_type, entity_id, trade_date,
                    metric, expected_numeric, actual_numeric, diff_numeric, status, message, details
                ) VALUES (
                    v_task_id, 'daily_vs_minute_check', v_instrument_type, v_daily_record.figi, p_date,
                    'volume_mismatch', v_daily_record.daily_volume, v_minute_record.minute_volume, v_diff_volume,
                    CASE WHEN ABS(v_diff_volume) > 1 THEN 'ERROR' ELSE 'WARNING' END,
                    'Volume mismatch between daily and minute candles within trading hours',
                    jsonb_build_object(
                        'instrument_type', v_instrument_type,
                        'is_weekend', v_is_weekend,
                        'is_special_hours', v_trading_hours.is_special,
                        'trading_hours', format('%02s:%02s-%02s:%02s', 
                            v_trading_hours.start_hour::text, v_trading_hours.start_minute::text,
                            v_trading_hours.end_hour::text, v_trading_hours.end_minute::text),
                        'daily_volume', v_daily_record.daily_volume,
                        'minute_volume', v_minute_record.minute_volume,
                        'volume_diff', v_diff_volume,
                        'high_diff', v_diff_high,
                        'low_diff', v_diff_low,
                        'open_diff', v_diff_open,
                        'close_diff', v_diff_close
                    )
                );
                v_mismatches := v_mismatches + 1;
            END IF;
        END IF;
    END LOOP;
    
    -- Определяем статус и сообщение для логирования
    IF v_mismatches = 0 THEN
        v_log_status := 'SUCCESS';
        v_log_message := 'Daily vs minute check completed successfully. Total rows: ' || v_total_rows || ', Mismatches: ' || v_mismatches;
    ELSE
        v_log_status := 'ERROR';
        v_log_message := 'Daily vs minute check completed with issues. Total rows: ' || v_total_rows || ', Mismatches: ' || v_mismatches;
    END IF;
    
    -- Логируем результат в system_logs
    INSERT INTO invest.system_logs (
        task_id, endpoint, method, status, message, start_time, end_time, duration_ms
    ) VALUES (
        v_task_id, 
        'data_quality_check', 
        'run_daily_vs_minute_check', 
        v_log_status, 
        v_log_message,
        v_start_time,
        NOW(),
        EXTRACT(EPOCH FROM (NOW() - v_start_time)) * 1000
    );
    
    -- Возвращаем результаты
    RETURN QUERY SELECT v_task_id, v_total_rows, v_mismatches;
END;
$$;

comment on function run_daily_vs_minute_check(date) is 'Проверяет соответствие дневных и минутных свечей с учетом специальных торговых часов, сохраняет результаты в data_quality_issues';

alter function run_daily_vs_minute_check(date) owner to postgres;

-- ============================================================================
-- СИНОНИМ В СХЕМЕ INVEST
-- ============================================================================

-- Создаем синоним функции в схеме invest
create or replace function invest.run_daily_vs_minute_check(p_date date)
    returns TABLE(task_id text, total_rows bigint, mismatches bigint)
    language sql
as $$
    select * from run_daily_vs_minute_check(p_date);
$$;

comment on function invest.run_daily_vs_minute_check(date) is 'Синоним для функции run_daily_vs_minute_check';

-- Устанавливаем владельца синонима
alter function invest.run_daily_vs_minute_check(date) owner to postgres;

-- Предоставляем права доступа на синоним
grant execute on function invest.run_daily_vs_minute_check(date) to admin;
grant execute on function invest.run_daily_vs_minute_check(date) to tester;

-- ============================================================================
-- ПРИМЕРЫ ИСПОЛЬЗОВАНИЯ СИСТЕМЫ
-- ============================================================================

-- Пример 1: Добавление специальных торговых часов для акции BBG004S689R0
-- (торговля только с 09:59 до 23:50 в рабочие дни)
/*
select invest.add_special_trading_hours(
    'BBG004S689R0',           -- FIGI
    'shares',                 -- тип инструмента
    'weekday',                -- рабочие дни
    9,                        -- час начала
    59,                       -- минута начала
    23,                       -- час окончания
    50,                       -- минута окончания
    'Торговля только с 09:59 до 23:50 в рабочие дни (без утренней сессии)',  -- описание
    'admin'                   -- создатель
);
*/

-- Пример 2: Добавление специальных торговых часов для выходных дней
/*
select invest.add_special_trading_hours(
    'BBG004S689R0',           -- FIGI
    'shares',                 -- тип инструмента
    'weekend',                -- выходные дни
    10,                       -- час начала
    0,                        -- минута начала
    18,                       -- час окончания
    0,                        -- минута окончания
    'Сокращенные часы торгов в выходные дни',  -- описание
    'admin'                   -- создатель
);
*/

-- Пример 3: Проверка торговых часов для инструмента
/*
select * from invest.get_trading_hours('BBG004S689R0', 'shares', false);  -- рабочий день
select * from invest.get_trading_hours('BBG004S689R0', 'shares', true);   -- выходной день
*/

-- Пример 4: Запуск проверки сверки дневных и минутных свечей
/*
-- Прямой вызов функции
select * from run_daily_vs_minute_check('2024-01-15');

-- Вызов через синоним в схеме invest
select * from invest.run_daily_vs_minute_check('2024-01-15');
*/

-- Пример 5: Просмотр проблем качества данных
/*
select 
    task_id,
    entity_id,
    trade_date,
    status,
    message,
    details->>'trading_hours' as trading_hours,
    details->>'is_special_hours' as is_special_hours
from invest.data_quality_issues 
where check_name = 'daily_vs_minute_check'
order by created_at desc
limit 10;
*/

-- Пример 6: Удаление специальных торговых часов
/*
select invest.remove_special_trading_hours('BBG004S689R0', 'shares', 'weekday');
*/
