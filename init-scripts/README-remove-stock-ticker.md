# Удаление столбца stock_ticker из таблицы futures

## Описание

Этот скрипт удаляет столбец `stock_ticker` из таблицы `invest.futures`, так как это поле больше не используется в API ответах.

## Причина удаления

- Поле `stock_ticker` не предоставляется T-Invest API для фьючерсов
- В `FutureDto` это поле было удалено
- Столбец в базе данных больше не нужен

## Скрипты

### 1. 06-remove-stock-ticker-column.sql
Отдельный скрипт для удаления столбца `stock_ticker` с проверками.

**Применение:**
```sql
\i init-scripts/06-remove-stock-ticker-column.sql
```

### 2. 05-update-futures-table.sql
Обновленный скрипт, который также удаляет столбец `stock_ticker` при обновлении таблицы.

## Что делает скрипт

1. **Удаляет столбец:**
   ```sql
   ALTER TABLE invest.futures DROP COLUMN IF EXISTS stock_ticker;
   ```

2. **Проверяет удаление:**
   - Проверяет, что столбец действительно удален
   - Выводит уведомление о результате

3. **Показывает структуру таблицы:**
   - Выводит текущую структуру таблицы `futures`
   - Показывает все оставшиеся столбцы

## Структура таблицы после удаления

```sql
CREATE TABLE invest.futures (
    figi VARCHAR(50) NOT NULL PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL,
    asset_type VARCHAR(50) NOT NULL,
    basic_asset VARCHAR(50) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    exchange VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);
```

## Безопасность

- Скрипт использует `IF EXISTS`, поэтому безопасен для повторного выполнения
- Не удаляет данные из других столбцов
- Только удаляет ненужный столбец

## Применение

1. **Если таблица уже существует:**
   ```sql
   \i init-scripts/06-remove-stock-ticker-column.sql
   ```

2. **Если обновляете всю таблицу:**
   ```sql
   \i init-scripts/05-update-futures-table.sql
   ```

## Проверка

После выполнения скрипта проверьте, что столбец удален:

```sql
SELECT column_name 
FROM information_schema.columns 
WHERE table_schema = 'invest' 
AND table_name = 'futures' 
AND column_name = 'stock_ticker';
```

Результат должен быть пустым (0 строк).
