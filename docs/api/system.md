# API — Системные эндпоинты (`/api/system`)

## GET /api/system/health
Статус здоровья системы и основных компонентов (БД, материализованные представления, шедулеры, API).
```bash
curl "http://localhost:8087/api/system/health"
```
Ответ (пример):
```json
{
  "status": "healthy",
  "timestamp": "2024-01-15T10:30:00",
  "components": {
    "database": "healthy",
    "materialized_views": "healthy",
    "schedulers": "healthy",
    "api": "healthy"
  },
  "uptime": 1737043200000
}
```

## GET /api/system/diagnostics
Детальная диагностика (версия Java, ОС, память, расписания, состояние матвью).
```bash
curl "http://localhost:8087/api/system/diagnostics"
```
Ответ (пример):
```json
{
  "success": true,
  "data": {
    "materialized_views": { "exists": true, "status": "ok" },
    "system": {
      "java_version": "21.0.8",
      "os_name": "Windows 10",
      "os_version": "10.0",
      "available_processors": 8,
      "max_memory": 4294967296,
      "total_memory": 1073741824,
      "free_memory": 536870912
    },
    "schedules": {
      "daily_refresh": "0 * * * * * (каждую минуту)",
      "full_refresh": "0 20 2 * * * (каждый день в 2:20)",
      "timezone": "Europe/Moscow"
    }
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

## GET /api/system/volume-aggregation/check
Проверка существования материализованных представлений.
```bash
curl "http://localhost:8087/api/system/volume-aggregation/check"
```
Ответ (пример):
```json
{ "success": true, "exists": true, "message": "Материализованные представления существуют", "timestamp": "2024-01-15T10:30:00" }
```

## GET /api/system/volume-aggregation/schedule-info
Информация о расписаниях обновлений матвью.
```bash
curl "http://localhost:8087/api/system/volume-aggregation/schedule-info"
```
Ответ (пример):
```json
{
  "success": true,
  "data": {
    "daily_refresh": "0 * * * * * (каждую минуту)",
    "full_refresh": "0 20 2 * * * (каждый день в 2:20)",
    "timezone": "Europe/Moscow",
    "description": "Дневное представление обновляется каждую минуту, общее - в 2:20"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

## GET /api/system/stats
Сводная статистика по ресурсам JVM и агрегированным данным (если доступно).
```bash
curl "http://localhost:8087/api/system/stats"
```
Ответ (пример):
```json
{
  "success": true,
  "data": {
    "memory": {
      "max_memory_mb": 4096,
      "total_memory_mb": 1024,
      "free_memory_mb": 512,
      "used_memory_mb": 512
    },
    "processor": { "available_processors": 8 },
    "uptime": { "start_time": 1737043200000, "uptime_ms": 1737046800000 },
    "volume_aggregation": {
      "total_records": 15000,
      "unique_instruments": 500,
      "last_updated": "2024-01-15T10:29:50"
    }
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

## GET /api/system/info
Информация о версии приложения, JVM и ОС.
```bash
curl "http://localhost:8087/api/system/info"
```
Ответ (пример):
```json
{
  "success": true,
  "data": {
    "java": { "version": "21.0.8", "vendor": "Oracle", "home": "/usr/lib/jvm/java-21" },
    "os": { "name": "Windows 10", "version": "10.0", "arch": "amd64" },
    "user": { "name": "admin", "home": "/home/admin", "dir": "/app" },
    "application": { "name": "Investment Data Loader Service", "version": "1.0.0", "description": "Сервис загрузки инвестиционных данных" }
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

## GET /api/system/external-services
Проверка доступности внешних сервисов (заготовка для расширения).
```bash
curl "http://localhost:8087/api/system/external-services"
```
Ответ (пример):
```json
{
  "success": true,
  "data": {
    "tinkoff_api": { "status": "unknown", "message": "Проверка не реализована" },
    "database": { "status": "healthy", "message": "Подключение к базе данных работает" }
  },
  "timestamp": "2024-01-15T10:30:00"
}
```
