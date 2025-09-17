# Использование временных зон в проекте

## Общие принципы

В проекте **всегда используется таймзона UTC+3 (Europe/Moscow)** для всех операций с временем.

## Константа таймзоны

Создан утилитный класс `TimeZoneUtils` с константой:

```java
public static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");
```

## Использование в коде

### Правильно:
```java
// Используйте константу из TimeZoneUtils
LocalDateTime now = LocalDateTime.now(TimeZoneUtils.getMoscowZone());

// Или напрямую
LocalDateTime now = LocalDateTime.now(TimeZoneUtils.MOSCOW_ZONE);
```

### Неправильно:
```java
// НЕ используйте UTC
LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));

// НЕ используйте другие таймзоны
LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/London"));
```

## Области применения

### 1. Entity классы
Все поля `createdAt` и `updatedAt` используют московское время:

```java
@Column(name = "created_at", nullable = false)
private LocalDateTime createdAt = LocalDateTime.now(TimeZoneUtils.getMoscowZone());
```

### 2. Scheduler'ы
Все cron-задачи выполняются в московской таймзоне:

```java
@Scheduled(cron = "0 0 1 * * *", zone = "Europe/Moscow")
```

### 3. API ответы
Все временные метки в API ответах представлены в московском времени.

### 4. База данных
SQL-скрипты используют московскую таймзону:

```sql
created_at TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow')
```

## Конвертация времени

### Из UTC в московское время:
```java
// Время от T-Invest API приходит в UTC
Instant utcTime = Instant.now();
ZonedDateTime moscowTime = utcTime.atZone(TimeZoneUtils.getMoscowZone());
```

### Из московского времени в UTC:
```java
LocalDateTime moscowTime = LocalDateTime.now(TimeZoneUtils.getMoscowZone());
Instant utcTime = moscowTime.atZone(TimeZoneUtils.getMoscowZone()).toInstant();
```

## Логирование

Все временные метки в логах отображаются в московском времени:

```java
System.out.println("Время запуска: " + LocalDateTime.now(TimeZoneUtils.getMoscowZone()));
```

## Исключения

Единственное исключение - получение данных от T-Invest API, где время приходит в UTC и конвертируется в московское время для хранения.

## Проверка

Для проверки правильности использования таймзоны выполните поиск:

```bash
# Найти все использования UTC (должно быть только в комментариях)
grep -r "UTC" src/main/java --exclude-dir=target

# Найти все использования Europe/Moscow (должно быть везде)
grep -r "Europe/Moscow" src/main/java --exclude-dir=target
```
