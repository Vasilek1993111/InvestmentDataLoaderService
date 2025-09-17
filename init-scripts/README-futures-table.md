# Создание и обновление таблицы futures

## Описание

Эти скрипты создают и обновляют таблицу `invest.futures` для хранения данных о фьючерсах из T-Invest API.

## Структура таблицы

Таблица содержит следующие поля:
- `figi` (VARCHAR(50), PRIMARY KEY) - уникальный идентификатор инструмента
- `ticker` (VARCHAR(20), NOT NULL) - тикер инструмента
- `asset_type` (VARCHAR(50), NOT NULL) - тип базового актива
- `basic_asset` (VARCHAR(50), NOT NULL) - базовый актив фьючерса
- `currency` (VARCHAR(10), NOT NULL) - валюта инструмента
- `exchange` (VARCHAR(50), NOT NULL) - биржа
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL) - дата создания записи
- `updated_at` (TIMESTAMP WITH TIME ZONE, NOT NULL) - дата последнего обновления

## Скрипты

### 1. 04-create-futures-table.sql
Создает новую таблицу `invest.futures` с полной структурой.

**Применение:**
```sql
\i init-scripts/04-create-futures-table.sql
```

### 2. 05-update-futures-table.sql
Обновляет существующую таблицу `invest.futures`:
- Удаляет поле `stock_ticker`
- Добавляет поля `created_at` и `updated_at`
- Создает необходимые индексы
- Настраивает триггеры для автоматического обновления `updated_at`

**Применение:**
```sql
\i init-scripts/05-update-futures-table.sql
```

## Индексы

Создаются следующие индексы для оптимизации запросов:
- `idx_futures_ticker` - по полю ticker
- `idx_futures_asset_type` - по полю asset_type
- `idx_futures_currency` - по полю currency
- `idx_futures_exchange` - по полю exchange
- `idx_futures_basic_asset` - по полю basic_asset

## Триггеры

Настроен триггер `update_futures_updated_at`, который автоматически обновляет поле `updated_at` при изменении записи.

## Пример данных

```json
{
    "figi": "FUTZINC03260",
    "ticker": "ZCH6",
    "assetType": "TYPE_COMMODITY",
    "basicAsset": "ZINC",
    "currency": "rub",
    "exchange": "forts_futures_weekend"
}
```

## Применение

1. Если таблица не существует - выполните `04-create-futures-table.sql`
2. Если таблица уже существует - выполните `05-update-futures-table.sql`
3. Перезапустите приложение для применения изменений в Entity
