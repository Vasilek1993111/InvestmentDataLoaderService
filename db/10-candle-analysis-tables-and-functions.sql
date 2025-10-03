-- Таблица для хранения результатов анализа свечей с одинаковым типом за 5 дней подряд
create table invest_candles.candle_pattern_analysis
(
    id                   bigserial                                 primary key,
    figi                 varchar(255)                              not null,
    analysis_date        date                                      not null,
    pattern_start_date   date                                      not null,
    pattern_end_date     date                                      not null,
    candle_type          varchar(20)                               not null,
    consecutive_days     integer                                   not null,
    avg_volume           bigint,
    avg_price_change     numeric(18, 9),
    total_price_change   numeric(18, 9),
    strategy_applicable  char(1) default null,
    created_at           timestamp(6) with time zone default now() not null,
    constraint chk_candle_type check (candle_type in ('BULLISH', 'BEARISH')),
    constraint chk_consecutive_days check (consecutive_days >= 2),
    constraint chk_strategy_applicable check (strategy_applicable in ('Y', 'N') or strategy_applicable is null)
);

comment on table invest_candles.candle_pattern_analysis is 'Таблица для хранения результатов анализа паттернов свечей с одинаковым типом за N дней подряд (минимум 2 дня)';

comment on column invest_candles.candle_pattern_analysis.id is 'Уникальный идентификатор записи';
comment on column invest_candles.candle_pattern_analysis.figi is 'Уникальный идентификатор инструмента (FIGI)';
comment on column invest_candles.candle_pattern_analysis.analysis_date is 'Дата проведения анализа';
comment on column invest_candles.candle_pattern_analysis.pattern_start_date is 'Дата начала паттерна (первый день одинакового типа свечи)';
comment on column invest_candles.candle_pattern_analysis.pattern_end_date is 'Дата окончания паттерна (последний день одинакового типа свечи)';
comment on column invest_candles.candle_pattern_analysis.candle_type is 'Тип свечи в паттерне (BULLISH или BEARISH)';
comment on column invest_candles.candle_pattern_analysis.consecutive_days is 'Количество дней подряд с одинаковым типом свечи';
comment on column invest_candles.candle_pattern_analysis.avg_volume is 'Средний объем торгов за период паттерна';
comment on column invest_candles.candle_pattern_analysis.avg_price_change is 'Среднее изменение цены за день в период паттерна';
comment on column invest_candles.candle_pattern_analysis.total_price_change is 'Общее изменение цены за весь период паттерна';
comment on column invest_candles.candle_pattern_analysis.strategy_applicable is 'Флаг применимости стратегии: Y - применима, N - не применима, NULL - не оценена';
comment on column invest_candles.candle_pattern_analysis.created_at is 'Время создания записи';

-- Индексы для оптимизации запросов
create index idx_candle_pattern_analysis_figi on invest_candles.candle_pattern_analysis (figi);
create index idx_candle_pattern_analysis_analysis_date on invest_candles.candle_pattern_analysis (analysis_date);
create index idx_candle_pattern_analysis_pattern_dates on invest_candles.candle_pattern_analysis (pattern_start_date, pattern_end_date);
create index idx_candle_pattern_analysis_candle_type on invest_candles.candle_pattern_analysis (candle_type);
create index idx_candle_pattern_analysis_strategy_applicable on invest_candles.candle_pattern_analysis (strategy_applicable);

-- Права доступа
alter table invest_candles.candle_pattern_analysis owner to postgres;
grant select on invest_candles.candle_pattern_analysis to tester;
grant delete, insert, references, select, trigger, truncate, update on invest_candles.candle_pattern_analysis to admin;

-- Функция для анализа свечей с одинаковым типом за N дней подряд
create or replace function invest_views.analyze_candle_patterns(p_analysis_date date, p_consecutive_days integer default 5)
returns void
language plpgsql
as $$
declare
    v_start_date date;
    v_end_date date;
    v_task_id varchar(255);
    v_start_time timestamp with time zone;
    v_end_time timestamp with time zone;
    v_patterns_found integer := 0;
    v_instruments_analyzed integer := 0;
    rec record;
begin
    -- Проверяем корректность параметра количества дней
    if p_consecutive_days < 2 then
        raise exception 'Количество дней последовательности должно быть не менее 2, получено: %', p_consecutive_days;
    end if;
    
    -- Генерируем уникальный ID задачи
    v_task_id := 'CANDLE_PATTERN_ANALYSIS_' || to_char(p_analysis_date, 'YYYY_MM_DD') || '_' || p_consecutive_days || 'D_' || extract(epoch from now())::bigint;
    v_start_time := now();
    
    -- Определяем период анализа: N торговых дней назад от входной даты (не включая её, исключая выходные)
    v_end_date := p_analysis_date - interval '1 day';
    
    -- Расширяем период поиска, учитывая выходные дни (примерно в 1.4 раза больше для покрытия выходных)
    -- Для надежности берем период в 2 раза больше, чтобы гарантированно покрыть все торговые дни
    v_start_date := v_end_date - make_interval(days => p_consecutive_days * 2);
    
    -- Логируем начало работы
    insert into invest.system_logs (task_id, endpoint, method, status, message, start_time)
    values (v_task_id, 'analyze_candle_patterns', 'FUNCTION', 'STARTED', 
            format('Начат анализ паттернов свечей за период с %s по %s (анализ на дату %s, минимум %s торговых дней подряд, исключая выходные)', 
                   v_start_date, v_end_date, p_analysis_date, p_consecutive_days), v_start_time);
    
    -- Основной запрос для поиска паттернов (исключая выходные дни)
    for rec in
        with trading_days_sequence as (
            -- Получаем последние N торговых дней для каждого FIGI
            select 
                figi,
                time::date as candle_date,
                candle_type,
                volume,
                price_change,
                row_number() over (partition by figi order by time::date desc) as day_rank
            from invest.daily_candles
            where time::date <= v_end_date
              and candle_type in ('BULLISH', 'BEARISH')
              and is_complete = true
              -- Исключаем выходные дни: суббота (6) и воскресенье (0)
              and extract(dow from time::date) not in (0, 6)
        ),
        last_n_days as (
            -- Берем только последние N торговых дней
            select 
                figi,
                candle_date,
                candle_type,
                volume,
                price_change,
                day_rank
            from trading_days_sequence
            where day_rank <= p_consecutive_days
        ),
        consecutive_check as (
            -- Проверяем, все ли дни имеют одинаковый тип свечи
            select 
                figi,
                candle_type,
                count(*) as consecutive_days,
                min(candle_date) as pattern_start_date,
                max(candle_date) as pattern_end_date,
                avg(volume) as avg_volume,
                avg(price_change) as avg_price_change,
                sum(price_change) as total_price_change,
                count(distinct candle_type) as unique_candle_types
            from last_n_days
            group by figi, candle_type
        ),
        pattern_groups as (
            -- Оставляем только те группы, где все дни имеют одинаковый тип
            select 
                figi,
                candle_type,
                pattern_start_date,
                pattern_end_date,
                consecutive_days,
                avg_volume,
                avg_price_change,
                total_price_change
            from consecutive_check
            where consecutive_days = p_consecutive_days  -- Точно N дней
              and unique_candle_types = 1  -- Все дни одного типа
        )
        select * from pattern_groups
        order by figi, pattern_start_date
    loop
        -- Проверяем, что паттерн не был уже записан для этой даты анализа
        if not exists (
            select 1 from invest_candles.candle_pattern_analysis 
            where figi = rec.figi 
              and analysis_date = p_analysis_date
              and pattern_start_date = rec.pattern_start_date
              and pattern_end_date = rec.pattern_end_date
              and candle_type = rec.candle_type
        ) then
            -- Вставляем найденный паттерн
            insert into invest_candles.candle_pattern_analysis (
                figi, analysis_date, pattern_start_date, pattern_end_date,
                candle_type, consecutive_days, avg_volume, avg_price_change, total_price_change
            ) values (
                rec.figi, p_analysis_date, rec.pattern_start_date, rec.pattern_end_date,
                rec.candle_type, rec.consecutive_days, rec.avg_volume, rec.avg_price_change, rec.total_price_change
            );
            
            v_patterns_found := v_patterns_found + 1;
        end if;
        
        v_instruments_analyzed := v_instruments_analyzed + 1;
    end loop;
    
    v_end_time := now();
    
    -- Логируем успешное завершение
    insert into invest.system_logs (task_id, endpoint, method, status, message, start_time, end_time)
    values (v_task_id, 'analyze_candle_patterns', 'FUNCTION', 'SUCCESS', 
            format('Анализ завершен успешно. Найдено паттернов: %s, проанализировано инструментов: %s', 
                   v_patterns_found, v_instruments_analyzed), v_start_time, v_end_time);
    
    -- Дополнительное логирование с деталями найденных паттернов
    if v_patterns_found > 0 then
        insert into invest.system_logs (task_id, endpoint, method, status, message, start_time, end_time)
        values (v_task_id || '_DETAILS', 'analyze_candle_patterns', 'FUNCTION', 'INFO', 
                format('Детали анализа: период %s - %s, найдено %s паттернов с %s+ торговыми днями одинакового типа свечи (исключая выходные)', 
                       v_start_date, v_end_date, v_patterns_found, p_consecutive_days), v_start_time, v_end_time);
    end if;
    
exception
    when others then
        v_end_time := now();
        
        -- Логируем ошибку
        insert into invest.system_logs (task_id, endpoint, method, status, message, start_time, end_time)
        values (v_task_id, 'analyze_candle_patterns', 'FUNCTION', 'ERROR', 
                format('Ошибка при анализе паттернов свечей: %s', sqlerrm), v_start_time, v_end_time);
        
        raise;
end;
$$;

comment on function invest_views.analyze_candle_patterns(date, integer) is 'Функция для анализа паттернов свечей с одинаковым типом (BULLISH/BEARISH) за N торговых дней подряд (по умолчанию 5), исключая выходные';

-- Права на функцию
alter function invest_views.analyze_candle_patterns(date, integer) owner to postgres;
grant execute on function invest_views.analyze_candle_patterns(date, integer) to admin;

-- Вспомогательная функция для получения статистики по найденным паттернам
create or replace function invest_views.get_candle_pattern_stats(p_analysis_date date default null)
returns table (
    analysis_date date,
    candle_type varchar(20),
    total_patterns bigint,
    avg_consecutive_days numeric,
    max_consecutive_days integer,
    unique_instruments bigint
)
language plpgsql
as $$
begin
    return query
    select 
        cpa.analysis_date,
        cpa.candle_type,
        count(*) as total_patterns,
        avg(cpa.consecutive_days) as avg_consecutive_days,
        max(cpa.consecutive_days) as max_consecutive_days,
        count(distinct cpa.figi) as unique_instruments
    from invest_candles.candle_pattern_analysis cpa
    where (p_analysis_date is null or cpa.analysis_date = p_analysis_date)
    group by cpa.analysis_date, cpa.candle_type
    order by cpa.analysis_date desc, cpa.candle_type;
end;
$$;

comment on function invest_views.get_candle_pattern_stats(date) is 'Функция для получения статистики по найденным паттернам свечей';

-- Права на вспомогательную функцию
alter function invest_views.get_candle_pattern_stats(date) owner to postgres;
grant execute on function invest_views.get_candle_pattern_stats(date) to admin;
grant execute on function invest_views.get_candle_pattern_stats(date) to tester;

-- Создание синонима (представления) в схеме invest для доступа к таблице из схеме invest_candles
create view invest.candle_pattern_analysis as
select 
    id,
    figi,
    analysis_date,
    pattern_start_date,
    pattern_end_date,
    candle_type,
    consecutive_days,
    avg_volume,
    avg_price_change,
    total_price_change,
    strategy_applicable,
    created_at
from invest_views.candle_pattern_analysis;

comment on view invest.candle_pattern_analysis is 'Синоним для таблицы candle_pattern_analysis из схемы invest_candles';

-- Права доступа на представление
alter view invest.candle_pattern_analysis owner to postgres;
grant select on invest.candle_pattern_analysis to tester;
grant select on invest.candle_pattern_analysis to admin;

-- Создание синонима для функции анализа в схеме invest
create or replace function invest.analyze_candle_patterns(p_analysis_date date, p_consecutive_days integer default 5)
returns void
language plpgsql
as $$
begin
    -- Вызываем оригинальную функцию из схемы invest_candles
    perform invest_views.analyze_candle_patterns(p_analysis_date, p_consecutive_days);
end;
$$;

comment on function invest_views.analyze_candle_patterns(date, integer) is 'Синоним для функции analyze_candle_patterns из схемы invest_candles (анализ торговых дней, исключая выходные)';

-- Права на функцию-синоним
alter function invest_views.analyze_candle_patterns(date, integer) owner to postgres;
grant execute on function invest_views.analyze_candle_patterns(date, integer) to admin;

-- Создание синонима для функции статистики в схеме invest
create or replace function invest.get_candle_pattern_stats(p_analysis_date date default null)
returns table (
    analysis_date date,
    candle_type varchar(20),
    total_patterns bigint,
    avg_consecutive_days numeric,
    max_consecutive_days integer,
    unique_instruments bigint
)
language plpgsql
as $$
begin
    -- Вызываем оригинальную функцию из схемы invest_candles
    return query select * from invest_views.get_candle_pattern_stats(p_analysis_date);
end;
$$;

comment on function invest.get_candle_pattern_stats(date) is 'Синоним для функции get_candle_pattern_stats из схемы invest_candles';

-- Права на функцию-синоним статистики
alter function invest_views.get_candle_pattern_stats(date) owner to postgres;
grant execute on function invest_views.get_candle_pattern_stats(date) to admin;
grant execute on function invest_views.get_candle_pattern_stats(date) to tester;
