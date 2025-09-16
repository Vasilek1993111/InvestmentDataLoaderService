# База данных

Схема по умолчанию: `invest`.

## Основные таблицы
- `shares(figi, ticker, name, currency, exchange, ...)`
- `futures(figi, ticker, asset_type, basic_asset, currency, exchange, stock_ticker, ...)`
- `indicatives(figi, ticker, name, currency, exchange, class_code, uid, ...)`
- `candles(figi, time, open, high, low, close, volume, is_complete, created_at, updated_at)`
- `close_prices(price_date, figi, instrument_type, close_price, currency, exchange, created_at, updated_at)`
- `open_prices(price_date, figi, instrument_type, open_price, currency, exchange, created_at, updated_at)`
- `shares_aggregated_data(figi, avg_volume_morning, avg_volume_weekend, total_trading_days, total_weekend_days, last_calculated, created_at, updated_at)`
- `futures_aggregated_data(figi, avg_volume_morning, avg_volume_evening, avg_volume_weekend, total_trading_days, total_weekend_days, last_calculated, created_at, updated_at)`

## Материализованные представления
- `daily_volume_aggregation` — общее агрегирование по figi за все время с аналитикой по сессиям
- `today_volume_aggregation` — агрегирование только за текущий день с группировкой по figi

Оба представления строятся из `candles` (таймзона `Europe/Moscow`) и содержат:

### Общие поля:
- `figi` — идентификатор инструмента
- `total_volume` — общий объем торгов за все время (daily) / за день (today)
- `total_candles` — общее количество свечей за все время (daily) / за день (today)
- `avg_volume_per_candle` — средний объем на одну свечу за все время (daily) / за день (today), округлено до 2 знаков

### Поля только для today_volume_aggregation:
- `trade_date` — дата торгов (DATE)

### Аналитика по сессиям:
- **Утренняя сессия (09:00-10:00):**
  - `morning_session_volume` — объем торгов в утренней сессии
  - `morning_session_candles` — количество свечей в утренней сессии
  - `morning_avg_volume_per_candle` — средний объем на свечу в утренней сессии

- **Основная сессия (10:00-18:45):**
  - `main_session_volume` — объем торгов в основной сессии
  - `main_session_candles` — количество свечей в основной сессии
  - `main_avg_volume_per_candle` — средний объем на свечу в основной сессии

- **Вечерняя сессия (19:05-23:50):**
  - `evening_session_volume` — объем торгов в вечерней сессии
  - `evening_session_candles` — количество свечей в вечерней сессии
  - `evening_avg_volume_per_candle` — средний объем на свечу в вечерней сессии

- **Сессия выходного дня:**
  - `weekend_session_volume` — объем торгов в выходные дни
  - `weekend_session_candles` — количество свечей в выходные дни
  - `weekend_avg_volume_per_candle` — средний объем на свечу в выходные дни

### Временные метки:
- `first_candle_time` — время первой свечи за день
- `last_candle_time` — время последней свечи за день
- `last_updated` — время последнего обновления записи

## Сервисные функции (PL/pgSQL)

### Обновление материализованных представлений:
- `invest.update_today_volume_aggregation()` → `REFRESH MATERIALIZED VIEW invest.today_volume_aggregation`
- `invest.update_daily_volume_aggregation()` → `REFRESH MATERIALIZED VIEW invest.daily_volume_aggregation`

### Аналитические функции:
- `invest.get_session_analytics(figi, start_date, end_date)` — получение аналитики по сессиям за период
- `invest.get_today_session_analytics(figi)` — получение аналитики по сессиям за сегодня

Все функции возвращают таблицу с полями:
- `figi, trade_date, total_volume, total_candles, avg_volume_per_candle`
- `morning_session_volume, morning_session_candles, morning_avg_volume_per_candle`
- `main_session_volume, main_session_candles, main_avg_volume_per_candle`
- `evening_session_volume, evening_session_candles, evening_avg_volume_per_candle`
- `weekend_session_volume, weekend_session_candles, weekend_avg_volume_per_candle`

## Индексы (рекомендации)
- `candles(figi, time)` — PK
- `daily_volume_aggregation(figi, trade_date)` — UNIQUE
- `today_volume_aggregation(figi)` — UNIQUE
