# SQL-скрипты для настройки базы данных

## Обзор

Этот набор SQL-скриптов настраивает базу данных для работы с Investment Data Loader Service.

## Порядок применения скриптов

### 1. Базовые скрипты
```sql
-- Создание материализованных представлений
\i init-scripts/01-create-materialized-view.sql

-- Создание функций аналитики сессий
\i init-scripts/02-create-session-analytics-functions.sql

-- Исправление проблем с таймзонами
\i init-scripts/03-fix-timezone-issue.sql
```

### 2. Создание таблицы futures

#### Если таблица не существует:
```sql
\i init-scripts/04-create-futures-table.sql
```

#### Если таблица уже существует:
```sql
\i init-scripts/05-update-futures-table.sql
```

### 3. Удаление столбца stock_ticker
```sql
\i init-scripts/06-remove-stock-ticker-column.sql
```

### 4. Создание таблицы indicatives
```sql
\i init-scripts/07-create-indicatives-table.sql
```

## Структура таблиц

### Таблица futures

После применения всех скриптов таблица `invest.futures` будет иметь следующую структуру:

```sql
CREATE TABLE invest.futures (
    figi VARCHAR(50) NOT NULL PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL,
    asset_type VARCHAR(50) NOT NULL,
    basic_asset VARCHAR(50) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    exchange VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow'),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow')
);
```

### Таблица indicatives

Таблица `invest.indicatives` для индикативных инструментов (индексы, товары):

```sql
CREATE TABLE invest.indicatives (
    figi VARCHAR(50) NOT NULL PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    exchange VARCHAR(50) NOT NULL,
    class_code VARCHAR(20),
    uid VARCHAR(50),
    sell_available_flag BOOLEAN DEFAULT false,
    buy_available_flag BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow'),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow')
);
```

## Индексы

### Для таблицы futures:
- `idx_futures_ticker` - по полю ticker
- `idx_futures_asset_type` - по полю asset_type
- `idx_futures_currency` - по полю currency
- `idx_futures_exchange` - по полю exchange
- `idx_futures_basic_asset` - по полю basic_asset

### Для таблицы indicatives:
- `idx_indicatives_ticker` - по полю ticker
- `idx_indicatives_currency` - по полю currency
- `idx_indicatives_exchange` - по полю exchange
- `idx_indicatives_class_code` - по полю class_code
- `idx_indicatives_uid` - по полю uid

## Триггеры

- `update_futures_updated_at` - автоматически обновляет поле `updated_at` при изменении записи

## Временные зоны

Все временные поля используют таймзону **UTC+3 (Europe/Moscow)**.

## Проверка

После применения скриптов проверьте структуру таблиц:

### Проверка таблицы futures:
```sql
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_schema = 'invest' 
  AND table_name = 'futures'
ORDER BY ordinal_position;
```

### Проверка таблицы indicatives:
```sql
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_schema = 'invest' 
  AND table_name = 'indicatives'
ORDER BY ordinal_position;
```

### Проверка всех таблиц в схеме invest:
```sql
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'invest' 
ORDER BY table_name;
```

## Откат изменений

Если нужно откатить изменения:

```sql
-- Удалить таблицы (ОСТОРОЖНО: удалит все данные!)
DROP TABLE IF EXISTS invest.futures CASCADE;
DROP TABLE IF EXISTS invest.indicatives CASCADE;

-- Удалить функцию обновления времени
DROP FUNCTION IF EXISTS update_updated_at_column();
```

## Безопасность

- Все скрипты используют `IF EXISTS` и `IF NOT EXISTS`
- Скрипты можно выполнять многократно без ошибок
- Данные не удаляются при обновлении структуры
