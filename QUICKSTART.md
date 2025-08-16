# Быстрый старт

Это руководство поможет вам быстро запустить Ingestion Service за 5 минут.

## Предварительные требования

- Docker и Docker Compose
- Токен API Тинькофф Инвестиций

## Шаг 1: Получение токена API

1. Зарегистрируйтесь в [Тинькофф Инвестиции](https://www.tinkoff.ru/invest/)
2. Войдите в личный кабинет
3. Перейдите в "Настройки" → "API"
4. Создайте новый токен
5. Скопируйте токен

## Шаг 2: Настройка окружения

1. **Клонируйте репозиторий**:
   ```bash
   git clone <repository-url>
   cd ingestion-service
   ```

2. **Создайте файл .env**:
   ```bash
   cp .env.example .env
   ```

3. **Отредактируйте .env**:
   ```bash
   # Замените на ваш токен
   TINKOFF_API_TOKEN=your_actual_token_here
   ```

## Шаг 3: Запуск приложения

### Вариант A: Docker Compose (рекомендуется)

```bash
# Запуск всех сервисов
docker-compose up -d

# Проверка статуса
docker-compose ps

# Просмотр логов
docker-compose logs -f app
```

### Вариант B: Локальный запуск

1. **Установите PostgreSQL**:
   ```bash
   # Ubuntu/Debian
   sudo apt install postgresql postgresql-contrib
   
   # macOS
   brew install postgresql
   
   # Windows
   # Скачайте с https://www.postgresql.org/download/windows/
   ```

2. **Создайте базу данных**:
   ```sql
   CREATE DATABASE postgres;
   CREATE SCHEMA invest;
   ```

3. **Соберите и запустите приложение**:
   ```bash
   mvn clean package
   java -jar target/ingestion-service-0.0.1-SNAPSHOT.jar
   ```

## Шаг 4: Проверка работы

### Проверка API

```bash
# Проверка здоровья приложения
curl http://localhost:8083/actuator/health

# Получение списка акций
curl http://localhost:8083/shares

# Получение списка фьючерсов
curl http://localhost:8083/futures
```

### Проверка базы данных

```bash
# Подключение к PostgreSQL
docker-compose exec db psql -U postgres -d postgres

# Просмотр таблиц
\dt
\dt invest.*

# Просмотр данных
SELECT COUNT(*) FROM shares;
SELECT COUNT(*) FROM futures;
SELECT COUNT(*) FROM invest.close_prices;
```

## Шаг 5: Первый сбор данных

### Автоматический сбор

Приложение автоматически собирает данные каждый день в 20:00 по МСК.

### Принудительный сбор

```bash
# Сбор цен закрытия за сегодня
curl -X POST http://localhost:8083/admin/load-close-prices

# Сбор цен за конкретную дату
curl -X POST http://localhost:8083/admin/load-close-prices/2024-01-02
```

## Шаг 6: Мониторинг

### Просмотр логов

```bash
# Логи приложения
docker-compose logs -f app

# Логи базы данных
docker-compose logs -f db
```

### Метрики (если включен мониторинг)

```bash
# Запуск с мониторингом
docker-compose --profile monitoring up -d

# Prometheus: http://localhost:9090
# Grafana: http://localhost:3000 (admin/admin123)
```

## Шаг 7: Остановка

```bash
# Остановка всех сервисов
docker-compose down

# Остановка с удалением данных
docker-compose down -v
```

## Полезные команды

### Управление контейнерами

```bash
# Перезапуск приложения
docker-compose restart app

# Обновление приложения
docker-compose pull
docker-compose up -d

# Просмотр использования ресурсов
docker stats
```

### Работа с базой данных

```bash
# Резервное копирование
docker-compose exec db pg_dump -U postgres postgres > backup.sql

# Восстановление
docker-compose exec -T db psql -U postgres postgres < backup.sql

# Очистка данных
docker-compose exec db psql -U postgres -d postgres -c "TRUNCATE TABLE shares, futures, invest.close_prices;"
```

### Отладка

```bash
# Подключение к контейнеру приложения
docker-compose exec app sh

# Просмотр переменных окружения
docker-compose exec app env

# Проверка сетевого подключения
docker-compose exec app curl -f http://localhost:8083/actuator/health
```

## Решение проблем

### Приложение не запускается

1. **Проверьте токен API**:
   ```bash
   # Проверьте в логах
   docker-compose logs app | grep -i token
   ```

2. **Проверьте подключение к БД**:
   ```bash
   # Проверьте статус PostgreSQL
   docker-compose ps db
   ```

3. **Проверьте порты**:
   ```bash
   # Проверьте, что порт 8083 свободен
   netstat -tulpn | grep 8083
   ```

### Данные не загружаются

1. **Проверьте расписание**:
   ```bash
   # Проверьте логи планировщика
   docker-compose logs app | grep -i scheduler
   ```

2. **Принудительно запустите сбор**:
   ```bash
   curl -X POST http://localhost:8083/admin/load-close-prices
   ```

3. **Проверьте API Тинькофф**:
   ```bash
   # Проверьте доступность API
   curl -H "Authorization: Bearer YOUR_TOKEN" \
        https://invest-public-api.tinkoff.ru/ru.tinkoff.piapi.contract.v1.UsersService/GetAccounts
   ```

### Медленная работа

1. **Увеличьте память**:
   ```bash
   # Отредактируйте .env
   JAVA_OPTS=-Xms1g -Xmx2g
   ```

2. **Проверьте индексы БД**:
   ```bash
   docker-compose exec db psql -U postgres -d postgres -c "\d+ shares"
   ```

3. **Мониторинг ресурсов**:
   ```bash
   docker stats ingestion-service-app-1
   ```

## Следующие шаги

После успешного запуска:

1. **Изучите API документацию**: [docs/API.md](docs/API.md)
2. **Посмотрите примеры использования**: [docs/EXAMPLES.md](docs/EXAMPLES.md)
3. **Настройте мониторинг**: [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)
4. **Изучите архитектуру**: [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)

## Получение помощи

- **FAQ**: [docs/FAQ.md](docs/FAQ.md)
- **Issues**: [GitHub Issues](https://github.com/your-username/ingestion-service/issues)
- **Документация**: [docs/](docs/)

---

Поздравляем! Ingestion Service успешно запущен и готов к работе! 🎉