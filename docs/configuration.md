# Конфигурация

Конфигурация задаётся через профили Spring Boot: `application.properties`, `application-test.properties`, `application-prod.properties`, `application-docker.properties` и переменные окружения.

## Профили окружений

### 1. **TEST** (по умолчанию)
- **Порт**: 8087
- **База данных**: localhost:5434
- **Токен**: T_INVEST_TEST_TOKEN
- **Логирование**: DEBUG
- **Кеширование**: отключено

### 2. **PROD** (продакшн)
- **Порт**: 8083
- **База данных**: 45.132.176.136:5432
- **Токен**: T_INVEST_PROD_TOKEN
- **Логирование**: INFO
- **Кеширование**: включено

### 3. **DOCKER** (контейнер)
- **Порт**: 8083
- **База данных**: host.docker.internal:5434
- **Токен**: T_INVEST_TOKEN
- **Логирование**: INFO
- **Кеширование**: включено

## Основные настройки

- **Имя приложения**: `spring.application.name=investment-data-loader-service`
- **Активный профиль по умолчанию**: `test`
- **Таймзона**: `app.timezone=Europe/Moscow`
- **Tinkoff API URL**: `https://invest-public-api.tinkoff.ru/rest`

## Tinkoff Invest API

### Переменные окружения:
- **TEST**: `T_INVEST_TEST_TOKEN` (по умолчанию: test-token-12345)
- **PROD**: `T_INVEST_PROD_TOKEN` (обязательно!)
- **DOCKER**: `T_INVEST_TOKEN`

### Настройки:
```properties
tinkoff.api.base-url=https://invest-public-api.tinkoff.ru/rest
tinkoff.api.timeout=30000  # test: 30s, prod: 60s
```

## База данных (PostgreSQL)

### TEST окружение:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5434/postgres
spring.datasource.username=postgres
spring.datasource.password=123password123
spring.jpa.properties.hibernate.default_schema=invest
```

### PROD окружение:
```properties
spring.datasource.url=jdbc:postgresql://45.132.176.136:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=${SPRING_DATASOURCE_PROD_PASSWORD}
spring.jpa.properties.hibernate.default_schema=invest
```

### DOCKER окружение:
```properties
spring.datasource.url=jdbc:postgresql://host.docker.internal:5434/postgres
spring.datasource.username=postgres
spring.datasource.password=123password123
spring.jpa.properties.hibernate.default_schema=invest
```

## Пул соединений HikariCP

### TEST (минимальные ресурсы):
```properties
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=10000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=900000
spring.datasource.hikari.leak-detection-threshold=30000
```

### PROD (оптимизирован для нагрузки):
```properties
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=60000
```

### DOCKER (средние ресурсы):
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=60000
```

## JPA/Hibernate

### Общие настройки:
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.default_schema=invest
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
spring.jpa.properties.hibernate.connection.autocommit=false
```

### TEST (подробное логирование):
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true
spring.jpa.properties.hibernate.jdbc.batch_size=25
spring.transaction.default-timeout=60
```

### PROD (оптимизирован):
```properties
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.use_sql_comments=false
spring.jpa.properties.hibernate.jdbc.batch_size=100
spring.transaction.default-timeout=300
```

## Логирование

### TEST (подробное):
```properties
logging.level.com.example.InvestmentDataLoaderService=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### PROD (минимальное):
```properties
logging.level.com.example.InvestmentDataLoaderService=INFO
logging.level.org.springframework.web=WARN
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN
logging.level.org.springframework.security=WARN
```

## Кеширование

### TEST:
```properties
spring.cache.type=none
```

### PROD:
```properties
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=1h
```

## Переменные окружения

### Обязательные для PROD:
```bash
T_INVEST_PROD_TOKEN=your_prod_token
SPRING_DATASOURCE_PROD_PASSWORD=your_db_password
```

### Для TEST (опционально):
```bash
T_INVEST_TEST_TOKEN=your_test_token
SPRING_DATASOURCE_TEST_URL=jdbc:postgresql://localhost:5434/postgres
SPRING_DATASOURCE_TEST_USERNAME=postgres
SPRING_DATASOURCE_TEST_PASSWORD=123password123
```

### Для DOCKER:
```bash
T_INVEST_TOKEN=your_token
SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5434/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=123password123
```

## Файлы конфигурации

### .env файлы:
- `.env.test` - переменные для тестового окружения
- `.env.prod` - переменные для продакшн окружения

### Примеры:
- `scripts/env.test.example` - шаблон для тестового окружения
- `scripts/env.prod.example` - шаблон для продакшн окружения

## Мониторинг и метрики (PROD)

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true
```

## Безопасность (PROD)

```properties
server.error.include-stacktrace=never
server.error.include-message=never
```

## DevTools (только TEST)

```properties
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true
spring.devtools.restart.poll-interval=1000
spring.devtools.restart.quiet-period=400
```
