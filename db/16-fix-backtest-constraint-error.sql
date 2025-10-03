-- Исправление ошибки ограничения chk_result в таблице backtest_results
-- Проблема: в поле result записываются значения, не разрешенные ограничением

-- ============================================================================
-- ИСПРАВЛЕНИЕ ОГРАНИЧЕНИЯ
-- ============================================================================

-- Удаляем старое ограничение chk_result
alter table invest_candles.backtest_results 
drop constraint if exists chk_result;

-- Добавляем обновленное ограничение chk_result
-- Поле result может содержать: STOP_LOSS, TAKE_PROFIT, NO_EXIT, MAIN_SESSION_CLOSE, EVENING_SESSION_CLOSE
alter table invest_candles.backtest_results 
add constraint chk_result check (result in ('STOP_LOSS', 'TAKE_PROFIT', 'NO_EXIT', 'MAIN_SESSION_CLOSE', 'EVENING_SESSION_CLOSE'));

-- ============================================================================
-- ИСПРАВЛЕНИЕ СУЩЕСТВУЮЩИХ ДАННЫХ (если есть некорректные записи)
-- ============================================================================

-- Проверяем, есть ли записи с некорректными значениями в поле result
select result, count(*) as count
from invest_candles.backtest_results 
group by result
order by result;

-- Если есть записи с некорректными значениями, исправляем их
-- Раскомментируйте следующие запросы при необходимости:

/*
-- Исправляем записи, где result содержит значения, не соответствующие ограничению
update invest_candles.backtest_results 
set result = 'NO_EXIT' 
where result not in ('STOP_LOSS', 'TAKE_PROFIT', 'NO_EXIT');

-- Или более специфично, если знаем, какие значения нужно исправить:
update invest_candles.backtest_results 
set result = 'NO_EXIT' 
where result = 'MAIN_SESSION_CLOSE';

update invest_candles.backtest_results 
set result = 'NO_EXIT' 
where result = 'EVENING_SESSION_CLOSE';
*/

-- ============================================================================
-- ПРОВЕРКА РЕЗУЛЬТАТА
-- ============================================================================

-- Проверяем, что ограничение работает корректно
select 
    constraint_name, 
    constraint_type, 
    check_clause
from information_schema.table_constraints tc
left join information_schema.check_constraints cc on tc.constraint_name = cc.constraint_name
where tc.table_schema = 'invest_candles' 
  and tc.table_name = 'backtest_results'
  and tc.constraint_name = 'chk_result';

-- Проверяем, что все значения в поле result соответствуют ограничению
select 
    result, 
    count(*) as count,
    case 
        when result in ('STOP_LOSS', 'TAKE_PROFIT', 'NO_EXIT', 'MAIN_SESSION_CLOSE', 'EVENING_SESSION_CLOSE') then 'OK'
        else 'ERROR'
    end as status
from invest_candles.backtest_results 
group by result
order by result;

-- ============================================================================
-- ДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ
-- ============================================================================

-- Логика полей в таблице backtest_results:
-- 
-- entry_type: тип входа (OPEN, CLOSE)
-- exit_type: тип выхода (STOP_LOSS, TAKE_PROFIT, MAIN_SESSION_CLOSE, EVENING_SESSION_CLOSE)
-- result: фактический результат сделки (STOP_LOSS, TAKE_PROFIT, NO_EXIT)
--
-- Поле result показывает, как фактически завершилась сделка:
-- - STOP_LOSS: сработал стоп-лосс
-- - TAKE_PROFIT: сработал тейк-профит  
-- - NO_EXIT: сделка не была закрыта по стоп-лоссу или тейк-профиту (редкий случай)
-- - MAIN_SESSION_CLOSE: сделка закрыта по цене закрытия основной сессии (18:40-18:59)
-- - EVENING_SESSION_CLOSE: сделка закрыта по цене закрытия вечерней сессии (последняя свеча)
--
-- Поле exit_type показывает, какой тип выхода был запланирован для анализа
