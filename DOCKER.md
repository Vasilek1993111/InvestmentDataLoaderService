# Docker Setup для Investment Data Loader Service

Этот документ описывает, как собрать и запустить Investment Data Loader Service в Docker контейнере.

## Предварительные требования

- Docker Desktop (Windows/Mac) или Docker Engine (Linux)
- Docker Compose
- Tinkoff Invest API токен

## Быстрый старт

### 1. Настройка переменных окружения

Скопируйте файл `env.example` в `.env` и укажите ваш Tinkoff Invest API токен:

```bash
# Windows (PowerShell)
Copy-Item env.example .env

# Linux/Mac
cp env.example .env
```

Отредактируйте файл `.env` и укажите реальный токен:

```env
T_INVEST_TOKEN=your_actual_tinkoff_invest_token_here
```

### 2. Запуск приложения

#### Windows (PowerShell)
```powershell
.\docker-build.ps1 start
```

#### Linux/Mac (Bash)
```bash
./docker-build.sh start
```

#### Альтернативно (любая ОС)
```bash
docker-compose up --build -d
```

## Доступные команды

### Windows (PowerShell)
- `.\docker-build.ps1 build` - Собрать Docker образ
- `.\docker-build.ps1 start` - Собрать и запустить контейнеры
- `.\docker-build.ps1 stop` - Остановить контейнеры
- `.\docker-build.ps1 restart` - Перезапустить контейнеры
- `.\docker-build.ps1 logs` - Показать логи
- `.\docker-build.ps1 cleanup` - Очистить неиспользуемые Docker ресурсы

### Linux/Mac (Bash)
- `./docker-build.sh build` - Собрать Docker образ
- `./docker-build.sh start` - Собрать и запустить контейнеры
- `./docker-build.sh stop` - Остановить контейнеры
- `./docker-build.sh restart` - Перезапустить контейнеры
- `./docker-build.sh logs` - Показать логи
- `./docker-build.sh cleanup` - Очистить неиспользуемые Docker ресурсы

## Доступ к сервисам

После успешного запуска:

- **Spring Boot приложение**: http://localhost:8083
- **Health Check**: http://localhost:8083/actuator/health
- **PostgreSQL**: localhost:5434
  - База данных: `postgres`
  - Пользователь: `postgres`
  - Пароль: `123password123`

## Структура Docker файлов

```
├── Dockerfile              # Основной Docker образ
├── docker-compose.yml      # Конфигурация для запуска с PostgreSQL
├── .dockerignore           # Исключения для Docker контекста
├── docker-build.sh         # Bash скрипт для управления (Linux/Mac)
├── docker-build.ps1        # PowerShell скрипт для управления (Windows)
├── env.example             # Пример переменных окружения
├── init-scripts/           # Скрипты инициализации БД
│   └── 01-init-schema.sql
└── DOCKER.md              # Этот файл
```

## Особенности конфигурации

### Multi-stage сборка
Dockerfile использует multi-stage сборку для оптимизации размера финального образа:
1. **Builder stage**: Сборка приложения с Maven
2. **Runtime stage**: Запуск приложения с минимальным JRE

### Безопасность
- Приложение запускается под непривилегированным пользователем
- Используется slim образ OpenJDK для уменьшения поверхности атак
- Переменные окружения для конфиденциальных данных

### Производительность
- Настроены JVM параметры для контейнеров
- Используется кэширование слоев Docker
- Health checks для проверки готовности сервисов

## Мониторинг и логи

### Просмотр логов
```bash
# Все сервисы
docker-compose logs -f

# Только приложение
docker-compose logs -f investment-service

# Только PostgreSQL
docker-compose logs -f postgres
```

### Health checks
```bash
# Проверка статуса контейнеров
docker-compose ps

# Проверка health check приложения
curl http://localhost:8083/actuator/health
```

## Устранение неполадок

### Проблемы с подключением к БД
1. Убедитесь, что PostgreSQL контейнер запущен: `docker-compose ps`
2. Проверьте логи PostgreSQL: `docker-compose logs postgres`
3. Убедитесь, что health check прошел успешно

### Проблемы с Tinkoff API
1. Проверьте, что токен указан в `.env` файле
2. Убедитесь, что токен действителен и имеет необходимые права
3. Проверьте логи приложения: `docker-compose logs investment-service`

### Очистка и пересборка
```bash
# Полная очистка и пересборка
docker-compose down -v
docker system prune -f
docker-compose up --build -d
```

## Разработка

### Локальная разработка с Docker
Для разработки с hot reload можно использовать:

```bash
# Запуск только PostgreSQL
docker-compose up postgres -d

# Запуск приложения локально с подключением к Docker PostgreSQL
./mvnw spring-boot:run
```

### Отладка
Для отладки можно подключиться к контейнеру:

```bash
# Подключение к контейнеру приложения
docker exec -it investment-data-loader-service bash

# Подключение к PostgreSQL
docker exec -it investment-postgres psql -U postgres
```
