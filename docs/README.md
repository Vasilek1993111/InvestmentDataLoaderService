# Investment Data Loader Service — Документация

Сервис для загрузки и агрегации инвестиционных данных (акции, фьючерсы, индикативы, рыночные данные) с использованием Tinkoff Invest API и PostgreSQL.

## 🔧 Основные возможности

### Работа с инструментами
- ✅ Загрузка справочников (акции, фьючерсы, индикативы) из Tinkoff API
- ✅ Сохранение в БД с защитой от дубликатов
- ✅ Фильтрация по различным параметрам
- ✅ Поиск по FIGI или тикеру
- ✅ Получение статистики по количеству инструментов
- ✅ Автоматический прогрев кэша при запуске приложения
- ✅ Ручное управление кэшем через REST API

### Рыночные данные
- ✅ Загрузка цен закрытия/открытия
- ✅ Исторические свечи и сделки
- ✅ Ежеминутная актуализация данных
- ✅ Агрегация по сессиям (утренняя, основная, вечерняя)

### API и интеграция
- ✅ RESTful API с JSON
- ✅ Автоматическая документация (Swagger/OpenAPI)
- ✅ Обработка ошибок и валидация
- ✅ Кэширование для повышения производительности
- ✅ Управление кэшем через REST API

## 🚦 Статус системы

**Порт**: 8083 - prod и 8087 - test 
**База данных**: PostgreSQL (по умолчанию `localhost:5434/postgres`)  
**Таймзона**: Europe/Moscow  
**Версия Java**: 17+  


## 📚 Структура документации

### Основная документация
- **Конфигурация**: `docs/configuration.md` - настройка приложения
- **Docker разработка**: `docs/docker-development.md` - разработка с Docker
- **База данных**: `docs/database.md` - схема БД и миграции
- **Планировщики**: `docs/schedulers.md` - автоматические задачи
- **Архитектура**: `docs/architecture.md` - архитектура системы
- **Использование таймзон**: `docs/timezone-usage.md` - работа с временными зонами

### API Документация
- **Обзор API**: [`docs/api/README.md`](docs/api/README.md) - общая документация по REST API
- **Инструменты**: `docs/api/instruments.md` - работа с акциями, фьючерсами, индикативами
- **Цены основной сессии**: `docs/api/main-session-prices.md` - цены закрытия
- **Утренняя сессия**: `docs/api/morning-session.md` - данные утренней сессии
- **Вечерняя сессия**: `docs/api/evening-session.md` - данные вечерней сессии
- **Последние сделки**: `docs/api/last-trades.md` - информация о сделках
- **Минутные свечи**: `docs/api/minute-candles.md` - исторические данные свечей
- **Дневные свечи**: `docs/api/daily-candles.md` - дневные свечи
- **Торговые данные**: `docs/api/trading.md` - торговые расписания и статусы
- **Системные эндпоинты**: `docs/api/system.md` - системная информация
- **Кэш**: `docs/api/cache.md` - управление кэшем

### Дополнительная документация
- **Rate Limiting**: `docs/rate-limiting-solution.md` - решение проблем с лимитами API
- **Анализ паттернов свечей**: `docs/database-candle-pattern-analysis.md` - анализ паттернов в БД
- **Исправление утечек БД**: `docs/database-connection-leaks-fix.md` - исправление проблем с БД
- **Gitignore для логов**: `docs/gitignore-logs.md` - настройка игнорирования логов

## 🏗️ Архитектура

### Основные компоненты
- **InstrumentsController** - REST API для работы с инструментами
- **InstrumentService** - бизнес-логика работы с инструментами
- **MarketDataService** - работа с рыночными данными
- **TradingService** - торговые данные и расписания
- **TInvestService** - фасад для внешних API

### Технологический стек
- **Spring Boot 3.x** - основной фреймворк
- **Spring Data JPA** - работа с БД
- **PostgreSQL** - основная БД
- **Tinkoff Invest API** - внешний источник данных
- **Spring Cache** - кэширование
- **Docker** - контейнеризация
- **Maven** - управление зависимостями

## 🔗 Полезные ссылки

- [Tinkoff Invest API](https://tinkoff.github.io/investAPI/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)
