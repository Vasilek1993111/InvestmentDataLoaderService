# Changelog

Все значимые изменения в проекте Investment Data Loader Service документируются в этом файле.

Формат основан на [Keep a Changelog](https://keepachangelog.com/ru/1.0.0/),
и проект придерживается [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Добавлено
- Объединение GET `/api/instruments/shares` и GET `/api/instruments/shares/database` в один endpoint
- Параметр `source` для выбора источника данных (API или БД)
- Расширенная документация API с примерами использования
- Автоматическое определение типа идентификатора (FIGI или тикер)
- Защита от дубликатов при сохранении инструментов

### Изменено
- `InstrumentsController` теперь напрямую общается с `InstrumentService`
- Удален фасад `TInvestService` для инструментов
- Упрощена архитектура - убраны промежуточные слои
- Обновлена документация по API

### Удалено
- `@PutMapping("/shares")` - заменен на `@PostMapping("/shares")`
- Метод `updateShare` из `InstrumentService`
- Дублирующие методы из `TInvestService`

## [1.0.0] - 2024-01-15

### Добавлено
- Базовая функциональность для работы с инструментами
- REST API для акций, фьючерсов и индикативов
- Интеграция с Tinkoff Invest API
- Сохранение данных в PostgreSQL
- Кэширование для повышения производительности
- Система логирования и мониторинга
- Docker контейнеризация
- Автоматические планировщики задач

### API Endpoints

#### Инструменты
- `GET /api/instruments/shares` - получение списка акций
- `GET /api/instruments/shares/{identifier}` - получение акции по ID
- `POST /api/instruments/shares` - сохранение акций
- `GET /api/instruments/futures` - получение списка фьючерсов
- `GET /api/instruments/futures/{identifier}` - получение фьючерса по ID
- `POST /api/instruments/futures` - сохранение фьючерсов
- `GET /api/instruments/indicatives` - получение индикативов
- `GET /api/instruments/indicatives/{identifier}` - получение индикатива по ID
- `POST /api/instruments/indicatives` - сохранение индикативов
- `GET /api/instruments/count` - статистика по инструментам

#### Рыночные данные
- `GET /api/market-data/close-prices` - цены закрытия
- `POST /api/market-data/close-prices` - сохранение цен закрытия
- `GET /api/market-data/candles` - исторические свечи
- `POST /api/market-data/candles` - сохранение свечей
- `GET /api/market-data/last-trades` - последние сделки

#### Система
- `GET /api/system/health` - состояние системы
- `GET /api/system/info` - информация о системе

### Технологический стек
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL 15
- Tinkoff Invest API (gRPC + REST)
- Spring Cache (Caffeine)
- Docker & Docker Compose
- Maven

## [0.9.0] - 2024-01-10

### Добавлено
- Начальная версия API
- Базовая интеграция с Tinkoff API
- Простые CRUD операции для инструментов
- Базовая документация

### Известные проблемы
- Отсутствует кэширование
- Нет защиты от дубликатов
- Ограниченная обработка ошибок
- Отсутствует мониторинг

## [0.8.0] - 2024-01-05

### Добавлено
- Начальная структура проекта
- Базовая конфигурация Spring Boot
- Подключение к PostgreSQL
- Первые тесты

---

## Планы на будущее

### [1.1.0] - Планируется
- [ ] Аутентификация и авторизация
- [ ] Rate limiting для API
- [ ] Расширенная аналитика
- [ ] WebSocket для real-time данных
- [ ] GraphQL API

### [1.2.0] - Планируется
- [ ] Kubernetes deployment
- [ ] Микросервисная архитектура
- [ ] Event-driven архитектура
- [ ] Расширенный мониторинг

### [2.0.0] - Планируется
- [ ] Полная переработка API
- [ ] Поддержка множественных брокеров
- [ ] Машинное обучение для прогнозирования
- [ ] Мобильное приложение

---

## Миграции

### v1.0.0 → Unreleased

#### API Changes
- **BREAKING**: `GET /api/instruments/shares/database` удален
- **BREAKING**: `PUT /api/instruments/shares` удален
- **NEW**: Параметр `source` в `GET /api/instruments/shares`

#### Database Changes
- Нет изменений в схеме БД

#### Configuration Changes
- Нет изменений в конфигурации

---

## Совместимость

### Версии Java
- **1.0.0+**: Java 17+

### Версии Spring Boot
- **1.0.0+**: Spring Boot 3.x

### Версии PostgreSQL
- **1.0.0+**: PostgreSQL 12+

### Версии Docker
- **1.0.0+**: Docker 20.10+

---

## Благодарности

- Команде Tinkoff за предоставление Invest API
- Spring Framework за отличную экосистему
- PostgreSQL за надежную БД
- Docker за упрощение развертывания
