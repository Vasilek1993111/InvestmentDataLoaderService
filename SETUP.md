# 🚀 Настройка проекта InvestmentDataLoaderService

Этот документ описывает процесс настройки проекта для локальной разработки, тестирования и продакшн развертывания.

## 📋 Предварительные требования

### Системные требования
- **Java 21+** (Eclipse Temurin рекомендуется)
- **Maven 3.8+**
- **PostgreSQL 13+**
- **Docker & Docker Compose** (опционально)

### Внешние сервисы
- **Tinkoff Invest API** - для получения данных о финансовых инструментах
- **PostgreSQL** - для хранения данных

## 🔑 Получение API токенов

### Tinkoff Invest API

1. Зарегистрируйтесь на [Tinkoff Invest](https://www.tinkoff.ru/invest/)
2. Перейдите в [настройки API](https://www.tinkoff.ru/invest/settings/api/)
3. Создайте токен для песочницы (тестирование)
4. При необходимости создайте продакшн токен

**Важно:** 
- Тестовый токен начинается с `t.` и работает только с тестовыми данными
- Продакшн токен работает с реальными данными
- Никогда не коммитьте токены в репозиторий!

## 🗄️ Настройка базы данных

### Локальная PostgreSQL

1. Установите PostgreSQL
2. Создайте базу данных:
   ```sql
   CREATE DATABASE postgres;
   CREATE SCHEMA invest;
   ```
3. Создайте пользователя (опционально):
   ```sql
   CREATE USER postgres WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE postgres TO postgres;
   ```

### Docker PostgreSQL (рекомендуется)

```bash
docker run --name postgres-dev \
  -e POSTGRES_PASSWORD=your_password \
  -e POSTGRES_DB=postgres \
  -p 5434:5432 \
  -d postgres:15
```

## ⚙️ Конфигурация проекта

### 1. Скопируйте файлы конфигурации

```bash
# Для локальной разработки
cp .env.example .env

# Для тестирования
cp scripts/env.test.example scripts/env.test

# Для продакшна
cp scripts/env.prod.example scripts/env.prod
```

### 2. Заполните переменные окружения

#### .env (локальная разработка)
```bash
# Tinkoff API
T_INVEST_TEST_TOKEN=your_test_token_here

# База данных
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_local_password

# Профиль
SPRING_PROFILES_ACTIVE=test
```

#### scripts/env.test (тестирование)
```bash
# Tinkoff API
T_INVEST_TEST_TOKEN=your_test_token_here

# База данных
SPRING_DATASOURCE_TEST_URL=jdbc:postgresql://localhost:5434/postgres
SPRING_DATASOURCE_TEST_USERNAME=postgres
SPRING_DATASOURCE_TEST_PASSWORD=your_test_password

# Профиль
SPRING_PROFILES_ACTIVE=test
```

#### scripts/env.prod (продакшн)
```bash
# Tinkoff API
T_INVEST_PROD_TOKEN=your_production_token_here

# База данных
SPRING_DATASOURCE_PROD_URL=jdbc:postgresql://your-prod-host:5432/your-database
SPRING_DATASOURCE_PROD_USERNAME=your_prod_username
SPRING_DATASOURCE_PROD_PASSWORD=your_prod_password

# Профиль
SPRING_PROFILES_ACTIVE=prod
```

## 🚀 Запуск приложения

### Локальная разработка

```bash
# Загрузка переменных окружения
export $(cat .env | xargs)

# Запуск приложения
mvn spring-boot:run
```

### Тестирование

```bash
# Загрузка переменных окружения
export $(cat scripts/env.test | xargs)

# Запуск тестов
mvn test

# Запуск приложения в тестовом режиме
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

### Docker

```bash
# Тестовое окружение
docker-compose up investment-service-test

# Продакшн окружение
docker-compose --profile production up investment-service-prod
```

## 🔧 Профили приложения

### test (по умолчанию)
- Порт: 8087
- Подробное логирование
- DevTools включены
- Кэширование отключено
- Тестовый токен Tinkoff API

### prod
- Порт: 8083
- Минимальное логирование
- DevTools отключены
- Кэширование включено
- Продакшн токен Tinkoff API

### docker
- Настройки для Docker контейнера
- Оптимизированный пул соединений
- Настройки для контейнеризации

## 🧪 Тестирование

### Запуск тестов

```bash
# Все тесты
mvn test

# Только unit тесты
mvn test -Dtest=*Test

# Только интеграционные тесты
mvn test -Dtest=*IntegrationTest

# С отчетом Allure
mvn test allure:report
```

### Проверка API

После запуска приложения доступны эндпоинты:

- **Тест**: http://localhost:8087/api/instruments/shares
- **Продакшн**: http://localhost:8083/api/instruments/shares

## 📊 Мониторинг

### Health Check
- **Тест**: http://localhost:8087/actuator/health
- **Продакшн**: http://localhost:8083/actuator/health

### Кэш управление
- **Прогрев**: POST /api/cache/warmup
- **Статистика**: GET /api/cache/stats
- **Очистка**: DELETE /api/cache/clear

## 🛡️ Безопасность

### Защита секретов

1. **Никогда не коммитьте**:
   - `.env` файлы
   - Реальные токены API
   - Пароли базы данных
   - Любые секретные данные

2. **Используйте**:
   - `.env.example` файлы
   - Переменные окружения
   - Системы управления секретами (для продакшна)

3. **Проверьте .gitignore**:
   ```gitignore
   .env
   .env.*
   *.secret
   *.key
   ```

### Переменные окружения

Все секретные данные должны передаваться через переменные окружения:

```bash
# Правильно
export T_INVEST_PROD_TOKEN=your_real_token
export SPRING_DATASOURCE_PASSWORD=your_real_password

# Неправильно (не коммитьте!)
echo "T_INVEST_PROD_TOKEN=your_real_token" >> .env
```

## 🚨 Устранение неполадок

### Проблемы с подключением к БД

```bash
# Проверка подключения
psql -h localhost -p 5434 -U postgres -d postgres

# Проверка переменных окружения
echo $SPRING_DATASOURCE_URL
echo $SPRING_DATASOURCE_USERNAME
```

### Проблемы с API токенами

```bash
# Проверка токена
echo $T_INVEST_TEST_TOKEN

# Тест API (замените YOUR_TOKEN)
curl -H "Authorization: Bearer YOUR_TOKEN" \
  "https://invest-public-api.tinkoff.ru/rest/tinkoff.public.invest.api.contract.v1.InstrumentsService/Shares"
```

### Проблемы с Docker

```bash
# Очистка контейнеров
docker-compose down -v

# Пересборка образов
docker-compose build --no-cache

# Проверка логов
docker-compose logs investment-service-test
```

## 📚 Дополнительная документация

- [API Documentation](docs/api/README.md)
- [Architecture](docs/architecture.md)
- [Database Schema](docs/database.md)
- [Docker Development](docs/docker-development.md)

## 🤝 Поддержка

Если у вас возникли проблемы:

1. Проверьте этот документ
2. Изучите логи приложения
3. Убедитесь, что все переменные окружения настроены
4. Проверьте подключение к внешним сервисам

---

**Важно:** Этот проект использует реальные API и базы данных. Всегда используйте тестовые токены для разработки и тестирования!
