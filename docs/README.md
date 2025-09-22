# Investment Data Loader Service — Документация

Сервис для загрузки и агрегации инвестиционных данных (акции, фьючерсы, индикативы) с использованием Tinkoff Invest API и PostgreSQL.

## 🚀 Быстрый старт

### Системные требования
- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Docker (опционально)

### Конфигурация

1) Установите переменные окружения (минимум токен Tinkoff):
```bash
# Windows (PowerShell)
$env:T_INVEST_TOKEN = "<ваш_токен>"

# Linux/Mac
export T_INVEST_TOKEN=<ваш_токен>
```

2) Настройте подключение к БД (опционально):
```bash
# Windows (PowerShell)
$env:DB_HOST = "localhost"
$env:DB_PORT = "5434"
$env:DB_NAME = "postgres"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "password"

# Linux/Mac
export DB_HOST=localhost
export DB_PORT=5434
export DB_NAME=postgres
export DB_USERNAME=postgres
export DB_PASSWORD=password
```

### Запуск

**Локальный запуск:**
```bash
mvn spring-boot:run
```

**Docker Compose (рекомендуется):**
```bash
docker-compose up -d
```

**Docker (ручная сборка):**
```bash
docker build -t investment-data-loader:latest .
docker run -p 8083:8083 -e T_INVEST_TOKEN=<ваш_токен> investment-data-loader:latest
```

### Проверка работоспособности

```bash
# Проверка здоровья API
curl http://localhost:8083/api/system/health

# Получение статистики инструментов
curl http://localhost:8083/api/instruments/count

# Получение списка акций
curl http://localhost:8083/api/instruments/shares?exchange=MOEX&currency=RUB
```

## 📚 Структура документации

### Основная документация
- **Конфигурация**: `docs/configuration.md` - настройка приложения
- **Развертывание**: `docs/deployment.md` - развертывание в различных средах
- **Docker разработка**: `docs/docker-development.md` - разработка с Docker
- **База данных**: `docs/database.md` - схема БД и миграции
- **Планировщики**: `docs/schedulers.md` - автоматические задачи
- **Тестирование**: `docs/TEST_DESCRIPTION.md` - описание тестов

### API Документация
- **Инструменты**: `docs/api/instruments.md` - работа с акциями, фьючерсами, индикативами
- **Цены основной сессии**: `docs/api/main-session-prices.md` - цены закрытия
- **Утренняя сессия**: `docs/api/morning-session.md` - данные утренней сессии
- **Вечерняя сессия**: `docs/api/evening-session.md` - данные вечерней сессии
- **Последние сделки**: `docs/api/last-trades.md` - информация о сделках
- **Свечи**: `docs/api/candles.md` - исторические данные свечей
- **Аналитика**: `docs/api/analytics.md` - аналитические данные
- **Торговые данные**: `docs/api/trading.md` - торговые расписания и статусы
- **Системные эндпоинты**: `docs/api/system.md` - системная информация

### Диаграммы и архитектура
- **Архитектура**: `docs/diagrams/architecture.md` - общая архитектура системы
- **Поток данных**: `docs/diagrams/data-flow.md` - схема потоков данных

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

## 🔧 Основные возможности

### Работа с инструментами
- ✅ Загрузка справочников (акции, фьючерсы, индикативы) из Tinkoff API
- ✅ Сохранение в БД с защитой от дубликатов
- ✅ Фильтрация по различным параметрам
- ✅ Поиск по FIGI или тикеру
- ✅ Получение статистики по количеству инструментов

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

## 🚦 Статус системы

**Порт**: 8083  
**База данных**: PostgreSQL (по умолчанию `localhost:5434/postgres`)  
**Таймзона**: Europe/Moscow  
**Версия Java**: 17+  

## 🔗 Полезные ссылки

- [Tinkoff Invest API](https://tinkoff.github.io/investAPI/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)

## 📞 Поддержка

При возникновении проблем:
1. Проверьте логи приложения
2. Убедитесь в корректности переменных окружения
3. Проверьте доступность Tinkoff API
4. Обратитесь к документации по API в папке `docs/api/`