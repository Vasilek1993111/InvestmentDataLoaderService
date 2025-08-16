# Development Guide

## Обзор архитектуры

Ingestion Service построен на Spring Boot 3.5.4 с использованием многослойной архитектуры. Приложение интегрируется с API Тинькофф Инвестиций через gRPC и сохраняет данные в PostgreSQL.

### Архитектурные слои

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │ TInvestController│  │ AdminController │                  │
│  └─────────────────┘  └─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                     Business Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │ TInvestService  │  │ClosePriceScheduler│                │
│  └─────────────────┘  └─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                     Data Access Layer                       │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │ ShareRepository │  │FutureRepository │                  │
│  └─────────────────┘  └─────────────────┘                  │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │ClosePriceRepo   │  │LastPriceRepo    │                  │
│  └─────────────────┘  └─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                     External APIs                           │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │ Tinkoff API     │  │   PostgreSQL    │                  │
│  │   (gRPC)        │  │   Database      │                  │
│  └─────────────────┘  └─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
```

## Компоненты системы

### 1. Конфигурация (config/)

#### GrpcConfig
```java
@Configuration
public class GrpcConfig {
    @Bean
    public ManagedChannel investChannel() {
        // Настройка gRPC канала с авторизацией
    }
}
```

**Назначение:** Конфигурирует gRPC соединение с API Тинькофф Инвестиций.

**Ключевые особенности:**
- Создает ManagedChannel с TLS
- Добавляет ClientInterceptor для авторизации
- Предоставляет stub'ы для различных сервисов API

**Зависимости:**
- `tinkoff.api.token` - токен авторизации
- `invest-public-api.tinkoff.ru:443` - адрес API

### 2. Модели данных (entity/)

#### ShareEntity
```java
@Entity
@Table(name = "shares")
public class ShareEntity {
    @Id
    private String figi;
    private String ticker;
    private String name;
    private String currency;
    private String exchange;
}
```

#### FutureEntity
```java
@Entity
@Table(name = "futures")
public class FutureEntity {
    @Id
    private String figi;
    private String ticker;
    private String assetType;
    private String basicAsset;
    private String currency;
    private String exchange;
}
```

#### ClosePriceEntity
```java
@Entity
@Table(name = "close_prices", schema = "invest")
public class ClosePriceEntity {
    @EmbeddedId
    private ClosePriceKey id;
    @Column(nullable = false, precision = 18, scale = 9)
    private BigDecimal closePrice;
    private String instrumentType;
    private String currency;
    private String exchange;
    private ZonedDateTime createdAt;
}
```

**Особенности модели данных:**
- Использует составные ключи для цен (дата + figi)
- Высокая точность для цен (18 цифр, 9 после запятой)
- Автоматическое добавление временных меток
- Разделение по схемам БД

### 3. Сервисы (service/)

#### TInvestService
Основной сервис для работы с API Тинькофф.

**Ключевые методы:**
- `getShares()` - получение и сохранение акций
- `getFutures()` - получение и сохранение фьючерсов
- `getClosePrices()` - получение цен закрытия
- `getTradingSchedules()` - получение торговых расписаний
- `getTradingStatuses()` - получение статусов торговли

**Особенности:**
- Автоматическое сохранение новых инструментов
- Фильтрация по бирже, валюте, статусу
- Сортировка результатов
- Валидация входных параметров

#### ClosePriceSchedulerService
Сервис для автоматического сбора данных.

**Планировщик:**
```java
@Scheduled(cron = "0 0 20 * * *", zone = "Europe/Moscow")
public void fetchAndStoreClosePrices() {
    // Сбор цен закрытия каждый день в 20:00 по МСК
}
```

**Функции:**
- Автоматический сбор цен закрытия
- Принудительная загрузка данных
- Перезагрузка данных за указанную дату
- Предотвращение дублирования записей

### 4. Контроллеры (controller/)

#### TInvestController
REST API для доступа к данным.

**Endpoints:**
- `GET /shares` - список акций
- `GET /futures` - список фьючерсов
- `GET /close-prices` - цены закрытия
- `GET /trading-schedules` - торговые расписания
- `GET /trading-statuses` - статусы торговли

#### AdminController
Административные функции.

**Endpoints:**
- `POST /admin/load-close-prices` - загрузка цен за сегодня
- `POST /admin/load-close-prices/{date}` - перезагрузка цен за дату

### 5. Репозитории (repository/)

Все репозитории наследуются от `JpaRepository` и предоставляют стандартные CRUD операции.

**Особые методы:**
```java
// ShareRepository
Optional<ShareEntity> findByTicker(String ticker);

// ClosePriceRepository
void deleteByIdPriceDate(LocalDate priceDate);
```

## Процесс разработки

### Настройка окружения

1. **Установите зависимости:**
   ```bash
   # Java 21
   sudo apt install openjdk-21-jdk
   
   # Maven
   sudo apt install maven
   
   # PostgreSQL
   sudo apt install postgresql postgresql-contrib
   ```

2. **Настройте базу данных:**
   ```sql
   CREATE DATABASE postgres;
   CREATE SCHEMA invest;
   CREATE USER postgres WITH PASSWORD '123password123';
   GRANT ALL PRIVILEGES ON DATABASE postgres TO postgres;
   ```

3. **Получите токен Тинькофф API:**
   - Зарегистрируйтесь в [Тинькофф Инвестиции](https://www.tinkoff.ru/invest/)
   - Получите токен в личном кабинете
   - Добавьте токен в `application.properties`

### Сборка и запуск

```bash
# Сборка проекта
mvn clean compile

# Запуск приложения
mvn spring-boot:run

# Создание JAR файла
mvn package

# Запуск JAR файла
java -jar target/ingestion-service-0.0.1-SNAPSHOT.jar
```

### Тестирование

```bash
# Запуск тестов
mvn test

# Запуск с профилем тестирования
mvn spring-boot:run -Dspring.profiles.active=test
```

### Логирование

Приложение использует стандартное логирование Spring Boot. Настройки в `application.properties`:

```properties
# Включение SQL логов
spring.jpa.show-sql=true

# Уровень логирования
logging.level.com.example.ingestionservice=DEBUG
logging.level.org.springframework.web=DEBUG
```

## Интеграция с API Тинькофф

### gRPC клиент

Приложение использует официальный SDK Тинькофф для Java:

```xml
<dependency>
    <groupId>ru.tinkoff.piapi</groupId>
    <artifactId>java-sdk-core</artifactId>
    <version>1.7</version>
</dependency>
```

### Основные сервисы API

1. **UsersService** - информация об аккаунтах
2. **InstrumentsService** - справочная информация об инструментах
3. **MarketDataService** - рыночные данные

### Обработка ошибок

```java
try {
    GetAccountsResponse res = usersService.getAccounts(
        GetAccountsRequest.newBuilder().build()
    );
} catch (Exception e) {
    log.error("Failed to get accounts", e);
    throw new RuntimeException("API call failed", e);
}
```

## База данных

### Схема

```sql
-- Схема для рыночных данных
CREATE SCHEMA invest;

-- Таблица акций
CREATE TABLE shares (
    figi VARCHAR PRIMARY KEY,
    ticker VARCHAR NOT NULL,
    name VARCHAR NOT NULL,
    currency VARCHAR NOT NULL,
    exchange VARCHAR NOT NULL
);

-- Таблица фьючерсов
CREATE TABLE futures (
    figi VARCHAR PRIMARY KEY,
    ticker VARCHAR NOT NULL,
    asset_type VARCHAR NOT NULL,
    basic_asset VARCHAR NOT NULL,
    currency VARCHAR NOT NULL,
    exchange VARCHAR NOT NULL,
    stock_ticker VARCHAR
);

-- Таблица цен закрытия
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

-- Таблица последних цен
CREATE TABLE invest.last_prices (
    figi VARCHAR,
    time TIMESTAMP,
    price DECIMAL(18,9) NOT NULL,
    currency VARCHAR NOT NULL,
    exchange VARCHAR NOT NULL,
    PRIMARY KEY (figi, time)
);
```

### Индексы

```sql
-- Индексы для оптимизации запросов
CREATE INDEX idx_shares_ticker ON shares(ticker);
CREATE INDEX idx_futures_ticker ON futures(ticker);
CREATE INDEX idx_close_prices_date ON invest.close_prices(price_date);
CREATE INDEX idx_close_prices_figi ON invest.close_prices(figi);
```

## Мониторинг и отладка

### Health Check

Приложение предоставляет стандартные Spring Boot Actuator endpoints:

```bash
# Проверка состояния приложения
curl http://localhost:8083/actuator/health

# Информация о приложении
curl http://localhost:8083/actuator/info

# Метрики
curl http://localhost:8083/actuator/metrics
```

### Логирование

Настройте логирование в `application.properties`:

```properties
# Логирование SQL запросов
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Логирование HTTP запросов
logging.level.org.springframework.web=DEBUG

# Логирование gRPC
logging.level.io.grpc=DEBUG
```

## Безопасность

### Текущие меры

1. **Токен API** хранится в конфигурации
2. **TLS** для соединения с API Тинькофф
3. **Валидация** входных параметров

### Рекомендации по улучшению

1. **Шифрование токена** в конфигурации
2. **Аутентификация** для административных endpoints
3. **Rate limiting** для API endpoints
4. **Аудит** операций с данными

## Производительность

### Оптимизации

1. **Пакетная обработка** для сохранения данных
2. **Индексы** в базе данных
3. **Кэширование** справочных данных
4. **Асинхронная обработка** для длительных операций

### Мониторинг

```bash
# Проверка использования памяти
curl http://localhost:8083/actuator/metrics/jvm.memory.used

# Проверка времени ответа
curl http://localhost:8083/actuator/metrics/http.server.requests
```

## Развертывание

### Docker

```dockerfile
FROM openjdk:21-jre-slim
COPY target/ingestion-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Docker Compose

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8083:8083"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/postgres
    depends_on:
      - db
  
  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=postgres
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=123password123
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

## Поддержка и развитие

### Добавление новых функций

1. **Создайте новую сущность** в пакете `entity/`
2. **Добавьте репозиторий** в пакете `repository/`
3. **Реализуйте сервис** в пакете `service/`
4. **Создайте контроллер** в пакете `controller/`
5. **Добавьте тесты** в пакете `test/`

### Стиль кода

- Используйте **Lombok** для уменьшения boilerplate кода
- Следуйте **Java naming conventions**
- Добавляйте **JavaDoc** для публичных методов
- Используйте **Spring Boot best practices**

### Тестирование

```java
@SpringBootTest
@AutoConfigureTestDatabase
class TInvestServiceTest {
    
    @Autowired
    private TInvestService service;
    
    @Test
    void shouldGetShares() {
        List<ShareDto> shares = service.getShares(null, null, null);
        assertThat(shares).isNotEmpty();
    }
}
```