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
- `daily_volume_aggregation` — общее историческое агрегирование по дням и сессиям
- `today_volume_aggregation` — агрегирование только за текущий день

Оба представления строятся из `candles` (таймзона `Europe/Moscow`) и содержат в т.ч.:
- `figi, trade_date, morning_session_volume, main_session_volume, evening_session_volume, weekend_session_volume, total_daily_volume`
- `morning_session_candles, main_session_candles, evening_session_candles, weekend_session_candles, total_daily_candles`
- `first_candle_time, last_candle_time, last_updated`

## Сервисные функции (PL/pgSQL)
- `invest.update_today_volume_aggregation()` → `REFRESH MATERIALIZED VIEW invest.today_volume_aggregation`
- `invest.update_daily_volume_aggregation()` → `REFRESH MATERIALIZED VIEW invest.daily_volume_aggregation`

## Индексы (рекомендации)
- `candles(figi, time)` — PK
- `daily_volume_aggregation(figi, trade_date)` — UNIQUE
- `today_volume_aggregation(figi)` — UNIQUE
