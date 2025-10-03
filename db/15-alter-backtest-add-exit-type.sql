-- ALTER запросы для добавления нового столбца exit_type в таблицу backtest_results
-- и обновления синонима

-- ============================================================================
-- ДОБАВЛЕНИЕ НОВОГО СТОЛБЦА В ТАБЛИЦУ
-- ============================================================================

-- Добавляем новый столбец exit_type в таблицу backtest_results
alter table invest_candles.backtest_results 
add column exit_type varchar(30) not null default 'STOP_LOSS';

-- Добавляем комментарий к новому столбцу
comment on column invest_candles.backtest_results.exit_type is 'Тип выхода из позиции: STOP_LOSS - по стоп-лоссу, TAKE_PROFIT - по тейк-профиту, MAIN_SESSION_CLOSE - по закрытию основной сессии (18:40-18:59), EVENING_SESSION_CLOSE - по закрытию вечерней сессии (последняя свеча)';

-- Добавляем ограничение для нового столбца
alter table invest_candles.backtest_results 
add constraint chk_exit_type check (exit_type in ('STOP_LOSS', 'TAKE_PROFIT', 'MAIN_SESSION_CLOSE', 'EVENING_SESSION_CLOSE'));

-- Обновляем существующее ограничение chk_result, если оно уже существует
-- Сначала удаляем старое ограничение
alter table invest_candles.backtest_results 
drop constraint if exists chk_result;

-- Добавляем обновленное ограничение
alter table invest_candles.backtest_results 
add constraint chk_result check (result in ('STOP_LOSS', 'TAKE_PROFIT', 'NO_EXIT', 'MAIN_SESSION_CLOSE', 'EVENING_SESSION_CLOSE'));

-- Создаем индекс для нового столбца (опционально, для оптимизации запросов)
create index idx_backtest_results_exit_type on invest_candles.backtest_results (exit_type);

-- ============================================================================
-- ОБНОВЛЕНИЕ СИНОНИМА (ПРЕДСТАВЛЕНИЯ)
-- ============================================================================

-- Удаляем старое представление
drop view if exists invest.backtest_results;

-- Создаем обновленное представление с новым столбцом
create view invest.backtest_results as
select 
    id, 
    pattern_analysis_id, 
    figi, 
    analysis_date, 
    entry_type, 
    entry_price, 
    amount,
    stop_loss_percent, 
    take_profit_percent, 
    stop_loss_price, 
    take_profit_price,
    exit_type, 
    result, 
    exit_price, 
    exit_time, 
    profit_loss, 
    profit_loss_percent, 
    duration_minutes, 
    created_at
from invest_candles.backtest_results;

-- Добавляем комментарий к обновленному представлению
comment on view invest.backtest_results is 'Синоним для таблицы backtest_results из схемы invest_candles с добавленным столбцом exit_type';

-- Настраиваем права доступа на обновленное представление
alter view invest.backtest_results owner to postgres;
grant select on invest.backtest_results to tester;
grant select on invest.backtest_results to admin;

-- ============================================================================
-- ОБНОВЛЕНИЕ СУЩЕСТВУЮЩИХ ЗАПИСЕЙ (ОПЦИОНАЛЬНО)
-- ============================================================================

-- Если в таблице уже есть записи, можно обновить их exit_type в зависимости от result
-- Раскомментируйте следующие запросы при необходимости:

/*
-- Обновляем exit_type для существующих записей на основе поля result
update invest_candles.backtest_results 
set exit_type = 'STOP_LOSS' 
where result = 'STOP_LOSS' and exit_type = 'STOP_LOSS';

update invest_candles.backtest_results 
set exit_type = 'TAKE_PROFIT' 
where result = 'TAKE_PROFIT' and exit_type = 'STOP_LOSS';

-- Для записей с result = 'NO_EXIT' можно установить подходящий тип выхода
-- Например, если они были созданы до введения новых типов выхода:
update invest_candles.backtest_results 
set exit_type = 'EVENING_SESSION_CLOSE' 
where result = 'NO_EXIT' and exit_type = 'STOP_LOSS';
*/

-- ============================================================================
-- ПРОВЕРКА РЕЗУЛЬТАТА
-- ============================================================================

-- Проверяем структуру обновленной таблицы
select 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
from information_schema.columns 
where table_schema = 'invest_candles' 
  and table_name = 'backtest_results'
order by ordinal_position;

-- Проверяем ограничения таблицы
select 
    constraint_name, 
    constraint_type, 
    check_clause
from information_schema.table_constraints tc
left join information_schema.check_constraints cc on tc.constraint_name = cc.constraint_name
where tc.table_schema = 'invest_candles' 
  and tc.table_name = 'backtest_results';

-- Проверяем индексы таблицы
select 
    indexname, 
    indexdef
from pg_indexes 
where schemaname = 'invest_candles' 
  and tablename = 'backtest_results';

-- Проверяем представление
select 
    column_name, 
    data_type
from information_schema.columns 
where table_schema = 'invest' 
  and table_name = 'backtest_results'
order by ordinal_position;
