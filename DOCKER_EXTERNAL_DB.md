# Docker Setup с внешней БД для Investment Data Loader Service

Этот документ описывает, как запустить Investment Data Loader Service в Docker контейнере с подключением к внешней PostgreSQL базе данных.

## 🔗 Подключение к внешней БД

### Варианты подключения

#### 1. БД на хосте (не в контейнере)
Если PostgreSQL запущена на вашем компьютере:
```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://host.docker.internal:5434/postgres
```

#### 2. БД в другом Docker контейнере
Если PostgreSQL в другом контейнере:
```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://your-db-container-name:5432/postgres
```

## 🚀 Быстрый старт

### 1. Настройка переменных окружения

Создайте файл `.env` в корне проекта:

```env
# Tinkoff Invest API токен
T_INVEST_TOKEN=your_actual_tinkoff_invest_token_here

# Настройки подключения к внешней БД
# Если БД на хосте (не в контейнере)
DB_HOST=host.docker.internal
DB_PORT=5434

# Если БД в другом контейнере, укажите имя контейнера
# DB_HOST=your-db-container-name
# DB_PORT=5432

# Учетные данные БД
DB_NAME=postgres
DB_USERNAME=postgres
DB_PASSWORD=123password123
```

### 2. Настройка подключения к БД

Отредактируйте файл `docker-compose.external-db.yml`:

**Для БД на хосте:**
```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://host.docker.internal:5434/postgres
```

**Для БД в контейнере:**
```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://your-db-container-name:5432/postgres
```

### 3. Запуск приложения

#### Windows (PowerShell)
```powershell
# Сборка и запуск
.\docker-build-external-db.ps1 start

# Только сборка
.\docker-build-external-db.ps1 build

# Просмотр логов
.\docker-build-external-db.ps1 logs

# Проверка подключения к БД
.\docker-build-external-db.ps1 test-db
```

#### Linux/Mac (Bash)
```bash
# Сборка и запуск
docker-compose -f docker-compose.external-db.yml up --build -d

# Просмотр логов
docker-compose -f docker-compose.external-db.yml logs -f

# Остановка
docker-compose -f docker-compose.external-db.yml down
```

## 🔧 Конфигурация

### Структура файлов

```
├── docker-compose.external-db.yml    # Конфигурация для внешней БД
├── docker-build-external-db.ps1      # PowerShell скрипт управления
├── .env                              # Переменные окружения
└── DOCKER_EXTERNAL_DB.md            # Эта документация
```

### Настройки подключения

В файле `docker-compose.external-db.yml` настройте:

1. **URL БД** - адрес и порт вашей БД
2. **Учетные данные** - логин и пароль
3. **Сеть** - если БД в другой Docker сети

### Переменные окружения

| Переменная | Описание | Пример |
|------------|----------|--------|
| `T_INVEST_TOKEN` | Токен Tinkoff Invest API | `t.1234567890abcdef` |
| `DB_HOST` | Хост БД | `host.docker.internal` |
| `DB_PORT` | Порт БД | `5434` |
| `DB_NAME` | Имя базы данных | `postgres` |
| `DB_USERNAME` | Пользователь БД | `postgres` |
| `DB_PASSWORD` | Пароль БД | `your_password` |

## 📊 Мониторинг

### Проверка статуса

```bash
# Статус контейнеров
docker-compose -f docker-compose.external-db.yml ps

# Health check приложения
curl http://localhost:8083/actuator/health

# Логи приложения
docker logs investment-data-loader-service
```

### Доступные endpoints

- **Приложение**: http://localhost:8083
- **Health Check**: http://localhost:8083/actuator/health
- **API Documentation**: http://localhost:8083/swagger-ui.html

## 🔍 Устранение неполадок

### Проблемы с подключением к БД

1. **Проверьте доступность БД:**
   ```bash
   # Для БД на хосте
   telnet host.docker.internal 5434
   
   # Для БД в контейнере
   docker exec -it your-db-container pg_isready -U postgres
   ```

2. **Проверьте логи приложения:**
   ```bash
   docker logs investment-data-loader-service
   ```

3. **Проверьте настройки сети:**
   ```bash
   # Список Docker сетей
   docker network ls
   
   # Подключение к сети БД
   docker network connect your-db-network investment-data-loader-service
   ```

### Частые ошибки

| Ошибка | Причина | Решение |
|--------|---------|---------|
| `Connection refused` | БД недоступна | Проверьте, что БД запущена |
| `Connection timed out` | Неправильный хост/порт | Проверьте настройки в .env |
| `Authentication failed` | Неправильные учетные данные | Проверьте логин/пароль |
| `Database does not exist` | База данных не создана | Создайте БД или измените имя |

### Отладка подключения

```bash
# Подключение к контейнеру приложения
docker exec -it investment-data-loader-service bash

# Проверка переменных окружения
env | grep SPRING_DATASOURCE

# Тест подключения к БД из контейнера
ping host.docker.internal
telnet host.docker.internal 5434
```

## 🔄 Миграция с внутренней БД

Если у вас уже есть данные в контейнере PostgreSQL:

1. **Экспорт данных:**
   ```bash
   docker exec investment-postgres pg_dump -U postgres postgres > backup.sql
   ```

2. **Импорт в новую БД:**
   ```bash
   psql -h your-db-host -U postgres -d postgres < backup.sql
   ```

3. **Обновление схемы:**
   Убедитесь, что в новой БД есть схема `invest`:
   ```sql
   CREATE SCHEMA IF NOT EXISTS invest;
   ```

## 📝 Дополнительные настройки

### Настройка сети Docker

Если БД в другой Docker сети:

```yaml
services:
  investment-service:
    networks:
      - investment-network
      - your-existing-db-network
    external_links:
      - your-db-container:postgres

networks:
  investment-network:
    driver: bridge
  your-existing-db-network:
    external: true
```

### Настройка SSL для БД

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://host.docker.internal:5434/postgres?sslmode=require
```

### Настройка пула соединений

```yaml
environment:
  SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE: 10
  SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE: 2
```

## 🆘 Поддержка

При возникновении проблем:

1. Проверьте логи: `.\docker-build-external-db.ps1 logs`
2. Проверьте подключение к БД: `.\docker-build-external-db.ps1 test-db`
3. Убедитесь, что внешняя БД доступна и настроена правильно
4. Проверьте настройки в файле `.env`
