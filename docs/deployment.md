# Развертывание

## Локальный запуск
```bash
mvn spring-boot:run
```

## Docker
Сборка образа:
```bash
docker build -t investment-data-loader:latest .
```

Запуск docker-compose:
```bash
docker-compose up -d
```

Порты:
- Приложение: `8083` (в Docker), `8083` (локально)
- PostgreSQL (по умолчанию): `5434`

## Переменные окружения (пример)
```bash
# База
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/postgres
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=123password123

# Tinkoff Invest API
export T_INVEST_TOKEN=<your_token>
```

## Проверка после запуска
```bash
curl http://localhost:8083/api/system/health
```
Ожидаемый ответ: статус `healthy`.
