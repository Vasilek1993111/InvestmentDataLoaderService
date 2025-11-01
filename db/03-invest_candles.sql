--Схема invest_candles - схема для хранения свечей
CREATE SCHEMA IF NOT EXISTS invest_candles;

-- Таблица хранения минутных свечей
create table invest_candles.minute_candles
(
    figi                 varchar(255)                              not null,
    time                 timestamp(6) with time zone               not null,
    close                numeric(18, 9)                            not null,
    created_at           timestamp(6) with time zone default now() not null,
    high                 numeric(18, 9)                            not null,
    is_complete          boolean                                   not null,
    low                  numeric(18, 9)                            not null,
    open                 numeric(18, 9)                            not null,
    updated_at           timestamp(6) with time zone default now() not null,
    volume               bigint                                    not null,
    price_change         numeric(18, 9),
    price_change_percent numeric(18, 4),
    candle_type          varchar(20),
    body_size            numeric(18, 9),
    upper_shadow         numeric(18, 9),
    lower_shadow         numeric(18, 9),
    high_low_range       numeric(18, 9),
    average_price        numeric(18, 2),
    primary key (figi, time)
)
    partition by RANGE ("time");

comment on table minute_candles is 'Таблица минутных свечей финансовых инструментов с ежедневным партиционированием';

comment on column minute_candles.figi is 'Уникальный идентификатор инструмента (Financial Instrument Global Identifier)';

comment on column minute_candles.time is 'Время начала минутной свечи в московской таймзоне';

comment on column minute_candles.close is 'Цена закрытия за минуту с точностью до 9 знаков после запятой';

comment on column minute_candles.created_at is 'Время создания записи в московской таймзоне';

comment on column minute_candles.high is 'Максимальная цена за минуту с точностью до 9 знаков после запятой';

comment on column minute_candles.is_complete is 'Флаг завершенности свечи (true - свеча завершена, false - формируется)';

comment on column minute_candles.low is 'Минимальная цена за минуту с точностью до 9 знаков после запятой';

comment on column minute_candles.open is 'Цена открытия за минуту с точностью до 9 знаков после запятой';

comment on column minute_candles.updated_at is 'Время последнего обновления записи в московской таймзоне';

comment on column minute_candles.volume is 'Объем торгов за минуту (количество лотов)';

comment on column minute_candles.price_change is 'Изменение цены (close - open)';

comment on column minute_candles.price_change_percent is 'Процентное изменение цены';

comment on column minute_candles.candle_type is 'Тип свечи: BULLISH, BEARISH, DOJI';

comment on column minute_candles.body_size is 'Размер тела свечи (абсолютное значение изменения цены)';

comment on column minute_candles.upper_shadow is 'Верхняя тень свечи';

comment on column minute_candles.lower_shadow is 'Нижняя тень свечи';

comment on column minute_candles.high_low_range is 'Диапазон цен (high - low)';

comment on column minute_candles.average_price is 'Средняя цена (high + low + open + close) / 4';

alter table minute_candles
    owner to postgres;

create index idx_minute_candles_time
    on minute_candles (time);

create index idx_minute_candles_figi_time
    on minute_candles (figi, time);

create table minute_candles_2024_06_01
    partition of minute_candles
        FOR VALUES FROM ('2024-05-31 21:00:00+00') TO ('2024-06-01 21:00:00+00');

alter table minute_candles_2024_06_01
    owner to postgres;

grant select on minute_candles_2024_06_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_01 to admin;

create table minute_candles_2024_06_02
    partition of minute_candles
        FOR VALUES FROM ('2024-06-01 21:00:00+00') TO ('2024-06-02 21:00:00+00');

alter table minute_candles_2024_06_02
    owner to postgres;

grant select on minute_candles_2024_06_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_02 to admin;

create table minute_candles_2024_06_03
    partition of minute_candles
        FOR VALUES FROM ('2024-06-02 21:00:00+00') TO ('2024-06-03 21:00:00+00');

alter table minute_candles_2024_06_03
    owner to postgres;

grant select on minute_candles_2024_06_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_03 to admin;

create table minute_candles_2024_06_04
    partition of minute_candles
        FOR VALUES FROM ('2024-06-03 21:00:00+00') TO ('2024-06-04 21:00:00+00');

alter table minute_candles_2024_06_04
    owner to postgres;

grant select on minute_candles_2024_06_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_04 to admin;

create table minute_candles_2024_06_05
    partition of minute_candles
        FOR VALUES FROM ('2024-06-04 21:00:00+00') TO ('2024-06-05 21:00:00+00');

alter table minute_candles_2024_06_05
    owner to postgres;

grant select on minute_candles_2024_06_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_05 to admin;

create table minute_candles_2024_06_06
    partition of minute_candles
        FOR VALUES FROM ('2024-06-05 21:00:00+00') TO ('2024-06-06 21:00:00+00');

alter table minute_candles_2024_06_06
    owner to postgres;

grant select on minute_candles_2024_06_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_06 to admin;

create table minute_candles_2024_06_07
    partition of minute_candles
        FOR VALUES FROM ('2024-06-06 21:00:00+00') TO ('2024-06-07 21:00:00+00');

alter table minute_candles_2024_06_07
    owner to postgres;

grant select on minute_candles_2024_06_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_07 to admin;

create table minute_candles_2024_06_08
    partition of minute_candles
        FOR VALUES FROM ('2024-06-07 21:00:00+00') TO ('2024-06-08 21:00:00+00');

alter table minute_candles_2024_06_08
    owner to postgres;

grant select on minute_candles_2024_06_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_08 to admin;

create table minute_candles_2024_06_09
    partition of minute_candles
        FOR VALUES FROM ('2024-06-08 21:00:00+00') TO ('2024-06-09 21:00:00+00');

alter table minute_candles_2024_06_09
    owner to postgres;

grant select on minute_candles_2024_06_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_09 to admin;

create table minute_candles_2024_06_10
    partition of minute_candles
        FOR VALUES FROM ('2024-06-09 21:00:00+00') TO ('2024-06-10 21:00:00+00');

alter table minute_candles_2024_06_10
    owner to postgres;

grant select on minute_candles_2024_06_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_10 to admin;

create table minute_candles_2024_06_11
    partition of minute_candles
        FOR VALUES FROM ('2024-06-10 21:00:00+00') TO ('2024-06-11 21:00:00+00');

alter table minute_candles_2024_06_11
    owner to postgres;

grant select on minute_candles_2024_06_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_11 to admin;

create table minute_candles_2024_06_12
    partition of minute_candles
        FOR VALUES FROM ('2024-06-11 21:00:00+00') TO ('2024-06-12 21:00:00+00');

alter table minute_candles_2024_06_12
    owner to postgres;

grant select on minute_candles_2024_06_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_12 to admin;

create table minute_candles_2024_06_13
    partition of minute_candles
        FOR VALUES FROM ('2024-06-12 21:00:00+00') TO ('2024-06-13 21:00:00+00');

alter table minute_candles_2024_06_13
    owner to postgres;

grant select on minute_candles_2024_06_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_13 to admin;

create table minute_candles_2024_06_14
    partition of minute_candles
        FOR VALUES FROM ('2024-06-13 21:00:00+00') TO ('2024-06-14 21:00:00+00');

alter table minute_candles_2024_06_14
    owner to postgres;

grant select on minute_candles_2024_06_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_14 to admin;

create table minute_candles_2024_06_15
    partition of minute_candles
        FOR VALUES FROM ('2024-06-14 21:00:00+00') TO ('2024-06-15 21:00:00+00');

alter table minute_candles_2024_06_15
    owner to postgres;

grant select on minute_candles_2024_06_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_15 to admin;

create table minute_candles_2024_06_16
    partition of minute_candles
        FOR VALUES FROM ('2024-06-15 21:00:00+00') TO ('2024-06-16 21:00:00+00');

alter table minute_candles_2024_06_16
    owner to postgres;

grant select on minute_candles_2024_06_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_16 to admin;

create table minute_candles_2024_06_17
    partition of minute_candles
        FOR VALUES FROM ('2024-06-16 21:00:00+00') TO ('2024-06-17 21:00:00+00');

alter table minute_candles_2024_06_17
    owner to postgres;

grant select on minute_candles_2024_06_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_17 to admin;

create table minute_candles_2024_06_18
    partition of minute_candles
        FOR VALUES FROM ('2024-06-17 21:00:00+00') TO ('2024-06-18 21:00:00+00');

alter table minute_candles_2024_06_18
    owner to postgres;

grant select on minute_candles_2024_06_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_18 to admin;

create table minute_candles_2024_06_19
    partition of minute_candles
        FOR VALUES FROM ('2024-06-18 21:00:00+00') TO ('2024-06-19 21:00:00+00');

alter table minute_candles_2024_06_19
    owner to postgres;

grant select on minute_candles_2024_06_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_19 to admin;

create table minute_candles_2024_06_20
    partition of minute_candles
        FOR VALUES FROM ('2024-06-19 21:00:00+00') TO ('2024-06-20 21:00:00+00');

alter table minute_candles_2024_06_20
    owner to postgres;

grant select on minute_candles_2024_06_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_20 to admin;

create table minute_candles_2024_06_21
    partition of minute_candles
        FOR VALUES FROM ('2024-06-20 21:00:00+00') TO ('2024-06-21 21:00:00+00');

alter table minute_candles_2024_06_21
    owner to postgres;

grant select on minute_candles_2024_06_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_21 to admin;

create table minute_candles_2024_06_22
    partition of minute_candles
        FOR VALUES FROM ('2024-06-21 21:00:00+00') TO ('2024-06-22 21:00:00+00');

alter table minute_candles_2024_06_22
    owner to postgres;

grant select on minute_candles_2024_06_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_22 to admin;

create table minute_candles_2024_06_23
    partition of minute_candles
        FOR VALUES FROM ('2024-06-22 21:00:00+00') TO ('2024-06-23 21:00:00+00');

alter table minute_candles_2024_06_23
    owner to postgres;

grant select on minute_candles_2024_06_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_23 to admin;

create table minute_candles_2024_06_24
    partition of minute_candles
        FOR VALUES FROM ('2024-06-23 21:00:00+00') TO ('2024-06-24 21:00:00+00');

alter table minute_candles_2024_06_24
    owner to postgres;

grant select on minute_candles_2024_06_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_24 to admin;

create table minute_candles_2024_06_25
    partition of minute_candles
        FOR VALUES FROM ('2024-06-24 21:00:00+00') TO ('2024-06-25 21:00:00+00');

alter table minute_candles_2024_06_25
    owner to postgres;

grant select on minute_candles_2024_06_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_25 to admin;

create table minute_candles_2024_06_26
    partition of minute_candles
        FOR VALUES FROM ('2024-06-25 21:00:00+00') TO ('2024-06-26 21:00:00+00');

alter table minute_candles_2024_06_26
    owner to postgres;

grant select on minute_candles_2024_06_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_26 to admin;

create table minute_candles_2024_06_27
    partition of minute_candles
        FOR VALUES FROM ('2024-06-26 21:00:00+00') TO ('2024-06-27 21:00:00+00');

alter table minute_candles_2024_06_27
    owner to postgres;

grant select on minute_candles_2024_06_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_27 to admin;

create table minute_candles_2024_06_28
    partition of minute_candles
        FOR VALUES FROM ('2024-06-27 21:00:00+00') TO ('2024-06-28 21:00:00+00');

alter table minute_candles_2024_06_28
    owner to postgres;

grant select on minute_candles_2024_06_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_28 to admin;

create table minute_candles_2024_06_29
    partition of minute_candles
        FOR VALUES FROM ('2024-06-28 21:00:00+00') TO ('2024-06-29 21:00:00+00');

alter table minute_candles_2024_06_29
    owner to postgres;

grant select on minute_candles_2024_06_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_29 to admin;

create table minute_candles_2024_06_30
    partition of minute_candles
        FOR VALUES FROM ('2024-06-29 21:00:00+00') TO ('2024-06-30 21:00:00+00');

alter table minute_candles_2024_06_30
    owner to postgres;

grant select on minute_candles_2024_06_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_06_30 to admin;

create table minute_candles_2024_07_01
    partition of minute_candles
        FOR VALUES FROM ('2024-06-30 21:00:00+00') TO ('2024-07-01 21:00:00+00');

alter table minute_candles_2024_07_01
    owner to postgres;

grant select on minute_candles_2024_07_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_01 to admin;

create table minute_candles_2024_07_02
    partition of minute_candles
        FOR VALUES FROM ('2024-07-01 21:00:00+00') TO ('2024-07-02 21:00:00+00');

alter table minute_candles_2024_07_02
    owner to postgres;

grant select on minute_candles_2024_07_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_02 to admin;

create table minute_candles_2024_07_03
    partition of minute_candles
        FOR VALUES FROM ('2024-07-02 21:00:00+00') TO ('2024-07-03 21:00:00+00');

alter table minute_candles_2024_07_03
    owner to postgres;

grant select on minute_candles_2024_07_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_03 to admin;

create table minute_candles_2024_07_04
    partition of minute_candles
        FOR VALUES FROM ('2024-07-03 21:00:00+00') TO ('2024-07-04 21:00:00+00');

alter table minute_candles_2024_07_04
    owner to postgres;

grant select on minute_candles_2024_07_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_04 to admin;

create table minute_candles_2024_07_05
    partition of minute_candles
        FOR VALUES FROM ('2024-07-04 21:00:00+00') TO ('2024-07-05 21:00:00+00');

alter table minute_candles_2024_07_05
    owner to postgres;

grant select on minute_candles_2024_07_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_05 to admin;

create table minute_candles_2024_07_06
    partition of minute_candles
        FOR VALUES FROM ('2024-07-05 21:00:00+00') TO ('2024-07-06 21:00:00+00');

alter table minute_candles_2024_07_06
    owner to postgres;

grant select on minute_candles_2024_07_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_06 to admin;

create table minute_candles_2024_07_07
    partition of minute_candles
        FOR VALUES FROM ('2024-07-06 21:00:00+00') TO ('2024-07-07 21:00:00+00');

alter table minute_candles_2024_07_07
    owner to postgres;

grant select on minute_candles_2024_07_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_07 to admin;

create table minute_candles_2024_07_08
    partition of minute_candles
        FOR VALUES FROM ('2024-07-07 21:00:00+00') TO ('2024-07-08 21:00:00+00');

alter table minute_candles_2024_07_08
    owner to postgres;

grant select on minute_candles_2024_07_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_08 to admin;

create table minute_candles_2024_07_09
    partition of minute_candles
        FOR VALUES FROM ('2024-07-08 21:00:00+00') TO ('2024-07-09 21:00:00+00');

alter table minute_candles_2024_07_09
    owner to postgres;

grant select on minute_candles_2024_07_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_09 to admin;

create table minute_candles_2024_07_10
    partition of minute_candles
        FOR VALUES FROM ('2024-07-09 21:00:00+00') TO ('2024-07-10 21:00:00+00');

alter table minute_candles_2024_07_10
    owner to postgres;

grant select on minute_candles_2024_07_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_10 to admin;

create table minute_candles_2024_07_11
    partition of minute_candles
        FOR VALUES FROM ('2024-07-10 21:00:00+00') TO ('2024-07-11 21:00:00+00');

alter table minute_candles_2024_07_11
    owner to postgres;

grant select on minute_candles_2024_07_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_11 to admin;

create table minute_candles_2024_07_12
    partition of minute_candles
        FOR VALUES FROM ('2024-07-11 21:00:00+00') TO ('2024-07-12 21:00:00+00');

alter table minute_candles_2024_07_12
    owner to postgres;

grant select on minute_candles_2024_07_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_12 to admin;

create table minute_candles_2024_07_13
    partition of minute_candles
        FOR VALUES FROM ('2024-07-12 21:00:00+00') TO ('2024-07-13 21:00:00+00');

alter table minute_candles_2024_07_13
    owner to postgres;

grant select on minute_candles_2024_07_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_13 to admin;

create table minute_candles_2024_07_14
    partition of minute_candles
        FOR VALUES FROM ('2024-07-13 21:00:00+00') TO ('2024-07-14 21:00:00+00');

alter table minute_candles_2024_07_14
    owner to postgres;

grant select on minute_candles_2024_07_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_14 to admin;

create table minute_candles_2024_07_15
    partition of minute_candles
        FOR VALUES FROM ('2024-07-14 21:00:00+00') TO ('2024-07-15 21:00:00+00');

alter table minute_candles_2024_07_15
    owner to postgres;

grant select on minute_candles_2024_07_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_15 to admin;

create table minute_candles_2024_07_16
    partition of minute_candles
        FOR VALUES FROM ('2024-07-15 21:00:00+00') TO ('2024-07-16 21:00:00+00');

alter table minute_candles_2024_07_16
    owner to postgres;

grant select on minute_candles_2024_07_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_16 to admin;

create table minute_candles_2024_07_17
    partition of minute_candles
        FOR VALUES FROM ('2024-07-16 21:00:00+00') TO ('2024-07-17 21:00:00+00');

alter table minute_candles_2024_07_17
    owner to postgres;

grant select on minute_candles_2024_07_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_17 to admin;

create table minute_candles_2024_07_18
    partition of minute_candles
        FOR VALUES FROM ('2024-07-17 21:00:00+00') TO ('2024-07-18 21:00:00+00');

alter table minute_candles_2024_07_18
    owner to postgres;

grant select on minute_candles_2024_07_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_18 to admin;

create table minute_candles_2024_07_19
    partition of minute_candles
        FOR VALUES FROM ('2024-07-18 21:00:00+00') TO ('2024-07-19 21:00:00+00');

alter table minute_candles_2024_07_19
    owner to postgres;

grant select on minute_candles_2024_07_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_19 to admin;

create table minute_candles_2024_07_20
    partition of minute_candles
        FOR VALUES FROM ('2024-07-19 21:00:00+00') TO ('2024-07-20 21:00:00+00');

alter table minute_candles_2024_07_20
    owner to postgres;

grant select on minute_candles_2024_07_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_20 to admin;

create table minute_candles_2024_07_21
    partition of minute_candles
        FOR VALUES FROM ('2024-07-20 21:00:00+00') TO ('2024-07-21 21:00:00+00');

alter table minute_candles_2024_07_21
    owner to postgres;

grant select on minute_candles_2024_07_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_21 to admin;

create table minute_candles_2024_07_22
    partition of minute_candles
        FOR VALUES FROM ('2024-07-21 21:00:00+00') TO ('2024-07-22 21:00:00+00');

alter table minute_candles_2024_07_22
    owner to postgres;

grant select on minute_candles_2024_07_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_22 to admin;

create table minute_candles_2024_07_23
    partition of minute_candles
        FOR VALUES FROM ('2024-07-22 21:00:00+00') TO ('2024-07-23 21:00:00+00');

alter table minute_candles_2024_07_23
    owner to postgres;

grant select on minute_candles_2024_07_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_23 to admin;

create table minute_candles_2024_07_24
    partition of minute_candles
        FOR VALUES FROM ('2024-07-23 21:00:00+00') TO ('2024-07-24 21:00:00+00');

alter table minute_candles_2024_07_24
    owner to postgres;

grant select on minute_candles_2024_07_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_24 to admin;

create table minute_candles_2024_07_25
    partition of minute_candles
        FOR VALUES FROM ('2024-07-24 21:00:00+00') TO ('2024-07-25 21:00:00+00');

alter table minute_candles_2024_07_25
    owner to postgres;

grant select on minute_candles_2024_07_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_25 to admin;

create table minute_candles_2024_07_26
    partition of minute_candles
        FOR VALUES FROM ('2024-07-25 21:00:00+00') TO ('2024-07-26 21:00:00+00');

alter table minute_candles_2024_07_26
    owner to postgres;

grant select on minute_candles_2024_07_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_26 to admin;

create table minute_candles_2024_07_27
    partition of minute_candles
        FOR VALUES FROM ('2024-07-26 21:00:00+00') TO ('2024-07-27 21:00:00+00');

alter table minute_candles_2024_07_27
    owner to postgres;

grant select on minute_candles_2024_07_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_27 to admin;

create table minute_candles_2024_07_28
    partition of minute_candles
        FOR VALUES FROM ('2024-07-27 21:00:00+00') TO ('2024-07-28 21:00:00+00');

alter table minute_candles_2024_07_28
    owner to postgres;

grant select on minute_candles_2024_07_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_28 to admin;

create table minute_candles_2024_07_29
    partition of minute_candles
        FOR VALUES FROM ('2024-07-28 21:00:00+00') TO ('2024-07-29 21:00:00+00');

alter table minute_candles_2024_07_29
    owner to postgres;

grant select on minute_candles_2024_07_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_29 to admin;

create table minute_candles_2024_07_30
    partition of minute_candles
        FOR VALUES FROM ('2024-07-29 21:00:00+00') TO ('2024-07-30 21:00:00+00');

alter table minute_candles_2024_07_30
    owner to postgres;

grant select on minute_candles_2024_07_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_30 to admin;

create table minute_candles_2024_07_31
    partition of minute_candles
        FOR VALUES FROM ('2024-07-30 21:00:00+00') TO ('2024-07-31 21:00:00+00');

alter table minute_candles_2024_07_31
    owner to postgres;

grant select on minute_candles_2024_07_31 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_07_31 to admin;

create table minute_candles_2024_08_01
    partition of minute_candles
        FOR VALUES FROM ('2024-07-31 21:00:00+00') TO ('2024-08-01 21:00:00+00');

alter table minute_candles_2024_08_01
    owner to postgres;

grant select on minute_candles_2024_08_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_01 to admin;

create table minute_candles_2024_08_02
    partition of minute_candles
        FOR VALUES FROM ('2024-08-01 21:00:00+00') TO ('2024-08-02 21:00:00+00');

alter table minute_candles_2024_08_02
    owner to postgres;

grant select on minute_candles_2024_08_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_02 to admin;

create table minute_candles_2024_08_03
    partition of minute_candles
        FOR VALUES FROM ('2024-08-02 21:00:00+00') TO ('2024-08-03 21:00:00+00');

alter table minute_candles_2024_08_03
    owner to postgres;

grant select on minute_candles_2024_08_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_03 to admin;

create table minute_candles_2024_08_04
    partition of minute_candles
        FOR VALUES FROM ('2024-08-03 21:00:00+00') TO ('2024-08-04 21:00:00+00');

alter table minute_candles_2024_08_04
    owner to postgres;

grant select on minute_candles_2024_08_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_04 to admin;

create table minute_candles_2024_08_05
    partition of minute_candles
        FOR VALUES FROM ('2024-08-04 21:00:00+00') TO ('2024-08-05 21:00:00+00');

alter table minute_candles_2024_08_05
    owner to postgres;

grant select on minute_candles_2024_08_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_05 to admin;

create table minute_candles_2024_08_06
    partition of minute_candles
        FOR VALUES FROM ('2024-08-05 21:00:00+00') TO ('2024-08-06 21:00:00+00');

alter table minute_candles_2024_08_06
    owner to postgres;

grant select on minute_candles_2024_08_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_06 to admin;

create table minute_candles_2024_08_07
    partition of minute_candles
        FOR VALUES FROM ('2024-08-06 21:00:00+00') TO ('2024-08-07 21:00:00+00');

alter table minute_candles_2024_08_07
    owner to postgres;

grant select on minute_candles_2024_08_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_07 to admin;

create table minute_candles_2024_08_08
    partition of minute_candles
        FOR VALUES FROM ('2024-08-07 21:00:00+00') TO ('2024-08-08 21:00:00+00');

alter table minute_candles_2024_08_08
    owner to postgres;

grant select on minute_candles_2024_08_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_08 to admin;

create table minute_candles_2024_08_09
    partition of minute_candles
        FOR VALUES FROM ('2024-08-08 21:00:00+00') TO ('2024-08-09 21:00:00+00');

alter table minute_candles_2024_08_09
    owner to postgres;

grant select on minute_candles_2024_08_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_09 to admin;

create table minute_candles_2024_08_10
    partition of minute_candles
        FOR VALUES FROM ('2024-08-09 21:00:00+00') TO ('2024-08-10 21:00:00+00');

alter table minute_candles_2024_08_10
    owner to postgres;

grant select on minute_candles_2024_08_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_10 to admin;

create table minute_candles_2024_08_11
    partition of minute_candles
        FOR VALUES FROM ('2024-08-10 21:00:00+00') TO ('2024-08-11 21:00:00+00');

alter table minute_candles_2024_08_11
    owner to postgres;

grant select on minute_candles_2024_08_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_11 to admin;

create table minute_candles_2024_08_12
    partition of minute_candles
        FOR VALUES FROM ('2024-08-11 21:00:00+00') TO ('2024-08-12 21:00:00+00');

alter table minute_candles_2024_08_12
    owner to postgres;

grant select on minute_candles_2024_08_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_12 to admin;

create table minute_candles_2024_08_13
    partition of minute_candles
        FOR VALUES FROM ('2024-08-12 21:00:00+00') TO ('2024-08-13 21:00:00+00');

alter table minute_candles_2024_08_13
    owner to postgres;

grant select on minute_candles_2024_08_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_13 to admin;

create table minute_candles_2024_08_14
    partition of minute_candles
        FOR VALUES FROM ('2024-08-13 21:00:00+00') TO ('2024-08-14 21:00:00+00');

alter table minute_candles_2024_08_14
    owner to postgres;

grant select on minute_candles_2024_08_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_14 to admin;

create table minute_candles_2024_08_15
    partition of minute_candles
        FOR VALUES FROM ('2024-08-14 21:00:00+00') TO ('2024-08-15 21:00:00+00');

alter table minute_candles_2024_08_15
    owner to postgres;

grant select on minute_candles_2024_08_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_15 to admin;

create table minute_candles_2024_08_16
    partition of minute_candles
        FOR VALUES FROM ('2024-08-15 21:00:00+00') TO ('2024-08-16 21:00:00+00');

alter table minute_candles_2024_08_16
    owner to postgres;

grant select on minute_candles_2024_08_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_16 to admin;

create table minute_candles_2024_08_17
    partition of minute_candles
        FOR VALUES FROM ('2024-08-16 21:00:00+00') TO ('2024-08-17 21:00:00+00');

alter table minute_candles_2024_08_17
    owner to postgres;

grant select on minute_candles_2024_08_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_17 to admin;

create table minute_candles_2024_08_18
    partition of minute_candles
        FOR VALUES FROM ('2024-08-17 21:00:00+00') TO ('2024-08-18 21:00:00+00');

alter table minute_candles_2024_08_18
    owner to postgres;

grant select on minute_candles_2024_08_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_18 to admin;

create table minute_candles_2024_08_19
    partition of minute_candles
        FOR VALUES FROM ('2024-08-18 21:00:00+00') TO ('2024-08-19 21:00:00+00');

alter table minute_candles_2024_08_19
    owner to postgres;

grant select on minute_candles_2024_08_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_19 to admin;

create table minute_candles_2024_08_20
    partition of minute_candles
        FOR VALUES FROM ('2024-08-19 21:00:00+00') TO ('2024-08-20 21:00:00+00');

alter table minute_candles_2024_08_20
    owner to postgres;

grant select on minute_candles_2024_08_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_20 to admin;

create table minute_candles_2024_08_21
    partition of minute_candles
        FOR VALUES FROM ('2024-08-20 21:00:00+00') TO ('2024-08-21 21:00:00+00');

alter table minute_candles_2024_08_21
    owner to postgres;

grant select on minute_candles_2024_08_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_21 to admin;

create table minute_candles_2024_08_22
    partition of minute_candles
        FOR VALUES FROM ('2024-08-21 21:00:00+00') TO ('2024-08-22 21:00:00+00');

alter table minute_candles_2024_08_22
    owner to postgres;

grant select on minute_candles_2024_08_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_22 to admin;

create table minute_candles_2024_08_23
    partition of minute_candles
        FOR VALUES FROM ('2024-08-22 21:00:00+00') TO ('2024-08-23 21:00:00+00');

alter table minute_candles_2024_08_23
    owner to postgres;

grant select on minute_candles_2024_08_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_23 to admin;

create table minute_candles_2024_08_24
    partition of minute_candles
        FOR VALUES FROM ('2024-08-23 21:00:00+00') TO ('2024-08-24 21:00:00+00');

alter table minute_candles_2024_08_24
    owner to postgres;

grant select on minute_candles_2024_08_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_24 to admin;

create table minute_candles_2024_08_25
    partition of minute_candles
        FOR VALUES FROM ('2024-08-24 21:00:00+00') TO ('2024-08-25 21:00:00+00');

alter table minute_candles_2024_08_25
    owner to postgres;

grant select on minute_candles_2024_08_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_25 to admin;

create table minute_candles_2024_08_26
    partition of minute_candles
        FOR VALUES FROM ('2024-08-25 21:00:00+00') TO ('2024-08-26 21:00:00+00');

alter table minute_candles_2024_08_26
    owner to postgres;

grant select on minute_candles_2024_08_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_26 to admin;

create table minute_candles_2024_08_27
    partition of minute_candles
        FOR VALUES FROM ('2024-08-26 21:00:00+00') TO ('2024-08-27 21:00:00+00');

alter table minute_candles_2024_08_27
    owner to postgres;

grant select on minute_candles_2024_08_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_27 to admin;

create table minute_candles_2024_08_28
    partition of minute_candles
        FOR VALUES FROM ('2024-08-27 21:00:00+00') TO ('2024-08-28 21:00:00+00');

alter table minute_candles_2024_08_28
    owner to postgres;

grant select on minute_candles_2024_08_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_28 to admin;

create table minute_candles_2024_08_29
    partition of minute_candles
        FOR VALUES FROM ('2024-08-28 21:00:00+00') TO ('2024-08-29 21:00:00+00');

alter table minute_candles_2024_08_29
    owner to postgres;

grant select on minute_candles_2024_08_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_29 to admin;

create table minute_candles_2024_08_30
    partition of minute_candles
        FOR VALUES FROM ('2024-08-29 21:00:00+00') TO ('2024-08-30 21:00:00+00');

alter table minute_candles_2024_08_30
    owner to postgres;

grant select on minute_candles_2024_08_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_30 to admin;

create table minute_candles_2024_08_31
    partition of minute_candles
        FOR VALUES FROM ('2024-08-30 21:00:00+00') TO ('2024-08-31 21:00:00+00');

alter table minute_candles_2024_08_31
    owner to postgres;

grant select on minute_candles_2024_08_31 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_08_31 to admin;

create table minute_candles_2024_09_01
    partition of minute_candles
        FOR VALUES FROM ('2024-08-31 21:00:00+00') TO ('2024-09-01 21:00:00+00');

alter table minute_candles_2024_09_01
    owner to postgres;

grant select on minute_candles_2024_09_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_01 to admin;

create table minute_candles_2024_09_02
    partition of minute_candles
        FOR VALUES FROM ('2024-09-01 21:00:00+00') TO ('2024-09-02 21:00:00+00');

alter table minute_candles_2024_09_02
    owner to postgres;

grant select on minute_candles_2024_09_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_02 to admin;

create table minute_candles_2024_09_03
    partition of minute_candles
        FOR VALUES FROM ('2024-09-02 21:00:00+00') TO ('2024-09-03 21:00:00+00');

alter table minute_candles_2024_09_03
    owner to postgres;

grant select on minute_candles_2024_09_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_03 to admin;

create table minute_candles_2024_09_04
    partition of minute_candles
        FOR VALUES FROM ('2024-09-03 21:00:00+00') TO ('2024-09-04 21:00:00+00');

alter table minute_candles_2024_09_04
    owner to postgres;

grant select on minute_candles_2024_09_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_04 to admin;

create table minute_candles_2024_09_05
    partition of minute_candles
        FOR VALUES FROM ('2024-09-04 21:00:00+00') TO ('2024-09-05 21:00:00+00');

alter table minute_candles_2024_09_05
    owner to postgres;

grant select on minute_candles_2024_09_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_05 to admin;

create table minute_candles_2024_09_06
    partition of minute_candles
        FOR VALUES FROM ('2024-09-05 21:00:00+00') TO ('2024-09-06 21:00:00+00');

alter table minute_candles_2024_09_06
    owner to postgres;

grant select on minute_candles_2024_09_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_06 to admin;

create table minute_candles_2024_09_07
    partition of minute_candles
        FOR VALUES FROM ('2024-09-06 21:00:00+00') TO ('2024-09-07 21:00:00+00');

alter table minute_candles_2024_09_07
    owner to postgres;

grant select on minute_candles_2024_09_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_07 to admin;

create table minute_candles_2024_09_08
    partition of minute_candles
        FOR VALUES FROM ('2024-09-07 21:00:00+00') TO ('2024-09-08 21:00:00+00');

alter table minute_candles_2024_09_08
    owner to postgres;

grant select on minute_candles_2024_09_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_08 to admin;

create table minute_candles_2024_09_09
    partition of minute_candles
        FOR VALUES FROM ('2024-09-08 21:00:00+00') TO ('2024-09-09 21:00:00+00');

alter table minute_candles_2024_09_09
    owner to postgres;

grant select on minute_candles_2024_09_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_09 to admin;

create table minute_candles_2024_09_10
    partition of minute_candles
        FOR VALUES FROM ('2024-09-09 21:00:00+00') TO ('2024-09-10 21:00:00+00');

alter table minute_candles_2024_09_10
    owner to postgres;

grant select on minute_candles_2024_09_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_10 to admin;

create table minute_candles_2024_09_11
    partition of minute_candles
        FOR VALUES FROM ('2024-09-10 21:00:00+00') TO ('2024-09-11 21:00:00+00');

alter table minute_candles_2024_09_11
    owner to postgres;

grant select on minute_candles_2024_09_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_11 to admin;

create table minute_candles_2024_09_12
    partition of minute_candles
        FOR VALUES FROM ('2024-09-11 21:00:00+00') TO ('2024-09-12 21:00:00+00');

alter table minute_candles_2024_09_12
    owner to postgres;

grant select on minute_candles_2024_09_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_12 to admin;

create table minute_candles_2024_09_13
    partition of minute_candles
        FOR VALUES FROM ('2024-09-12 21:00:00+00') TO ('2024-09-13 21:00:00+00');

alter table minute_candles_2024_09_13
    owner to postgres;

grant select on minute_candles_2024_09_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_13 to admin;

create table minute_candles_2024_09_14
    partition of minute_candles
        FOR VALUES FROM ('2024-09-13 21:00:00+00') TO ('2024-09-14 21:00:00+00');

alter table minute_candles_2024_09_14
    owner to postgres;

grant select on minute_candles_2024_09_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_14 to admin;

create table minute_candles_2024_09_15
    partition of minute_candles
        FOR VALUES FROM ('2024-09-14 21:00:00+00') TO ('2024-09-15 21:00:00+00');

alter table minute_candles_2024_09_15
    owner to postgres;

grant select on minute_candles_2024_09_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_15 to admin;

create table minute_candles_2024_09_16
    partition of minute_candles
        FOR VALUES FROM ('2024-09-15 21:00:00+00') TO ('2024-09-16 21:00:00+00');

alter table minute_candles_2024_09_16
    owner to postgres;

grant select on minute_candles_2024_09_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_16 to admin;

create table minute_candles_2024_09_17
    partition of minute_candles
        FOR VALUES FROM ('2024-09-16 21:00:00+00') TO ('2024-09-17 21:00:00+00');

alter table minute_candles_2024_09_17
    owner to postgres;

grant select on minute_candles_2024_09_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_17 to admin;

create table minute_candles_2024_09_18
    partition of minute_candles
        FOR VALUES FROM ('2024-09-17 21:00:00+00') TO ('2024-09-18 21:00:00+00');

alter table minute_candles_2024_09_18
    owner to postgres;

grant select on minute_candles_2024_09_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_18 to admin;

create table minute_candles_2024_09_19
    partition of minute_candles
        FOR VALUES FROM ('2024-09-18 21:00:00+00') TO ('2024-09-19 21:00:00+00');

alter table minute_candles_2024_09_19
    owner to postgres;

grant select on minute_candles_2024_09_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_19 to admin;

create table minute_candles_2024_09_20
    partition of minute_candles
        FOR VALUES FROM ('2024-09-19 21:00:00+00') TO ('2024-09-20 21:00:00+00');

alter table minute_candles_2024_09_20
    owner to postgres;

grant select on minute_candles_2024_09_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_20 to admin;

create table minute_candles_2024_09_21
    partition of minute_candles
        FOR VALUES FROM ('2024-09-20 21:00:00+00') TO ('2024-09-21 21:00:00+00');

alter table minute_candles_2024_09_21
    owner to postgres;

grant select on minute_candles_2024_09_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_21 to admin;

create table minute_candles_2024_09_22
    partition of minute_candles
        FOR VALUES FROM ('2024-09-21 21:00:00+00') TO ('2024-09-22 21:00:00+00');

alter table minute_candles_2024_09_22
    owner to postgres;

grant select on minute_candles_2024_09_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_22 to admin;

create table minute_candles_2024_09_23
    partition of minute_candles
        FOR VALUES FROM ('2024-09-22 21:00:00+00') TO ('2024-09-23 21:00:00+00');

alter table minute_candles_2024_09_23
    owner to postgres;

grant select on minute_candles_2024_09_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_23 to admin;

create table minute_candles_2024_09_24
    partition of minute_candles
        FOR VALUES FROM ('2024-09-23 21:00:00+00') TO ('2024-09-24 21:00:00+00');

alter table minute_candles_2024_09_24
    owner to postgres;

grant select on minute_candles_2024_09_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_24 to admin;

create table minute_candles_2024_09_25
    partition of minute_candles
        FOR VALUES FROM ('2024-09-24 21:00:00+00') TO ('2024-09-25 21:00:00+00');

alter table minute_candles_2024_09_25
    owner to postgres;

grant select on minute_candles_2024_09_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_25 to admin;

create table minute_candles_2024_09_26
    partition of minute_candles
        FOR VALUES FROM ('2024-09-25 21:00:00+00') TO ('2024-09-26 21:00:00+00');

alter table minute_candles_2024_09_26
    owner to postgres;

grant select on minute_candles_2024_09_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_26 to admin;

create table minute_candles_2024_09_27
    partition of minute_candles
        FOR VALUES FROM ('2024-09-26 21:00:00+00') TO ('2024-09-27 21:00:00+00');

alter table minute_candles_2024_09_27
    owner to postgres;

grant select on minute_candles_2024_09_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_27 to admin;

create table minute_candles_2024_09_28
    partition of minute_candles
        FOR VALUES FROM ('2024-09-27 21:00:00+00') TO ('2024-09-28 21:00:00+00');

alter table minute_candles_2024_09_28
    owner to postgres;

grant select on minute_candles_2024_09_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_28 to admin;

create table minute_candles_2024_09_29
    partition of minute_candles
        FOR VALUES FROM ('2024-09-28 21:00:00+00') TO ('2024-09-29 21:00:00+00');

alter table minute_candles_2024_09_29
    owner to postgres;

grant select on minute_candles_2024_09_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_29 to admin;

create table minute_candles_2024_09_30
    partition of minute_candles
        FOR VALUES FROM ('2024-09-29 21:00:00+00') TO ('2024-09-30 21:00:00+00');

alter table minute_candles_2024_09_30
    owner to postgres;

grant select on minute_candles_2024_09_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_09_30 to admin;

create table minute_candles_2024_10_01
    partition of minute_candles
        FOR VALUES FROM ('2024-09-30 21:00:00+00') TO ('2024-10-01 21:00:00+00');

alter table minute_candles_2024_10_01
    owner to postgres;

grant select on minute_candles_2024_10_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_01 to admin;

create table minute_candles_2024_10_02
    partition of minute_candles
        FOR VALUES FROM ('2024-10-01 21:00:00+00') TO ('2024-10-02 21:00:00+00');

alter table minute_candles_2024_10_02
    owner to postgres;

grant select on minute_candles_2024_10_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_02 to admin;

create table minute_candles_2024_10_03
    partition of minute_candles
        FOR VALUES FROM ('2024-10-02 21:00:00+00') TO ('2024-10-03 21:00:00+00');

alter table minute_candles_2024_10_03
    owner to postgres;

grant select on minute_candles_2024_10_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_03 to admin;

create table minute_candles_2024_10_04
    partition of minute_candles
        FOR VALUES FROM ('2024-10-03 21:00:00+00') TO ('2024-10-04 21:00:00+00');

alter table minute_candles_2024_10_04
    owner to postgres;

grant select on minute_candles_2024_10_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_04 to admin;

create table minute_candles_2024_10_05
    partition of minute_candles
        FOR VALUES FROM ('2024-10-04 21:00:00+00') TO ('2024-10-05 21:00:00+00');

alter table minute_candles_2024_10_05
    owner to postgres;

grant select on minute_candles_2024_10_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_05 to admin;

create table minute_candles_2024_10_06
    partition of minute_candles
        FOR VALUES FROM ('2024-10-05 21:00:00+00') TO ('2024-10-06 21:00:00+00');

alter table minute_candles_2024_10_06
    owner to postgres;

grant select on minute_candles_2024_10_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_06 to admin;

create table minute_candles_2024_10_07
    partition of minute_candles
        FOR VALUES FROM ('2024-10-06 21:00:00+00') TO ('2024-10-07 21:00:00+00');

alter table minute_candles_2024_10_07
    owner to postgres;

grant select on minute_candles_2024_10_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_07 to admin;

create table minute_candles_2024_10_08
    partition of minute_candles
        FOR VALUES FROM ('2024-10-07 21:00:00+00') TO ('2024-10-08 21:00:00+00');

alter table minute_candles_2024_10_08
    owner to postgres;

grant select on minute_candles_2024_10_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_08 to admin;

create table minute_candles_2024_10_09
    partition of minute_candles
        FOR VALUES FROM ('2024-10-08 21:00:00+00') TO ('2024-10-09 21:00:00+00');

alter table minute_candles_2024_10_09
    owner to postgres;

grant select on minute_candles_2024_10_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_09 to admin;

create table minute_candles_2024_10_10
    partition of minute_candles
        FOR VALUES FROM ('2024-10-09 21:00:00+00') TO ('2024-10-10 21:00:00+00');

alter table minute_candles_2024_10_10
    owner to postgres;

grant select on minute_candles_2024_10_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_10 to admin;

create table minute_candles_2024_10_11
    partition of minute_candles
        FOR VALUES FROM ('2024-10-10 21:00:00+00') TO ('2024-10-11 21:00:00+00');

alter table minute_candles_2024_10_11
    owner to postgres;

grant select on minute_candles_2024_10_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_11 to admin;

create table minute_candles_2024_10_12
    partition of minute_candles
        FOR VALUES FROM ('2024-10-11 21:00:00+00') TO ('2024-10-12 21:00:00+00');

alter table minute_candles_2024_10_12
    owner to postgres;

grant select on minute_candles_2024_10_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_12 to admin;

create table minute_candles_2024_10_13
    partition of minute_candles
        FOR VALUES FROM ('2024-10-12 21:00:00+00') TO ('2024-10-13 21:00:00+00');

alter table minute_candles_2024_10_13
    owner to postgres;

grant select on minute_candles_2024_10_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_13 to admin;

create table minute_candles_2024_10_14
    partition of minute_candles
        FOR VALUES FROM ('2024-10-13 21:00:00+00') TO ('2024-10-14 21:00:00+00');

alter table minute_candles_2024_10_14
    owner to postgres;

grant select on minute_candles_2024_10_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_14 to admin;

create table minute_candles_2024_10_15
    partition of minute_candles
        FOR VALUES FROM ('2024-10-14 21:00:00+00') TO ('2024-10-15 21:00:00+00');

alter table minute_candles_2024_10_15
    owner to postgres;

grant select on minute_candles_2024_10_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_15 to admin;

create table minute_candles_2024_10_16
    partition of minute_candles
        FOR VALUES FROM ('2024-10-15 21:00:00+00') TO ('2024-10-16 21:00:00+00');

alter table minute_candles_2024_10_16
    owner to postgres;

grant select on minute_candles_2024_10_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_16 to admin;

create table minute_candles_2024_10_17
    partition of minute_candles
        FOR VALUES FROM ('2024-10-16 21:00:00+00') TO ('2024-10-17 21:00:00+00');

alter table minute_candles_2024_10_17
    owner to postgres;

grant select on minute_candles_2024_10_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_17 to admin;

create table minute_candles_2024_10_18
    partition of minute_candles
        FOR VALUES FROM ('2024-10-17 21:00:00+00') TO ('2024-10-18 21:00:00+00');

alter table minute_candles_2024_10_18
    owner to postgres;

grant select on minute_candles_2024_10_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_18 to admin;

create table minute_candles_2024_10_19
    partition of minute_candles
        FOR VALUES FROM ('2024-10-18 21:00:00+00') TO ('2024-10-19 21:00:00+00');

alter table minute_candles_2024_10_19
    owner to postgres;

grant select on minute_candles_2024_10_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_19 to admin;

create table minute_candles_2024_10_20
    partition of minute_candles
        FOR VALUES FROM ('2024-10-19 21:00:00+00') TO ('2024-10-20 21:00:00+00');

alter table minute_candles_2024_10_20
    owner to postgres;

grant select on minute_candles_2024_10_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_20 to admin;

create table minute_candles_2024_10_21
    partition of minute_candles
        FOR VALUES FROM ('2024-10-20 21:00:00+00') TO ('2024-10-21 21:00:00+00');

alter table minute_candles_2024_10_21
    owner to postgres;

grant select on minute_candles_2024_10_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_21 to admin;

create table minute_candles_2024_10_22
    partition of minute_candles
        FOR VALUES FROM ('2024-10-21 21:00:00+00') TO ('2024-10-22 21:00:00+00');

alter table minute_candles_2024_10_22
    owner to postgres;

grant select on minute_candles_2024_10_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_22 to admin;

create table minute_candles_2024_10_23
    partition of minute_candles
        FOR VALUES FROM ('2024-10-22 21:00:00+00') TO ('2024-10-23 21:00:00+00');

alter table minute_candles_2024_10_23
    owner to postgres;

grant select on minute_candles_2024_10_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_23 to admin;

create table minute_candles_2024_10_24
    partition of minute_candles
        FOR VALUES FROM ('2024-10-23 21:00:00+00') TO ('2024-10-24 21:00:00+00');

alter table minute_candles_2024_10_24
    owner to postgres;

grant select on minute_candles_2024_10_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_24 to admin;

create table minute_candles_2024_10_25
    partition of minute_candles
        FOR VALUES FROM ('2024-10-24 21:00:00+00') TO ('2024-10-25 21:00:00+00');

alter table minute_candles_2024_10_25
    owner to postgres;

grant select on minute_candles_2024_10_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_25 to admin;

create table minute_candles_2024_10_26
    partition of minute_candles
        FOR VALUES FROM ('2024-10-25 21:00:00+00') TO ('2024-10-26 21:00:00+00');

alter table minute_candles_2024_10_26
    owner to postgres;

grant select on minute_candles_2024_10_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_26 to admin;

create table minute_candles_2024_10_27
    partition of minute_candles
        FOR VALUES FROM ('2024-10-26 21:00:00+00') TO ('2024-10-27 21:00:00+00');

alter table minute_candles_2024_10_27
    owner to postgres;

grant select on minute_candles_2024_10_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_27 to admin;

create table minute_candles_2024_10_28
    partition of minute_candles
        FOR VALUES FROM ('2024-10-27 21:00:00+00') TO ('2024-10-28 21:00:00+00');

alter table minute_candles_2024_10_28
    owner to postgres;

grant select on minute_candles_2024_10_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_28 to admin;

create table minute_candles_2024_10_29
    partition of minute_candles
        FOR VALUES FROM ('2024-10-28 21:00:00+00') TO ('2024-10-29 21:00:00+00');

alter table minute_candles_2024_10_29
    owner to postgres;

grant select on minute_candles_2024_10_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_29 to admin;

create table minute_candles_2024_10_30
    partition of minute_candles
        FOR VALUES FROM ('2024-10-29 21:00:00+00') TO ('2024-10-30 21:00:00+00');

alter table minute_candles_2024_10_30
    owner to postgres;

grant select on minute_candles_2024_10_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_30 to admin;

create table minute_candles_2024_10_31
    partition of minute_candles
        FOR VALUES FROM ('2024-10-30 21:00:00+00') TO ('2024-10-31 21:00:00+00');

alter table minute_candles_2024_10_31
    owner to postgres;

grant select on minute_candles_2024_10_31 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_10_31 to admin;

create table minute_candles_2024_11_01
    partition of minute_candles
        FOR VALUES FROM ('2024-10-31 21:00:00+00') TO ('2024-11-01 21:00:00+00');

alter table minute_candles_2024_11_01
    owner to postgres;

grant select on minute_candles_2024_11_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_01 to admin;

create table minute_candles_2024_11_02
    partition of minute_candles
        FOR VALUES FROM ('2024-11-01 21:00:00+00') TO ('2024-11-02 21:00:00+00');

alter table minute_candles_2024_11_02
    owner to postgres;

grant select on minute_candles_2024_11_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_02 to admin;

create table minute_candles_2024_11_03
    partition of minute_candles
        FOR VALUES FROM ('2024-11-02 21:00:00+00') TO ('2024-11-03 21:00:00+00');

alter table minute_candles_2024_11_03
    owner to postgres;

grant select on minute_candles_2024_11_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_03 to admin;

create table minute_candles_2024_11_04
    partition of minute_candles
        FOR VALUES FROM ('2024-11-03 21:00:00+00') TO ('2024-11-04 21:00:00+00');

alter table minute_candles_2024_11_04
    owner to postgres;

grant select on minute_candles_2024_11_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_04 to admin;

create table minute_candles_2024_11_05
    partition of minute_candles
        FOR VALUES FROM ('2024-11-04 21:00:00+00') TO ('2024-11-05 21:00:00+00');

alter table minute_candles_2024_11_05
    owner to postgres;

grant select on minute_candles_2024_11_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_05 to admin;

create table minute_candles_2024_11_06
    partition of minute_candles
        FOR VALUES FROM ('2024-11-05 21:00:00+00') TO ('2024-11-06 21:00:00+00');

alter table minute_candles_2024_11_06
    owner to postgres;

grant select on minute_candles_2024_11_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_06 to admin;

create table minute_candles_2024_11_07
    partition of minute_candles
        FOR VALUES FROM ('2024-11-06 21:00:00+00') TO ('2024-11-07 21:00:00+00');

alter table minute_candles_2024_11_07
    owner to postgres;

grant select on minute_candles_2024_11_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_07 to admin;

create table minute_candles_2024_11_08
    partition of minute_candles
        FOR VALUES FROM ('2024-11-07 21:00:00+00') TO ('2024-11-08 21:00:00+00');

alter table minute_candles_2024_11_08
    owner to postgres;

grant select on minute_candles_2024_11_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_08 to admin;

create table minute_candles_2024_11_09
    partition of minute_candles
        FOR VALUES FROM ('2024-11-08 21:00:00+00') TO ('2024-11-09 21:00:00+00');

alter table minute_candles_2024_11_09
    owner to postgres;

grant select on minute_candles_2024_11_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_09 to admin;

create table minute_candles_2024_11_10
    partition of minute_candles
        FOR VALUES FROM ('2024-11-09 21:00:00+00') TO ('2024-11-10 21:00:00+00');

alter table minute_candles_2024_11_10
    owner to postgres;

grant select on minute_candles_2024_11_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_10 to admin;

create table minute_candles_2024_11_11
    partition of minute_candles
        FOR VALUES FROM ('2024-11-10 21:00:00+00') TO ('2024-11-11 21:00:00+00');

alter table minute_candles_2024_11_11
    owner to postgres;

grant select on minute_candles_2024_11_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_11 to admin;

create table minute_candles_2024_11_12
    partition of minute_candles
        FOR VALUES FROM ('2024-11-11 21:00:00+00') TO ('2024-11-12 21:00:00+00');

alter table minute_candles_2024_11_12
    owner to postgres;

grant select on minute_candles_2024_11_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_12 to admin;

create table minute_candles_2024_11_13
    partition of minute_candles
        FOR VALUES FROM ('2024-11-12 21:00:00+00') TO ('2024-11-13 21:00:00+00');

alter table minute_candles_2024_11_13
    owner to postgres;

grant select on minute_candles_2024_11_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_13 to admin;

create table minute_candles_2024_11_14
    partition of minute_candles
        FOR VALUES FROM ('2024-11-13 21:00:00+00') TO ('2024-11-14 21:00:00+00');

alter table minute_candles_2024_11_14
    owner to postgres;

grant select on minute_candles_2024_11_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_14 to admin;

create table minute_candles_2024_11_15
    partition of minute_candles
        FOR VALUES FROM ('2024-11-14 21:00:00+00') TO ('2024-11-15 21:00:00+00');

alter table minute_candles_2024_11_15
    owner to postgres;

grant select on minute_candles_2024_11_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_15 to admin;

create table minute_candles_2024_11_16
    partition of minute_candles
        FOR VALUES FROM ('2024-11-15 21:00:00+00') TO ('2024-11-16 21:00:00+00');

alter table minute_candles_2024_11_16
    owner to postgres;

grant select on minute_candles_2024_11_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_16 to admin;

create table minute_candles_2024_11_17
    partition of minute_candles
        FOR VALUES FROM ('2024-11-16 21:00:00+00') TO ('2024-11-17 21:00:00+00');

alter table minute_candles_2024_11_17
    owner to postgres;

grant select on minute_candles_2024_11_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_17 to admin;

create table minute_candles_2024_11_18
    partition of minute_candles
        FOR VALUES FROM ('2024-11-17 21:00:00+00') TO ('2024-11-18 21:00:00+00');

alter table minute_candles_2024_11_18
    owner to postgres;

grant select on minute_candles_2024_11_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_18 to admin;

create table minute_candles_2024_11_19
    partition of minute_candles
        FOR VALUES FROM ('2024-11-18 21:00:00+00') TO ('2024-11-19 21:00:00+00');

alter table minute_candles_2024_11_19
    owner to postgres;

grant select on minute_candles_2024_11_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_19 to admin;

create table minute_candles_2024_11_20
    partition of minute_candles
        FOR VALUES FROM ('2024-11-19 21:00:00+00') TO ('2024-11-20 21:00:00+00');

alter table minute_candles_2024_11_20
    owner to postgres;

grant select on minute_candles_2024_11_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_20 to admin;

create table minute_candles_2024_11_21
    partition of minute_candles
        FOR VALUES FROM ('2024-11-20 21:00:00+00') TO ('2024-11-21 21:00:00+00');

alter table minute_candles_2024_11_21
    owner to postgres;

grant select on minute_candles_2024_11_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_21 to admin;

create table minute_candles_2024_11_22
    partition of minute_candles
        FOR VALUES FROM ('2024-11-21 21:00:00+00') TO ('2024-11-22 21:00:00+00');

alter table minute_candles_2024_11_22
    owner to postgres;

grant select on minute_candles_2024_11_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_22 to admin;

create table minute_candles_2024_11_23
    partition of minute_candles
        FOR VALUES FROM ('2024-11-22 21:00:00+00') TO ('2024-11-23 21:00:00+00');

alter table minute_candles_2024_11_23
    owner to postgres;

grant select on minute_candles_2024_11_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_23 to admin;

create table minute_candles_2024_11_24
    partition of minute_candles
        FOR VALUES FROM ('2024-11-23 21:00:00+00') TO ('2024-11-24 21:00:00+00');

alter table minute_candles_2024_11_24
    owner to postgres;

grant select on minute_candles_2024_11_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_24 to admin;

create table minute_candles_2024_11_25
    partition of minute_candles
        FOR VALUES FROM ('2024-11-24 21:00:00+00') TO ('2024-11-25 21:00:00+00');

alter table minute_candles_2024_11_25
    owner to postgres;

grant select on minute_candles_2024_11_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_25 to admin;

create table minute_candles_2024_11_26
    partition of minute_candles
        FOR VALUES FROM ('2024-11-25 21:00:00+00') TO ('2024-11-26 21:00:00+00');

alter table minute_candles_2024_11_26
    owner to postgres;

grant select on minute_candles_2024_11_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_26 to admin;

create table minute_candles_2024_11_27
    partition of minute_candles
        FOR VALUES FROM ('2024-11-26 21:00:00+00') TO ('2024-11-27 21:00:00+00');

alter table minute_candles_2024_11_27
    owner to postgres;

grant select on minute_candles_2024_11_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_27 to admin;

create table minute_candles_2024_11_28
    partition of minute_candles
        FOR VALUES FROM ('2024-11-27 21:00:00+00') TO ('2024-11-28 21:00:00+00');

alter table minute_candles_2024_11_28
    owner to postgres;

grant select on minute_candles_2024_11_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_28 to admin;

create table minute_candles_2024_11_29
    partition of minute_candles
        FOR VALUES FROM ('2024-11-28 21:00:00+00') TO ('2024-11-29 21:00:00+00');

alter table minute_candles_2024_11_29
    owner to postgres;

grant select on minute_candles_2024_11_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_29 to admin;

create table minute_candles_2024_11_30
    partition of minute_candles
        FOR VALUES FROM ('2024-11-29 21:00:00+00') TO ('2024-11-30 21:00:00+00');

alter table minute_candles_2024_11_30
    owner to postgres;

grant select on minute_candles_2024_11_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_11_30 to admin;

create table minute_candles_2024_12_01
    partition of minute_candles
        FOR VALUES FROM ('2024-11-30 21:00:00+00') TO ('2024-12-01 21:00:00+00');

alter table minute_candles_2024_12_01
    owner to postgres;

grant select on minute_candles_2024_12_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_01 to admin;

create table minute_candles_2024_12_02
    partition of minute_candles
        FOR VALUES FROM ('2024-12-01 21:00:00+00') TO ('2024-12-02 21:00:00+00');

alter table minute_candles_2024_12_02
    owner to postgres;

grant select on minute_candles_2024_12_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_02 to admin;

create table minute_candles_2024_12_03
    partition of minute_candles
        FOR VALUES FROM ('2024-12-02 21:00:00+00') TO ('2024-12-03 21:00:00+00');

alter table minute_candles_2024_12_03
    owner to postgres;

grant select on minute_candles_2024_12_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_03 to admin;

create table minute_candles_2024_12_04
    partition of minute_candles
        FOR VALUES FROM ('2024-12-03 21:00:00+00') TO ('2024-12-04 21:00:00+00');

alter table minute_candles_2024_12_04
    owner to postgres;

grant select on minute_candles_2024_12_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_04 to admin;

create table minute_candles_2024_12_05
    partition of minute_candles
        FOR VALUES FROM ('2024-12-04 21:00:00+00') TO ('2024-12-05 21:00:00+00');

alter table minute_candles_2024_12_05
    owner to postgres;

grant select on minute_candles_2024_12_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_05 to admin;

create table minute_candles_2024_12_06
    partition of minute_candles
        FOR VALUES FROM ('2024-12-05 21:00:00+00') TO ('2024-12-06 21:00:00+00');

alter table minute_candles_2024_12_06
    owner to postgres;

grant select on minute_candles_2024_12_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_06 to admin;

create table minute_candles_2024_12_07
    partition of minute_candles
        FOR VALUES FROM ('2024-12-06 21:00:00+00') TO ('2024-12-07 21:00:00+00');

alter table minute_candles_2024_12_07
    owner to postgres;

grant select on minute_candles_2024_12_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_07 to admin;

create table minute_candles_2024_12_08
    partition of minute_candles
        FOR VALUES FROM ('2024-12-07 21:00:00+00') TO ('2024-12-08 21:00:00+00');

alter table minute_candles_2024_12_08
    owner to postgres;

grant select on minute_candles_2024_12_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_08 to admin;

create table minute_candles_2024_12_09
    partition of minute_candles
        FOR VALUES FROM ('2024-12-08 21:00:00+00') TO ('2024-12-09 21:00:00+00');

alter table minute_candles_2024_12_09
    owner to postgres;

grant select on minute_candles_2024_12_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_09 to admin;

create table minute_candles_2024_12_10
    partition of minute_candles
        FOR VALUES FROM ('2024-12-09 21:00:00+00') TO ('2024-12-10 21:00:00+00');

alter table minute_candles_2024_12_10
    owner to postgres;

grant select on minute_candles_2024_12_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_10 to admin;

create table minute_candles_2024_12_11
    partition of minute_candles
        FOR VALUES FROM ('2024-12-10 21:00:00+00') TO ('2024-12-11 21:00:00+00');

alter table minute_candles_2024_12_11
    owner to postgres;

grant select on minute_candles_2024_12_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_11 to admin;

create table minute_candles_2024_12_12
    partition of minute_candles
        FOR VALUES FROM ('2024-12-11 21:00:00+00') TO ('2024-12-12 21:00:00+00');

alter table minute_candles_2024_12_12
    owner to postgres;

grant select on minute_candles_2024_12_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_12 to admin;

create table minute_candles_2024_12_13
    partition of minute_candles
        FOR VALUES FROM ('2024-12-12 21:00:00+00') TO ('2024-12-13 21:00:00+00');

alter table minute_candles_2024_12_13
    owner to postgres;

grant select on minute_candles_2024_12_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_13 to admin;

create table minute_candles_2024_12_14
    partition of minute_candles
        FOR VALUES FROM ('2024-12-13 21:00:00+00') TO ('2024-12-14 21:00:00+00');

alter table minute_candles_2024_12_14
    owner to postgres;

grant select on minute_candles_2024_12_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_14 to admin;

create table minute_candles_2024_12_15
    partition of minute_candles
        FOR VALUES FROM ('2024-12-14 21:00:00+00') TO ('2024-12-15 21:00:00+00');

alter table minute_candles_2024_12_15
    owner to postgres;

grant select on minute_candles_2024_12_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_15 to admin;

create table minute_candles_2024_12_16
    partition of minute_candles
        FOR VALUES FROM ('2024-12-15 21:00:00+00') TO ('2024-12-16 21:00:00+00');

alter table minute_candles_2024_12_16
    owner to postgres;

grant select on minute_candles_2024_12_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_16 to admin;

create table minute_candles_2024_12_17
    partition of minute_candles
        FOR VALUES FROM ('2024-12-16 21:00:00+00') TO ('2024-12-17 21:00:00+00');

alter table minute_candles_2024_12_17
    owner to postgres;

grant select on minute_candles_2024_12_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_17 to admin;

create table minute_candles_2024_12_18
    partition of minute_candles
        FOR VALUES FROM ('2024-12-17 21:00:00+00') TO ('2024-12-18 21:00:00+00');

alter table minute_candles_2024_12_18
    owner to postgres;

grant select on minute_candles_2024_12_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_18 to admin;

create table minute_candles_2024_12_19
    partition of minute_candles
        FOR VALUES FROM ('2024-12-18 21:00:00+00') TO ('2024-12-19 21:00:00+00');

alter table minute_candles_2024_12_19
    owner to postgres;

grant select on minute_candles_2024_12_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_19 to admin;

create table minute_candles_2024_12_20
    partition of minute_candles
        FOR VALUES FROM ('2024-12-19 21:00:00+00') TO ('2024-12-20 21:00:00+00');

alter table minute_candles_2024_12_20
    owner to postgres;

grant select on minute_candles_2024_12_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_20 to admin;

create table minute_candles_2024_12_21
    partition of minute_candles
        FOR VALUES FROM ('2024-12-20 21:00:00+00') TO ('2024-12-21 21:00:00+00');

alter table minute_candles_2024_12_21
    owner to postgres;

grant select on minute_candles_2024_12_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_21 to admin;

create table minute_candles_2024_12_22
    partition of minute_candles
        FOR VALUES FROM ('2024-12-21 21:00:00+00') TO ('2024-12-22 21:00:00+00');

alter table minute_candles_2024_12_22
    owner to postgres;

grant select on minute_candles_2024_12_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_22 to admin;

create table minute_candles_2024_12_23
    partition of minute_candles
        FOR VALUES FROM ('2024-12-22 21:00:00+00') TO ('2024-12-23 21:00:00+00');

alter table minute_candles_2024_12_23
    owner to postgres;

grant select on minute_candles_2024_12_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_23 to admin;

create table minute_candles_2024_12_24
    partition of minute_candles
        FOR VALUES FROM ('2024-12-23 21:00:00+00') TO ('2024-12-24 21:00:00+00');

alter table minute_candles_2024_12_24
    owner to postgres;

grant select on minute_candles_2024_12_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_24 to admin;

create table minute_candles_2024_12_25
    partition of minute_candles
        FOR VALUES FROM ('2024-12-24 21:00:00+00') TO ('2024-12-25 21:00:00+00');

alter table minute_candles_2024_12_25
    owner to postgres;

grant select on minute_candles_2024_12_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_25 to admin;

create table minute_candles_2024_12_26
    partition of minute_candles
        FOR VALUES FROM ('2024-12-25 21:00:00+00') TO ('2024-12-26 21:00:00+00');

alter table minute_candles_2024_12_26
    owner to postgres;

grant select on minute_candles_2024_12_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_26 to admin;

create table minute_candles_2024_12_27
    partition of minute_candles
        FOR VALUES FROM ('2024-12-26 21:00:00+00') TO ('2024-12-27 21:00:00+00');

alter table minute_candles_2024_12_27
    owner to postgres;

grant select on minute_candles_2024_12_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_27 to admin;

create table minute_candles_2024_12_28
    partition of minute_candles
        FOR VALUES FROM ('2024-12-27 21:00:00+00') TO ('2024-12-28 21:00:00+00');

alter table minute_candles_2024_12_28
    owner to postgres;

grant select on minute_candles_2024_12_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_28 to admin;

create table minute_candles_2024_12_29
    partition of minute_candles
        FOR VALUES FROM ('2024-12-28 21:00:00+00') TO ('2024-12-29 21:00:00+00');

alter table minute_candles_2024_12_29
    owner to postgres;

grant select on minute_candles_2024_12_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_29 to admin;

create table minute_candles_2024_12_30
    partition of minute_candles
        FOR VALUES FROM ('2024-12-29 21:00:00+00') TO ('2024-12-30 21:00:00+00');

alter table minute_candles_2024_12_30
    owner to postgres;

grant select on minute_candles_2024_12_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_30 to admin;

create table minute_candles_2024_12_31
    partition of minute_candles
        FOR VALUES FROM ('2024-12-30 21:00:00+00') TO ('2024-12-31 21:00:00+00');

alter table minute_candles_2024_12_31
    owner to postgres;

grant select on minute_candles_2024_12_31 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2024_12_31 to admin;

create table minute_candles_2025_01_01
    partition of minute_candles
        FOR VALUES FROM ('2024-12-31 21:00:00+00') TO ('2025-01-01 21:00:00+00');

alter table minute_candles_2025_01_01
    owner to postgres;

grant select on minute_candles_2025_01_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_01 to admin;

create table minute_candles_2025_01_02
    partition of minute_candles
        FOR VALUES FROM ('2025-01-01 21:00:00+00') TO ('2025-01-02 21:00:00+00');

alter table minute_candles_2025_01_02
    owner to postgres;

grant select on minute_candles_2025_01_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_02 to admin;

create table minute_candles_2025_01_03
    partition of minute_candles
        FOR VALUES FROM ('2025-01-02 21:00:00+00') TO ('2025-01-03 21:00:00+00');

alter table minute_candles_2025_01_03
    owner to postgres;

grant select on minute_candles_2025_01_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_03 to admin;

create table minute_candles_2025_01_04
    partition of minute_candles
        FOR VALUES FROM ('2025-01-03 21:00:00+00') TO ('2025-01-04 21:00:00+00');

alter table minute_candles_2025_01_04
    owner to postgres;

grant select on minute_candles_2025_01_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_04 to admin;

create table minute_candles_2025_01_05
    partition of minute_candles
        FOR VALUES FROM ('2025-01-04 21:00:00+00') TO ('2025-01-05 21:00:00+00');

alter table minute_candles_2025_01_05
    owner to postgres;

grant select on minute_candles_2025_01_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_05 to admin;

create table minute_candles_2025_01_06
    partition of minute_candles
        FOR VALUES FROM ('2025-01-05 21:00:00+00') TO ('2025-01-06 21:00:00+00');

alter table minute_candles_2025_01_06
    owner to postgres;

grant select on minute_candles_2025_01_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_06 to admin;

create table minute_candles_2025_01_07
    partition of minute_candles
        FOR VALUES FROM ('2025-01-06 21:00:00+00') TO ('2025-01-07 21:00:00+00');

alter table minute_candles_2025_01_07
    owner to postgres;

grant select on minute_candles_2025_01_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_07 to admin;

create table minute_candles_2025_01_08
    partition of minute_candles
        FOR VALUES FROM ('2025-01-07 21:00:00+00') TO ('2025-01-08 21:00:00+00');

alter table minute_candles_2025_01_08
    owner to postgres;

grant select on minute_candles_2025_01_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_08 to admin;

create table minute_candles_2025_01_09
    partition of minute_candles
        FOR VALUES FROM ('2025-01-08 21:00:00+00') TO ('2025-01-09 21:00:00+00');

alter table minute_candles_2025_01_09
    owner to postgres;

grant select on minute_candles_2025_01_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_09 to admin;

create table minute_candles_2025_01_10
    partition of minute_candles
        FOR VALUES FROM ('2025-01-09 21:00:00+00') TO ('2025-01-10 21:00:00+00');

alter table minute_candles_2025_01_10
    owner to postgres;

grant select on minute_candles_2025_01_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_10 to admin;

create table minute_candles_2025_01_11
    partition of minute_candles
        FOR VALUES FROM ('2025-01-10 21:00:00+00') TO ('2025-01-11 21:00:00+00');

alter table minute_candles_2025_01_11
    owner to postgres;

grant select on minute_candles_2025_01_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_11 to admin;

create table minute_candles_2025_01_12
    partition of minute_candles
        FOR VALUES FROM ('2025-01-11 21:00:00+00') TO ('2025-01-12 21:00:00+00');

alter table minute_candles_2025_01_12
    owner to postgres;

grant select on minute_candles_2025_01_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_12 to admin;

create table minute_candles_2025_01_13
    partition of minute_candles
        FOR VALUES FROM ('2025-01-12 21:00:00+00') TO ('2025-01-13 21:00:00+00');

alter table minute_candles_2025_01_13
    owner to postgres;

grant select on minute_candles_2025_01_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_13 to admin;

create table minute_candles_2025_01_14
    partition of minute_candles
        FOR VALUES FROM ('2025-01-13 21:00:00+00') TO ('2025-01-14 21:00:00+00');

alter table minute_candles_2025_01_14
    owner to postgres;

grant select on minute_candles_2025_01_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_14 to admin;

create table minute_candles_2025_01_15
    partition of minute_candles
        FOR VALUES FROM ('2025-01-14 21:00:00+00') TO ('2025-01-15 21:00:00+00');

alter table minute_candles_2025_01_15
    owner to postgres;

grant select on minute_candles_2025_01_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_15 to admin;

create table minute_candles_2025_01_16
    partition of minute_candles
        FOR VALUES FROM ('2025-01-15 21:00:00+00') TO ('2025-01-16 21:00:00+00');

alter table minute_candles_2025_01_16
    owner to postgres;

grant select on minute_candles_2025_01_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_16 to admin;

create table minute_candles_2025_01_17
    partition of minute_candles
        FOR VALUES FROM ('2025-01-16 21:00:00+00') TO ('2025-01-17 21:00:00+00');

alter table minute_candles_2025_01_17
    owner to postgres;

grant select on minute_candles_2025_01_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_17 to admin;

create table minute_candles_2025_01_18
    partition of minute_candles
        FOR VALUES FROM ('2025-01-17 21:00:00+00') TO ('2025-01-18 21:00:00+00');

alter table minute_candles_2025_01_18
    owner to postgres;

grant select on minute_candles_2025_01_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_18 to admin;

create table minute_candles_2025_01_19
    partition of minute_candles
        FOR VALUES FROM ('2025-01-18 21:00:00+00') TO ('2025-01-19 21:00:00+00');

alter table minute_candles_2025_01_19
    owner to postgres;

grant select on minute_candles_2025_01_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_19 to admin;

create table minute_candles_2025_01_20
    partition of minute_candles
        FOR VALUES FROM ('2025-01-19 21:00:00+00') TO ('2025-01-20 21:00:00+00');

alter table minute_candles_2025_01_20
    owner to postgres;

grant select on minute_candles_2025_01_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_20 to admin;

create table minute_candles_2025_01_21
    partition of minute_candles
        FOR VALUES FROM ('2025-01-20 21:00:00+00') TO ('2025-01-21 21:00:00+00');

alter table minute_candles_2025_01_21
    owner to postgres;

grant select on minute_candles_2025_01_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_21 to admin;

create table minute_candles_2025_01_22
    partition of minute_candles
        FOR VALUES FROM ('2025-01-21 21:00:00+00') TO ('2025-01-22 21:00:00+00');

alter table minute_candles_2025_01_22
    owner to postgres;

grant select on minute_candles_2025_01_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_22 to admin;

create table minute_candles_2025_01_23
    partition of minute_candles
        FOR VALUES FROM ('2025-01-22 21:00:00+00') TO ('2025-01-23 21:00:00+00');

alter table minute_candles_2025_01_23
    owner to postgres;

grant select on minute_candles_2025_01_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_23 to admin;

create table minute_candles_2025_01_24
    partition of minute_candles
        FOR VALUES FROM ('2025-01-23 21:00:00+00') TO ('2025-01-24 21:00:00+00');

alter table minute_candles_2025_01_24
    owner to postgres;

grant select on minute_candles_2025_01_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_24 to admin;

create table minute_candles_2025_01_25
    partition of minute_candles
        FOR VALUES FROM ('2025-01-24 21:00:00+00') TO ('2025-01-25 21:00:00+00');

alter table minute_candles_2025_01_25
    owner to postgres;

grant select on minute_candles_2025_01_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_25 to admin;

create table minute_candles_2025_01_26
    partition of minute_candles
        FOR VALUES FROM ('2025-01-25 21:00:00+00') TO ('2025-01-26 21:00:00+00');

alter table minute_candles_2025_01_26
    owner to postgres;

grant select on minute_candles_2025_01_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_26 to admin;

create table minute_candles_2025_01_27
    partition of minute_candles
        FOR VALUES FROM ('2025-01-26 21:00:00+00') TO ('2025-01-27 21:00:00+00');

alter table minute_candles_2025_01_27
    owner to postgres;

grant select on minute_candles_2025_01_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_27 to admin;

create table minute_candles_2025_01_28
    partition of minute_candles
        FOR VALUES FROM ('2025-01-27 21:00:00+00') TO ('2025-01-28 21:00:00+00');

alter table minute_candles_2025_01_28
    owner to postgres;

grant select on minute_candles_2025_01_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_28 to admin;

create table minute_candles_2025_01_29
    partition of minute_candles
        FOR VALUES FROM ('2025-01-28 21:00:00+00') TO ('2025-01-29 21:00:00+00');

alter table minute_candles_2025_01_29
    owner to postgres;

grant select on minute_candles_2025_01_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_29 to admin;

create table minute_candles_2025_01_30
    partition of minute_candles
        FOR VALUES FROM ('2025-01-29 21:00:00+00') TO ('2025-01-30 21:00:00+00');

alter table minute_candles_2025_01_30
    owner to postgres;

grant select on minute_candles_2025_01_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_30 to admin;

create table minute_candles_2025_01_31
    partition of minute_candles
        FOR VALUES FROM ('2025-01-30 21:00:00+00') TO ('2025-01-31 21:00:00+00');

alter table minute_candles_2025_01_31
    owner to postgres;

grant select on minute_candles_2025_01_31 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_01_31 to admin;

create table minute_candles_2025_02_01
    partition of minute_candles
        FOR VALUES FROM ('2025-01-31 21:00:00+00') TO ('2025-02-01 21:00:00+00');

alter table minute_candles_2025_02_01
    owner to postgres;

grant select on minute_candles_2025_02_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_01 to admin;

create table minute_candles_2025_02_02
    partition of minute_candles
        FOR VALUES FROM ('2025-02-01 21:00:00+00') TO ('2025-02-02 21:00:00+00');

alter table minute_candles_2025_02_02
    owner to postgres;

grant select on minute_candles_2025_02_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_02 to admin;

create table minute_candles_2025_02_03
    partition of minute_candles
        FOR VALUES FROM ('2025-02-02 21:00:00+00') TO ('2025-02-03 21:00:00+00');

alter table minute_candles_2025_02_03
    owner to postgres;

grant select on minute_candles_2025_02_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_03 to admin;

create table minute_candles_2025_02_04
    partition of minute_candles
        FOR VALUES FROM ('2025-02-03 21:00:00+00') TO ('2025-02-04 21:00:00+00');

alter table minute_candles_2025_02_04
    owner to postgres;

grant select on minute_candles_2025_02_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_04 to admin;

create table minute_candles_2025_02_05
    partition of minute_candles
        FOR VALUES FROM ('2025-02-04 21:00:00+00') TO ('2025-02-05 21:00:00+00');

alter table minute_candles_2025_02_05
    owner to postgres;

grant select on minute_candles_2025_02_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_05 to admin;

create table minute_candles_2025_02_06
    partition of minute_candles
        FOR VALUES FROM ('2025-02-05 21:00:00+00') TO ('2025-02-06 21:00:00+00');

alter table minute_candles_2025_02_06
    owner to postgres;

grant select on minute_candles_2025_02_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_06 to admin;

create table minute_candles_2025_02_07
    partition of minute_candles
        FOR VALUES FROM ('2025-02-06 21:00:00+00') TO ('2025-02-07 21:00:00+00');

alter table minute_candles_2025_02_07
    owner to postgres;

grant select on minute_candles_2025_02_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_07 to admin;

create table minute_candles_2025_02_08
    partition of minute_candles
        FOR VALUES FROM ('2025-02-07 21:00:00+00') TO ('2025-02-08 21:00:00+00');

alter table minute_candles_2025_02_08
    owner to postgres;

grant select on minute_candles_2025_02_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_08 to admin;

create table minute_candles_2025_02_09
    partition of minute_candles
        FOR VALUES FROM ('2025-02-08 21:00:00+00') TO ('2025-02-09 21:00:00+00');

alter table minute_candles_2025_02_09
    owner to postgres;

grant select on minute_candles_2025_02_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_09 to admin;

create table minute_candles_2025_02_10
    partition of minute_candles
        FOR VALUES FROM ('2025-02-09 21:00:00+00') TO ('2025-02-10 21:00:00+00');

alter table minute_candles_2025_02_10
    owner to postgres;

grant select on minute_candles_2025_02_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_10 to admin;

create table minute_candles_2025_02_11
    partition of minute_candles
        FOR VALUES FROM ('2025-02-10 21:00:00+00') TO ('2025-02-11 21:00:00+00');

alter table minute_candles_2025_02_11
    owner to postgres;

grant select on minute_candles_2025_02_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_11 to admin;

create table minute_candles_2025_02_12
    partition of minute_candles
        FOR VALUES FROM ('2025-02-11 21:00:00+00') TO ('2025-02-12 21:00:00+00');

alter table minute_candles_2025_02_12
    owner to postgres;

grant select on minute_candles_2025_02_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_12 to admin;

create table minute_candles_2025_02_13
    partition of minute_candles
        FOR VALUES FROM ('2025-02-12 21:00:00+00') TO ('2025-02-13 21:00:00+00');

alter table minute_candles_2025_02_13
    owner to postgres;

grant select on minute_candles_2025_02_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_13 to admin;

create table minute_candles_2025_02_14
    partition of minute_candles
        FOR VALUES FROM ('2025-02-13 21:00:00+00') TO ('2025-02-14 21:00:00+00');

alter table minute_candles_2025_02_14
    owner to postgres;

grant select on minute_candles_2025_02_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_14 to admin;

create table minute_candles_2025_02_15
    partition of minute_candles
        FOR VALUES FROM ('2025-02-14 21:00:00+00') TO ('2025-02-15 21:00:00+00');

alter table minute_candles_2025_02_15
    owner to postgres;

grant select on minute_candles_2025_02_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_15 to admin;

create table minute_candles_2025_02_16
    partition of minute_candles
        FOR VALUES FROM ('2025-02-15 21:00:00+00') TO ('2025-02-16 21:00:00+00');

alter table minute_candles_2025_02_16
    owner to postgres;

grant select on minute_candles_2025_02_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_16 to admin;

create table minute_candles_2025_02_17
    partition of minute_candles
        FOR VALUES FROM ('2025-02-16 21:00:00+00') TO ('2025-02-17 21:00:00+00');

alter table minute_candles_2025_02_17
    owner to postgres;

grant select on minute_candles_2025_02_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_17 to admin;

create table minute_candles_2025_02_18
    partition of minute_candles
        FOR VALUES FROM ('2025-02-17 21:00:00+00') TO ('2025-02-18 21:00:00+00');

alter table minute_candles_2025_02_18
    owner to postgres;

grant select on minute_candles_2025_02_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_18 to admin;

create table minute_candles_2025_02_19
    partition of minute_candles
        FOR VALUES FROM ('2025-02-18 21:00:00+00') TO ('2025-02-19 21:00:00+00');

alter table minute_candles_2025_02_19
    owner to postgres;

grant select on minute_candles_2025_02_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_19 to admin;

create table minute_candles_2025_02_20
    partition of minute_candles
        FOR VALUES FROM ('2025-02-19 21:00:00+00') TO ('2025-02-20 21:00:00+00');

alter table minute_candles_2025_02_20
    owner to postgres;

grant select on minute_candles_2025_02_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_20 to admin;

create table minute_candles_2025_02_21
    partition of minute_candles
        FOR VALUES FROM ('2025-02-20 21:00:00+00') TO ('2025-02-21 21:00:00+00');

alter table minute_candles_2025_02_21
    owner to postgres;

grant select on minute_candles_2025_02_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_21 to admin;

create table minute_candles_2025_02_22
    partition of minute_candles
        FOR VALUES FROM ('2025-02-21 21:00:00+00') TO ('2025-02-22 21:00:00+00');

alter table minute_candles_2025_02_22
    owner to postgres;

grant select on minute_candles_2025_02_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_22 to admin;

create table minute_candles_2025_02_23
    partition of minute_candles
        FOR VALUES FROM ('2025-02-22 21:00:00+00') TO ('2025-02-23 21:00:00+00');

alter table minute_candles_2025_02_23
    owner to postgres;

grant select on minute_candles_2025_02_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_23 to admin;

create table minute_candles_2025_02_24
    partition of minute_candles
        FOR VALUES FROM ('2025-02-23 21:00:00+00') TO ('2025-02-24 21:00:00+00');

alter table minute_candles_2025_02_24
    owner to postgres;

grant select on minute_candles_2025_02_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_24 to admin;

create table minute_candles_2025_02_25
    partition of minute_candles
        FOR VALUES FROM ('2025-02-24 21:00:00+00') TO ('2025-02-25 21:00:00+00');

alter table minute_candles_2025_02_25
    owner to postgres;

grant select on minute_candles_2025_02_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_25 to admin;

create table minute_candles_2025_02_26
    partition of minute_candles
        FOR VALUES FROM ('2025-02-25 21:00:00+00') TO ('2025-02-26 21:00:00+00');

alter table minute_candles_2025_02_26
    owner to postgres;

grant select on minute_candles_2025_02_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_26 to admin;

create table minute_candles_2025_02_27
    partition of minute_candles
        FOR VALUES FROM ('2025-02-26 21:00:00+00') TO ('2025-02-27 21:00:00+00');

alter table minute_candles_2025_02_27
    owner to postgres;

grant select on minute_candles_2025_02_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_27 to admin;

create table minute_candles_2025_02_28
    partition of minute_candles
        FOR VALUES FROM ('2025-02-27 21:00:00+00') TO ('2025-02-28 21:00:00+00');

alter table minute_candles_2025_02_28
    owner to postgres;

grant select on minute_candles_2025_02_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_02_28 to admin;

create table minute_candles_2025_03_01
    partition of minute_candles
        FOR VALUES FROM ('2025-02-28 21:00:00+00') TO ('2025-03-01 21:00:00+00');

alter table minute_candles_2025_03_01
    owner to postgres;

grant select on minute_candles_2025_03_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_01 to admin;

create table minute_candles_2025_03_02
    partition of minute_candles
        FOR VALUES FROM ('2025-03-01 21:00:00+00') TO ('2025-03-02 21:00:00+00');

alter table minute_candles_2025_03_02
    owner to postgres;

grant select on minute_candles_2025_03_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_02 to admin;

create table minute_candles_2025_03_03
    partition of minute_candles
        FOR VALUES FROM ('2025-03-02 21:00:00+00') TO ('2025-03-03 21:00:00+00');

alter table minute_candles_2025_03_03
    owner to postgres;

grant select on minute_candles_2025_03_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_03 to admin;

create table minute_candles_2025_03_04
    partition of minute_candles
        FOR VALUES FROM ('2025-03-03 21:00:00+00') TO ('2025-03-04 21:00:00+00');

alter table minute_candles_2025_03_04
    owner to postgres;

grant select on minute_candles_2025_03_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_04 to admin;

create table minute_candles_2025_03_05
    partition of minute_candles
        FOR VALUES FROM ('2025-03-04 21:00:00+00') TO ('2025-03-05 21:00:00+00');

alter table minute_candles_2025_03_05
    owner to postgres;

grant select on minute_candles_2025_03_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_05 to admin;

create table minute_candles_2025_03_06
    partition of minute_candles
        FOR VALUES FROM ('2025-03-05 21:00:00+00') TO ('2025-03-06 21:00:00+00');

alter table minute_candles_2025_03_06
    owner to postgres;

grant select on minute_candles_2025_03_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_06 to admin;

create table minute_candles_2025_03_07
    partition of minute_candles
        FOR VALUES FROM ('2025-03-06 21:00:00+00') TO ('2025-03-07 21:00:00+00');

alter table minute_candles_2025_03_07
    owner to postgres;

grant select on minute_candles_2025_03_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_07 to admin;

create table minute_candles_2025_03_08
    partition of minute_candles
        FOR VALUES FROM ('2025-03-07 21:00:00+00') TO ('2025-03-08 21:00:00+00');

alter table minute_candles_2025_03_08
    owner to postgres;

grant select on minute_candles_2025_03_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_08 to admin;

create table minute_candles_2025_03_09
    partition of minute_candles
        FOR VALUES FROM ('2025-03-08 21:00:00+00') TO ('2025-03-09 21:00:00+00');

alter table minute_candles_2025_03_09
    owner to postgres;

grant select on minute_candles_2025_03_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_09 to admin;

create table minute_candles_2025_03_10
    partition of minute_candles
        FOR VALUES FROM ('2025-03-09 21:00:00+00') TO ('2025-03-10 21:00:00+00');

alter table minute_candles_2025_03_10
    owner to postgres;

grant select on minute_candles_2025_03_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_10 to admin;

create table minute_candles_2025_03_11
    partition of minute_candles
        FOR VALUES FROM ('2025-03-10 21:00:00+00') TO ('2025-03-11 21:00:00+00');

alter table minute_candles_2025_03_11
    owner to postgres;

grant select on minute_candles_2025_03_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_11 to admin;

create table minute_candles_2025_03_12
    partition of minute_candles
        FOR VALUES FROM ('2025-03-11 21:00:00+00') TO ('2025-03-12 21:00:00+00');

alter table minute_candles_2025_03_12
    owner to postgres;

grant select on minute_candles_2025_03_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_12 to admin;

create table minute_candles_2025_03_13
    partition of minute_candles
        FOR VALUES FROM ('2025-03-12 21:00:00+00') TO ('2025-03-13 21:00:00+00');

alter table minute_candles_2025_03_13
    owner to postgres;

grant select on minute_candles_2025_03_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_13 to admin;

create table minute_candles_2025_03_14
    partition of minute_candles
        FOR VALUES FROM ('2025-03-13 21:00:00+00') TO ('2025-03-14 21:00:00+00');

alter table minute_candles_2025_03_14
    owner to postgres;

grant select on minute_candles_2025_03_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_14 to admin;

create table minute_candles_2025_03_15
    partition of minute_candles
        FOR VALUES FROM ('2025-03-14 21:00:00+00') TO ('2025-03-15 21:00:00+00');

alter table minute_candles_2025_03_15
    owner to postgres;

grant select on minute_candles_2025_03_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_15 to admin;

create table minute_candles_2025_03_16
    partition of minute_candles
        FOR VALUES FROM ('2025-03-15 21:00:00+00') TO ('2025-03-16 21:00:00+00');

alter table minute_candles_2025_03_16
    owner to postgres;

grant select on minute_candles_2025_03_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_16 to admin;

create table minute_candles_2025_03_17
    partition of minute_candles
        FOR VALUES FROM ('2025-03-16 21:00:00+00') TO ('2025-03-17 21:00:00+00');

alter table minute_candles_2025_03_17
    owner to postgres;

grant select on minute_candles_2025_03_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_17 to admin;

create table minute_candles_2025_03_18
    partition of minute_candles
        FOR VALUES FROM ('2025-03-17 21:00:00+00') TO ('2025-03-18 21:00:00+00');

alter table minute_candles_2025_03_18
    owner to postgres;

grant select on minute_candles_2025_03_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_18 to admin;

create table minute_candles_2025_03_19
    partition of minute_candles
        FOR VALUES FROM ('2025-03-18 21:00:00+00') TO ('2025-03-19 21:00:00+00');

alter table minute_candles_2025_03_19
    owner to postgres;

grant select on minute_candles_2025_03_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_19 to admin;

create table minute_candles_2025_03_20
    partition of minute_candles
        FOR VALUES FROM ('2025-03-19 21:00:00+00') TO ('2025-03-20 21:00:00+00');

alter table minute_candles_2025_03_20
    owner to postgres;

grant select on minute_candles_2025_03_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_20 to admin;

create table minute_candles_2025_03_21
    partition of minute_candles
        FOR VALUES FROM ('2025-03-20 21:00:00+00') TO ('2025-03-21 21:00:00+00');

alter table minute_candles_2025_03_21
    owner to postgres;

grant select on minute_candles_2025_03_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_21 to admin;

create table minute_candles_2025_03_22
    partition of minute_candles
        FOR VALUES FROM ('2025-03-21 21:00:00+00') TO ('2025-03-22 21:00:00+00');

alter table minute_candles_2025_03_22
    owner to postgres;

grant select on minute_candles_2025_03_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_22 to admin;

create table minute_candles_2025_03_23
    partition of minute_candles
        FOR VALUES FROM ('2025-03-22 21:00:00+00') TO ('2025-03-23 21:00:00+00');

alter table minute_candles_2025_03_23
    owner to postgres;

grant select on minute_candles_2025_03_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_23 to admin;

create table minute_candles_2025_03_24
    partition of minute_candles
        FOR VALUES FROM ('2025-03-23 21:00:00+00') TO ('2025-03-24 21:00:00+00');

alter table minute_candles_2025_03_24
    owner to postgres;

grant select on minute_candles_2025_03_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_24 to admin;

create table minute_candles_2025_03_25
    partition of minute_candles
        FOR VALUES FROM ('2025-03-24 21:00:00+00') TO ('2025-03-25 21:00:00+00');

alter table minute_candles_2025_03_25
    owner to postgres;

grant select on minute_candles_2025_03_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_25 to admin;

create table minute_candles_2025_03_26
    partition of minute_candles
        FOR VALUES FROM ('2025-03-25 21:00:00+00') TO ('2025-03-26 21:00:00+00');

alter table minute_candles_2025_03_26
    owner to postgres;

grant select on minute_candles_2025_03_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_26 to admin;

create table minute_candles_2025_03_27
    partition of minute_candles
        FOR VALUES FROM ('2025-03-26 21:00:00+00') TO ('2025-03-27 21:00:00+00');

alter table minute_candles_2025_03_27
    owner to postgres;

grant select on minute_candles_2025_03_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_27 to admin;

create table minute_candles_2025_03_28
    partition of minute_candles
        FOR VALUES FROM ('2025-03-27 21:00:00+00') TO ('2025-03-28 21:00:00+00');

alter table minute_candles_2025_03_28
    owner to postgres;

grant select on minute_candles_2025_03_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_28 to admin;

create table minute_candles_2025_03_29
    partition of minute_candles
        FOR VALUES FROM ('2025-03-28 21:00:00+00') TO ('2025-03-29 21:00:00+00');

alter table minute_candles_2025_03_29
    owner to postgres;

grant select on minute_candles_2025_03_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_29 to admin;

create table minute_candles_2025_03_30
    partition of minute_candles
        FOR VALUES FROM ('2025-03-29 21:00:00+00') TO ('2025-03-30 21:00:00+00');

alter table minute_candles_2025_03_30
    owner to postgres;

grant select on minute_candles_2025_03_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_30 to admin;

create table minute_candles_2025_03_31
    partition of minute_candles
        FOR VALUES FROM ('2025-03-30 21:00:00+00') TO ('2025-03-31 21:00:00+00');

alter table minute_candles_2025_03_31
    owner to postgres;

grant select on minute_candles_2025_03_31 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_03_31 to admin;

create table minute_candles_2025_04_01
    partition of minute_candles
        FOR VALUES FROM ('2025-03-31 21:00:00+00') TO ('2025-04-01 21:00:00+00');

alter table minute_candles_2025_04_01
    owner to postgres;

grant select on minute_candles_2025_04_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_01 to admin;

create table minute_candles_2025_04_02
    partition of minute_candles
        FOR VALUES FROM ('2025-04-01 21:00:00+00') TO ('2025-04-02 21:00:00+00');

alter table minute_candles_2025_04_02
    owner to postgres;

grant select on minute_candles_2025_04_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_02 to admin;

create table minute_candles_2025_04_03
    partition of minute_candles
        FOR VALUES FROM ('2025-04-02 21:00:00+00') TO ('2025-04-03 21:00:00+00');

alter table minute_candles_2025_04_03
    owner to postgres;

grant select on minute_candles_2025_04_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_03 to admin;

create table minute_candles_2025_04_04
    partition of minute_candles
        FOR VALUES FROM ('2025-04-03 21:00:00+00') TO ('2025-04-04 21:00:00+00');

alter table minute_candles_2025_04_04
    owner to postgres;

grant select on minute_candles_2025_04_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_04 to admin;

create table minute_candles_2025_04_05
    partition of minute_candles
        FOR VALUES FROM ('2025-04-04 21:00:00+00') TO ('2025-04-05 21:00:00+00');

alter table minute_candles_2025_04_05
    owner to postgres;

grant select on minute_candles_2025_04_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_05 to admin;

create table minute_candles_2025_04_06
    partition of minute_candles
        FOR VALUES FROM ('2025-04-05 21:00:00+00') TO ('2025-04-06 21:00:00+00');

alter table minute_candles_2025_04_06
    owner to postgres;

grant select on minute_candles_2025_04_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_06 to admin;

create table minute_candles_2025_04_07
    partition of minute_candles
        FOR VALUES FROM ('2025-04-06 21:00:00+00') TO ('2025-04-07 21:00:00+00');

alter table minute_candles_2025_04_07
    owner to postgres;

grant select on minute_candles_2025_04_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_07 to admin;

create table minute_candles_2025_04_08
    partition of minute_candles
        FOR VALUES FROM ('2025-04-07 21:00:00+00') TO ('2025-04-08 21:00:00+00');

alter table minute_candles_2025_04_08
    owner to postgres;

grant select on minute_candles_2025_04_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_08 to admin;

create table minute_candles_2025_04_09
    partition of minute_candles
        FOR VALUES FROM ('2025-04-08 21:00:00+00') TO ('2025-04-09 21:00:00+00');

alter table minute_candles_2025_04_09
    owner to postgres;

grant select on minute_candles_2025_04_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_09 to admin;

create table minute_candles_2025_04_10
    partition of minute_candles
        FOR VALUES FROM ('2025-04-09 21:00:00+00') TO ('2025-04-10 21:00:00+00');

alter table minute_candles_2025_04_10
    owner to postgres;

grant select on minute_candles_2025_04_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_10 to admin;

create table minute_candles_2025_04_11
    partition of minute_candles
        FOR VALUES FROM ('2025-04-10 21:00:00+00') TO ('2025-04-11 21:00:00+00');

alter table minute_candles_2025_04_11
    owner to postgres;

grant select on minute_candles_2025_04_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_11 to admin;

create table minute_candles_2025_04_12
    partition of minute_candles
        FOR VALUES FROM ('2025-04-11 21:00:00+00') TO ('2025-04-12 21:00:00+00');

alter table minute_candles_2025_04_12
    owner to postgres;

grant select on minute_candles_2025_04_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_12 to admin;

create table minute_candles_2025_04_13
    partition of minute_candles
        FOR VALUES FROM ('2025-04-12 21:00:00+00') TO ('2025-04-13 21:00:00+00');

alter table minute_candles_2025_04_13
    owner to postgres;

grant select on minute_candles_2025_04_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_13 to admin;

create table minute_candles_2025_04_14
    partition of minute_candles
        FOR VALUES FROM ('2025-04-13 21:00:00+00') TO ('2025-04-14 21:00:00+00');

alter table minute_candles_2025_04_14
    owner to postgres;

grant select on minute_candles_2025_04_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_14 to admin;

create table minute_candles_2025_04_15
    partition of minute_candles
        FOR VALUES FROM ('2025-04-14 21:00:00+00') TO ('2025-04-15 21:00:00+00');

alter table minute_candles_2025_04_15
    owner to postgres;

grant select on minute_candles_2025_04_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_15 to admin;

create table minute_candles_2025_04_16
    partition of minute_candles
        FOR VALUES FROM ('2025-04-15 21:00:00+00') TO ('2025-04-16 21:00:00+00');

alter table minute_candles_2025_04_16
    owner to postgres;

grant select on minute_candles_2025_04_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_16 to admin;

create table minute_candles_2025_04_17
    partition of minute_candles
        FOR VALUES FROM ('2025-04-16 21:00:00+00') TO ('2025-04-17 21:00:00+00');

alter table minute_candles_2025_04_17
    owner to postgres;

grant select on minute_candles_2025_04_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_17 to admin;

create table minute_candles_2025_04_18
    partition of minute_candles
        FOR VALUES FROM ('2025-04-17 21:00:00+00') TO ('2025-04-18 21:00:00+00');

alter table minute_candles_2025_04_18
    owner to postgres;

grant select on minute_candles_2025_04_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_18 to admin;

create table minute_candles_2025_04_19
    partition of minute_candles
        FOR VALUES FROM ('2025-04-18 21:00:00+00') TO ('2025-04-19 21:00:00+00');

alter table minute_candles_2025_04_19
    owner to postgres;

grant select on minute_candles_2025_04_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_19 to admin;

create table minute_candles_2025_04_20
    partition of minute_candles
        FOR VALUES FROM ('2025-04-19 21:00:00+00') TO ('2025-04-20 21:00:00+00');

alter table minute_candles_2025_04_20
    owner to postgres;

grant select on minute_candles_2025_04_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_20 to admin;

create table minute_candles_2025_04_21
    partition of minute_candles
        FOR VALUES FROM ('2025-04-20 21:00:00+00') TO ('2025-04-21 21:00:00+00');

alter table minute_candles_2025_04_21
    owner to postgres;

grant select on minute_candles_2025_04_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_21 to admin;

create table minute_candles_2025_04_22
    partition of minute_candles
        FOR VALUES FROM ('2025-04-21 21:00:00+00') TO ('2025-04-22 21:00:00+00');

alter table minute_candles_2025_04_22
    owner to postgres;

grant select on minute_candles_2025_04_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_22 to admin;

create table minute_candles_2025_04_23
    partition of minute_candles
        FOR VALUES FROM ('2025-04-22 21:00:00+00') TO ('2025-04-23 21:00:00+00');

alter table minute_candles_2025_04_23
    owner to postgres;

grant select on minute_candles_2025_04_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_23 to admin;

create table minute_candles_2025_04_24
    partition of minute_candles
        FOR VALUES FROM ('2025-04-23 21:00:00+00') TO ('2025-04-24 21:00:00+00');

alter table minute_candles_2025_04_24
    owner to postgres;

grant select on minute_candles_2025_04_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_24 to admin;

create table minute_candles_2025_04_25
    partition of minute_candles
        FOR VALUES FROM ('2025-04-24 21:00:00+00') TO ('2025-04-25 21:00:00+00');

alter table minute_candles_2025_04_25
    owner to postgres;

grant select on minute_candles_2025_04_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_25 to admin;

create table minute_candles_2025_04_26
    partition of minute_candles
        FOR VALUES FROM ('2025-04-25 21:00:00+00') TO ('2025-04-26 21:00:00+00');

alter table minute_candles_2025_04_26
    owner to postgres;

grant select on minute_candles_2025_04_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_26 to admin;

create table minute_candles_2025_04_27
    partition of minute_candles
        FOR VALUES FROM ('2025-04-26 21:00:00+00') TO ('2025-04-27 21:00:00+00');

alter table minute_candles_2025_04_27
    owner to postgres;

grant select on minute_candles_2025_04_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_27 to admin;

create table minute_candles_2025_04_28
    partition of minute_candles
        FOR VALUES FROM ('2025-04-27 21:00:00+00') TO ('2025-04-28 21:00:00+00');

alter table minute_candles_2025_04_28
    owner to postgres;

grant select on minute_candles_2025_04_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_28 to admin;

create table minute_candles_2025_04_29
    partition of minute_candles
        FOR VALUES FROM ('2025-04-28 21:00:00+00') TO ('2025-04-29 21:00:00+00');

alter table minute_candles_2025_04_29
    owner to postgres;

grant select on minute_candles_2025_04_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_29 to admin;

create table minute_candles_2025_04_30
    partition of minute_candles
        FOR VALUES FROM ('2025-04-29 21:00:00+00') TO ('2025-04-30 21:00:00+00');

alter table minute_candles_2025_04_30
    owner to postgres;

grant select on minute_candles_2025_04_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_04_30 to admin;

create table minute_candles_2025_05_01
    partition of minute_candles
        FOR VALUES FROM ('2025-04-30 21:00:00+00') TO ('2025-05-01 21:00:00+00');

alter table minute_candles_2025_05_01
    owner to postgres;

grant select on minute_candles_2025_05_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_01 to admin;

create table minute_candles_2025_05_02
    partition of minute_candles
        FOR VALUES FROM ('2025-05-01 21:00:00+00') TO ('2025-05-02 21:00:00+00');

alter table minute_candles_2025_05_02
    owner to postgres;

grant select on minute_candles_2025_05_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_02 to admin;

create table minute_candles_2025_05_03
    partition of minute_candles
        FOR VALUES FROM ('2025-05-02 21:00:00+00') TO ('2025-05-03 21:00:00+00');

alter table minute_candles_2025_05_03
    owner to postgres;

grant select on minute_candles_2025_05_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_03 to admin;

create table minute_candles_2025_05_04
    partition of minute_candles
        FOR VALUES FROM ('2025-05-03 21:00:00+00') TO ('2025-05-04 21:00:00+00');

alter table minute_candles_2025_05_04
    owner to postgres;

grant select on minute_candles_2025_05_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_04 to admin;

create table minute_candles_2025_05_05
    partition of minute_candles
        FOR VALUES FROM ('2025-05-04 21:00:00+00') TO ('2025-05-05 21:00:00+00');

alter table minute_candles_2025_05_05
    owner to postgres;

grant select on minute_candles_2025_05_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_05 to admin;

create table minute_candles_2025_05_06
    partition of minute_candles
        FOR VALUES FROM ('2025-05-05 21:00:00+00') TO ('2025-05-06 21:00:00+00');

alter table minute_candles_2025_05_06
    owner to postgres;

grant select on minute_candles_2025_05_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_06 to admin;

create table minute_candles_2025_05_07
    partition of minute_candles
        FOR VALUES FROM ('2025-05-06 21:00:00+00') TO ('2025-05-07 21:00:00+00');

alter table minute_candles_2025_05_07
    owner to postgres;

grant select on minute_candles_2025_05_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_07 to admin;

create table minute_candles_2025_05_08
    partition of minute_candles
        FOR VALUES FROM ('2025-05-07 21:00:00+00') TO ('2025-05-08 21:00:00+00');

alter table minute_candles_2025_05_08
    owner to postgres;

grant select on minute_candles_2025_05_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_08 to admin;

create table minute_candles_2025_05_09
    partition of minute_candles
        FOR VALUES FROM ('2025-05-08 21:00:00+00') TO ('2025-05-09 21:00:00+00');

alter table minute_candles_2025_05_09
    owner to postgres;

grant select on minute_candles_2025_05_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_09 to admin;

create table minute_candles_2025_05_10
    partition of minute_candles
        FOR VALUES FROM ('2025-05-09 21:00:00+00') TO ('2025-05-10 21:00:00+00');

alter table minute_candles_2025_05_10
    owner to postgres;

grant select on minute_candles_2025_05_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_10 to admin;

create table minute_candles_2025_05_11
    partition of minute_candles
        FOR VALUES FROM ('2025-05-10 21:00:00+00') TO ('2025-05-11 21:00:00+00');

alter table minute_candles_2025_05_11
    owner to postgres;

grant select on minute_candles_2025_05_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_11 to admin;

create table minute_candles_2025_05_12
    partition of minute_candles
        FOR VALUES FROM ('2025-05-11 21:00:00+00') TO ('2025-05-12 21:00:00+00');

alter table minute_candles_2025_05_12
    owner to postgres;

grant select on minute_candles_2025_05_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_12 to admin;

create table minute_candles_2025_05_13
    partition of minute_candles
        FOR VALUES FROM ('2025-05-12 21:00:00+00') TO ('2025-05-13 21:00:00+00');

alter table minute_candles_2025_05_13
    owner to postgres;

grant select on minute_candles_2025_05_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_13 to admin;

create table minute_candles_2025_05_14
    partition of minute_candles
        FOR VALUES FROM ('2025-05-13 21:00:00+00') TO ('2025-05-14 21:00:00+00');

alter table minute_candles_2025_05_14
    owner to postgres;

grant select on minute_candles_2025_05_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_14 to admin;

create table minute_candles_2025_05_15
    partition of minute_candles
        FOR VALUES FROM ('2025-05-14 21:00:00+00') TO ('2025-05-15 21:00:00+00');

alter table minute_candles_2025_05_15
    owner to postgres;

grant select on minute_candles_2025_05_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_15 to admin;

create table minute_candles_2025_05_16
    partition of minute_candles
        FOR VALUES FROM ('2025-05-15 21:00:00+00') TO ('2025-05-16 21:00:00+00');

alter table minute_candles_2025_05_16
    owner to postgres;

grant select on minute_candles_2025_05_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_16 to admin;

create table minute_candles_2025_05_17
    partition of minute_candles
        FOR VALUES FROM ('2025-05-16 21:00:00+00') TO ('2025-05-17 21:00:00+00');

alter table minute_candles_2025_05_17
    owner to postgres;

grant select on minute_candles_2025_05_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_17 to admin;

create table minute_candles_2025_05_18
    partition of minute_candles
        FOR VALUES FROM ('2025-05-17 21:00:00+00') TO ('2025-05-18 21:00:00+00');

alter table minute_candles_2025_05_18
    owner to postgres;

grant select on minute_candles_2025_05_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_18 to admin;

create table minute_candles_2025_05_19
    partition of minute_candles
        FOR VALUES FROM ('2025-05-18 21:00:00+00') TO ('2025-05-19 21:00:00+00');

alter table minute_candles_2025_05_19
    owner to postgres;

grant select on minute_candles_2025_05_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_19 to admin;

create table minute_candles_2025_05_20
    partition of minute_candles
        FOR VALUES FROM ('2025-05-19 21:00:00+00') TO ('2025-05-20 21:00:00+00');

alter table minute_candles_2025_05_20
    owner to postgres;

grant select on minute_candles_2025_05_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_20 to admin;

create table minute_candles_2025_05_21
    partition of minute_candles
        FOR VALUES FROM ('2025-05-20 21:00:00+00') TO ('2025-05-21 21:00:00+00');

alter table minute_candles_2025_05_21
    owner to postgres;

grant select on minute_candles_2025_05_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_21 to admin;

create table minute_candles_2025_05_22
    partition of minute_candles
        FOR VALUES FROM ('2025-05-21 21:00:00+00') TO ('2025-05-22 21:00:00+00');

alter table minute_candles_2025_05_22
    owner to postgres;

grant select on minute_candles_2025_05_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_22 to admin;

create table minute_candles_2025_05_23
    partition of minute_candles
        FOR VALUES FROM ('2025-05-22 21:00:00+00') TO ('2025-05-23 21:00:00+00');

alter table minute_candles_2025_05_23
    owner to postgres;

grant select on minute_candles_2025_05_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_23 to admin;

create table minute_candles_2025_05_24
    partition of minute_candles
        FOR VALUES FROM ('2025-05-23 21:00:00+00') TO ('2025-05-24 21:00:00+00');

alter table minute_candles_2025_05_24
    owner to postgres;

grant select on minute_candles_2025_05_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_24 to admin;

create table minute_candles_2025_05_25
    partition of minute_candles
        FOR VALUES FROM ('2025-05-24 21:00:00+00') TO ('2025-05-25 21:00:00+00');

alter table minute_candles_2025_05_25
    owner to postgres;

grant select on minute_candles_2025_05_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_25 to admin;

create table minute_candles_2025_05_26
    partition of minute_candles
        FOR VALUES FROM ('2025-05-25 21:00:00+00') TO ('2025-05-26 21:00:00+00');

alter table minute_candles_2025_05_26
    owner to postgres;

grant select on minute_candles_2025_05_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_26 to admin;

create table minute_candles_2025_05_27
    partition of minute_candles
        FOR VALUES FROM ('2025-05-26 21:00:00+00') TO ('2025-05-27 21:00:00+00');

alter table minute_candles_2025_05_27
    owner to postgres;

grant select on minute_candles_2025_05_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_27 to admin;

create table minute_candles_2025_05_28
    partition of minute_candles
        FOR VALUES FROM ('2025-05-27 21:00:00+00') TO ('2025-05-28 21:00:00+00');

alter table minute_candles_2025_05_28
    owner to postgres;

grant select on minute_candles_2025_05_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_28 to admin;

create table minute_candles_2025_05_29
    partition of minute_candles
        FOR VALUES FROM ('2025-05-28 21:00:00+00') TO ('2025-05-29 21:00:00+00');

alter table minute_candles_2025_05_29
    owner to postgres;

grant select on minute_candles_2025_05_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_29 to admin;

create table minute_candles_2025_05_30
    partition of minute_candles
        FOR VALUES FROM ('2025-05-29 21:00:00+00') TO ('2025-05-30 21:00:00+00');

alter table minute_candles_2025_05_30
    owner to postgres;

grant select on minute_candles_2025_05_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_30 to admin;

create table minute_candles_2025_05_31
    partition of minute_candles
        FOR VALUES FROM ('2025-05-30 21:00:00+00') TO ('2025-05-31 21:00:00+00');

alter table minute_candles_2025_05_31
    owner to postgres;

grant select on minute_candles_2025_05_31 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_05_31 to admin;

create table minute_candles_2025_06_01
    partition of minute_candles
        FOR VALUES FROM ('2025-05-31 21:00:00+00') TO ('2025-06-01 21:00:00+00');

alter table minute_candles_2025_06_01
    owner to postgres;

grant select on minute_candles_2025_06_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_01 to admin;

create table minute_candles_2025_06_02
    partition of minute_candles
        FOR VALUES FROM ('2025-06-01 21:00:00+00') TO ('2025-06-02 21:00:00+00');

alter table minute_candles_2025_06_02
    owner to postgres;

grant select on minute_candles_2025_06_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_02 to admin;

create table minute_candles_2025_06_03
    partition of minute_candles
        FOR VALUES FROM ('2025-06-02 21:00:00+00') TO ('2025-06-03 21:00:00+00');

alter table minute_candles_2025_06_03
    owner to postgres;

grant select on minute_candles_2025_06_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_03 to admin;

create table minute_candles_2025_06_04
    partition of minute_candles
        FOR VALUES FROM ('2025-06-03 21:00:00+00') TO ('2025-06-04 21:00:00+00');

alter table minute_candles_2025_06_04
    owner to postgres;

grant select on minute_candles_2025_06_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_04 to admin;

create table minute_candles_2025_06_05
    partition of minute_candles
        FOR VALUES FROM ('2025-06-04 21:00:00+00') TO ('2025-06-05 21:00:00+00');

alter table minute_candles_2025_06_05
    owner to postgres;

grant select on minute_candles_2025_06_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_05 to admin;

create table minute_candles_2025_06_06
    partition of minute_candles
        FOR VALUES FROM ('2025-06-05 21:00:00+00') TO ('2025-06-06 21:00:00+00');

alter table minute_candles_2025_06_06
    owner to postgres;

grant select on minute_candles_2025_06_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_06 to admin;

create table minute_candles_2025_06_07
    partition of minute_candles
        FOR VALUES FROM ('2025-06-06 21:00:00+00') TO ('2025-06-07 21:00:00+00');

alter table minute_candles_2025_06_07
    owner to postgres;

grant select on minute_candles_2025_06_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_07 to admin;

create table minute_candles_2025_06_08
    partition of minute_candles
        FOR VALUES FROM ('2025-06-07 21:00:00+00') TO ('2025-06-08 21:00:00+00');

alter table minute_candles_2025_06_08
    owner to postgres;

grant select on minute_candles_2025_06_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_08 to admin;

create table minute_candles_2025_06_09
    partition of minute_candles
        FOR VALUES FROM ('2025-06-08 21:00:00+00') TO ('2025-06-09 21:00:00+00');

alter table minute_candles_2025_06_09
    owner to postgres;

grant select on minute_candles_2025_06_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_09 to admin;

create table minute_candles_2025_06_10
    partition of minute_candles
        FOR VALUES FROM ('2025-06-09 21:00:00+00') TO ('2025-06-10 21:00:00+00');

alter table minute_candles_2025_06_10
    owner to postgres;

grant select on minute_candles_2025_06_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_10 to admin;

create table minute_candles_2025_06_11
    partition of minute_candles
        FOR VALUES FROM ('2025-06-10 21:00:00+00') TO ('2025-06-11 21:00:00+00');

alter table minute_candles_2025_06_11
    owner to postgres;

grant select on minute_candles_2025_06_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_11 to admin;

create table minute_candles_2025_06_12
    partition of minute_candles
        FOR VALUES FROM ('2025-06-11 21:00:00+00') TO ('2025-06-12 21:00:00+00');

alter table minute_candles_2025_06_12
    owner to postgres;

grant select on minute_candles_2025_06_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_12 to admin;

create table minute_candles_2025_06_13
    partition of minute_candles
        FOR VALUES FROM ('2025-06-12 21:00:00+00') TO ('2025-06-13 21:00:00+00');

alter table minute_candles_2025_06_13
    owner to postgres;

grant select on minute_candles_2025_06_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_13 to admin;

create table minute_candles_2025_06_14
    partition of minute_candles
        FOR VALUES FROM ('2025-06-13 21:00:00+00') TO ('2025-06-14 21:00:00+00');

alter table minute_candles_2025_06_14
    owner to postgres;

grant select on minute_candles_2025_06_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_14 to admin;

create table minute_candles_2025_06_15
    partition of minute_candles
        FOR VALUES FROM ('2025-06-14 21:00:00+00') TO ('2025-06-15 21:00:00+00');

alter table minute_candles_2025_06_15
    owner to postgres;

grant select on minute_candles_2025_06_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_15 to admin;

create table minute_candles_2025_06_16
    partition of minute_candles
        FOR VALUES FROM ('2025-06-15 21:00:00+00') TO ('2025-06-16 21:00:00+00');

alter table minute_candles_2025_06_16
    owner to postgres;

grant select on minute_candles_2025_06_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_16 to admin;

create table minute_candles_2025_06_17
    partition of minute_candles
        FOR VALUES FROM ('2025-06-16 21:00:00+00') TO ('2025-06-17 21:00:00+00');

alter table minute_candles_2025_06_17
    owner to postgres;

grant select on minute_candles_2025_06_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_17 to admin;

create table minute_candles_2025_06_18
    partition of minute_candles
        FOR VALUES FROM ('2025-06-17 21:00:00+00') TO ('2025-06-18 21:00:00+00');

alter table minute_candles_2025_06_18
    owner to postgres;

grant select on minute_candles_2025_06_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_18 to admin;

create table minute_candles_2025_06_19
    partition of minute_candles
        FOR VALUES FROM ('2025-06-18 21:00:00+00') TO ('2025-06-19 21:00:00+00');

alter table minute_candles_2025_06_19
    owner to postgres;

grant select on minute_candles_2025_06_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_19 to admin;

create table minute_candles_2025_06_20
    partition of minute_candles
        FOR VALUES FROM ('2025-06-19 21:00:00+00') TO ('2025-06-20 21:00:00+00');

alter table minute_candles_2025_06_20
    owner to postgres;

grant select on minute_candles_2025_06_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_20 to admin;

create table minute_candles_2025_06_21
    partition of minute_candles
        FOR VALUES FROM ('2025-06-20 21:00:00+00') TO ('2025-06-21 21:00:00+00');

alter table minute_candles_2025_06_21
    owner to postgres;

grant select on minute_candles_2025_06_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_21 to admin;

create table minute_candles_2025_06_22
    partition of minute_candles
        FOR VALUES FROM ('2025-06-21 21:00:00+00') TO ('2025-06-22 21:00:00+00');

alter table minute_candles_2025_06_22
    owner to postgres;

grant select on minute_candles_2025_06_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_22 to admin;

create table minute_candles_2025_06_23
    partition of minute_candles
        FOR VALUES FROM ('2025-06-22 21:00:00+00') TO ('2025-06-23 21:00:00+00');

alter table minute_candles_2025_06_23
    owner to postgres;

grant select on minute_candles_2025_06_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_23 to admin;

create table minute_candles_2025_06_24
    partition of minute_candles
        FOR VALUES FROM ('2025-06-23 21:00:00+00') TO ('2025-06-24 21:00:00+00');

alter table minute_candles_2025_06_24
    owner to postgres;

grant select on minute_candles_2025_06_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_24 to admin;

create table minute_candles_2025_06_25
    partition of minute_candles
        FOR VALUES FROM ('2025-06-24 21:00:00+00') TO ('2025-06-25 21:00:00+00');

alter table minute_candles_2025_06_25
    owner to postgres;

grant select on minute_candles_2025_06_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_25 to admin;

create table minute_candles_2025_06_26
    partition of minute_candles
        FOR VALUES FROM ('2025-06-25 21:00:00+00') TO ('2025-06-26 21:00:00+00');

alter table minute_candles_2025_06_26
    owner to postgres;

grant select on minute_candles_2025_06_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_26 to admin;

create table minute_candles_2025_06_27
    partition of minute_candles
        FOR VALUES FROM ('2025-06-26 21:00:00+00') TO ('2025-06-27 21:00:00+00');

alter table minute_candles_2025_06_27
    owner to postgres;

grant select on minute_candles_2025_06_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_27 to admin;

create table minute_candles_2025_06_28
    partition of minute_candles
        FOR VALUES FROM ('2025-06-27 21:00:00+00') TO ('2025-06-28 21:00:00+00');

alter table minute_candles_2025_06_28
    owner to postgres;

grant select on minute_candles_2025_06_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_28 to admin;

create table minute_candles_2025_06_29
    partition of minute_candles
        FOR VALUES FROM ('2025-06-28 21:00:00+00') TO ('2025-06-29 21:00:00+00');

alter table minute_candles_2025_06_29
    owner to postgres;

grant select on minute_candles_2025_06_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_29 to admin;

create table minute_candles_2025_06_30
    partition of minute_candles
        FOR VALUES FROM ('2025-06-29 21:00:00+00') TO ('2025-06-30 21:00:00+00');

alter table minute_candles_2025_06_30
    owner to postgres;

grant select on minute_candles_2025_06_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_06_30 to admin;

create table minute_candles_2025_07_01
    partition of minute_candles
        FOR VALUES FROM ('2025-06-30 21:00:00+00') TO ('2025-07-01 21:00:00+00');

alter table minute_candles_2025_07_01
    owner to postgres;

grant select on minute_candles_2025_07_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_01 to admin;

create table minute_candles_2025_07_02
    partition of minute_candles
        FOR VALUES FROM ('2025-07-01 21:00:00+00') TO ('2025-07-02 21:00:00+00');

alter table minute_candles_2025_07_02
    owner to postgres;

grant select on minute_candles_2025_07_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_02 to admin;

create table minute_candles_2025_07_03
    partition of minute_candles
        FOR VALUES FROM ('2025-07-02 21:00:00+00') TO ('2025-07-03 21:00:00+00');

alter table minute_candles_2025_07_03
    owner to postgres;

grant select on minute_candles_2025_07_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_03 to admin;

create table minute_candles_2025_07_04
    partition of minute_candles
        FOR VALUES FROM ('2025-07-03 21:00:00+00') TO ('2025-07-04 21:00:00+00');

alter table minute_candles_2025_07_04
    owner to postgres;

grant select on minute_candles_2025_07_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_04 to admin;

create table minute_candles_2025_07_05
    partition of minute_candles
        FOR VALUES FROM ('2025-07-04 21:00:00+00') TO ('2025-07-05 21:00:00+00');

alter table minute_candles_2025_07_05
    owner to postgres;

grant select on minute_candles_2025_07_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_05 to admin;

create table minute_candles_2025_07_06
    partition of minute_candles
        FOR VALUES FROM ('2025-07-05 21:00:00+00') TO ('2025-07-06 21:00:00+00');

alter table minute_candles_2025_07_06
    owner to postgres;

grant select on minute_candles_2025_07_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_06 to admin;

create table minute_candles_2025_07_07
    partition of minute_candles
        FOR VALUES FROM ('2025-07-06 21:00:00+00') TO ('2025-07-07 21:00:00+00');

alter table minute_candles_2025_07_07
    owner to postgres;

grant select on minute_candles_2025_07_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_07 to admin;

create table minute_candles_2025_07_08
    partition of minute_candles
        FOR VALUES FROM ('2025-07-07 21:00:00+00') TO ('2025-07-08 21:00:00+00');

alter table minute_candles_2025_07_08
    owner to postgres;

grant select on minute_candles_2025_07_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_08 to admin;

create table minute_candles_2025_07_09
    partition of minute_candles
        FOR VALUES FROM ('2025-07-08 21:00:00+00') TO ('2025-07-09 21:00:00+00');

alter table minute_candles_2025_07_09
    owner to postgres;

grant select on minute_candles_2025_07_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_09 to admin;

create table minute_candles_2025_07_10
    partition of minute_candles
        FOR VALUES FROM ('2025-07-09 21:00:00+00') TO ('2025-07-10 21:00:00+00');

alter table minute_candles_2025_07_10
    owner to postgres;

grant select on minute_candles_2025_07_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_10 to admin;

create table minute_candles_2025_07_11
    partition of minute_candles
        FOR VALUES FROM ('2025-07-10 21:00:00+00') TO ('2025-07-11 21:00:00+00');

alter table minute_candles_2025_07_11
    owner to postgres;

grant select on minute_candles_2025_07_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_11 to admin;

create table minute_candles_2025_07_12
    partition of minute_candles
        FOR VALUES FROM ('2025-07-11 21:00:00+00') TO ('2025-07-12 21:00:00+00');

alter table minute_candles_2025_07_12
    owner to postgres;

grant select on minute_candles_2025_07_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_12 to admin;

create table minute_candles_2025_07_13
    partition of minute_candles
        FOR VALUES FROM ('2025-07-12 21:00:00+00') TO ('2025-07-13 21:00:00+00');

alter table minute_candles_2025_07_13
    owner to postgres;

grant select on minute_candles_2025_07_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_13 to admin;

create table minute_candles_2025_07_14
    partition of minute_candles
        FOR VALUES FROM ('2025-07-13 21:00:00+00') TO ('2025-07-14 21:00:00+00');

alter table minute_candles_2025_07_14
    owner to postgres;

grant select on minute_candles_2025_07_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_14 to admin;

create table minute_candles_2025_07_15
    partition of minute_candles
        FOR VALUES FROM ('2025-07-14 21:00:00+00') TO ('2025-07-15 21:00:00+00');

alter table minute_candles_2025_07_15
    owner to postgres;

grant select on minute_candles_2025_07_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_15 to admin;

create table minute_candles_2025_07_16
    partition of minute_candles
        FOR VALUES FROM ('2025-07-15 21:00:00+00') TO ('2025-07-16 21:00:00+00');

alter table minute_candles_2025_07_16
    owner to postgres;

grant select on minute_candles_2025_07_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_16 to admin;

create table minute_candles_2025_07_17
    partition of minute_candles
        FOR VALUES FROM ('2025-07-16 21:00:00+00') TO ('2025-07-17 21:00:00+00');

alter table minute_candles_2025_07_17
    owner to postgres;

grant select on minute_candles_2025_07_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_17 to admin;

create table minute_candles_2025_07_18
    partition of minute_candles
        FOR VALUES FROM ('2025-07-17 21:00:00+00') TO ('2025-07-18 21:00:00+00');

alter table minute_candles_2025_07_18
    owner to postgres;

grant select on minute_candles_2025_07_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_18 to admin;

create table minute_candles_2025_07_19
    partition of minute_candles
        FOR VALUES FROM ('2025-07-18 21:00:00+00') TO ('2025-07-19 21:00:00+00');

alter table minute_candles_2025_07_19
    owner to postgres;

grant select on minute_candles_2025_07_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_19 to admin;

create table minute_candles_2025_07_20
    partition of minute_candles
        FOR VALUES FROM ('2025-07-19 21:00:00+00') TO ('2025-07-20 21:00:00+00');

alter table minute_candles_2025_07_20
    owner to postgres;

grant select on minute_candles_2025_07_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_20 to admin;

create table minute_candles_2025_07_21
    partition of minute_candles
        FOR VALUES FROM ('2025-07-20 21:00:00+00') TO ('2025-07-21 21:00:00+00');

alter table minute_candles_2025_07_21
    owner to postgres;

grant select on minute_candles_2025_07_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_21 to admin;

create table minute_candles_2025_07_22
    partition of minute_candles
        FOR VALUES FROM ('2025-07-21 21:00:00+00') TO ('2025-07-22 21:00:00+00');

alter table minute_candles_2025_07_22
    owner to postgres;

grant select on minute_candles_2025_07_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_22 to admin;

create table minute_candles_2025_07_23
    partition of minute_candles
        FOR VALUES FROM ('2025-07-22 21:00:00+00') TO ('2025-07-23 21:00:00+00');

alter table minute_candles_2025_07_23
    owner to postgres;

grant select on minute_candles_2025_07_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_23 to admin;

create table minute_candles_2025_07_24
    partition of minute_candles
        FOR VALUES FROM ('2025-07-23 21:00:00+00') TO ('2025-07-24 21:00:00+00');

alter table minute_candles_2025_07_24
    owner to postgres;

grant select on minute_candles_2025_07_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_24 to admin;

create table minute_candles_2025_07_25
    partition of minute_candles
        FOR VALUES FROM ('2025-07-24 21:00:00+00') TO ('2025-07-25 21:00:00+00');

alter table minute_candles_2025_07_25
    owner to postgres;

grant select on minute_candles_2025_07_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_25 to admin;

create table minute_candles_2025_07_26
    partition of minute_candles
        FOR VALUES FROM ('2025-07-25 21:00:00+00') TO ('2025-07-26 21:00:00+00');

alter table minute_candles_2025_07_26
    owner to postgres;

grant select on minute_candles_2025_07_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_26 to admin;

create table minute_candles_2025_07_27
    partition of minute_candles
        FOR VALUES FROM ('2025-07-26 21:00:00+00') TO ('2025-07-27 21:00:00+00');

alter table minute_candles_2025_07_27
    owner to postgres;

grant select on minute_candles_2025_07_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_27 to admin;

create table minute_candles_2025_07_28
    partition of minute_candles
        FOR VALUES FROM ('2025-07-27 21:00:00+00') TO ('2025-07-28 21:00:00+00');

alter table minute_candles_2025_07_28
    owner to postgres;

grant select on minute_candles_2025_07_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_28 to admin;

create table minute_candles_2025_07_29
    partition of minute_candles
        FOR VALUES FROM ('2025-07-28 21:00:00+00') TO ('2025-07-29 21:00:00+00');

alter table minute_candles_2025_07_29
    owner to postgres;

grant select on minute_candles_2025_07_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_29 to admin;

create table minute_candles_2025_07_30
    partition of minute_candles
        FOR VALUES FROM ('2025-07-29 21:00:00+00') TO ('2025-07-30 21:00:00+00');

alter table minute_candles_2025_07_30
    owner to postgres;

grant select on minute_candles_2025_07_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_30 to admin;

create table minute_candles_2025_07_31
    partition of minute_candles
        FOR VALUES FROM ('2025-07-30 21:00:00+00') TO ('2025-07-31 21:00:00+00');

alter table minute_candles_2025_07_31
    owner to postgres;

grant select on minute_candles_2025_07_31 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_07_31 to admin;

create table minute_candles_2025_08_01
    partition of minute_candles
        FOR VALUES FROM ('2025-07-31 21:00:00+00') TO ('2025-08-01 21:00:00+00');

alter table minute_candles_2025_08_01
    owner to postgres;

grant select on minute_candles_2025_08_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_01 to admin;

create table minute_candles_2025_08_02
    partition of minute_candles
        FOR VALUES FROM ('2025-08-01 21:00:00+00') TO ('2025-08-02 21:00:00+00');

alter table minute_candles_2025_08_02
    owner to postgres;

grant select on minute_candles_2025_08_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_02 to admin;

create table minute_candles_2025_08_03
    partition of minute_candles
        FOR VALUES FROM ('2025-08-02 21:00:00+00') TO ('2025-08-03 21:00:00+00');

alter table minute_candles_2025_08_03
    owner to postgres;

grant select on minute_candles_2025_08_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_03 to admin;

create table minute_candles_2025_08_04
    partition of minute_candles
        FOR VALUES FROM ('2025-08-03 21:00:00+00') TO ('2025-08-04 21:00:00+00');

alter table minute_candles_2025_08_04
    owner to postgres;

grant select on minute_candles_2025_08_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_04 to admin;

create table minute_candles_2025_08_05
    partition of minute_candles
        FOR VALUES FROM ('2025-08-04 21:00:00+00') TO ('2025-08-05 21:00:00+00');

alter table minute_candles_2025_08_05
    owner to postgres;

grant select on minute_candles_2025_08_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_05 to admin;

create table minute_candles_2025_08_06
    partition of minute_candles
        FOR VALUES FROM ('2025-08-05 21:00:00+00') TO ('2025-08-06 21:00:00+00');

alter table minute_candles_2025_08_06
    owner to postgres;

grant select on minute_candles_2025_08_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_06 to admin;

create table minute_candles_2025_08_07
    partition of minute_candles
        FOR VALUES FROM ('2025-08-06 21:00:00+00') TO ('2025-08-07 21:00:00+00');

alter table minute_candles_2025_08_07
    owner to postgres;

grant select on minute_candles_2025_08_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_07 to admin;

create table minute_candles_2025_08_08
    partition of minute_candles
        FOR VALUES FROM ('2025-08-07 21:00:00+00') TO ('2025-08-08 21:00:00+00');

alter table minute_candles_2025_08_08
    owner to postgres;

grant select on minute_candles_2025_08_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_08 to admin;

create table minute_candles_2025_08_09
    partition of minute_candles
        FOR VALUES FROM ('2025-08-08 21:00:00+00') TO ('2025-08-09 21:00:00+00');

alter table minute_candles_2025_08_09
    owner to postgres;

grant select on minute_candles_2025_08_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_09 to admin;

create table minute_candles_2025_08_10
    partition of minute_candles
        FOR VALUES FROM ('2025-08-09 21:00:00+00') TO ('2025-08-10 21:00:00+00');

alter table minute_candles_2025_08_10
    owner to postgres;

grant select on minute_candles_2025_08_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_10 to admin;

create table minute_candles_2025_08_11
    partition of minute_candles
        FOR VALUES FROM ('2025-08-10 21:00:00+00') TO ('2025-08-11 21:00:00+00');

alter table minute_candles_2025_08_11
    owner to postgres;

grant select on minute_candles_2025_08_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_11 to admin;

create table minute_candles_2025_08_12
    partition of minute_candles
        FOR VALUES FROM ('2025-08-11 21:00:00+00') TO ('2025-08-12 21:00:00+00');

alter table minute_candles_2025_08_12
    owner to postgres;

grant select on minute_candles_2025_08_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_12 to admin;

create table minute_candles_2025_08_13
    partition of minute_candles
        FOR VALUES FROM ('2025-08-12 21:00:00+00') TO ('2025-08-13 21:00:00+00');

alter table minute_candles_2025_08_13
    owner to postgres;

grant select on minute_candles_2025_08_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_13 to admin;

create table minute_candles_2025_08_14
    partition of minute_candles
        FOR VALUES FROM ('2025-08-13 21:00:00+00') TO ('2025-08-14 21:00:00+00');

alter table minute_candles_2025_08_14
    owner to postgres;

grant select on minute_candles_2025_08_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_14 to admin;

create table minute_candles_2025_08_15
    partition of minute_candles
        FOR VALUES FROM ('2025-08-14 21:00:00+00') TO ('2025-08-15 21:00:00+00');

alter table minute_candles_2025_08_15
    owner to postgres;

grant select on minute_candles_2025_08_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_15 to admin;

create table minute_candles_2025_08_16
    partition of minute_candles
        FOR VALUES FROM ('2025-08-15 21:00:00+00') TO ('2025-08-16 21:00:00+00');

alter table minute_candles_2025_08_16
    owner to postgres;

grant select on minute_candles_2025_08_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_16 to admin;

create table minute_candles_2025_08_17
    partition of minute_candles
        FOR VALUES FROM ('2025-08-16 21:00:00+00') TO ('2025-08-17 21:00:00+00');

alter table minute_candles_2025_08_17
    owner to postgres;

grant select on minute_candles_2025_08_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_17 to admin;

create table minute_candles_2025_08_18
    partition of minute_candles
        FOR VALUES FROM ('2025-08-17 21:00:00+00') TO ('2025-08-18 21:00:00+00');

alter table minute_candles_2025_08_18
    owner to postgres;

grant select on minute_candles_2025_08_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_18 to admin;

create table minute_candles_2025_08_19
    partition of minute_candles
        FOR VALUES FROM ('2025-08-18 21:00:00+00') TO ('2025-08-19 21:00:00+00');

alter table minute_candles_2025_08_19
    owner to postgres;

grant select on minute_candles_2025_08_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_19 to admin;

create table minute_candles_2025_08_20
    partition of minute_candles
        FOR VALUES FROM ('2025-08-19 21:00:00+00') TO ('2025-08-20 21:00:00+00');

alter table minute_candles_2025_08_20
    owner to postgres;

grant select on minute_candles_2025_08_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_20 to admin;

create table minute_candles_2025_08_21
    partition of minute_candles
        FOR VALUES FROM ('2025-08-20 21:00:00+00') TO ('2025-08-21 21:00:00+00');

alter table minute_candles_2025_08_21
    owner to postgres;

grant select on minute_candles_2025_08_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_21 to admin;

create table minute_candles_2025_08_22
    partition of minute_candles
        FOR VALUES FROM ('2025-08-21 21:00:00+00') TO ('2025-08-22 21:00:00+00');

alter table minute_candles_2025_08_22
    owner to postgres;

grant select on minute_candles_2025_08_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_22 to admin;

create table minute_candles_2025_08_23
    partition of minute_candles
        FOR VALUES FROM ('2025-08-22 21:00:00+00') TO ('2025-08-23 21:00:00+00');

alter table minute_candles_2025_08_23
    owner to postgres;

grant select on minute_candles_2025_08_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_23 to admin;

create table minute_candles_2025_08_24
    partition of minute_candles
        FOR VALUES FROM ('2025-08-23 21:00:00+00') TO ('2025-08-24 21:00:00+00');

alter table minute_candles_2025_08_24
    owner to postgres;

grant select on minute_candles_2025_08_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_24 to admin;

create table minute_candles_2025_08_25
    partition of minute_candles
        FOR VALUES FROM ('2025-08-24 21:00:00+00') TO ('2025-08-25 21:00:00+00');

alter table minute_candles_2025_08_25
    owner to postgres;

grant select on minute_candles_2025_08_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_25 to admin;

create table minute_candles_2025_08_26
    partition of minute_candles
        FOR VALUES FROM ('2025-08-25 21:00:00+00') TO ('2025-08-26 21:00:00+00');

alter table minute_candles_2025_08_26
    owner to postgres;

grant select on minute_candles_2025_08_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_26 to admin;

create table minute_candles_2025_08_27
    partition of minute_candles
        FOR VALUES FROM ('2025-08-26 21:00:00+00') TO ('2025-08-27 21:00:00+00');

alter table minute_candles_2025_08_27
    owner to postgres;

grant select on minute_candles_2025_08_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_27 to admin;

create table minute_candles_2025_08_28
    partition of minute_candles
        FOR VALUES FROM ('2025-08-27 21:00:00+00') TO ('2025-08-28 21:00:00+00');

alter table minute_candles_2025_08_28
    owner to postgres;

grant select on minute_candles_2025_08_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_28 to admin;

create table minute_candles_2025_08_29
    partition of minute_candles
        FOR VALUES FROM ('2025-08-28 21:00:00+00') TO ('2025-08-29 21:00:00+00');

alter table minute_candles_2025_08_29
    owner to postgres;

grant select on minute_candles_2025_08_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_29 to admin;

create table minute_candles_2025_08_30
    partition of minute_candles
        FOR VALUES FROM ('2025-08-29 21:00:00+00') TO ('2025-08-30 21:00:00+00');

alter table minute_candles_2025_08_30
    owner to postgres;

grant select on minute_candles_2025_08_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_30 to admin;

create table minute_candles_2025_08_31
    partition of minute_candles
        FOR VALUES FROM ('2025-08-30 21:00:00+00') TO ('2025-08-31 21:00:00+00');

alter table minute_candles_2025_08_31
    owner to postgres;

grant select on minute_candles_2025_08_31 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_08_31 to admin;

create table minute_candles_2025_09_01
    partition of minute_candles
        FOR VALUES FROM ('2025-08-31 21:00:00+00') TO ('2025-09-01 21:00:00+00');

alter table minute_candles_2025_09_01
    owner to postgres;

grant select on minute_candles_2025_09_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_01 to admin;

create table minute_candles_2025_09_02
    partition of minute_candles
        FOR VALUES FROM ('2025-09-01 21:00:00+00') TO ('2025-09-02 21:00:00+00');

alter table minute_candles_2025_09_02
    owner to postgres;

grant select on minute_candles_2025_09_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_02 to admin;

create table minute_candles_2025_09_03
    partition of minute_candles
        FOR VALUES FROM ('2025-09-02 21:00:00+00') TO ('2025-09-03 21:00:00+00');

alter table minute_candles_2025_09_03
    owner to postgres;

grant select on minute_candles_2025_09_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_03 to admin;

create table minute_candles_2025_09_04
    partition of minute_candles
        FOR VALUES FROM ('2025-09-03 21:00:00+00') TO ('2025-09-04 21:00:00+00');

alter table minute_candles_2025_09_04
    owner to postgres;

grant select on minute_candles_2025_09_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_04 to admin;

create table minute_candles_2025_09_05
    partition of minute_candles
        FOR VALUES FROM ('2025-09-04 21:00:00+00') TO ('2025-09-05 21:00:00+00');

alter table minute_candles_2025_09_05
    owner to postgres;

grant select on minute_candles_2025_09_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_05 to admin;

create table minute_candles_2025_09_06
    partition of minute_candles
        FOR VALUES FROM ('2025-09-05 21:00:00+00') TO ('2025-09-06 21:00:00+00');

alter table minute_candles_2025_09_06
    owner to postgres;

grant select on minute_candles_2025_09_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_06 to admin;

create table minute_candles_2025_09_07
    partition of minute_candles
        FOR VALUES FROM ('2025-09-06 21:00:00+00') TO ('2025-09-07 21:00:00+00');

alter table minute_candles_2025_09_07
    owner to postgres;

grant select on minute_candles_2025_09_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_07 to admin;

create table minute_candles_2025_09_08
    partition of minute_candles
        FOR VALUES FROM ('2025-09-07 21:00:00+00') TO ('2025-09-08 21:00:00+00');

alter table minute_candles_2025_09_08
    owner to postgres;

grant select on minute_candles_2025_09_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_08 to admin;

create table minute_candles_2025_09_09
    partition of minute_candles
        FOR VALUES FROM ('2025-09-08 21:00:00+00') TO ('2025-09-09 21:00:00+00');

alter table minute_candles_2025_09_09
    owner to postgres;

grant select on minute_candles_2025_09_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_09 to admin;

create table minute_candles_2025_09_10
    partition of minute_candles
        FOR VALUES FROM ('2025-09-09 21:00:00+00') TO ('2025-09-10 21:00:00+00');

alter table minute_candles_2025_09_10
    owner to postgres;

grant select on minute_candles_2025_09_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_10 to admin;

create table minute_candles_2025_09_11
    partition of minute_candles
        FOR VALUES FROM ('2025-09-10 21:00:00+00') TO ('2025-09-11 21:00:00+00');

alter table minute_candles_2025_09_11
    owner to postgres;

grant select on minute_candles_2025_09_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_11 to admin;

create table minute_candles_2025_09_12
    partition of minute_candles
        FOR VALUES FROM ('2025-09-11 21:00:00+00') TO ('2025-09-12 21:00:00+00');

alter table minute_candles_2025_09_12
    owner to postgres;

grant select on minute_candles_2025_09_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_12 to admin;

create table minute_candles_2025_09_13
    partition of minute_candles
        FOR VALUES FROM ('2025-09-12 21:00:00+00') TO ('2025-09-13 21:00:00+00');

alter table minute_candles_2025_09_13
    owner to postgres;

grant select on minute_candles_2025_09_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_13 to admin;

create table minute_candles_2025_09_14
    partition of minute_candles
        FOR VALUES FROM ('2025-09-13 21:00:00+00') TO ('2025-09-14 21:00:00+00');

alter table minute_candles_2025_09_14
    owner to postgres;

grant select on minute_candles_2025_09_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_14 to admin;

create table minute_candles_2025_09_15
    partition of minute_candles
        FOR VALUES FROM ('2025-09-14 21:00:00+00') TO ('2025-09-15 21:00:00+00');

alter table minute_candles_2025_09_15
    owner to postgres;

grant select on minute_candles_2025_09_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_15 to admin;

create table minute_candles_2025_09_16
    partition of minute_candles
        FOR VALUES FROM ('2025-09-15 21:00:00+00') TO ('2025-09-16 21:00:00+00');

alter table minute_candles_2025_09_16
    owner to postgres;

grant select on minute_candles_2025_09_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_16 to admin;

create table minute_candles_2025_09_17
    partition of minute_candles
        FOR VALUES FROM ('2025-09-16 21:00:00+00') TO ('2025-09-17 21:00:00+00');

alter table minute_candles_2025_09_17
    owner to postgres;

grant select on minute_candles_2025_09_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_17 to admin;

create table minute_candles_2025_09_18
    partition of minute_candles
        FOR VALUES FROM ('2025-09-17 21:00:00+00') TO ('2025-09-18 21:00:00+00');

alter table minute_candles_2025_09_18
    owner to postgres;

grant select on minute_candles_2025_09_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_18 to admin;

create table minute_candles_2025_09_19
    partition of minute_candles
        FOR VALUES FROM ('2025-09-18 21:00:00+00') TO ('2025-09-19 21:00:00+00');

alter table minute_candles_2025_09_19
    owner to postgres;

grant select on minute_candles_2025_09_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_19 to admin;

create table minute_candles_2025_09_20
    partition of minute_candles
        FOR VALUES FROM ('2025-09-19 21:00:00+00') TO ('2025-09-20 21:00:00+00');

alter table minute_candles_2025_09_20
    owner to postgres;

grant select on minute_candles_2025_09_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_20 to admin;

create table minute_candles_2025_09_21
    partition of minute_candles
        FOR VALUES FROM ('2025-09-20 21:00:00+00') TO ('2025-09-21 21:00:00+00');

alter table minute_candles_2025_09_21
    owner to postgres;

grant select on minute_candles_2025_09_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_21 to admin;

create table minute_candles_2025_09_22
    partition of minute_candles
        FOR VALUES FROM ('2025-09-21 21:00:00+00') TO ('2025-09-22 21:00:00+00');

alter table minute_candles_2025_09_22
    owner to postgres;

grant select on minute_candles_2025_09_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_22 to admin;

create table minute_candles_2025_09_23
    partition of minute_candles
        FOR VALUES FROM ('2025-09-22 21:00:00+00') TO ('2025-09-23 21:00:00+00');

alter table minute_candles_2025_09_23
    owner to postgres;

grant select on minute_candles_2025_09_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_23 to admin;

create table minute_candles_2025_09_24
    partition of minute_candles
        FOR VALUES FROM ('2025-09-23 21:00:00+00') TO ('2025-09-24 21:00:00+00');

alter table minute_candles_2025_09_24
    owner to postgres;

grant select on minute_candles_2025_09_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_24 to admin;

create table minute_candles_2025_09_25
    partition of minute_candles
        FOR VALUES FROM ('2025-09-24 21:00:00+00') TO ('2025-09-25 21:00:00+00');

alter table minute_candles_2025_09_25
    owner to postgres;

grant select on minute_candles_2025_09_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_25 to admin;

create table minute_candles_2025_09_26
    partition of minute_candles
        FOR VALUES FROM ('2025-09-25 21:00:00+00') TO ('2025-09-26 21:00:00+00');

alter table minute_candles_2025_09_26
    owner to postgres;

grant select on minute_candles_2025_09_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_26 to admin;

create table minute_candles_2025_09_27
    partition of minute_candles
        FOR VALUES FROM ('2025-09-26 21:00:00+00') TO ('2025-09-27 21:00:00+00');

alter table minute_candles_2025_09_27
    owner to postgres;

grant select on minute_candles_2025_09_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_27 to admin;

create table minute_candles_2025_09_28
    partition of minute_candles
        FOR VALUES FROM ('2025-09-27 21:00:00+00') TO ('2025-09-28 21:00:00+00');

alter table minute_candles_2025_09_28
    owner to postgres;

grant select on minute_candles_2025_09_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_28 to admin;

create table minute_candles_2025_09_29
    partition of minute_candles
        FOR VALUES FROM ('2025-09-28 21:00:00+00') TO ('2025-09-29 21:00:00+00');

alter table minute_candles_2025_09_29
    owner to postgres;

grant select on minute_candles_2025_09_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_29 to admin;

create table minute_candles_2025_09_30
    partition of minute_candles
        FOR VALUES FROM ('2025-09-29 21:00:00+00') TO ('2025-09-30 21:00:00+00');

alter table minute_candles_2025_09_30
    owner to postgres;

grant select on minute_candles_2025_09_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_09_30 to admin;

create table minute_candles_2025_10_01
    partition of minute_candles
        FOR VALUES FROM ('2025-09-30 21:00:00+00') TO ('2025-10-01 21:00:00+00');

alter table minute_candles_2025_10_01
    owner to postgres;

grant select on minute_candles_2025_10_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_01 to admin;

create table minute_candles_2025_10_02
    partition of minute_candles
        FOR VALUES FROM ('2025-10-01 21:00:00+00') TO ('2025-10-02 21:00:00+00');

alter table minute_candles_2025_10_02
    owner to postgres;

grant select on minute_candles_2025_10_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_02 to admin;

create table minute_candles_2025_10_03
    partition of minute_candles
        FOR VALUES FROM ('2025-10-02 21:00:00+00') TO ('2025-10-03 21:00:00+00');

alter table minute_candles_2025_10_03
    owner to postgres;

grant select on minute_candles_2025_10_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_03 to admin;

create table minute_candles_2025_10_04
    partition of minute_candles
        FOR VALUES FROM ('2025-10-03 21:00:00+00') TO ('2025-10-04 21:00:00+00');

alter table minute_candles_2025_10_04
    owner to postgres;

grant select on minute_candles_2025_10_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_04 to admin;

create table minute_candles_2025_10_05
    partition of minute_candles
        FOR VALUES FROM ('2025-10-04 21:00:00+00') TO ('2025-10-05 21:00:00+00');

alter table minute_candles_2025_10_05
    owner to postgres;

grant select on minute_candles_2025_10_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_05 to admin;

create table minute_candles_2025_10_06
    partition of minute_candles
        FOR VALUES FROM ('2025-10-05 21:00:00+00') TO ('2025-10-06 21:00:00+00');

alter table minute_candles_2025_10_06
    owner to postgres;

grant select on minute_candles_2025_10_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_06 to admin;

create table minute_candles_2025_10_07
    partition of minute_candles
        FOR VALUES FROM ('2025-10-06 21:00:00+00') TO ('2025-10-07 21:00:00+00');

alter table minute_candles_2025_10_07
    owner to postgres;

grant select on minute_candles_2025_10_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_07 to admin;

create table minute_candles_2025_10_08
    partition of minute_candles
        FOR VALUES FROM ('2025-10-07 21:00:00+00') TO ('2025-10-08 21:00:00+00');

alter table minute_candles_2025_10_08
    owner to postgres;

grant select on minute_candles_2025_10_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_08 to admin;

create table minute_candles_2025_10_09
    partition of minute_candles
        FOR VALUES FROM ('2025-10-08 21:00:00+00') TO ('2025-10-09 21:00:00+00');

alter table minute_candles_2025_10_09
    owner to postgres;

grant select on minute_candles_2025_10_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_09 to admin;

create table minute_candles_2025_10_10
    partition of minute_candles
        FOR VALUES FROM ('2025-10-09 21:00:00+00') TO ('2025-10-10 21:00:00+00');

alter table minute_candles_2025_10_10
    owner to postgres;

grant select on minute_candles_2025_10_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_10 to admin;

create table minute_candles_2025_10_11
    partition of minute_candles
        FOR VALUES FROM ('2025-10-10 21:00:00+00') TO ('2025-10-11 21:00:00+00');

alter table minute_candles_2025_10_11
    owner to postgres;

grant select on minute_candles_2025_10_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_11 to admin;

create table minute_candles_2025_10_12
    partition of minute_candles
        FOR VALUES FROM ('2025-10-11 21:00:00+00') TO ('2025-10-12 21:00:00+00');

alter table minute_candles_2025_10_12
    owner to postgres;

grant select on minute_candles_2025_10_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_12 to admin;

create table minute_candles_2025_10_13
    partition of minute_candles
        FOR VALUES FROM ('2025-10-12 21:00:00+00') TO ('2025-10-13 21:00:00+00');

alter table minute_candles_2025_10_13
    owner to postgres;

grant select on minute_candles_2025_10_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_13 to admin;

create table minute_candles_2025_10_14
    partition of minute_candles
        FOR VALUES FROM ('2025-10-13 21:00:00+00') TO ('2025-10-14 21:00:00+00');

alter table minute_candles_2025_10_14
    owner to postgres;

grant select on minute_candles_2025_10_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_14 to admin;

create table minute_candles_2025_10_15
    partition of minute_candles
        FOR VALUES FROM ('2025-10-14 21:00:00+00') TO ('2025-10-15 21:00:00+00');

alter table minute_candles_2025_10_15
    owner to postgres;

grant select on minute_candles_2025_10_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_15 to admin;

create table minute_candles_2025_10_16
    partition of minute_candles
        FOR VALUES FROM ('2025-10-15 21:00:00+00') TO ('2025-10-16 21:00:00+00');

alter table minute_candles_2025_10_16
    owner to postgres;

grant select on minute_candles_2025_10_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_16 to admin;

create table minute_candles_2025_10_17
    partition of minute_candles
        FOR VALUES FROM ('2025-10-16 21:00:00+00') TO ('2025-10-17 21:00:00+00');

alter table minute_candles_2025_10_17
    owner to postgres;

grant select on minute_candles_2025_10_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_17 to admin;

create table minute_candles_2025_10_18
    partition of minute_candles
        FOR VALUES FROM ('2025-10-17 21:00:00+00') TO ('2025-10-18 21:00:00+00');

alter table minute_candles_2025_10_18
    owner to postgres;

grant select on minute_candles_2025_10_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_18 to admin;

create table minute_candles_2025_10_19
    partition of minute_candles
        FOR VALUES FROM ('2025-10-18 21:00:00+00') TO ('2025-10-19 21:00:00+00');

alter table minute_candles_2025_10_19
    owner to postgres;

grant select on minute_candles_2025_10_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_19 to admin;

create table minute_candles_2025_10_20
    partition of minute_candles
        FOR VALUES FROM ('2025-10-19 21:00:00+00') TO ('2025-10-20 21:00:00+00');

alter table minute_candles_2025_10_20
    owner to postgres;

grant select on minute_candles_2025_10_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_20 to admin;

create table minute_candles_2025_10_21
    partition of minute_candles
        FOR VALUES FROM ('2025-10-20 21:00:00+00') TO ('2025-10-21 21:00:00+00');

alter table minute_candles_2025_10_21
    owner to postgres;

grant select on minute_candles_2025_10_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_21 to admin;

create table minute_candles_2025_10_22
    partition of minute_candles
        FOR VALUES FROM ('2025-10-21 21:00:00+00') TO ('2025-10-22 21:00:00+00');

alter table minute_candles_2025_10_22
    owner to postgres;

grant select on minute_candles_2025_10_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_22 to admin;

create table minute_candles_2025_10_23
    partition of minute_candles
        FOR VALUES FROM ('2025-10-22 21:00:00+00') TO ('2025-10-23 21:00:00+00');

alter table minute_candles_2025_10_23
    owner to postgres;

grant select on minute_candles_2025_10_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_23 to admin;

create table minute_candles_2025_10_24
    partition of minute_candles
        FOR VALUES FROM ('2025-10-23 21:00:00+00') TO ('2025-10-24 21:00:00+00');

alter table minute_candles_2025_10_24
    owner to postgres;

grant select on minute_candles_2025_10_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_24 to admin;

create table minute_candles_2025_10_25
    partition of minute_candles
        FOR VALUES FROM ('2025-10-24 21:00:00+00') TO ('2025-10-25 21:00:00+00');

alter table minute_candles_2025_10_25
    owner to postgres;

grant select on minute_candles_2025_10_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_25 to admin;

create table minute_candles_2025_10_26
    partition of minute_candles
        FOR VALUES FROM ('2025-10-25 21:00:00+00') TO ('2025-10-26 21:00:00+00');

alter table minute_candles_2025_10_26
    owner to postgres;

grant select on minute_candles_2025_10_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_26 to admin;

create table minute_candles_2025_10_27
    partition of minute_candles
        FOR VALUES FROM ('2025-10-26 21:00:00+00') TO ('2025-10-27 21:00:00+00');

alter table minute_candles_2025_10_27
    owner to postgres;

grant select on minute_candles_2025_10_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_27 to admin;

create table minute_candles_2025_10_28
    partition of minute_candles
        FOR VALUES FROM ('2025-10-27 21:00:00+00') TO ('2025-10-28 21:00:00+00');

alter table minute_candles_2025_10_28
    owner to postgres;

grant select on minute_candles_2025_10_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_28 to admin;

create table minute_candles_2025_10_29
    partition of minute_candles
        FOR VALUES FROM ('2025-10-28 21:00:00+00') TO ('2025-10-29 21:00:00+00');

alter table minute_candles_2025_10_29
    owner to postgres;

grant select on minute_candles_2025_10_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_29 to admin;

create table minute_candles_2025_10_30
    partition of minute_candles
        FOR VALUES FROM ('2025-10-29 21:00:00+00') TO ('2025-10-30 21:00:00+00');

alter table minute_candles_2025_10_30
    owner to postgres;

grant select on minute_candles_2025_10_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_30 to admin;

create table minute_candles_2025_10_31
    partition of minute_candles
        FOR VALUES FROM ('2025-10-30 21:00:00+00') TO ('2025-10-31 21:00:00+00');

alter table minute_candles_2025_10_31
    owner to postgres;

grant select on minute_candles_2025_10_31 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_10_31 to admin;

create table minute_candles_2025_11_01
    partition of minute_candles
        FOR VALUES FROM ('2025-10-31 21:00:00+00') TO ('2025-11-01 21:00:00+00');

alter table minute_candles_2025_11_01
    owner to postgres;

grant select on minute_candles_2025_11_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_01 to admin;

create table minute_candles_2025_11_02
    partition of minute_candles
        FOR VALUES FROM ('2025-11-01 21:00:00+00') TO ('2025-11-02 21:00:00+00');

alter table minute_candles_2025_11_02
    owner to postgres;

grant select on minute_candles_2025_11_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_02 to admin;

create table minute_candles_2025_11_03
    partition of minute_candles
        FOR VALUES FROM ('2025-11-02 21:00:00+00') TO ('2025-11-03 21:00:00+00');

alter table minute_candles_2025_11_03
    owner to postgres;

grant select on minute_candles_2025_11_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_03 to admin;

create table minute_candles_2025_11_04
    partition of minute_candles
        FOR VALUES FROM ('2025-11-03 21:00:00+00') TO ('2025-11-04 21:00:00+00');

alter table minute_candles_2025_11_04
    owner to postgres;

grant select on minute_candles_2025_11_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_04 to admin;

create table minute_candles_2025_11_05
    partition of minute_candles
        FOR VALUES FROM ('2025-11-04 21:00:00+00') TO ('2025-11-05 21:00:00+00');

alter table minute_candles_2025_11_05
    owner to postgres;

grant select on minute_candles_2025_11_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_05 to admin;

create table minute_candles_2025_11_06
    partition of minute_candles
        FOR VALUES FROM ('2025-11-05 21:00:00+00') TO ('2025-11-06 21:00:00+00');

alter table minute_candles_2025_11_06
    owner to postgres;

grant select on minute_candles_2025_11_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_06 to admin;

create table minute_candles_2025_11_07
    partition of minute_candles
        FOR VALUES FROM ('2025-11-06 21:00:00+00') TO ('2025-11-07 21:00:00+00');

alter table minute_candles_2025_11_07
    owner to postgres;

grant select on minute_candles_2025_11_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_07 to admin;

create table minute_candles_2025_11_08
    partition of minute_candles
        FOR VALUES FROM ('2025-11-07 21:00:00+00') TO ('2025-11-08 21:00:00+00');

alter table minute_candles_2025_11_08
    owner to postgres;

grant select on minute_candles_2025_11_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_08 to admin;

create table minute_candles_2025_11_09
    partition of minute_candles
        FOR VALUES FROM ('2025-11-08 21:00:00+00') TO ('2025-11-09 21:00:00+00');

alter table minute_candles_2025_11_09
    owner to postgres;

grant select on minute_candles_2025_11_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_09 to admin;

create table minute_candles_2025_11_10
    partition of minute_candles
        FOR VALUES FROM ('2025-11-09 21:00:00+00') TO ('2025-11-10 21:00:00+00');

alter table minute_candles_2025_11_10
    owner to postgres;

grant select on minute_candles_2025_11_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_10 to admin;

create table minute_candles_2025_11_11
    partition of minute_candles
        FOR VALUES FROM ('2025-11-10 21:00:00+00') TO ('2025-11-11 21:00:00+00');

alter table minute_candles_2025_11_11
    owner to postgres;

grant select on minute_candles_2025_11_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_11 to admin;

create table minute_candles_2025_11_12
    partition of minute_candles
        FOR VALUES FROM ('2025-11-11 21:00:00+00') TO ('2025-11-12 21:00:00+00');

alter table minute_candles_2025_11_12
    owner to postgres;

grant select on minute_candles_2025_11_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_12 to admin;

create table minute_candles_2025_11_13
    partition of minute_candles
        FOR VALUES FROM ('2025-11-12 21:00:00+00') TO ('2025-11-13 21:00:00+00');

alter table minute_candles_2025_11_13
    owner to postgres;

grant select on minute_candles_2025_11_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_13 to admin;

create table minute_candles_2025_11_14
    partition of minute_candles
        FOR VALUES FROM ('2025-11-13 21:00:00+00') TO ('2025-11-14 21:00:00+00');

alter table minute_candles_2025_11_14
    owner to postgres;

grant select on minute_candles_2025_11_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_14 to admin;

create table minute_candles_2025_11_15
    partition of minute_candles
        FOR VALUES FROM ('2025-11-14 21:00:00+00') TO ('2025-11-15 21:00:00+00');

alter table minute_candles_2025_11_15
    owner to postgres;

grant select on minute_candles_2025_11_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_15 to admin;

create table minute_candles_2025_11_16
    partition of minute_candles
        FOR VALUES FROM ('2025-11-15 21:00:00+00') TO ('2025-11-16 21:00:00+00');

alter table minute_candles_2025_11_16
    owner to postgres;

grant select on minute_candles_2025_11_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_16 to admin;

create table minute_candles_2025_11_17
    partition of minute_candles
        FOR VALUES FROM ('2025-11-16 21:00:00+00') TO ('2025-11-17 21:00:00+00');

alter table minute_candles_2025_11_17
    owner to postgres;

grant select on minute_candles_2025_11_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_17 to admin;

create table minute_candles_2025_11_18
    partition of minute_candles
        FOR VALUES FROM ('2025-11-17 21:00:00+00') TO ('2025-11-18 21:00:00+00');

alter table minute_candles_2025_11_18
    owner to postgres;

grant select on minute_candles_2025_11_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_18 to admin;

create table minute_candles_2025_11_19
    partition of minute_candles
        FOR VALUES FROM ('2025-11-18 21:00:00+00') TO ('2025-11-19 21:00:00+00');

alter table minute_candles_2025_11_19
    owner to postgres;

grant select on minute_candles_2025_11_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_19 to admin;

create table minute_candles_2025_11_20
    partition of minute_candles
        FOR VALUES FROM ('2025-11-19 21:00:00+00') TO ('2025-11-20 21:00:00+00');

alter table minute_candles_2025_11_20
    owner to postgres;

grant select on minute_candles_2025_11_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_20 to admin;

create table minute_candles_2025_11_21
    partition of minute_candles
        FOR VALUES FROM ('2025-11-20 21:00:00+00') TO ('2025-11-21 21:00:00+00');

alter table minute_candles_2025_11_21
    owner to postgres;

grant select on minute_candles_2025_11_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_21 to admin;

create table minute_candles_2025_11_22
    partition of minute_candles
        FOR VALUES FROM ('2025-11-21 21:00:00+00') TO ('2025-11-22 21:00:00+00');

alter table minute_candles_2025_11_22
    owner to postgres;

grant select on minute_candles_2025_11_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_22 to admin;

create table minute_candles_2025_11_23
    partition of minute_candles
        FOR VALUES FROM ('2025-11-22 21:00:00+00') TO ('2025-11-23 21:00:00+00');

alter table minute_candles_2025_11_23
    owner to postgres;

grant select on minute_candles_2025_11_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_23 to admin;

create table minute_candles_2025_11_24
    partition of minute_candles
        FOR VALUES FROM ('2025-11-23 21:00:00+00') TO ('2025-11-24 21:00:00+00');

alter table minute_candles_2025_11_24
    owner to postgres;

grant select on minute_candles_2025_11_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_24 to admin;

create table minute_candles_2025_11_25
    partition of minute_candles
        FOR VALUES FROM ('2025-11-24 21:00:00+00') TO ('2025-11-25 21:00:00+00');

alter table minute_candles_2025_11_25
    owner to postgres;

grant select on minute_candles_2025_11_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_25 to admin;

create table minute_candles_2025_11_26
    partition of minute_candles
        FOR VALUES FROM ('2025-11-25 21:00:00+00') TO ('2025-11-26 21:00:00+00');

alter table minute_candles_2025_11_26
    owner to postgres;

grant select on minute_candles_2025_11_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_26 to admin;

create table minute_candles_2025_11_27
    partition of minute_candles
        FOR VALUES FROM ('2025-11-26 21:00:00+00') TO ('2025-11-27 21:00:00+00');

alter table minute_candles_2025_11_27
    owner to postgres;

grant select on minute_candles_2025_11_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_27 to admin;

create table minute_candles_2025_11_28
    partition of minute_candles
        FOR VALUES FROM ('2025-11-27 21:00:00+00') TO ('2025-11-28 21:00:00+00');

alter table minute_candles_2025_11_28
    owner to postgres;

grant select on minute_candles_2025_11_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_28 to admin;

create table minute_candles_2025_11_29
    partition of minute_candles
        FOR VALUES FROM ('2025-11-28 21:00:00+00') TO ('2025-11-29 21:00:00+00');

alter table minute_candles_2025_11_29
    owner to postgres;

grant select on minute_candles_2025_11_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_29 to admin;

create table minute_candles_2025_11_30
    partition of minute_candles
        FOR VALUES FROM ('2025-11-29 21:00:00+00') TO ('2025-11-30 21:00:00+00');

alter table minute_candles_2025_11_30
    owner to postgres;

grant select on minute_candles_2025_11_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_11_30 to admin;

create table minute_candles_2025_12_01
    partition of minute_candles
        FOR VALUES FROM ('2025-11-30 21:00:00+00') TO ('2025-12-01 21:00:00+00');

alter table minute_candles_2025_12_01
    owner to postgres;

grant select on minute_candles_2025_12_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_01 to admin;

create table minute_candles_2025_12_02
    partition of minute_candles
        FOR VALUES FROM ('2025-12-01 21:00:00+00') TO ('2025-12-02 21:00:00+00');

alter table minute_candles_2025_12_02
    owner to postgres;

grant select on minute_candles_2025_12_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_02 to admin;

create table minute_candles_2025_12_03
    partition of minute_candles
        FOR VALUES FROM ('2025-12-02 21:00:00+00') TO ('2025-12-03 21:00:00+00');

alter table minute_candles_2025_12_03
    owner to postgres;

grant select on minute_candles_2025_12_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_03 to admin;

create table minute_candles_2025_12_04
    partition of minute_candles
        FOR VALUES FROM ('2025-12-03 21:00:00+00') TO ('2025-12-04 21:00:00+00');

alter table minute_candles_2025_12_04
    owner to postgres;

grant select on minute_candles_2025_12_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_04 to admin;

create table minute_candles_2025_12_05
    partition of minute_candles
        FOR VALUES FROM ('2025-12-04 21:00:00+00') TO ('2025-12-05 21:00:00+00');

alter table minute_candles_2025_12_05
    owner to postgres;

grant select on minute_candles_2025_12_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_05 to admin;

create table minute_candles_2025_12_06
    partition of minute_candles
        FOR VALUES FROM ('2025-12-05 21:00:00+00') TO ('2025-12-06 21:00:00+00');

alter table minute_candles_2025_12_06
    owner to postgres;

grant select on minute_candles_2025_12_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_06 to admin;

create table minute_candles_2025_12_07
    partition of minute_candles
        FOR VALUES FROM ('2025-12-06 21:00:00+00') TO ('2025-12-07 21:00:00+00');

alter table minute_candles_2025_12_07
    owner to postgres;

grant select on minute_candles_2025_12_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_07 to admin;

create table minute_candles_2025_12_08
    partition of minute_candles
        FOR VALUES FROM ('2025-12-07 21:00:00+00') TO ('2025-12-08 21:00:00+00');

alter table minute_candles_2025_12_08
    owner to postgres;

grant select on minute_candles_2025_12_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_08 to admin;

create table minute_candles_2025_12_09
    partition of minute_candles
        FOR VALUES FROM ('2025-12-08 21:00:00+00') TO ('2025-12-09 21:00:00+00');

alter table minute_candles_2025_12_09
    owner to postgres;

grant select on minute_candles_2025_12_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_09 to admin;

create table minute_candles_2025_12_10
    partition of minute_candles
        FOR VALUES FROM ('2025-12-09 21:00:00+00') TO ('2025-12-10 21:00:00+00');

alter table minute_candles_2025_12_10
    owner to postgres;

grant select on minute_candles_2025_12_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_10 to admin;

create table minute_candles_2025_12_11
    partition of minute_candles
        FOR VALUES FROM ('2025-12-10 21:00:00+00') TO ('2025-12-11 21:00:00+00');

alter table minute_candles_2025_12_11
    owner to postgres;

grant select on minute_candles_2025_12_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_11 to admin;

create table minute_candles_2025_12_12
    partition of minute_candles
        FOR VALUES FROM ('2025-12-11 21:00:00+00') TO ('2025-12-12 21:00:00+00');

alter table minute_candles_2025_12_12
    owner to postgres;

grant select on minute_candles_2025_12_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_12 to admin;

create table minute_candles_2025_12_13
    partition of minute_candles
        FOR VALUES FROM ('2025-12-12 21:00:00+00') TO ('2025-12-13 21:00:00+00');

alter table minute_candles_2025_12_13
    owner to postgres;

grant select on minute_candles_2025_12_13 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_13 to admin;

create table minute_candles_2025_12_14
    partition of minute_candles
        FOR VALUES FROM ('2025-12-13 21:00:00+00') TO ('2025-12-14 21:00:00+00');

alter table minute_candles_2025_12_14
    owner to postgres;

grant select on minute_candles_2025_12_14 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_14 to admin;

create table minute_candles_2025_12_15
    partition of minute_candles
        FOR VALUES FROM ('2025-12-14 21:00:00+00') TO ('2025-12-15 21:00:00+00');

alter table minute_candles_2025_12_15
    owner to postgres;

grant select on minute_candles_2025_12_15 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_15 to admin;

create table minute_candles_2025_12_16
    partition of minute_candles
        FOR VALUES FROM ('2025-12-15 21:00:00+00') TO ('2025-12-16 21:00:00+00');

alter table minute_candles_2025_12_16
    owner to postgres;

grant select on minute_candles_2025_12_16 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_16 to admin;

create table minute_candles_2025_12_17
    partition of minute_candles
        FOR VALUES FROM ('2025-12-16 21:00:00+00') TO ('2025-12-17 21:00:00+00');

alter table minute_candles_2025_12_17
    owner to postgres;

grant select on minute_candles_2025_12_17 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_17 to admin;

create table minute_candles_2025_12_18
    partition of minute_candles
        FOR VALUES FROM ('2025-12-17 21:00:00+00') TO ('2025-12-18 21:00:00+00');

alter table minute_candles_2025_12_18
    owner to postgres;

grant select on minute_candles_2025_12_18 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_18 to admin;

create table minute_candles_2025_12_19
    partition of minute_candles
        FOR VALUES FROM ('2025-12-18 21:00:00+00') TO ('2025-12-19 21:00:00+00');

alter table minute_candles_2025_12_19
    owner to postgres;

grant select on minute_candles_2025_12_19 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_19 to admin;

create table minute_candles_2025_12_20
    partition of minute_candles
        FOR VALUES FROM ('2025-12-19 21:00:00+00') TO ('2025-12-20 21:00:00+00');

alter table minute_candles_2025_12_20
    owner to postgres;

grant select on minute_candles_2025_12_20 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_20 to admin;

create table minute_candles_2025_12_21
    partition of minute_candles
        FOR VALUES FROM ('2025-12-20 21:00:00+00') TO ('2025-12-21 21:00:00+00');

alter table minute_candles_2025_12_21
    owner to postgres;

grant select on minute_candles_2025_12_21 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_21 to admin;

create table minute_candles_2025_12_22
    partition of minute_candles
        FOR VALUES FROM ('2025-12-21 21:00:00+00') TO ('2025-12-22 21:00:00+00');

alter table minute_candles_2025_12_22
    owner to postgres;

grant select on minute_candles_2025_12_22 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_22 to admin;

create table minute_candles_2025_12_23
    partition of minute_candles
        FOR VALUES FROM ('2025-12-22 21:00:00+00') TO ('2025-12-23 21:00:00+00');

alter table minute_candles_2025_12_23
    owner to postgres;

grant select on minute_candles_2025_12_23 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_23 to admin;

create table minute_candles_2025_12_24
    partition of minute_candles
        FOR VALUES FROM ('2025-12-23 21:00:00+00') TO ('2025-12-24 21:00:00+00');

alter table minute_candles_2025_12_24
    owner to postgres;

grant select on minute_candles_2025_12_24 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_24 to admin;

create table minute_candles_2025_12_25
    partition of minute_candles
        FOR VALUES FROM ('2025-12-24 21:00:00+00') TO ('2025-12-25 21:00:00+00');

alter table minute_candles_2025_12_25
    owner to postgres;

grant select on minute_candles_2025_12_25 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_25 to admin;

create table minute_candles_2025_12_26
    partition of minute_candles
        FOR VALUES FROM ('2025-12-25 21:00:00+00') TO ('2025-12-26 21:00:00+00');

alter table minute_candles_2025_12_26
    owner to postgres;

grant select on minute_candles_2025_12_26 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_26 to admin;

create table minute_candles_2025_12_27
    partition of minute_candles
        FOR VALUES FROM ('2025-12-26 21:00:00+00') TO ('2025-12-27 21:00:00+00');

alter table minute_candles_2025_12_27
    owner to postgres;

grant select on minute_candles_2025_12_27 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_27 to admin;

create table minute_candles_2025_12_28
    partition of minute_candles
        FOR VALUES FROM ('2025-12-27 21:00:00+00') TO ('2025-12-28 21:00:00+00');

alter table minute_candles_2025_12_28
    owner to postgres;

grant select on minute_candles_2025_12_28 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_28 to admin;

create table minute_candles_2025_12_29
    partition of minute_candles
        FOR VALUES FROM ('2025-12-28 21:00:00+00') TO ('2025-12-29 21:00:00+00');

alter table minute_candles_2025_12_29
    owner to postgres;

grant select on minute_candles_2025_12_29 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_29 to admin;

create table minute_candles_2025_12_30
    partition of minute_candles
        FOR VALUES FROM ('2025-12-29 21:00:00+00') TO ('2025-12-30 21:00:00+00');

alter table minute_candles_2025_12_30
    owner to postgres;

grant select on minute_candles_2025_12_30 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_30 to admin;

create table minute_candles_2025_12_31
    partition of minute_candles
        FOR VALUES FROM ('2025-12-30 21:00:00+00') TO ('2025-12-31 21:00:00+00');

alter table minute_candles_2025_12_31
    owner to postgres;

grant select on minute_candles_2025_12_31 to tester;

grant delete, insert, references, select, trigger, truncate, update on minute_candles_2025_12_31 to admin;

--Таблица хранения дневных свечей
create table invest_candles.daily_candles
(
    figi                 varchar(255)                              not null,
    time                 timestamp(6) with time zone               not null,
    close                numeric(18, 9)                            not null,
    created_at           timestamp(6) with time zone default now() not null,
    high                 numeric(18, 9)                            not null,
    is_complete          boolean                                   not null,
    low                  numeric(18, 9)                            not null,
    open                 numeric(18, 9)                            not null,
    updated_at           timestamp(6) with time zone default now() not null,
    volume               bigint                                    not null,
    price_change         numeric(18, 9),
    price_change_percent numeric(18, 9),
    candle_type          varchar(20),
    body_size            numeric(18, 9),
    upper_shadow         numeric(18, 9),
    lower_shadow         numeric(18, 9),
    high_low_range       numeric(18, 9),
    average_price        numeric(18, 9),
    primary key (figi, time)
)
    partition by RANGE ("time");

comment on table invest_candles.daily_candles is 'Таблица дневных свечей финансовых инструментов с месячным партиционированием';

comment on column invest_candles.daily_candles.figi is 'Уникальный идентификатор инструмента (Financial Instrument Global Identifier)';

comment on column invest_candles.daily_candles.time is 'Время начала дневной свечи в московской таймзоне';

comment on column invest_candles.daily_candles.close is 'Цена закрытия за день с точностью до 9 знаков после запятой';

comment on column invest_candles.daily_candles.created_at is 'Время создания записи в московской таймзоне';

comment on column invest_candles.daily_candles.high is 'Максимальная цена за день с точностью до 9 знаков после запятой';

comment on column invest_candles.daily_candles.is_complete is 'Флаг завершенности свечи (true - свеча завершена, false - формируется)';

comment on column invest_candles.daily_candles.low is 'Минимальная цена за день с точностью до 9 знаков после запятой';

comment on column invest_candles.daily_candles.open is 'Цена открытия за день с точностью до 9 знаков после запятой';

comment on column invest_candles.daily_candles.updated_at is 'Время последнего обновления записи в московской таймзоне';

comment on column invest_candles.daily_candles.volume is 'Объем торгов за день (количество лотов)';

comment on column invest_candles.daily_candles.price_change is 'Изменение цены за день (close - open) с точностью до 9 знаков после запятой';

comment on column invest_candles.daily_candles.price_change_percent is 'Процентное изменение цены за день с точностью до 4 знаков после запятой';

comment on column invest_candles.daily_candles.candle_type is 'Тип свечи: BULLISH (бычья), BEARISH (медвежья), DOJI (доджи)';

comment on column invest_candles.daily_candles.body_size is 'Размер тела свечи (абсолютное значение изменения цены) с точностью до 9 знаков после запятой';

comment on column invest_candles.daily_candles.upper_shadow is 'Верхняя тень свечи (high - max(open, close)) с точностью до 9 знаков после запятой';

comment on column invest_candles.daily_candles.lower_shadow is 'Нижняя тень свечи (min(open, close) - low) с точностью до 9 знаков после запятой';

comment on column invest_candles.daily_candles.high_low_range is 'Диапазон цен за день (high - low) с точностью до 9 знаков после запятой';

comment on column invest_candles.daily_candles.average_price is 'Средняя цена за день ((high + low + close) / 3) с точностью до 2 знаков после запятой';

alter table invest_candles.daily_candles
    owner to postgres;

create index idx_daily_candles_time
    on invest_candles.daily_candles (time);

create index idx_daily_candles_figi_time
    on invest_candles.daily_candles (figi, time);

create table daily_candles_2024_06
    partition of daily_candles
        FOR VALUES FROM ('2024-05-31 21:00:00+00') TO ('2024-06-30 21:00:00+00');

alter table daily_candles_2024_06
    owner to postgres;

grant select on daily_candles_2024_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2024_06 to admin;

create table daily_candles_2024_07
    partition of daily_candles
        FOR VALUES FROM ('2024-06-30 21:00:00+00') TO ('2024-07-31 21:00:00+00');

alter table daily_candles_2024_07
    owner to postgres;

grant select on daily_candles_2024_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2024_07 to admin;

create table daily_candles_2024_08
    partition of daily_candles
        FOR VALUES FROM ('2024-07-31 21:00:00+00') TO ('2024-08-31 21:00:00+00');

alter table daily_candles_2024_08
    owner to postgres;

grant select on daily_candles_2024_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2024_08 to admin;

create table daily_candles_2024_09
    partition of daily_candles
        FOR VALUES FROM ('2024-08-31 21:00:00+00') TO ('2024-09-30 21:00:00+00');

alter table daily_candles_2024_09
    owner to postgres;

grant select on daily_candles_2024_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2024_09 to admin;

create table daily_candles_2024_10
    partition of daily_candles
        FOR VALUES FROM ('2024-09-30 21:00:00+00') TO ('2024-10-31 21:00:00+00');

alter table daily_candles_2024_10
    owner to postgres;

grant select on daily_candles_2024_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2024_10 to admin;

create table daily_candles_2024_11
    partition of daily_candles
        FOR VALUES FROM ('2024-10-31 21:00:00+00') TO ('2024-11-30 21:00:00+00');

alter table daily_candles_2024_11
    owner to postgres;

grant select on daily_candles_2024_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2024_11 to admin;

create table daily_candles_2024_12
    partition of daily_candles
        FOR VALUES FROM ('2024-11-30 21:00:00+00') TO ('2024-12-31 21:00:00+00');

alter table daily_candles_2024_12
    owner to postgres;

grant select on daily_candles_2024_12 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2024_12 to admin;

create table daily_candles_2025_01
    partition of daily_candles
        FOR VALUES FROM ('2024-12-31 21:00:00+00') TO ('2025-01-31 21:00:00+00');

alter table daily_candles_2025_01
    owner to postgres;

grant select on daily_candles_2025_01 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2025_01 to admin;

create table daily_candles_2025_02
    partition of daily_candles
        FOR VALUES FROM ('2025-01-31 21:00:00+00') TO ('2025-02-28 21:00:00+00');

alter table daily_candles_2025_02
    owner to postgres;

grant select on daily_candles_2025_02 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2025_02 to admin;

create table daily_candles_2025_03
    partition of daily_candles
        FOR VALUES FROM ('2025-02-28 21:00:00+00') TO ('2025-03-31 21:00:00+00');

alter table daily_candles_2025_03
    owner to postgres;

grant select on daily_candles_2025_03 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2025_03 to admin;

create table daily_candles_2025_04
    partition of daily_candles
        FOR VALUES FROM ('2025-03-31 21:00:00+00') TO ('2025-04-30 21:00:00+00');

alter table daily_candles_2025_04
    owner to postgres;

grant select on daily_candles_2025_04 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2025_04 to admin;

create table daily_candles_2025_05
    partition of daily_candles
        FOR VALUES FROM ('2025-04-30 21:00:00+00') TO ('2025-05-31 21:00:00+00');

alter table daily_candles_2025_05
    owner to postgres;

grant select on daily_candles_2025_05 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2025_05 to admin;

create table daily_candles_2025_06
    partition of daily_candles
        FOR VALUES FROM ('2025-05-31 21:00:00+00') TO ('2025-06-30 21:00:00+00');

alter table daily_candles_2025_06
    owner to postgres;

grant select on daily_candles_2025_06 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2025_06 to admin;

create table daily_candles_2025_07
    partition of daily_candles
        FOR VALUES FROM ('2025-06-30 21:00:00+00') TO ('2025-07-31 21:00:00+00');

alter table daily_candles_2025_07
    owner to postgres;

grant select on daily_candles_2025_07 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2025_07 to admin;

create table daily_candles_2025_08
    partition of daily_candles
        FOR VALUES FROM ('2025-07-31 21:00:00+00') TO ('2025-08-31 21:00:00+00');

alter table daily_candles_2025_08
    owner to postgres;

grant select on daily_candles_2025_08 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2025_08 to admin;

create table daily_candles_2025_09
    partition of daily_candles
        FOR VALUES FROM ('2025-08-31 21:00:00+00') TO ('2025-09-30 21:00:00+00');

alter table daily_candles_2025_09
    owner to postgres;

grant select on daily_candles_2025_09 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2025_09 to admin;

create table daily_candles_2025_10
    partition of daily_candles
        FOR VALUES FROM ('2025-09-30 21:00:00+00') TO ('2025-10-31 21:00:00+00');

alter table daily_candles_2025_10
    owner to postgres;

grant select on daily_candles_2025_10 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2025_10 to admin;

create table daily_candles_2025_11
    partition of daily_candles
        FOR VALUES FROM ('2025-10-31 21:00:00+00') TO ('2025-11-30 21:00:00+00');

alter table daily_candles_2025_11
    owner to postgres;

grant select on daily_candles_2025_11 to tester;

grant delete, insert, references, select, trigger, truncate, update on daily_candles_2025_11 to admin;

create table daily_candles_2025_12
    partition of daily_candles
        FOR VALUES FROM ('2025-11-30 21:00:00+00') TO ('2025-12-31 21:00:00+00');

alter table daily_candles_2025_12
    owner to postgres;


-- Таблица для хранения специальных торговых часов инструментов
create table invest_candles.special_trading_hours
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

-- Устанавливаем владельца
alter table invest_dq.special_trading_hours owner to postgres;

-- Таблица для хранения проблем качества данных по свечам
create table invest_candles.data_quality_issues
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

comment on table invest_candles.data_quality_issues is 'Таблица для хранения проблем качества данных по свечам';

comment on column invest_candles.data_quality_issues.id is 'Уникальный идентификатор записи';
comment on column invest_candles.data_quality_issues.task_id is 'ID задачи, которая выявила проблему';
comment on column invest_candles.data_quality_issues.check_name is 'Название проверки';
comment on column invest_candles.data_quality_issues.entity_type is 'Тип сущности (shares, futures, etc.)';
comment on column invest_candles.data_quality_issues.entity_id is 'ID сущности (обычно FIGI)';
comment on column invest_candles.data_quality_issues.trade_date is 'Дата торгов, к которой относится проблема';
comment on column invest_candles.data_quality_issues.metric is 'Название метрики';
comment on column invest_candles.data_quality_issues.status is 'Статус проблемы: ERROR, WARNING, INFO';
comment on column invest_candles.data_quality_issues.message is 'Описание проблемы';
comment on column invest_candles.data_quality_issues.expected_numeric is 'Ожидаемое числовое значение';
comment on column invest_candles.data_quality_issues.actual_numeric is 'Фактическое числовое значение';
comment on column invest_candles.data_quality_issues.diff_numeric is 'Разность между ожидаемым и фактическим значением';
comment on column invest_candles.data_quality_issues.details is 'Дополнительные детали в формате JSON';
comment on column invest_candles.data_quality_issues.created_at is 'Дата и время создания записи';

-- Устанавливаем владельца
alter table invest_candles.data_quality_issues owner to postgres;

-- Функция для добавления специальных торговых часов
create or replace function invest_candles.add_special_trading_hours(
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
    update invest_candles.special_trading_hours 
    set is_active = false, updated_at = now()
    where figi = p_figi 
    and day_type = p_day_type 
    and instrument_type = p_instrument_type
    and is_active = true;
    
    -- Добавляем новую запись
    insert into invest_candles.special_trading_hours (
        figi, instrument_type, day_type, start_hour, start_minute, 
        end_hour, end_minute, description, created_by
    ) values (
        p_figi, p_instrument_type, p_day_type, p_start_hour, p_start_minute,
        p_end_hour, p_end_minute, p_description, p_created_by
    ) returning id into v_id;
    
    return v_id;
end;
$$;

comment on function invest_candles.add_special_trading_hours(varchar, varchar, varchar, integer, integer, integer, integer, text, varchar) is 'Добавляет специальные торговые часы для инструмента';

-- Функция для получения торговых часов инструмента
create or replace function invest_candles.get_trading_hours(
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
    from invest_candles.special_trading_hours sth
    where sth.figi = p_figi
    and sth.is_active = true
    order by sth.day_type desc, sth.created_at desc;
    
    -- Если ничего не найдено, функция возвращает пустой результат
end;
$$;

comment on function invest_candles.get_trading_hours(varchar) is 'Возвращает специальные торговые часы для инструмента по FIGI, или пустой результат если не найдено';


-- Функция для удаления специальных торговых часов
create or replace function invest_candles.remove_special_trading_hours(
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
    update invest_candles.special_trading_hours 
    set is_active = false, updated_at = now()
    where figi = p_figi 
    and (p_instrument_type is null or instrument_type = p_instrument_type)
    and (p_day_type is null or day_type = p_day_type)
    and is_active = true;
    
    get diagnostics v_count = row_count;
    return v_count;
end;
$$;

comment on function invest_candles.remove_special_trading_hours(varchar, varchar, varchar) is 'Удаляет специальные торговые часы для инструмента';


-- ============================================================================
-- СИНОНИМЫ И VIEWS В СХЕМЕ INVEST
-- ============================================================================

-- View для специальных торговых часов в схеме invest
create or replace view invest.special_trading_hours as
select 
    sth.id,
    sth.figi,
    sth.instrument_type,
    sth.day_type,
    sth.start_hour,
    sth.start_minute,
    sth.end_hour,
    sth.end_minute,
    sth.description,
    sth.created_by,
    sth.created_at,
    sth.updated_at,
    sth.is_active,
    -- Добавляем информацию об инструменте
    CASE 
        WHEN sth.instrument_type = 'shares' THEN s.name
        WHEN sth.instrument_type = 'futures' THEN f.ticker
        WHEN sth.instrument_type = 'indicatives' THEN i.name
    END AS instrument_name,
    CASE 
        WHEN sth.instrument_type = 'shares' THEN s.ticker
        WHEN sth.instrument_type = 'futures' THEN f.ticker
        WHEN sth.instrument_type = 'indicatives' THEN i.ticker
    END AS instrument_ticker
from invest_candles.special_trading_hours sth
left join invest_ref.shares s on sth.figi = s.figi and sth.instrument_type = 'shares'
left join invest_ref.futures f on sth.figi = f.figi and sth.instrument_type = 'futures'
left join invest_ref.indicatives i on sth.figi = i.figi and sth.instrument_type = 'indicatives';

comment on view invest.special_trading_hours is 'Синоним для таблицы special_trading_hours из схемы invest_candles с дополнительной информацией об инструментах';

-- Устанавливаем владельца view
alter view invest.special_trading_hours owner to postgres;

-- Предоставляем права доступа на view

-- Синоним функции добавления торговых часов в схеме invest
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
language plpgsql
as $$
begin
    return invest_candles.add_special_trading_hours(
        p_figi, p_instrument_type, p_day_type,
        p_start_hour, p_start_minute, p_end_hour, p_end_minute,
        p_description, p_created_by
    );
end;
$$;

comment on function invest.add_special_trading_hours(varchar, varchar, varchar, integer, integer, integer, integer, text, varchar) is 'Синоним для функции invest_candles.add_special_trading_hours - добавляет специальные торговые часы для инструмента';

alter function invest.add_special_trading_hours(varchar, varchar, varchar, integer, integer, integer, integer, text, varchar) owner to postgres;

-- Синоним функции получения торговых часов в схеме invest
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
    select * from invest_candles.get_trading_hours(p_figi);
$$;

comment on function invest.get_trading_hours(varchar) is 'Синоним для функции invest_candles.get_trading_hours - возвращает специальные торговые часы для инструмента по FIGI';

alter function invest.get_trading_hours(varchar) owner to postgres;

-- Синоним функции удаления торговых часов в схеме invest
create or replace function invest.remove_special_trading_hours(
    p_figi varchar(255),
    p_instrument_type varchar(20) default null,
    p_day_type varchar(20) default null
)
returns integer
language plpgsql
as $$
begin
    return invest_candles.remove_special_trading_hours(
        p_figi, p_instrument_type, p_day_type
    );
end;
$$;

comment on function invest.remove_special_trading_hours(varchar, varchar, varchar) is 'Синоним для функции invest_candles.remove_special_trading_hours - удаляет специальные торговые часы для инструмента';

alter function invest.remove_special_trading_hours(varchar, varchar, varchar) owner to postgres;


-- ============================================================================
-- ПРИМЕРЫ ИСПОЛЬЗОВАНИЯ ФУНКЦИЙ ДЛЯ РАБОТЫ СО СПЕЦИАЛЬНЫМИ ТОРГОВЫМИ ЧАСАМИ
-- ============================================================================

-- Пример 1: Добавление специальных торговых часов для акции в будние дни
-- Устанавливаем торговые часы с 10:00 до 18:45 для акции SBER
-- Использование через схему invest_candles:
SELECT invest_candles.add_special_trading_hours(
    p_figi => 'BBG004730N88',                    -- FIGI инструмента SBER
    p_instrument_type => 'shares',                -- Тип инструмента: акция
    p_day_type => 'weekday',                     -- Тип дня: будние дни
    p_start_hour => 10,                          -- Начало торгов: 10:00
    p_start_minute => 0,
    p_end_hour => 18,                            -- Конец торгов: 18:45
    p_end_minute => 45,
    p_description => 'Стандартные торговые часы для акций MOEX',  -- Описание
    p_created_by => 'admin'                      -- Кто создал запись
);

-- Использование через схему invest (синоним):
SELECT invest.add_special_trading_hours(
    'BBG004730N88', 'shares', 'weekday',
    10, 0, 18, 45,
    'Стандартные торговые часы для акций MOEX',
    'admin'
);

-- Пример 2: Добавление специальных торговых часов для фьючерса в выходные дни
-- Устанавливаем торговые часы с 10:00 до 23:50 для фьючерса Si
SELECT invest_candles.add_special_trading_hours(
    'BBG004730ZJ9',                              -- FIGI фьючерса
    'futures',                                   -- Тип: фьючерс
    'weekend',                                   -- Тип дня: выходные
    10,                                          -- Начало: 10:00
    0,
    23,                                          -- Конец: 23:50
    50,
    'Торговые часы для фьючерсов в выходные дни',
    'system'
);

-- Пример 3: Добавление торговых часов для всех дней недели
-- Устанавливаем торговые часы с 9:50 до 19:00 для всех дней
SELECT invest_candles.add_special_trading_hours(
    'BBG004730N88',                              -- FIGI инструмента
    'shares',                                    -- Тип: акция
    'all',                                       -- Тип дня: все дни недели
    9,                                           -- Начало: 9:50
    50,
    19,                                          -- Конец: 19:00
    0,
    'Расширенные торговые часы для всех дней',
    'user'
);

-- Пример 4: Получение всех активных торговых часов для инструмента
-- Возвращает все специальные торговые часы для указанного FIGI
-- Использование через схему invest_candles:
SELECT * FROM invest_candles.get_trading_hours('BBG004730N88');

-- Использование через схему invest (синоним):
SELECT * FROM invest.get_trading_hours('BBG004730N88');

-- Использование через view в схеме invest (с информацией об инструменте):
SELECT * FROM invest.special_trading_hours 
WHERE figi = 'BBG004730N88' AND is_active = true;

-- Пример 5: Получение торговых часов с фильтрацией по типу дня
-- Получаем только торговые часы для будних дней
SELECT * 
FROM invest_candles.get_trading_hours('BBG004730N88')
WHERE day_type = 'weekday';

-- Пример 6: Получение торговых часов с форматированием времени
-- Форматируем время в читаемый вид
SELECT 
    figi,
    instrument_type,
    day_type,
    start_hour || ':' || LPAD(start_minute::text, 2, '0') AS start_time,
    end_hour || ':' || LPAD(end_minute::text, 2, '0') AS end_time,
    description,
    is_active,
    created_at
FROM invest_candles.special_trading_hours
WHERE figi = 'BBG004730N88'
  AND is_active = true
ORDER BY day_type DESC, created_at DESC;

-- Пример 7: Удаление всех специальных торговых часов для инструмента
-- Деактивирует все записи для указанного FIGI независимо от типа инструмента и дня
SELECT invest_candles.remove_special_trading_hours(
    p_figi => 'BBG004730N88',                    -- FIGI инструмента
    p_instrument_type => NULL,                   -- NULL = все типы инструментов
    p_day_type => NULL                           -- NULL = все типы дней
);
-- Возвращает количество деактивированных записей

-- Пример 8: Удаление торговых часов только для акций (shares)
-- Деактивирует только записи для акций, оставляя другие типы инструментов
SELECT invest_candles.remove_special_trading_hours(
    'BBG004730N88',                              -- FIGI инструмента
    'shares',                                    -- Только для акций
    NULL                                         -- Все типы дней
);

-- Пример 9: Удаление торговых часов только для будних дней
-- Деактивирует только записи для будних дней, оставляя выходные
SELECT invest_candles.remove_special_trading_hours(
    'BBG004730N88',                              -- FIGI инструмента
    NULL,                                        -- Все типы инструментов
    'weekday'                                    -- Только будние дни
);

-- Пример 10: Удаление конкретной комбинации (акции + будние дни)
-- Деактивирует только записи для акций в будние дни
SELECT invest_candles.remove_special_trading_hours(
    'BBG004730N88',                              -- FIGI инструмента
    'shares',                                    -- Только акции
    'weekday'                                    -- Только будние дни
);

-- Пример 11: Проверка существования специальных торговых часов перед добавлением
-- Сначала проверяем, есть ли уже активные записи
DO $$
DECLARE
    v_count integer;
    v_id bigint;
BEGIN
    -- Проверяем наличие активных записей
    SELECT COUNT(*) INTO v_count
    FROM invest_candles.special_trading_hours
    WHERE figi = 'BBG004730N88'
      AND instrument_type = 'shares'
      AND day_type = 'weekday'
      AND is_active = true;
    
    IF v_count = 0 THEN
        -- Добавляем, если нет активных записей
        SELECT invest_candles.add_special_trading_hours(
            'BBG004730N88', 'shares', 'weekday',
            10, 0, 18, 45,
            'Первая запись торговых часов',
            'system'
        ) INTO v_id;
        
        RAISE NOTICE 'Добавлена запись с ID: %', v_id;
    ELSE
        RAISE NOTICE 'Уже существует % активных записей', v_count;
    END IF;
END $$;

-- Пример 12: Получение торговых часов с подробной информацией
-- Расширенный запрос с информацией об инструменте
SELECT 
    sth.figi,
    sth.instrument_type,
    sth.day_type,
    sth.start_hour,
    sth.start_minute,
    sth.end_hour,
    sth.end_minute,
    sth.description,
    sth.created_by,
    sth.created_at,
    sth.updated_at,
    sth.is_active,
    CASE 
        WHEN sth.instrument_type = 'shares' THEN s.name
        WHEN sth.instrument_type = 'futures' THEN f.ticker
        WHEN sth.instrument_type = 'indicatives' THEN i.name
    END AS instrument_name
FROM invest_candles.special_trading_hours sth
LEFT JOIN invest_ref.shares s ON sth.figi = s.figi AND sth.instrument_type = 'shares'
LEFT JOIN invest_ref.futures f ON sth.figi = f.figi AND sth.instrument_type = 'futures'
LEFT JOIN invest_ref.indicatives i ON sth.figi = i.figi AND sth.instrument_type = 'indicatives'
WHERE sth.figi = 'BBG004730N88'
  AND sth.is_active = true
ORDER BY sth.day_type DESC, sth.created_at DESC;

-- Пример 13: Массовое добавление торговых часов для нескольких инструментов
-- Используем цикл для добавления часов для списка FIGI
DO $$
DECLARE
    v_figi_list varchar(255)[] := ARRAY['BBG004730N88', 'BBG004730ZJ9', 'BBG0013HJJ31'];
    v_figi varchar(255);
    v_id bigint;
BEGIN
    FOREACH v_figi IN ARRAY v_figi_list
    LOOP
        SELECT invest_candles.add_special_trading_hours(
            v_figi,
            'shares',
            'weekday',
            10, 0, 18, 45,
            'Массовое добавление торговых часов',
            'batch_script'
        ) INTO v_id;
        
        RAISE NOTICE 'Добавлены торговые часы для % с ID: %', v_figi, v_id;
    END LOOP;
END $$;

-- Пример 14: Получение торговых часов только для акций через view в схеме invest
SELECT 
    figi,
    instrument_ticker,
    instrument_name,
    day_type,
    start_hour || ':' || LPAD(start_minute::text, 2, '0') AS start_time,
    end_hour || ':' || LPAD(end_minute::text, 2, '0') AS end_time,
    description,
    is_active
FROM invest.special_trading_hours
WHERE instrument_type = 'shares'
  AND is_active = true
ORDER BY instrument_ticker, day_type DESC;

-- Пример 15: Получение торговых часов только для фьючерсов через view в схеме invest
SELECT 
    figi,
    instrument_ticker,
    day_type,
    start_hour || ':' || LPAD(start_minute::text, 2, '0') AS start_time,
    end_hour || ':' || LPAD(end_minute::text, 2, '0') AS end_time,
    description,
    is_active
FROM invest.special_trading_hours
WHERE instrument_type = 'futures'
  AND is_active = true
ORDER BY instrument_ticker, day_type DESC;

-- Пример 16: Получение торговых часов для конкретной акции с информацией об инструменте
SELECT 
    sth.figi,
    sth.instrument_ticker,
    sth.instrument_name,
    sth.day_type,
    sth.start_hour,
    sth.start_minute,
    sth.end_hour,
    sth.end_minute,
    sth.description,
    sth.created_by,
    sth.created_at,
    s.currency,
    s.exchange
FROM invest.special_trading_hours sth
LEFT JOIN invest_ref.shares s ON sth.figi = s.figi
WHERE sth.figi = 'BBG004730N88'
  AND sth.instrument_type = 'shares'
  AND sth.is_active = true
ORDER BY sth.day_type DESC;

-- Пример 17: Получение торговых часов для конкретного фьючерса с информацией об инструменте
SELECT 
    sth.figi,
    sth.instrument_ticker,
    sth.day_type,
    sth.start_hour,
    sth.start_minute,
    sth.end_hour,
    sth.end_minute,
    sth.description,
    f.basic_asset,
    f.expiration_date,
    f.currency
FROM invest.special_trading_hours sth
LEFT JOIN invest_ref.futures f ON sth.figi = f.figi
WHERE sth.figi = 'BBG004730ZJ9'
  AND sth.instrument_type = 'futures'
  AND sth.is_active = true
ORDER BY sth.day_type DESC;
