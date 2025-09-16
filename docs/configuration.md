# Конфигурация

Конфигурация задаётся через `application.properties`, `application-docker.properties` и переменные окружения.

## Основные настройки

- Имя приложения: `spring.application.name=investment-data-loader-service`
- Порт (локально): `server.port=8087`
- Порт (Docker): `server.port=8083`
- Таймзона: `app.timezone=Europe/Moscow`

## Доступ к Tinkoff Invest API
- Переменная: `tinkoff.api.token`
- Источник: переменная окружения `T_INVEST_TOKEN` (может быть пустой для запуска без внешних вызовов)

Пример:
```bash
export T_INVEST_TOKEN=<ваш_токен>
```

## База данных (PostgreSQL)
По умолчанию (локально):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5434/postgres
spring.datasource.username=postgres
spring.datasource.password=123password123
spring.jpa.properties.hibernate.default_schema=invest
```

Docker-профиль:
```properties
spring.datasource.url=jdbc:postgresql://host.docker.internal:5434/postgres
spring.datasource.username=postgres
spring.datasource.password=123password123
```

## Пул соединений HikariCP
```properties
spring.datasource.hikari.maximum-pool-size=20   # 10 в Docker
spring.datasource.hikari.minimum-idle=5         # 2 в Docker
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=60000
```

## JPA/Hibernate
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.jdbc.batch_size=100  # 50 в Docker
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
spring.jpa.properties.hibernate.connection.autocommit=false
```

## Переменные окружения
- `T_INVEST_TOKEN` — токен доступа к Tinkoff Invest API
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` — переопределение подключения к БД
