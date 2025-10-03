-- Система для управления специальными торговыми часами инструментов
-- Позволяет настраивать особые периоды торговли для конкретных инструментов

-- ============================================================================
-- ТАБЛИЦА ДЛЯ СПЕЦИАЛЬНЫХ ТОРГОВЫХ ЧАСОВ
-- ============================================================================

-- Таблица для хранения специальных торговых часов инструментов
create table invest_dq.special_trading_hours
(
    id                    bigserial                                 primary key,
    figi                  varchar(255)                              not null,
    instrument_type       varchar(20)                               not null,
    day_type              varchar(20)                               not null,
    start_hour            integer                                   not null,
    start_minute          integer                                   not null,
    end_hour              integer                                   not null,
    end_minute            integer                                   not null,
    description           text,
    is_active             boolean                  default true     not null,
    created_at            timestamp with time zone default now()   not null,
    updated_at            timestamp with time zone default now()   not null,
    created_by            varchar(255),
    constraint chk_instrument_type check (instrument_type in ('shares', 'futures', 'indicatives')),
    constraint chk_day_type check (day_type in ('weekday', 'weekend', 'all')),
    constraint chk_start_hour check (start_hour >= 0 and start_hour <= 23),
    constraint chk_start_minute check (start_minute >= 0 and start_minute <= 59),
    constraint chk_end_hour check (end_hour >= 0 and end_hour <= 23),
    constraint chk_end_minute check (end_minute >= 0 and end_minute <= 59),
    constraint chk_time_order check (
        (start_hour < end_hour) or 
        (start_hour = end_hour and start_minute <= end_minute)
    )
);

comment on table invest_dq.special_trading_hours is 'Таблица для хранения специальных торговых часов инструментов';

comment on column invest_dq.special_trading_hours.id is 'Уникальный идентификатор записи';
comment on column invest_dq.special_trading_hours.figi is 'FIGI инструмента';
comment on column invest_dq.special_trading_hours.instrument_type is 'Тип инструмента: shares, futures, indicatives';
comment on column invest_dq.special_trading_hours.day_type is 'Тип дня: weekday (рабочие дни), weekend (выходные), all (все дни)';
comment on column invest_dq.special_trading_hours.start_hour is 'Час начала торгов (0-23)';
comment on column invest_dq.special_trading_hours.start_minute is 'Минута начала торгов (0-59)';
comment on column invest_dq.special_trading_hours.end_hour is 'Час окончания торгов (0-23)';
comment on column invest_dq.special_trading_hours.end_minute is 'Минута окончания торгов (0-59)';
comment on column invest_dq.special_trading_hours.description is 'Описание специальных торговых часов';
comment on column invest_dq.special_trading_hours.is_active is 'Активна ли запись';
comment on column invest_dq.special_trading_hours.created_at is 'Дата и время создания записи';
comment on column invest_dq.special_trading_hours.updated_at is 'Дата и время последнего обновления записи';
comment on column invest_dq.special_trading_hours.created_by is 'Пользователь, создавший запись';

-- Индексы для оптимизации запросов
create index idx_special_trading_hours_figi on invest_dq.special_trading_hours (figi);
create index idx_special_trading_hours_instrument_type on invest_dq.special_trading_hours (instrument_type);
create index idx_special_trading_hours_day_type on invest_dq.special_trading_hours (day_type);
create index idx_special_trading_hours_active on invest_dq.special_trading_hours (is_active);
create unique index idx_special_trading_hours_unique on invest_dq.special_trading_hours (figi, day_type, instrument_type) where is_active = true;

-- Устанавливаем владельца
alter table invest_dq.special_trading_hours owner to postgres;

-- Предоставляем права доступа
grant select on invest_dq.special_trading_hours to tester;
grant delete, insert, references, select, trigger, truncate, update on invest_dq.special_trading_hours to admin;

-- Создаем синоним в схеме invest
create view invest.special_trading_hours as
select 
    id,
    figi,
    instrument_type,
    day_type,
    start_hour,
    start_minute,
    end_hour,
    end_minute,
    description,
    is_active,
    created_at,
    updated_at,
    created_by
from invest_dq.special_trading_hours;

comment on view invest.special_trading_hours is 'Синоним для таблицы special_trading_hours из схемы invest_dq';

-- Устанавливаем владельца синонима
alter view invest.special_trading_hours owner to postgres;

-- Предоставляем права доступа на синоним
grant select on invest.special_trading_hours to tester;
grant select on invest.special_trading_hours to admin;

-- ============================================================================
-- СИНОНИМЫ ФУНКЦИЙ В СХЕМЕ INVEST
-- ============================================================================

-- Создаем синонимы функций в схеме invest
create or replace function invest.add_special_trading_hours(
    p_figi varchar(255),
    p_instrument_type varchar(20),
    p_day_type varchar(20),
    p_start_hour integer,
    p_start_minute integer,
    p_end_hour integer,
    p_end_minute integer,
    p_description text default null,
    p_created_by varchar(255) default null
)
returns bigint
language sql
as $$
    select invest_views.add_special_trading_hours(
        p_figi, p_instrument_type, p_day_type, p_start_hour, p_start_minute,
        p_end_hour, p_end_minute, p_description, p_created_by
    );
$$;

comment on function invest.add_special_trading_hours(varchar, varchar, varchar, integer, integer, integer, integer, text, varchar) is 'Синоним для функции add_special_trading_hours из схемы invest_views';

create or replace function invest.remove_special_trading_hours(
    p_figi varchar(255),
    p_instrument_type varchar(20) default null,
    p_day_type varchar(20) default null
)
returns integer
language sql
as $$
    select invest_views.remove_special_trading_hours(p_figi, p_instrument_type, p_day_type);
$$;

comment on function invest.remove_special_trading_hours(varchar, varchar, varchar) is 'Синоним для функции remove_special_trading_hours из схемы invest_views';

-- Удаляем существующий синоним перед созданием нового
drop function if exists invest.get_trading_hours(varchar);

create or replace function invest.get_trading_hours(
    p_figi varchar(255)
)
returns table(
    start_hour integer,
    start_minute integer,
    end_hour integer,
    end_minute integer,
    is_special boolean,
    day_type varchar(20),
    created_at timestamp with time zone
)
language sql
as $$
    select * from invest_views.get_trading_hours(p_figi);
$$;

comment on function invest.get_trading_hours(varchar) is 'Синоним для функции get_trading_hours из схемы invest_views';

-- Устанавливаем владельцев синонимов функций
alter function invest.add_special_trading_hours(varchar, varchar, varchar, integer, integer, integer, integer, text, varchar) owner to postgres;
alter function invest.remove_special_trading_hours(varchar, varchar, varchar) owner to postgres;
alter function invest.get_trading_hours(varchar) owner to postgres;

-- Предоставляем права доступа на синонимы функций
grant execute on function invest.add_special_trading_hours(varchar, varchar, varchar, integer, integer, integer, integer, text, varchar) to admin;
grant execute on function invest.remove_special_trading_hours(varchar, varchar, varchar) to admin;
grant execute on function invest.get_trading_hours(varchar) to tester;
grant execute on function invest.get_trading_hours(varchar) to admin;

-- ============================================================================
-- ТАБЛИЦА ДЛЯ ПРОБЛЕМ КАЧЕСТВА ДАННЫХ
-- ============================================================================

-- Таблица для хранения проблем качества данных
create table invest.data_quality_issues
(
    id                    bigserial                                 primary key,
    task_id               varchar(255)                              not null,
    check_name            varchar(255)                              not null,
    entity_type           varchar(50),
    entity_id             varchar(255),
    trade_date            date,
    metric                varchar(255),
    status                varchar(20)                               not null,
    message               text                                      not null,
    expected_numeric      numeric(18, 9),
    actual_numeric        numeric(18, 9),
    diff_numeric          numeric(18, 9),
    details               jsonb,
    created_at            timestamp with time zone default now()   not null,
    constraint chk_status check (status in ('ERROR', 'WARNING', 'INFO'))
);

comment on table invest.data_quality_issues is 'Таблица для хранения проблем качества данных';

comment on column invest.data_quality_issues.id is 'Уникальный идентификатор записи';
comment on column invest.data_quality_issues.task_id is 'ID задачи, которая выявила проблему';
comment on column invest.data_quality_issues.check_name is 'Название проверки';
comment on column invest.data_quality_issues.entity_type is 'Тип сущности (shares, futures, etc.)';
comment on column invest.data_quality_issues.entity_id is 'ID сущности (обычно FIGI)';
comment on column invest.data_quality_issues.trade_date is 'Дата торгов, к которой относится проблема';
comment on column invest.data_quality_issues.metric is 'Название метрики';
comment on column invest.data_quality_issues.status is 'Статус проблемы: ERROR, WARNING, INFO';
comment on column invest.data_quality_issues.message is 'Описание проблемы';
comment on column invest.data_quality_issues.expected_numeric is 'Ожидаемое числовое значение';
comment on column invest.data_quality_issues.actual_numeric is 'Фактическое числовое значение';
comment on column invest.data_quality_issues.diff_numeric is 'Разность между ожидаемым и фактическим значением';
comment on column invest.data_quality_issues.details is 'Дополнительные детали в формате JSON';
comment on column invest.data_quality_issues.created_at is 'Дата и время создания записи';

-- Индексы для оптимизации запросов
create index idx_data_quality_issues_task_id on invest.data_quality_issues (task_id);
create index idx_data_quality_issues_check_name on invest.data_quality_issues (check_name);
create index idx_data_quality_issues_entity on invest.data_quality_issues (entity_type, entity_id);
create index idx_data_quality_issues_trade_date on invest.data_quality_issues (trade_date);
create index idx_data_quality_issues_status on invest.data_quality_issues (status);
create index idx_data_quality_issues_created_at on invest.data_quality_issues (created_at);

-- Устанавливаем владельца
alter table invest.data_quality_issues owner to postgres;

-- Предоставляем права доступа
grant select on invest.data_quality_issues to tester;
grant delete, insert, references, select, trigger, truncate, update on invest.data_quality_issues to admin;

-- ============================================================================
-- ФУНКЦИИ ДЛЯ УПРАВЛЕНИЯ СПЕЦИАЛЬНЫМИ ТОРГОВЫМИ ЧАСАМИ
-- ============================================================================

-- Функция для добавления специальных торговых часов
create or replace function invest_views.add_special_trading_hours(
    p_figi varchar(255),
    p_instrument_type varchar(20),
    p_day_type varchar(20),
    p_start_hour integer,
    p_start_minute integer,
    p_end_hour integer,
    p_end_minute integer,
    p_description text default null,
    p_created_by varchar(255) default null
)
returns bigint
language plpgsql
as
$$
declare
    v_id bigint;
begin
    -- Проверяем входные параметры
    if p_figi is null or p_instrument_type is null or p_day_type is null then
        raise exception 'FIGI, instrument_type и day_type не могут быть NULL';
    end if;
    
    if p_instrument_type not in ('shares', 'futures', 'indicatives') then
        raise exception 'instrument_type должен быть: shares, futures или indicatives';
    end if;
    
    if p_day_type not in ('weekday', 'weekend', 'all') then
        raise exception 'day_type должен быть: weekday, weekend или all';
    end if;
    
    -- Деактивируем существующие записи для этого инструмента и типа дня
    update invest_dq.special_trading_hours 
    set is_active = false, updated_at = now()
    where figi = p_figi 
    and day_type = p_day_type 
    and instrument_type = p_instrument_type
    and is_active = true;
    
    -- Добавляем новую запись
    insert into invest_dq.special_trading_hours (
        figi, instrument_type, day_type, start_hour, start_minute, 
        end_hour, end_minute, description, created_by
    ) values (
        p_figi, p_instrument_type, p_day_type, p_start_hour, p_start_minute,
        p_end_hour, p_end_minute, p_description, p_created_by
    ) returning id into v_id;
    
    return v_id;
end;
$$;

comment on function invest_views.add_special_trading_hours(varchar, varchar, varchar, integer, integer, integer, integer, text, varchar) is 'Добавляет специальные торговые часы для инструмента';

-- Функция для удаления специальных торговых часов
create or replace function invest_views.remove_special_trading_hours(
    p_figi varchar(255),
    p_instrument_type varchar(20) default null,
    p_day_type varchar(20) default null
)
returns integer
language plpgsql
as
$$
declare
    v_count integer;
begin
    -- Деактивируем записи
    update invest_dq.special_trading_hours 
    set is_active = false, updated_at = now()
    where figi = p_figi 
    and (p_instrument_type is null or instrument_type = p_instrument_type)
    and (p_day_type is null or day_type = p_day_type)
    and is_active = true;
    
    get diagnostics v_count = row_count;
    return v_count;
end;
$$;

comment on function invest_views.remove_special_trading_hours(varchar, varchar, varchar) is 'Удаляет специальные торговые часы для инструмента';

-- Удаляем существующую функцию перед созданием новой
drop function if exists invest_views.get_trading_hours(varchar);

-- Функция для получения торговых часов инструмента
create or replace function invest_views.get_trading_hours(
    p_figi varchar(255)
)
returns table(
    start_hour integer,
    start_minute integer,
    end_hour integer,
    end_minute integer,
    is_special boolean,
    day_type varchar(20),
    created_at timestamp with time zone
)
language plpgsql
as
$$
begin
    -- Возвращаем все активные записи специальных торговых часов для данного FIGI
    return query
    select 
        sth.start_hour,
        sth.start_minute,
        sth.end_hour,
        sth.end_minute,
        true as is_special,
        sth.day_type,
        sth.created_at
    from invest_dq.special_trading_hours sth
    where sth.figi = p_figi
    and sth.is_active = true
    order by sth.day_type desc, sth.created_at desc;
    
    -- Если ничего не найдено, функция возвращает пустой результат
end;
$$;

comment on function invest_views.get_trading_hours(varchar) is 'Возвращает специальные торговые часы для инструмента по FIGI, или пустой результат если не найдено';

-- ============================================================================
-- ПРИМЕРЫ ИСПОЛЬЗОВАНИЯ
-- ============================================================================

-- Пример добавления специальных торговых часов для акции BBG004S689R0
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
    'Торговля только с 09:59 до 23:50 в рабочие дни',  -- описание
    'admin'                   -- создатель
);
*/

-- Пример удаления специальных торговых часов
/*
select invest.remove_special_trading_hours('BBG004S689R0', 'shares', 'weekday');
*/

-- Пример получения торговых часов
/*
select * from invest.get_trading_hours('BBG004S689R0');
*/

-- Альтернативные вызовы через схемы invest_views и invest_dq:
/*
select invest_views.add_special_trading_hours('BBG004S689R0', 'shares', 'weekday', 9, 59, 23, 50);
select * from invest_views.get_trading_hours('BBG004S689R0');
select * from invest_dq.special_trading_hours where figi = 'BBG004S689R0';
*/
