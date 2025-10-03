-- Система бектестов для анализа торговых стратегий
-- Таблица для хранения результатов бектестов

-- Таблица для хранения результатов бектестов
create table invest_candles.backtest_results
(
    id                      bigserial                                 primary key,
    pattern_analysis_id     bigint                                    not null,
    figi                    varchar(255)                              not null,
    analysis_date           date                                      not null,
    entry_type              varchar(20)                               not null,
    entry_price             numeric(18, 9)                            not null,
    amount                  numeric(18, 2)                            not null,
    stop_loss_percent       numeric(5, 2)                             not null,
    take_profit_percent     numeric(5, 2)                             not null,
    stop_loss_price         numeric(18, 9)                            not null,
    take_profit_price       numeric(18, 9)                            not null,
    exit_type               varchar(30)                               not null,
    result                  varchar(20)                               not null,
    exit_price              numeric(18, 9)                            not null,
    exit_time               timestamp(6) with time zone               not null,
    profit_loss             numeric(18, 2)                            not null,
    profit_loss_percent     numeric(8, 4)                             not null,
    duration_minutes        integer                                   not null,
    created_at              timestamp(6) with time zone default now() not null,
    constraint fk_backtest_pattern_analysis foreign key (pattern_analysis_id) 
        references invest_candles.candle_pattern_analysis (id) on delete cascade,
    constraint chk_entry_type check (entry_type in ('OPEN', 'CLOSE')),
    constraint chk_exit_type check (exit_type in ('STOP_LOSS', 'TAKE_PROFIT', 'MAIN_SESSION_CLOSE', 'EVENING_SESSION_CLOSE')),
    constraint chk_result check (result in ('STOP_LOSS', 'TAKE_PROFIT', 'NO_EXIT', 'MAIN_SESSION_CLOSE', 'EVENING_SESSION_CLOSE')),
    constraint chk_stop_loss_percent check (stop_loss_percent > 0 and stop_loss_percent <= 100),
    constraint chk_take_profit_percent check (take_profit_percent > 0 and take_profit_percent <= 1000)
);

comment on table invest_candles.backtest_results is 'Таблица для хранения результатов бектестов торговых стратегий';

comment on column invest_candles.backtest_results.id is 'Уникальный идентификатор результата бектеста';
comment on column invest_candles.backtest_results.pattern_analysis_id is 'ID записи из таблицы candle_pattern_analysis';
comment on column invest_candles.backtest_results.figi is 'Уникальный идентификатор инструмента (FIGI)';
comment on column invest_candles.backtest_results.analysis_date is 'Дата анализа паттерна';
comment on column invest_candles.backtest_results.entry_type is 'Тип входа в позицию: OPEN - по цене открытия, CLOSE - по цене закрытия';
comment on column invest_candles.backtest_results.entry_price is 'Цена входа в позицию';
comment on column invest_candles.backtest_results.exit_type is 'Тип выхода из позиции: STOP_LOSS - по стоп-лоссу, TAKE_PROFIT - по тейк-профиту, MAIN_SESSION_CLOSE - по закрытию основной сессии (18:40-18:59), EVENING_SESSION_CLOSE - по закрытию вечерней сессии (последняя свеча)';
comment on column invest_candles.backtest_results.amount is 'Сумма сделки';
comment on column invest_candles.backtest_results.stop_loss_percent is 'Стоп-лосс в процентах';
comment on column invest_candles.backtest_results.take_profit_percent is 'Тейк-профит в процентах';
comment on column invest_candles.backtest_results.stop_loss_price is 'Цена стоп-лосса';
comment on column invest_candles.backtest_results.take_profit_price is 'Цена тейк-профита';
comment on column invest_candles.backtest_results.result is 'Результат сделки: STOP_LOSS - сработал стоп-лосс, TAKE_PROFIT - сработал тейк-профит, NO_EXIT - не закрыта по уровням, MAIN_SESSION_CLOSE - закрыта по основной сессии, EVENING_SESSION_CLOSE - закрыта по вечерней сессии';
comment on column invest_candles.backtest_results.exit_price is 'Цена выхода из позицииы';
comment on column invest_candles.backtest_results.exit_time is 'Время выхода из позиции';
comment on column invest_candles.backtest_results.profit_loss is 'Прибыль/убыток в абсолютных значениях';
comment on column invest_candles.backtest_results.profit_loss_percent is 'Прибыль/убыток в процентах';
comment on column invest_candles.backtest_results.duration_minutes is 'Длительность сделки в минутах';
comment on column invest_candles.backtest_results.created_at is 'Время создания записи';

-- Индексы для оптимизации запросов
create index idx_backtest_results_pattern_analysis_id on invest_candles.backtest_results (pattern_analysis_id);
create index idx_backtest_results_figi on invest_candles.backtest_results (figi);
create index idx_backtest_results_analysis_date on invest_candles.backtest_results (analysis_date);
create index idx_backtest_results_result on invest_candles.backtest_results (result);
create index idx_backtest_results_entry_type on invest_candles.backtest_results (entry_type);

-- Права доступа
alter table invest_candles.backtest_results owner to postgres;
grant select on invest_candles.backtest_results to tester;
grant delete, insert, references, select, trigger, truncate, update on invest_candles.backtest_results to admin;

-- Функция для выполнения бектеста по ID из candle_pattern_analysis
create or replace function invest_views.run_backtest(
    p_pattern_analysis_id bigint,
    p_entry_type varchar(20),
    p_amount numeric(18, 2),
    p_stop_loss_percent numeric(5, 2),
    p_take_profit_percent numeric(5, 2),
    p_exit_type varchar(30) default 'STOP_LOSS'
)
returns bigint
language plpgsql
as $$
declare
    v_pattern_record record;
    v_entry_price numeric(18, 9);
    v_stop_loss_price numeric(18, 9);
    v_take_profit_price numeric(18, 9);
    v_candle_record record;
    v_result varchar(20) := 'NO_EXIT';
    v_exit_price numeric(18, 9);
    v_exit_time timestamp with time zone;
    v_profit_loss numeric(18, 2);
    v_profit_loss_percent numeric(8, 4);
    v_duration_minutes integer;
    v_backtest_id bigint;
    v_task_id varchar(255);
    v_start_time timestamp with time zone;
    v_end_time timestamp with time zone;
    v_candles_analyzed integer := 0;
    v_trading_day_start timestamp with time zone;
    v_trading_day_end timestamp with time zone;
begin
    -- Валидация входных параметров
    if p_entry_type not in ('OPEN', 'CLOSE') then
        raise exception 'Неверный тип входа: %. Допустимые значения: OPEN, CLOSE', p_entry_type;
    end if;
    
    if p_exit_type not in ('STOP_LOSS', 'TAKE_PROFIT', 'MAIN_SESSION_CLOSE', 'EVENING_SESSION_CLOSE') then
        raise exception 'Неверный тип выхода: %. Допустимые значения: STOP_LOSS, TAKE_PROFIT, MAIN_SESSION_CLOSE, EVENING_SESSION_CLOSE', p_exit_type;
    end if;
    
    if p_amount <= 0 then
        raise exception 'Сумма должна быть больше 0, получено: %', p_amount;
    end if;
    
    if p_stop_loss_percent <= 0 or p_stop_loss_percent > 100 then
        raise exception 'Стоп-лосс должен быть от 0 до 100%%, получено: %', p_stop_loss_percent;
    end if;
    
    if p_take_profit_percent <= 0 or p_take_profit_percent > 1000 then
        raise exception 'Тейк-профит должен быть от 0 до 1000%%, получено: %', p_take_profit_percent;
    end if;

    -- Генерируем уникальный ID задачи для логирования
    v_task_id := 'BACKTEST_' || p_pattern_analysis_id || '_' || extract(epoch from now())::bigint;
    v_start_time := now();
    
    -- Получаем данные из candle_pattern_analysis
    select * into v_pattern_record
    from invest_candles.candle_pattern_analysis
    where id = p_pattern_analysis_id;
    
    if not found then
        raise exception 'Запись с ID % не найдена в таблице candle_pattern_analysis', p_pattern_analysis_id;
    end if;
    
    -- Определяем рабочие дни для анализа
    declare
        v_analysis_workday date;
        v_previous_friday date;
        v_next_monday date;
    begin
        if extract(dow from v_pattern_record.analysis_date) in (0, 6) then
            -- Если analysis_date - выходной, определяем ближайшие рабочие дни
            if extract(dow from v_pattern_record.analysis_date) = 0 then -- Воскресенье
                v_previous_friday := v_pattern_record.analysis_date - interval '2 days';
                v_next_monday := v_pattern_record.analysis_date + interval '1 day';
            else -- Суббота
                v_previous_friday := v_pattern_record.analysis_date - interval '1 day';
                v_next_monday := v_pattern_record.analysis_date + interval '2 days';
            end if;
            v_analysis_workday := v_next_monday;
        else
            -- Если analysis_date - рабочий день, используем его
            v_analysis_workday := v_pattern_record.analysis_date;
            v_previous_friday := v_pattern_record.analysis_date - interval '1 day';
            v_next_monday := v_pattern_record.analysis_date;
        end if;
    
    -- Логируем начало работы
    insert into invest.system_logs (task_id, endpoint, method, status, message, start_time)
    values (v_task_id, 'run_backtest', 'FUNCTION', 'STARTED', 
            format('Начат бектест для паттерна ID %s, FIGI %s, дата анализа %s', 
                   p_pattern_analysis_id, v_pattern_record.figi, v_pattern_record.analysis_date), v_start_time);
    
        -- Определяем цену входа в зависимости от типа
        if p_entry_type = 'OPEN' then
            -- Для OPEN: ищем первую свечу в понедельнике (или analysis_date если рабочий день)
            v_trading_day_start := (v_next_monday + interval '7 hours')::timestamp with time zone;
            v_trading_day_end := (v_next_monday + interval '23 hours 50 minutes')::timestamp with time zone;
            
            select * into v_candle_record
            from minute_candles
            where figi = v_pattern_record.figi
              and time >= v_trading_day_start
              and time < v_trading_day_end
              and is_complete = true
            order by time
            limit 1;
            
            if not found then
                raise exception 'Не найдены минутные свечи для FIGI % на дату % (OPEN)', v_pattern_record.figi, v_next_monday;
            end if;
            
            v_entry_price := v_candle_record.open;
            
        else -- CLOSE
            -- Для CLOSE: ищем последнюю свечу в пятницу
            v_trading_day_start := (v_previous_friday + interval '7 hours')::timestamp with time zone;
            v_trading_day_end := (v_previous_friday + interval '23 hours 50 minutes')::timestamp with time zone;
            
            select * into v_candle_record
            from minute_candles
            where figi = v_pattern_record.figi
              and time >= v_trading_day_start
              and time < v_trading_day_end
              and is_complete = true
            order by time desc
            limit 1;
            
            if not found then
                raise exception 'Не найдены минутные свечи для FIGI % на дату % (CLOSE)', v_pattern_record.figi, v_previous_friday;
            end if;
            
            v_entry_price := v_candle_record.close;
        end if;
    
        -- Определяем торговый день для анализа (ближайший понедельник или analysis_date если рабочий день)
        v_trading_day_start := (v_analysis_workday + interval '7 hours')::timestamp with time zone;
        v_trading_day_end := (v_analysis_workday + interval '23 hours 50 minutes')::timestamp with time zone;
    
    -- Рассчитываем цены стоп-лосса и тейк-профита
    -- Для бычьего паттерна: покупаем, стоп-лосс ниже, тейк-профит выше
    -- Для медвежьего паттерна: продаем, стоп-лосс выше, тейк-профит ниже
    if v_pattern_record.candle_type = 'BULLISH' then
        -- Покупка: стоп-лосс ниже цены входа, тейк-профит выше
        v_stop_loss_price := v_entry_price * (1 - p_stop_loss_percent / 100);
        v_take_profit_price := v_entry_price * (1 + p_take_profit_percent / 100);
    else -- BEARISH
        -- Продажа: стоп-лосс выше цены входа, тейк-профит ниже
        v_stop_loss_price := v_entry_price * (1 + p_stop_loss_percent / 100);
        v_take_profit_price := v_entry_price * (1 - p_take_profit_percent / 100);
    end if;
    
    -- Инициализируем значения по умолчанию
    v_exit_price := v_entry_price;
    v_exit_time := v_candle_record.time;
    
        -- Анализируем минутные свечи в зависимости от типа выхода
        if p_exit_type in ('STOP_LOSS', 'TAKE_PROFIT') then
            -- Анализируем минутные свечи в течение торгового дня для стоп-лосса/тейк-профита
            for v_candle_record in
                select *
                from minute_candles
                where figi = v_pattern_record.figi
                  and time >= v_trading_day_start
                  and time < v_trading_day_end
                  and is_complete = true
                order by time
            loop
                v_candles_analyzed := v_candles_analyzed + 1;
                
                -- Проверяем условия выхода в зависимости от типа паттерна
                if v_pattern_record.candle_type = 'BULLISH' then
                    -- Для бычьего паттерна (покупка)
                    if v_candle_record.low <= v_stop_loss_price then
                        -- Сработал стоп-лосс
                        v_result := 'STOP_LOSS';
                        v_exit_price := v_stop_loss_price;
                        v_exit_time := v_candle_record.time;
                        exit;
                    elsif v_candle_record.high >= v_take_profit_price then
                        -- Сработал тейк-профит
                        v_result := 'TAKE_PROFIT';
                        v_exit_price := v_take_profit_price;
                        v_exit_time := v_candle_record.time;
                        exit;
                    end if;
                else -- BEARISH
                    -- Для медвежьего паттерна (продажа)
                    if v_candle_record.high >= v_stop_loss_price then
                        -- Сработал стоп-лосс
                        v_result := 'STOP_LOSS';
                        v_exit_price := v_stop_loss_price;
                        v_exit_time := v_candle_record.time;
                        exit;
                    elsif v_candle_record.low <= v_take_profit_price then
                        -- Сработал тейк-профит
                        v_result := 'TAKE_PROFIT';
                        v_exit_price := v_take_profit_price;
                        v_exit_time := v_candle_record.time;
                        exit;
                    end if;
                end if;
                
                -- Обновляем цену выхода на случай, если не сработают условия
                v_exit_price := v_candle_record.close;
                v_exit_time := v_candle_record.time;
            end loop;
            
            -- Если сделка не закрылась по стоп-лоссу или тейк-профиту, ищем соответствующую свечу закрытия сессии
            if v_result = 'NO_EXIT' then
                -- Определяем, какую свечу закрытия искать в зависимости от типа входа
                if p_entry_type = 'OPEN' then
                    -- Для OPEN ищем свечу закрытия основной сессии (18:40-18:59)
                    select * into v_candle_record
                    from minute_candles
                    where figi = v_pattern_record.figi
                      and time >= v_trading_day_start
                      and time < v_trading_day_end
                      and is_complete = true
                      -- Основная сессия: 18:40-18:59
                      and extract(hour from time) = 18
                      and extract(minute from time) between 40 and 59
                    order by time desc
                    limit 1;
                    
                    if found then
                        v_result := 'MAIN_SESSION_CLOSE';
                        v_exit_price := v_candle_record.close;
                        v_exit_time := v_candle_record.time;
                    else
                        -- Если не найдена свеча основной сессии, берем последнюю свечу
                        select * into v_candle_record
                        from minute_candles
                        where figi = v_pattern_record.figi
                          and time >= v_trading_day_start
                          and time < v_trading_day_end
                          and is_complete = true
                        order by time desc
                        limit 1;
                        
                        if found then
                            v_result := 'EVENING_SESSION_CLOSE';
                            v_exit_price := v_candle_record.close;
                            v_exit_time := v_candle_record.time;
                        end if;
                    end if;
                else -- CLOSE
                    -- Для CLOSE ищем последнюю свечу вечерней сессии
                    select * into v_candle_record
                    from minute_candles
                    where figi = v_pattern_record.figi
                      and time >= v_trading_day_start
                      and time < v_trading_day_end
                      and is_complete = true
                    order by time desc
                    limit 1;
                    
                    if found then
                        v_result := 'EVENING_SESSION_CLOSE';
                        v_exit_price := v_candle_record.close;
                        v_exit_time := v_candle_record.time;
                    end if;
                end if;
            end if;
            
        elsif p_exit_type = 'MAIN_SESSION_CLOSE' then
            -- Ищем свечу закрытия основной сессии (18:40-18:59)
            select * into v_candle_record
            from minute_candles
            where figi = v_pattern_record.figi
              and time >= v_trading_day_start
              and time < v_trading_day_end
              and is_complete = true
              -- Основная сессия: 18:40-18:59
              and extract(hour from time) = 18
              and extract(minute from time) between 40 and 59
            order by time desc
            limit 1;
            
            if found then
                v_result := 'MAIN_SESSION_CLOSE';
                v_exit_price := v_candle_record.close;
                v_exit_time := v_candle_record.time;
                v_candles_analyzed := 1;
            else
                raise exception 'Не найдены минутные свечи основной сессии для FIGI % на дату %', v_pattern_record.figi, v_analysis_workday;
            end if;
            
        else -- EVENING_SESSION_CLOSE
            -- Ищем последнюю свечу вечерней сессии
            select * into v_candle_record
            from minute_candles
            where figi = v_pattern_record.figi
              and time >= v_trading_day_start
              and time < v_trading_day_end
              and is_complete = true
            order by time desc
            limit 1;
            
            if found then
                v_result := 'EVENING_SESSION_CLOSE';
                v_exit_price := v_candle_record.close;
                v_exit_time := v_candle_record.time;
                v_candles_analyzed := 1;
            else
                raise exception 'Не найдены минутные свечи для FIGI % на дату %', v_pattern_record.figi, v_analysis_workday;
            end if;
        end if;
    end;
    
    -- Рассчитываем прибыль/убыток
    if v_pattern_record.candle_type = 'BULLISH' then
        -- Для покупки: прибыль = (цена выхода - цена входа) * количество
        v_profit_loss := (v_exit_price - v_entry_price) * (p_amount / v_entry_price);
        v_profit_loss_percent := ((v_exit_price - v_entry_price) / v_entry_price) * 100;
    else -- BEARISH
        -- Для продажи: прибыль = (цена входа - цена выхода) * количество
        v_profit_loss := (v_entry_price - v_exit_price) * (p_amount / v_entry_price);
        v_profit_loss_percent := ((v_entry_price - v_exit_price) / v_entry_price) * 100;
    end if;
    
    -- Рассчитываем длительность в минутах
    v_duration_minutes := extract(epoch from (v_exit_time - v_trading_day_start))::integer / 60;
    
    -- Сохраняем результат бектеста
    insert into invest_candles.backtest_results (
        pattern_analysis_id, figi, analysis_date, entry_type, entry_price, amount,
        stop_loss_percent, take_profit_percent, stop_loss_price, take_profit_price,
        exit_type, result, exit_price, exit_time, profit_loss, profit_loss_percent, duration_minutes
    ) values (
        p_pattern_analysis_id, v_pattern_record.figi, v_pattern_record.analysis_date, p_entry_type, v_entry_price, p_amount,
        p_stop_loss_percent, p_take_profit_percent, v_stop_loss_price, v_take_profit_price,
        p_exit_type, v_result, v_exit_price, v_exit_time, v_profit_loss, v_profit_loss_percent, v_duration_minutes
    ) returning id into v_backtest_id;
    
    v_end_time := now();
    
    -- Логируем успешное завершение
    insert into invest.system_logs (task_id, endpoint, method, status, message, start_time, end_time)
    values (v_task_id, 'run_backtest', 'FUNCTION', 'SUCCESS', 
            format('Бектест завершен успешно. ID результата: %s, результат: %s, P&L: %s%%, свечей проанализировано: %s', 
                   v_backtest_id, v_result, round(v_profit_loss_percent, 2), v_candles_analyzed), v_start_time, v_end_time);
    
    return v_backtest_id;
    
exception
    when others then
        v_end_time := now();
        
        -- Логируем ошибку
        insert into invest.system_logs (task_id, endpoint, method, status, message, start_time, end_time)
        values (v_task_id, 'run_backtest', 'FUNCTION', 'ERROR', 
                format('Ошибка при выполнении бектеста: %s', sqlerrm), v_start_time, v_end_time);
        
        raise;
end;
$$;

comment on function invest_views.run_backtest(bigint, varchar, numeric, numeric, numeric, varchar) is 'Функция для выполнения бектеста торговой стратегии по ID из candle_pattern_analysis. entry_type: OPEN - первая свеча в понедельнике (цена open), CLOSE - последняя свеча в пятнице (цена close). exit_type: STOP_LOSS/TAKE_PROFIT - по уровням, если не сработали - закрытие по сессии (OPEN→основная сессия, CLOSE→вечерняя сессия), MAIN_SESSION_CLOSE - закрытие основной сессии (18:40-18:59), EVENING_SESSION_CLOSE - последняя свеча вечерней сессии. Если analysis_date - выходной, анализ происходит в ближайший понедельник. Торговый интервал: 07:00-23:50 МСК';

-- Права на функцию
alter function invest_views.run_backtest(bigint, varchar, numeric, numeric, numeric, varchar) owner to postgres;
grant execute on function invest_views.run_backtest(bigint, varchar, numeric, numeric, numeric, varchar) to admin;

-- Вспомогательная функция для получения статистики по бектестам
create or replace function invest_views.get_backtest_stats(
    p_analysis_date_from date default null,
    p_analysis_date_to date default null,
    p_figi varchar(255) default null
)
returns table (
    total_backtests bigint,
    successful_backtests bigint,
    failed_backtests bigint,
    no_exit_backtests bigint,
    success_rate numeric(5, 2),
    avg_profit_loss_percent numeric(8, 4),
    total_profit_loss numeric(18, 2),
    avg_duration_minutes numeric(8, 2),
    best_trade_percent numeric(8, 4),
    worst_trade_percent numeric(8, 4)
)
language plpgsql
as $$
begin
    return query
    select 
        count(*) as total_backtests,
        count(*) filter (where result = 'TAKE_PROFIT') as successful_backtests,
        count(*) filter (where result = 'STOP_LOSS') as failed_backtests,
        count(*) filter (where result = 'NO_EXIT') as no_exit_backtests,
        round((count(*) filter (where result = 'TAKE_PROFIT')::numeric / nullif(count(*), 0)) * 100, 2) as success_rate,
        avg(br.profit_loss_percent) as avg_profit_loss_percent,
        sum(br.profit_loss) as total_profit_loss,
        avg(br.duration_minutes) as avg_duration_minutes,
        max(br.profit_loss_percent) as best_trade_percent,
        min(br.profit_loss_percent) as worst_trade_percent
    from invest_candles.backtest_results br
    where (p_analysis_date_from is null or br.analysis_date >= p_analysis_date_from)
      and (p_analysis_date_to is null or br.analysis_date <= p_analysis_date_to)
      and (p_figi is null or br.figi = p_figi);
end;
$$;

comment on function invest_views.get_backtest_stats(date, date, varchar) is 'Функция для получения статистики по результатам бектестов';

-- Права на вспомогательную функцию
alter function invest_views.get_backtest_stats(date, date, varchar) owner to postgres;
grant execute on function invest_views.get_backtest_stats(date, date, varchar) to admin;
grant execute on function invest_views.get_backtest_stats(date, date, varchar) to tester;

-- Создание синонимов в схеме invest для удобства использования
create view invest.backtest_results as
select 
    id, pattern_analysis_id, figi, analysis_date, entry_type, entry_price, amount,
    stop_loss_percent, take_profit_percent, stop_loss_price, take_profit_price,
    exit_type, result, exit_price, exit_time, profit_loss, profit_loss_percent, duration_minutes, created_at
from invest_candles.backtest_results;

comment on view invest.backtest_results is 'Синоним для таблицы backtest_results из схемы invest_candles';

-- Права доступа на представление
alter view invest.backtest_results owner to postgres;
grant select on invest.backtest_results to tester;
grant select on invest.backtest_results to admin;

-- Создание синонима для функции бектеста в схеме invest
create or replace function invest.run_backtest(
    p_pattern_analysis_id bigint,
    p_entry_type varchar(20),
    p_amount numeric(18, 2),
    p_stop_loss_percent numeric(5, 2),
    p_take_profit_percent numeric(5, 2),
    p_exit_type varchar(30) default 'STOP_LOSS'
)
returns bigint
language plpgsql
as $$
begin
    -- Вызываем оригинальную функцию из схемы invest_views
    return invest_views.run_backtest(p_pattern_analysis_id, p_entry_type, p_amount, p_stop_loss_percent, p_take_profit_percent, p_exit_type);
end;
$$;

comment on function invest.run_backtest(bigint, varchar, numeric, numeric, numeric, varchar) is 'Синоним для функции run_backtest из схемы invest_views. entry_type: OPEN - первая свеча в понедельнике (цена open), CLOSE - последняя свеча в пятнице (цена close). exit_type: STOP_LOSS/TAKE_PROFIT - по уровням, если не сработали - закрытие по сессии (OPEN→основная сессия, CLOSE→вечерняя сессия), MAIN_SESSION_CLOSE - закрытие основной сессии (18:40-18:59), EVENING_SESSION_CLOSE - последняя свеча вечерней сессии. Если analysis_date - выходной, анализ происходит в ближайший понедельник. Торговый интервал: 07:00-23:50 МСК';

-- Права на функцию-синоним
alter function invest.run_backtest(bigint, varchar, numeric, numeric, numeric, varchar) owner to postgres;
grant execute on function invest.run_backtest(bigint, varchar, numeric, numeric, numeric, varchar) to admin;

-- Создание синонима для функции статистики в схеме invest
create or replace function invest.get_backtest_stats(
    p_analysis_date_from date default null,
    p_analysis_date_to date default null,
    p_figi varchar(255) default null
)
returns table (
    total_backtests bigint,
    successful_backtests bigint,
    failed_backtests bigint,
    no_exit_backtests bigint,
    success_rate numeric(5, 2),
    avg_profit_loss_percent numeric(8, 4),
    total_profit_loss numeric(18, 2),
    avg_duration_minutes numeric(8, 2),
    best_trade_percent numeric(8, 4),
    worst_trade_percent numeric(8, 4)
)
language plpgsql
as $$
begin
    -- Вызываем оригинальную функцию из схемы invest_views
    return query select * from invest_views.get_backtest_stats(p_analysis_date_from, p_analysis_date_to, p_figi);
end;
$$;

comment on function invest.get_backtest_stats(date, date, varchar) is 'Синоним для функции get_backtest_stats из схемы invest_views';

-- Права на функцию-синоним статистики
alter function invest.get_backtest_stats(date, date, varchar) owner to postgres;
grant execute on function invest.get_backtest_stats(date, date, varchar) to admin;
grant execute on function invest.get_backtest_stats(date, date, varchar) to tester;
