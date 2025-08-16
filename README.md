# Ingestion Service

Сервис для сбора и хранения данных о финансовых инструментах через API Тинькофф Инвестиций.

## 📋 Описание

Ingestion Service - это Spring Boot приложение, которое автоматически собирает данные о российских акциях и фьючерсах с API Тинькофф Инвестиций и сохраняет их в PostgreSQL базу данных. Сервис предоставляет REST API для доступа к собранным данным.

### Основные возможности

- 🔄 **Автоматический сбор данных** - ежедневное получение цен закрытия в 20:00 по МСК
- 📊 **Хранение информации** об акциях, фьючерсах и их ценах
- 🌐 **REST API** для доступа к данным
- 🔧 **Административные функции** для управления данными
- ⏰ **Планировщик задач** для автоматизации процессов

## 🏗️ Архитектура

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   REST API      │    │  Tinkoff API    │    │   PostgreSQL    │
│   Controllers   │◄──►│   gRPC Client   │◄──►│   Database      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Services      │    │   Scheduler     │    │   Repositories  │
│   Business      │    │   Auto Tasks    │    │   Data Access   │
│   Logic         │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🚀 Быстрый старт

### Предварительные требования

- Java 21+
- Maven 3.6+
- PostgreSQL 12+
- Токен API Тинькофф Инвестиций

### Установка и запуск

1. **Клонируйте репозиторий**
   ```bash
   git clone <repository-url>
   cd ingestion-service
   ```

2. **Настройте базу данных**
   ```sql
   CREATE DATABASE postgres;
   CREATE SCHEMA invest;
   ```

3. **Настройте конфигурацию**
   
   Отредактируйте `src/main/resources/application.properties`:
   ```properties
   # Замените на ваш токен Тинькофф API
   tinkoff.api.token=your_tinkoff_token_here
   
   # Настройте подключение к БД
   spring.datasource.url=jdbc:postgresql://localhost:5434/postgres
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

4. **Запустите приложение**
   ```bash
   mvn spring-boot:run
   ```

5. **Проверьте работу**
   ```bash
   curl http://localhost:8083/shares
   ```

## 📊 API Endpoints

### Основные endpoints

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/shares` | Получить список акций |
| GET | `/futures` | Получить список фьючерсов |
| GET | `/close-prices` | Получить цены закрытия |
| GET | `/trading-schedules` | Получить торговые расписания |
| GET | `/trading-statuses` | Получить статусы торговли |

### Административные endpoints

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/admin/load-close-prices` | Загрузить цены закрытия за сегодня |
| POST | `/admin/load-close-prices/{date}` | Перезагрузить цены за указанную дату |

### Примеры использования

#### Получение акций
```bash
# Все акции
curl "http://localhost:8083/shares"

# Акции только с MOEX
curl "http://localhost:8083/shares?exchange=MOEX"

# Акции в рублях
curl "http://localhost:8083/shares?currency=RUB"
```

#### Получение цен закрытия
```bash
# Цены закрытия для конкретных инструментов
curl "http://localhost:8083/close-prices?instrumentId=BBG000B9XRY4&instrumentId=BBG000B9XRY5"
```

## 🗄️ Структура базы данных

### Таблицы

- `shares` - информация об акциях
- `futures` - информация о фьючерсах  
- `invest.close_prices` - цены закрытия
- `invest.last_prices` - последние цены

### Схема данных

```sql
-- Акции
CREATE TABLE shares (
    figi VARCHAR PRIMARY KEY,
    ticker VARCHAR NOT NULL,
    name VARCHAR NOT NULL,
    currency VARCHAR NOT NULL,
    exchange VARCHAR NOT NULL
);

-- Фьючерсы
CREATE TABLE futures (
    figi VARCHAR PRIMARY KEY,
    ticker VARCHAR NOT NULL,
    asset_type VARCHAR NOT NULL,
    basic_asset VARCHAR NOT NULL,
    currency VARCHAR NOT NULL,
    exchange VARCHAR NOT NULL,
    stock_ticker VARCHAR
);

-- Цены закрытия
CREATE TABLE invest.close_prices (
    price_date DATE,
    figi VARCHAR,
    close_price DECIMAL(18,9) NOT NULL,
    instrument_type VARCHAR NOT NULL,
    currency VARCHAR NOT NULL,
    exchange VARCHAR NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (price_date, figi)
);
```

## ⚙️ Конфигурация

### Основные настройки

| Параметр | Описание | По умолчанию |
|----------|----------|--------------|
| `server.port` | Порт приложения | 8083 |
| `tinkoff.api.token` | Токен API Тинькофф | - |
| `app.timezone` | Часовой пояс | Europe/Moscow |

### Планировщик задач

- **Сбор цен закрытия**: каждый день в 20:00 по МСК
- **Cron выражение**: `0 0 20 * * *`

## 🔧 Разработка

### Структура проекта

```
src/main/java/com/example/ingestionservice/
├── config/          # Конфигурация
├── controller/      # REST контроллеры
├── dto/            # Объекты передачи данных
├── entity/         # Модели данных
├── repository/     # Репозитории для доступа к БД
├── service/        # Бизнес-логика
└── IngestionServiceApplication.java
```

### Зависимости

- **Spring Boot 3.5.4** - основной фреймворк
- **Spring Data JPA** - работа с базой данных
- **Tinkoff PI API** - SDK для работы с API Тинькофф
- **PostgreSQL** - база данных
- **Lombok** - упрощение кода

### Сборка

```bash
# Сборка проекта
mvn clean compile

# Запуск тестов
mvn test

# Создание JAR файла
mvn package
```

## 🐛 Устранение неполадок

### Частые проблемы

1. **Ошибка подключения к БД**
   - Проверьте настройки в `application.properties`
   - Убедитесь, что PostgreSQL запущен

2. **Ошибка авторизации API Тинькофф**
   - Проверьте токен в настройках
   - Убедитесь, что токен активен

3. **Ошибки gRPC**
   - Проверьте версии зависимостей
   - Убедитесь в совместимости SDK

### Логи

Приложение использует стандартное логирование Spring Boot. Логи можно найти в консоли или настроить вывод в файл.

## 📝 Лицензия

[Укажите лицензию проекта]

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте ветку для новой функции
3. Внесите изменения
4. Создайте Pull Request

## 📞 Поддержка

Для вопросов и предложений создавайте Issues в репозитории проекта.