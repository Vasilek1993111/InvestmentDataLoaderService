-- Таблица для хранения информации о дивидендах в схеме invest_ref
create table invest_ref.dividends
(
    id              bigserial
        primary key,
    figi            varchar(255) not null,
    declared_date   date,
    record_date     date         not null,
    payment_date    date,
    dividend_value  decimal(18, 9),
    currency        varchar(10)  not null,
    dividend_type   varchar(50),
    created_at      timestamp with time zone default (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow'::text),
    updated_at      timestamp with time zone default (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow'::text)
);

comment on table invest_ref.dividends is 'Таблица для хранения информации о дивидендах';

comment on column invest_ref.dividends.figi is 'Уникальный идентификатор инструмента (FIGI)';
comment on column invest_ref.dividends.declared_date is 'Дата объявления дивидендов';
comment on column invest_ref.dividends.record_date is 'Дата фиксации реестра';
comment on column invest_ref.dividends.payment_date is 'Дата выплаты дивидендов';
comment on column invest_ref.dividends.dividend_value is 'Размер дивиденда на одну акцию';
comment on column invest_ref.dividends.currency is 'Валюта дивиденда';
comment on column invest_ref.dividends.dividend_type is 'Тип дивиденда (обычный, специальный и т.д.)';

-- Индексы для оптимизации запросов
create index idx_dividends_figi on invest_ref.dividends (figi);
create index idx_dividends_record_date on invest_ref.dividends (record_date);
create index idx_dividends_payment_date on invest_ref.dividends (payment_date);
create index idx_dividends_figi_record_date on invest_ref.dividends (figi, record_date);

-- Права доступа
alter table invest_ref.dividends owner to postgres;
grant select on invest_ref.dividends to tester;
grant delete, insert, references, select, trigger, truncate, update on invest_ref.dividends to admin;

-- Создание синонима в схеме invest
create synonym invest.dividends for invest_ref.dividends;

-- Комментарий для синонима
comment on synonym invest.dividends is 'Синоним для таблицы дивидендов из схемы invest_ref';
