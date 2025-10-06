# ⚡ Быстрый старт

## 🎯 Минимальная настройка для запуска

### 1. Клонирование и настройка
```bash
git clone <repository-url>
cd InvestmentDataLoaderService

# Скопируйте файл конфигурации
cp .env.example .env
```

### 2. Настройка переменных окружения
Отредактируйте файл `.env`:
```bash
# Tinkoff API токен (получите на https://www.tinkoff.ru/invest/settings/api/)
T_INVEST_TEST_TOKEN=your_test_token_here

# База данных
SPRING_DATASOURCE_PASSWORD=your_password
```

### 3. Запуск PostgreSQL
```bash
# Через Docker (рекомендуется)
docker run --name postgres-dev \
  -e POSTGRES_PASSWORD=your_password \
  -e POSTGRES_DB=postgres \
  -p 5434:5432 \
  -d postgres:15
```

### 4. Запуск приложения
```bash
# Загрузка переменных окружения
export $(cat .env | xargs)

# Запуск
mvn spring-boot:run
```

### 5. Проверка работы
```bash
# Health check
curl http://localhost:8087/actuator/health

# Получение акций
curl http://localhost:8087/api/instruments/shares
```

## 🔧 Полезные команды

```bash
# Запуск тестов
mvn test

# Запуск в Docker
docker-compose up investment-service-test

# Прогрев кэша
curl -X POST http://localhost:8087/api/cache/warmup

# Просмотр кэша
curl http://localhost:8087/api/cache/stats
```

## 📚 Документация

- [Полная настройка](SETUP.md)
- [API документация](docs/api/README.md)
- [Архитектура](docs/architecture.md)

---

**Готово!** 🎉 Приложение должно работать на http://localhost:8087
