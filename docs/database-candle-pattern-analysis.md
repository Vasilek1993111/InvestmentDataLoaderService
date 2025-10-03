# Анализ паттернов свечей (База данных)

## Описание

Модуль анализа паттернов свечей предназначен для поиска ситуаций, когда тип свечи (BULLISH или BEARISH) остается одинаковым в течение заданного количества торговых дней подряд. Это может указывать на устойчивые тренды в движении цены инструмента.

## Компоненты базы данных

### Таблица `candle_pattern_analysis`

Таблица для хранения результатов анализа паттернов свечей.

**Схема:** `invest_candles`

**Структура:**
- `id` - уникальный идентификатор записи (bigserial)
- `figi` - идентификатор финансового инструмента (varchar(255))
- `analysis_date` - дата проведения анализа (date)
- `pattern_start_date` - дата начала паттерна (date)
- `pattern_end_date` - дата окончания паттерна (date)
- `candle_type` - тип свечи в паттерне: BULLISH/BEARISH (varchar(20))
- `consecutive_days` - количество торговых дней подряд с одинаковым типом свечи (integer)
- `avg_volume` - средний объем торгов за период паттерна (bigint)
- `avg_price_change` - среднее изменение цены за день (numeric(18,9))
- `total_price_change` - общее изменение цены за весь период (numeric(18,9))
- `created_at` - время создания записи (timestamp with time zone)

**Ограничения:**
- `chk_candle_type`: тип свечи должен быть 'BULLISH' или 'BEARISH'
- `chk_consecutive_days`: количество дней должно быть >= 2
- Первичный ключ: `id`

### Функция `analyze_candle_patterns(p_analysis_date date, p_consecutive_days integer default 5)`

Основная функция для анализа паттернов свечей.

**Схема:** `invest_candles`

**Параметры:**
- `p_analysis_date` - дата анализа (анализируются N торговых дней назад от этой даты, не включая её)
- `p_consecutive_days` - количество торговых дней подряд для поиска паттерна (по умолчанию 5, минимум 2)

**Логика работы:**
1. Проверяет корректность параметра количества дней (минимум 2)
2. Определяет период анализа: N торговых дней назад от входной даты
3. **Исключает выходные дни** (субботу и воскресенье) из анализа
4. Ищет последовательности из N+ торговых дней с одинаковым типом свечи
5. Сохраняет найденные паттерны в таблицу `candle_pattern_analysis`
6. Логирует процесс выполнения в `system_logs`

**Примеры использования:**
```sql
-- Анализ паттернов на 20 января 2024 (5 торговых дней по умолчанию)
SELECT analyze_candle_patterns('2024-01-20'::date);

-- Анализ паттернов для 3 торговых дней подряд
SELECT analyze_candle_patterns('2024-01-20'::date, 3);

-- Анализ паттернов для 10 торговых дней подряд
SELECT analyze_candle_patterns('2024-01-20'::date, 10);

-- Пример: анализ на 30.09.2025 (понедельник)
-- Будет анализировать: 27.09 (пт), 26.09 (чт), 25.09 (ср), 24.09 (вт), 23.09 (пн)
-- Пропустит выходные: 28.09 (сб), 29.09 (вс)
SELECT analyze_candle_patterns('2025-09-30'::date, 5);
```

### Функция `get_candle_pattern_stats(p_analysis_date date default null)`

Вспомогательная функция для получения статистики по найденным паттернам.

**Схема:** `invest_candles`

**Параметры:**
- `p_analysis_date` - дата анализа (опционально, если не указана - показывает все)

**Возвращает:**
- `analysis_date` - дата анализа
- `candle_type` - тип свечи
- `total_patterns` - общее количество паттернов
- `avg_consecutive_days` - среднее количество дней в паттерне
- `max_consecutive_days` - максимальное количество дней в паттерне
- `unique_instruments` - количество уникальных инструментов

## Синонимы в схеме invest

Для удобства доступа созданы синонимы в схеме `invest`:

### Представление `invest.candle_pattern_analysis`
Синоним таблицы `invest_candles.candle_pattern_analysis` для прозрачного доступа из схемы `invest`.

```sql
-- Использование через синоним
SELECT * FROM invest.candle_pattern_analysis 
WHERE analysis_date = current_date;
```

### Функция `invest.analyze_candle_patterns(date, integer default 5)`
Синоним функции `invest_candles.analyze_candle_patterns()`.

```sql
-- Использование через синоним (5 дней по умолчанию)
SELECT invest.analyze_candle_patterns(current_date);

-- Использование через синоним (3 дня подряд)
SELECT invest.analyze_candle_patterns(current_date, 3);
```

### Функция `invest.get_candle_pattern_stats(date)`
Синоним функции `invest_candles.get_candle_pattern_stats()`.

```sql
-- Использование через синоним
SELECT * FROM invest.get_candle_pattern_stats(current_date);
```

## Примеры SQL запросов

### Просмотр результатов анализа

```sql
-- Все найденные паттерны за конкретную дату анализа
SELECT 
    figi,
    candle_type,
    pattern_start_date,
    pattern_end_date,
    consecutive_days,
    avg_volume,
    total_price_change
FROM candle_pattern_analysis 
WHERE analysis_date = '2024-01-20'::date
ORDER BY figi, pattern_start_date;

-- Статистика по паттернам
SELECT * FROM get_candle_pattern_stats('2024-01-20'::date);
```

### Аналитические запросы

```sql
-- Топ инструментов по количеству паттернов за месяц
SELECT 
    figi,
    count(*) as total_patterns,
    sum(case when candle_type = 'BULLISH' then 1 else 0 end) as bullish_patterns,
    sum(case when candle_type = 'BEARISH' then 1 else 0 end) as bearish_patterns,
    avg(consecutive_days) as avg_consecutive_days
FROM candle_pattern_analysis 
WHERE analysis_date >= current_date - interval '30 days'
GROUP BY figi
ORDER BY total_patterns DESC
LIMIT 10;

-- Динамика паттернов по дням
SELECT 
    analysis_date,
    count(*) as total_patterns,
    count(distinct figi) as unique_instruments,
    avg(consecutive_days) as avg_pattern_length
FROM candle_pattern_analysis 
WHERE analysis_date >= current_date - interval '30 days'
GROUP BY analysis_date
ORDER BY analysis_date DESC;
```

## Работа с выходными днями

Функция автоматически исключает выходные дни (субботу и воскресенье) из анализа, так как торги на фондовом рынке в эти дни не ведутся.

### Пример расчета торговых дней

Если вызвать функцию 30.09.2025 (понедельник) с параметром 5 дней:

| Дата | День недели | Статус |
|------|-------------|---------|
| 29.09.2025 | Воскресенье | ❌ Исключается |
| 28.09.2025 | Суббота | ❌ Исключается |
| 27.09.2025 | Пятница | ✅ Анализируется (день 1) |
| 26.09.2025 | Четверг | ✅ Анализируется (день 2) |
| 25.09.2025 | Среда | ✅ Анализируется (день 3) |
| 24.09.2025 | Вторник | ✅ Анализируется (день 4) |
| 23.09.2025 | Понедельник | ✅ Анализируется (день 5) |

### Определение дней недели в PostgreSQL

- 0 = Воскресенье (исключается)
- 1 = Понедельник
- 2 = Вторник  
- 3 = Среда
- 4 = Четверг
- 5 = Пятница
- 6 = Суббота (исключается)

## Логирование

Функция автоматически логирует свою работу в таблицу `system_logs`:

- **STARTED** - начало выполнения анализа
- **SUCCESS** - успешное завершение с количеством найденных паттернов
- **INFO** - дополнительная информация о деталях анализа
- **ERROR** - ошибки выполнения

```sql
-- Просмотр логов анализа
SELECT 
    task_id,
    status,
    message,
    start_time,
    end_time,
    duration_ms
FROM system_logs 
WHERE endpoint = 'analyze_candle_patterns'
ORDER BY start_time DESC;
```

## Ограничения и особенности

1. **Торговые дни**: Функция анализирует только торговые дни, **исключая выходные** (субботу и воскресенье)
2. **Период анализа**: Функция анализирует N торговых дней назад от указанной даты (не включая её)
3. **Минимальная длина**: Паттерн должен содержать минимум 2 торговых дня подряд (по умолчанию ищет 5+ дней)
4. **Типы свечей**: Анализируются только BULLISH и BEARISH свечи, DOJI игнорируются
5. **Завершенные свечи**: Учитываются только свечи с `is_complete = true`
6. **Дубликаты**: Функция не создает дубликаты - проверяет существование паттерна перед вставкой
7. **Валидация параметров**: Количество дней должно быть не менее 2, иначе функция выбросит исключение
8. **Расширенный поиск**: Для надежного покрытия торговых дней функция расширяет период поиска в 2 раза

## Индексы

Для оптимизации производительности созданы следующие индексы:
- `idx_candle_pattern_analysis_figi` - по FIGI инструмента
- `idx_candle_pattern_analysis_analysis_date` - по дате анализа
- `idx_candle_pattern_analysis_pattern_dates` - по датам начала и окончания паттерна
- `idx_candle_pattern_analysis_candle_type` - по типу свечи

## Права доступа

- **postgres** - владелец всех объектов
- **admin** - полные права на таблицу и функции (включая синонимы)
- **tester** - права на чтение таблицы и выполнение функции статистики (включая синонимы)

## Файлы

- **Схема БД**: `db/10-candle-analysis-tables-and-functions.sql`
- **Тестовые скрипты**: `sql-scripts/test-candle-pattern-analysis.sql`
