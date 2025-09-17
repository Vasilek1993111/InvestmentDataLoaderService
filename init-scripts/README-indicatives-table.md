# Создание таблицы indicatives

## Описание

Скрипт `07-create-indicatives-table.sql` создает таблицу `invest.indicatives` для хранения индикативных инструментов (индексы, товары и другие) из T-Invest API.

## Структура таблицы

### Основные поля:
- `figi` (VARCHAR(50), PRIMARY KEY) - FIGI инструмента
- `ticker` (VARCHAR(20), NOT NULL) - Тикер инструмента
- `name` (VARCHAR(255), NOT NULL) - Название инструмента
- `currency` (VARCHAR(10), NOT NULL) - Валюта инструмента
- `exchange` (VARCHAR(50), NOT NULL) - Биржа/площадка

### Дополнительные поля:
- `class_code` (VARCHAR(20)) - Код класса инструмента
- `uid` (VARCHAR(50)) - Уникальный идентификатор
- `sell_available_flag` (BOOLEAN, DEFAULT false) - Флаг доступности для продажи
- `buy_available_flag` (BOOLEAN, DEFAULT false) - Флаг доступности для покупки

### Служебные поля:
- `created_at` (TIMESTAMP WITH TIME ZONE) - Дата создания записи (UTC+3)
- `updated_at` (TIMESTAMP WITH TIME ZONE) - Дата последнего обновления записи (UTC+3)

## Индексы

Созданы индексы для оптимизации запросов:
- `idx_indicatives_ticker` - по тикеру
- `idx_indicatives_currency` - по валюте
- `idx_indicatives_exchange` - по бирже
- `idx_indicatives_class_code` - по коду класса
- `idx_indicatives_uid` - по UID

## Пример данных

```json
{
    "figi": "BBG000M09F01",
    "ticker": "ALMN",
    "name": "Алюминий",
    "currency": "usd",
    "exchange": "Issuance",
    "classCode": "",
    "uid": "ac55d547-fdf2-437a-94c5-bce9a50feb83",
    "sellAvailableFlag": false,
    "buyAvailableFlag": false
}
```

## Применение скрипта

```bash
# Подключение к PostgreSQL
psql -h localhost -p 5434 -U postgres -d postgres

# Выполнение скрипта
\i init-scripts/07-create-indicatives-table.sql
```

## Проверка создания

```sql
-- Проверка структуры таблицы
\d invest.indicatives

-- Проверка индексов
\di invest.idx_indicatives_*

-- Проверка комментариев
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

## Связанные файлы

- `src/main/java/com/example/InvestmentDataLoaderService/entity/IndicativeEntity.java` - JPA сущность
- `src/main/java/com/example/InvestmentDataLoaderService/repository/IndicativeRepository.java` - Репозиторий
- `src/main/java/com/example/InvestmentDataLoaderService/dto/IndicativeDto.java` - DTO
