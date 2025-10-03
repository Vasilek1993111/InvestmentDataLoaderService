-- Примеры использования системы бектестов

-- ============================================================================
-- ЛОГИКА ОПРЕДЕЛЕНИЯ ЦЕНЫ ВХОДА И ВЫХОДА:
-- ============================================================================
-- ENTRY_TYPE (тип входа):
--   OPEN:  первая свеча в понедельнике (цена open)
--   CLOSE: последняя свеча в пятницу (цена close)
--
-- EXIT_TYPE (тип выхода):
--   STOP_LOSS: выход по стоп-лоссу (по умолчанию), если не сработал - закрытие по сессии
--   TAKE_PROFIT: выход по тейк-профиту, если не сработал - закрытие по сессии
--   MAIN_SESSION_CLOSE: выход по закрытию основной сессии (18:40-18:59)
--   EVENING_SESSION_CLOSE: выход по последней свече вечерней сессии
--
-- ЛОГИКА ЗАКРЫТИЯ ПО СЕССИИ (если не сработали STOP_LOSS/TAKE_PROFIT):
--   Если entry_type = OPEN: ищется свеча закрытия основной сессии (18:40-18:59)
--   Если entry_type = CLOSE: ищется последняя свеча вечерней сессии
--
-- Анализ сделки происходит в ближайший понедельник (с 07:00 до 23:50 МСК)
-- 
-- Если analysis_date - выходной день:
--   - Суббота: CLOSE берется с пятницы, OPEN с понедельника, анализ в понедельник
--   - Воскресенье: CLOSE берется с пятницы, OPEN с понедельника, анализ в понедельник
-- Если analysis_date - рабочий день: используется сам analysis_date

-- ============================================================================
-- ПРИМЕРЫ ЗАПУСКА БЕКТЕСТОВ
-- ============================================================================

-- Пример 1: Простой бектест с входом по цене открытия
-- Параметры: ID паттерна = 1, вход по OPEN (первая свеча в понедельнике, цена open), сумма = 10000, стоп-лосс = 2%, тейк-профит = 5%
/*
select invest.run_backtest(
    p_pattern_analysis_id := 1,
    p_entry_type := 'OPEN',
    p_amount := 10000.00,
    p_stop_loss_percent := 2.0,
    p_take_profit_percent := 5.0
);
*/

-- Пример 2: Бектест с входом по цене закрытия
-- Параметры: ID паттерна = 2, вход по CLOSE (последняя свеча в пятницу, цена close), сумма = 50000, стоп-лосс = 1.5%, тейк-профит = 3%
/*
select invest.run_backtest(
    p_pattern_analysis_id := 2,
    p_entry_type := 'CLOSE',
    p_amount := 50000.00,
    p_stop_loss_percent := 1.5,
    p_take_profit_percent := 3.0
);
*/

-- Пример 3: Консервативный бектест
-- Параметры: ID паттерна = 3, вход по OPEN, сумма = 25000, стоп-лосс = 1%, тейк-профит = 2%
/*
select invest.run_backtest(
    p_pattern_analysis_id := 3,
    p_entry_type := 'OPEN',
    p_amount := 25000.00,
    p_stop_loss_percent := 1.0,
    p_take_profit_percent := 2.0
);
*/

-- Пример 4: Агрессивный бектест
-- Параметры: ID паттерна = 4, вход по CLOSE, сумма = 100000, стоп-лосс = 5%, тейк-профит = 15%
/*
select invest.run_backtest(
    p_pattern_analysis_id := 4,
    p_entry_type := 'CLOSE',
    p_amount := 100000.00,
    p_stop_loss_percent := 5.0,
    p_take_profit_percent := 15.0
);
*/

-- Пример 5: Бектест с выходом по закрытию основной сессии
-- Параметры: ID паттерна = 5, вход по OPEN, сумма = 20000, стоп-лосс = 2%, тейк-профит = 4%, выход по закрытию основной сессии
/*
select invest.run_backtest(
    p_pattern_analysis_id := 5,
    p_entry_type := 'OPEN',
    p_amount := 20000.00,
    p_stop_loss_percent := 2.0,
    p_take_profit_percent := 4.0,
    p_exit_type := 'MAIN_SESSION_CLOSE'
);
*/

-- Пример 6: Бектест с выходом по закрытию вечерней сессии
-- Параметры: ID паттерна = 6, вход по CLOSE, сумма = 30000, стоп-лосс = 1.5%, тейк-профит = 3%, выход по последней свече вечерней сессии
/*
select invest.run_backtest(
    p_pattern_analysis_id := 6,
    p_entry_type := 'CLOSE',
    p_amount := 30000.00,
    p_stop_loss_percent := 1.5,
    p_take_profit_percent := 3.0,
    p_exit_type := 'EVENING_SESSION_CLOSE'
);
*/

-- Пример 7: Бектест с автоматическим закрытием по сессии (STOP_LOSS)
-- Параметры: ID паттерна = 7, вход по OPEN, сумма = 15000, стоп-лосс = 3%, тейк-профит = 6%
-- Логика: если не сработает стоп-лосс/тейк-профит, закроется по основной сессии (18:40-18:59)
/*
select invest.run_backtest(
    p_pattern_analysis_id := 7,
    p_entry_type := 'OPEN',
    p_amount := 15000.00,
    p_stop_loss_percent := 3.0,
    p_take_profit_percent := 6.0,
    p_exit_type := 'STOP_LOSS'
);
*/

-- Пример 8: Бектест с автоматическим закрытием по сессии (TAKE_PROFIT)
-- Параметры: ID паттерна = 8, вход по CLOSE, сумма = 25000, стоп-лосс = 2%, тейк-профит = 4%
-- Логика: если не сработает стоп-лосс/тейк-профит, закроется по вечерней сессии (последняя свеча)
/*
select invest.run_backtest(
    p_pattern_analysis_id := 8,
    p_entry_type := 'CLOSE',
    p_amount := 25000.00,
    p_stop_loss_percent := 2.0,
    p_take_profit_percent := 4.0,
    p_exit_type := 'TAKE_PROFIT'
);
*/

-- ============================================================================
-- ЗАПРОСЫ ДЛЯ АНАЛИЗА РЕЗУЛЬТАТОВ
-- ============================================================================

-- Просмотр всех результатов бектестов
select 
    br.id,
    br.pattern_analysis_id,
    br.figi,
    br.analysis_date,
    br.entry_type,
    br.entry_price,
    br.amount,
    br.stop_loss_percent,
    br.take_profit_percent,
    br.exit_type,
    br.result,
    br.exit_price,
    br.profit_loss,
    br.profit_loss_percent,
    br.duration_minutes,
    br.created_at
from invest.backtest_results br
order by br.created_at desc;

-- Просмотр результатов с информацией о паттернах
select 
    br.id as backtest_id,
    cpa.figi,
    cpa.analysis_date,
    cpa.candle_type,
    cpa.consecutive_days,
    br.entry_type,
    br.entry_price,
    br.amount,
    br.stop_loss_percent || '%' as stop_loss,
    br.take_profit_percent || '%' as take_profit,
    br.exit_type,
    br.result,
    br.exit_price,
    round(br.profit_loss, 2) as profit_loss,
    round(br.profit_loss_percent, 2) || '%' as profit_loss_percent,
    br.duration_minutes || ' мин' as duration,
    br.created_at
from invest.backtest_results br
join invest.candle_pattern_analysis cpa on br.pattern_analysis_id = cpa.id
order by br.created_at desc;

-- Статистика по всем бектестам
select * from invest.get_backtest_stats();

-- Статистика по бектестам за определенный период
select * from invest.get_backtest_stats(
    p_analysis_date_from := '2024-01-01',
    p_analysis_date_to := '2024-12-31'
);

-- Статистика по конкретному инструменту
select * from invest.get_backtest_stats(
    p_figi := 'BBG004730N88'  -- Сбербанк
);

-- ============================================================================
-- АНАЛИТИЧЕСКИЕ ЗАПРОСЫ
-- ============================================================================

-- Топ-10 самых прибыльных сделок
select 
    br.id,
    cpa.figi,
    cpa.analysis_date,
    cpa.candle_type,
    br.entry_type,
    br.result,
    round(br.profit_loss_percent, 2) as profit_percent,
    round(br.profit_loss, 2) as profit_loss,
    br.duration_minutes
from invest.backtest_results br
join invest.candle_pattern_analysis cpa on br.pattern_analysis_id = cpa.id
order by br.profit_loss_percent desc
limit 10;

-- Топ-10 самых убыточных сделок
select 
    br.id,
    cpa.figi,
    cpa.analysis_date,
    cpa.candle_type,
    br.entry_type,
    br.result,
    round(br.profit_loss_percent, 2) as profit_percent,
    round(br.profit_loss, 2) as profit_loss,
    br.duration_minutes
from invest.backtest_results br
join invest.candle_pattern_analysis cpa on br.pattern_analysis_id = cpa.id
order by br.profit_loss_percent asc
limit 10;

-- Анализ эффективности по типу паттерна
select 
    cpa.candle_type,
    count(*) as total_trades,
    count(*) filter (where br.result = 'TAKE_PROFIT') as successful_trades,
    count(*) filter (where br.result = 'STOP_LOSS') as failed_trades,
    count(*) filter (where br.result = 'NO_EXIT') as no_exit_trades,
    round((count(*) filter (where br.result = 'TAKE_PROFIT')::numeric / count(*)) * 100, 2) as success_rate_percent,
    round(avg(br.profit_loss_percent), 2) as avg_profit_percent,
    round(sum(br.profit_loss), 2) as total_profit_loss,
    round(avg(br.duration_minutes), 0) as avg_duration_minutes
from invest.backtest_results br
join invest.candle_pattern_analysis cpa on br.pattern_analysis_id = cpa.id
group by cpa.candle_type
order by success_rate_percent desc;

-- Анализ эффективности по типу входа
select 
    br.entry_type,
    count(*) as total_trades,
    count(*) filter (where br.result = 'TAKE_PROFIT') as successful_trades,
    count(*) filter (where br.result = 'STOP_LOSS') as failed_trades,
    round((count(*) filter (where br.result = 'TAKE_PROFIT')::numeric / count(*)) * 100, 2) as success_rate_percent,
    round(avg(br.profit_loss_percent), 2) as avg_profit_percent,
    round(sum(br.profit_loss), 2) as total_profit_loss
from invest.backtest_results br
group by br.entry_type
order by success_rate_percent desc;

-- Анализ эффективности по типу выхода
select 
    br.exit_type,
    count(*) as total_trades,
    count(*) filter (where br.result = 'TAKE_PROFIT') as take_profit_exits,
    count(*) filter (where br.result = 'STOP_LOSS') as stop_loss_exits,
    count(*) filter (where br.result = 'MAIN_SESSION_CLOSE') as main_session_exits,
    count(*) filter (where br.result = 'EVENING_SESSION_CLOSE') as evening_session_exits,
    count(*) filter (where br.result = 'NO_EXIT') as no_exit_trades,
    round((count(*) filter (where br.result = 'TAKE_PROFIT')::numeric / count(*)) * 100, 2) as take_profit_rate_percent,
    round(avg(br.profit_loss_percent), 2) as avg_profit_percent,
    round(sum(br.profit_loss), 2) as total_profit_loss,
    round(avg(br.duration_minutes), 0) as avg_duration_minutes
from invest.backtest_results br
group by br.exit_type
order by take_profit_rate_percent desc;

-- Анализ фактических результатов сделок
select 
    br.result,
    count(*) as total_trades,
    round((count(*)::numeric / (select count(*) from invest.backtest_results)) * 100, 2) as percentage,
    round(avg(br.profit_loss_percent), 2) as avg_profit_percent,
    round(sum(br.profit_loss), 2) as total_profit_loss,
    round(avg(br.duration_minutes), 0) as avg_duration_minutes
from invest.backtest_results br
group by br.result
order by count(*) desc;

-- Анализ эффективности по длительности паттерна
select 
    cpa.consecutive_days,
    count(*) as total_trades,
    count(*) filter (where br.result = 'TAKE_PROFIT') as successful_trades,
    round((count(*) filter (where br.result = 'TAKE_PROFIT')::numeric / count(*)) * 100, 2) as success_rate_percent,
    round(avg(br.profit_loss_percent), 2) as avg_profit_percent,
    round(avg(br.duration_minutes), 0) as avg_duration_minutes
from invest.backtest_results br
join invest.candle_pattern_analysis cpa on br.pattern_analysis_id = cpa.id
group by cpa.consecutive_days
order by cpa.consecutive_days;

-- Анализ по инструментам (топ-10 по количеству сделок)
select 
    cpa.figi,
    count(*) as total_trades,
    count(*) filter (where br.result = 'TAKE_PROFIT') as successful_trades,
    round((count(*) filter (where br.result = 'TAKE_PROFIT')::numeric / count(*)) * 100, 2) as success_rate_percent,
    round(avg(br.profit_loss_percent), 2) as avg_profit_percent,
    round(sum(br.profit_loss), 2) as total_profit_loss
from invest.backtest_results br
join invest.candle_pattern_analysis cpa on br.pattern_analysis_id = cpa.id
group by cpa.figi
having count(*) >= 5  -- Минимум 5 сделок для статистической значимости
order by total_trades desc
limit 10;

-- Распределение длительности сделок
select 
    case 
        when br.duration_minutes <= 30 then '0-30 мин'
        when br.duration_minutes <= 60 then '31-60 мин'
        when br.duration_minutes <= 120 then '1-2 часа'
        when br.duration_minutes <= 240 then '2-4 часа'
        when br.duration_minutes <= 480 then '4-8 часов'
        else '8+ часов'
    end as duration_range,
    count(*) as trades_count,
    count(*) filter (where br.result = 'TAKE_PROFIT') as successful_trades,
    round((count(*) filter (where br.result = 'TAKE_PROFIT')::numeric / count(*)) * 100, 2) as success_rate_percent,
    round(avg(br.profit_loss_percent), 2) as avg_profit_percent
from invest.backtest_results br
group by 
    case 
        when br.duration_minutes <= 30 then '0-30 мин'
        when br.duration_minutes <= 60 then '31-60 мин'
        when br.duration_minutes <= 120 then '1-2 часа'
        when br.duration_minutes <= 240 then '2-4 часа'
        when br.duration_minutes <= 480 then '4-8 часов'
        else '8+ часов'
    end
order by 
    case 
        when duration_range = '0-30 мин' then 1
        when duration_range = '31-60 мин' then 2
        when duration_range = '1-2 часа' then 3
        when duration_range = '2-4 часа' then 4
        when duration_range = '4-8 часов' then 5
        else 6
    end;

-- ============================================================================
-- МАССОВЫЙ ЗАПУСК БЕКТЕСТОВ
-- ============================================================================

-- Функция для массового запуска бектестов по всем найденным паттернам
-- ВНИМАНИЕ: Этот запрос может выполняться долго, используйте с осторожностью!
/*
do $$
declare
    pattern_rec record;
    backtest_id bigint;
    processed_count integer := 0;
    error_count integer := 0;
begin
    -- Запускаем бектесты для всех паттернов с strategy_applicable = 'Y'
    for pattern_rec in
        select id, figi, analysis_date, candle_type
        from invest.candle_pattern_analysis
        where strategy_applicable = 'Y'
          and id not in (select distinct pattern_analysis_id from invest.backtest_results)
        order by analysis_date desc
        limit 100  -- Ограничиваем количество для безопасности
    loop
        begin
            -- Запускаем бектест с стандартными параметрами
            select invest.run_backtest(
                pattern_rec.id,
                'OPEN',  -- Вход по цене открытия
                10000.00,  -- Сумма 10,000
                2.0,     -- Стоп-лосс 2%
                5.0      -- Тейк-профит 5%
            ) into backtest_id;
            
            processed_count := processed_count + 1;
            
            -- Логируем каждые 10 обработанных записей
            if processed_count % 10 = 0 then
                raise notice 'Обработано паттернов: %, последний ID: %', processed_count, pattern_rec.id;
            end if;
            
        exception
            when others then
                error_count := error_count + 1;
                raise notice 'Ошибка при обработке паттерна ID %: %', pattern_rec.id, sqlerrm;
        end;
    end loop;
    
    raise notice 'Массовый бектест завершен. Обработано: %, ошибок: %', processed_count, error_count;
end;
$$;
*/

-- ============================================================================
-- ОЧИСТКА ДАННЫХ (используйте с осторожностью!)
-- ============================================================================

-- Удаление всех результатов бектестов
-- ВНИМАНИЕ: Это действие необратимо!
/*
-- delete from invest.backtest_results;
*/

-- Удаление результатов бектестов за определенный период
/*
-- delete from invest.backtest_results 
-- where analysis_date between '2024-01-01' and '2024-01-31';
*/

-- Удаление результатов бектестов по конкретному инструменту
/*
-- delete from invest.backtest_results 
-- where figi = 'BBG004730N88';
*/
