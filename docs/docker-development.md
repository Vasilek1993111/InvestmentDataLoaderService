# Режим разработки - Hot Reload

## 🚀 Быстрый старт

### Вариант 1: Docker с Hot Reload (рекомендуется)
```bash
# Запуск тестового окружения
scripts\docker-run-test.bat

# Или вручную:
docker-compose up -d investment-service-test --build
```

### Вариант 2: Локальный запуск (самый быстрый)
```bash
# 1. Создайте .env.test файл на основе env.test.example
copy scripts\env.test.example .env.test

# 2. Отредактируйте .env.test файл и добавьте ваш токен
# T_INVEST_TEST_TOKEN=your_actual_token_here

# 3. Запустите локально
scripts\run-test.bat

# Или вручную:
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

## 🔧 Как работает Hot Reload

### Volume Mapping
- `./src` → `/app/src` - исходный код
- `./target` → `/app/target` - скомпилированные классы
- `./src/main/resources` → `/app/src/main/resources` - конфигурация
- `./.env.test` → `/app/.env` - переменные окружения (только чтение)

### Spring Boot DevTools
- Автоматически перезапускает приложение при изменении Java файлов
- Время перезапуска: 1-2 секунды
- Отслеживает изменения в classpath
- Работает только в тестовом контейнере (investment-service-test)

## 📝 Workflow разработки

### 1. Изменение Java кода
```java
// Измените любой Java файл
@GetMapping("/test")
public String test() {
    return "Hello from hot reload!";
}
```
**Результат**: DevTools автоматически перезапустит приложение через 1-2 секунды

### 2. Изменение конфигурации
```properties
# Измените application.properties или application-dev.properties
server.port=8084
```
**Результат**: DevTools перезапустит приложение

### 3. Изменение зависимостей (pom.xml)
```xml
<!-- Добавьте новую зависимость -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```
**Результат**: Нужно пересобрать контейнер
```bash
docker-compose up -d investment-service-test --build
```

### 4. Изменение переменных окружения (.env.test)
```bash
# Измените токен или настройки БД
T_INVEST_TEST_TOKEN=new_token_here
SPRING_DATASOURCE_TEST_PASSWORD=new_password
```
**Результат**: Нужно перезапустить контейнер
```bash
docker-compose restart investment-service-test
```

## 🐛 Отладка

### Просмотр логов
```bash
# Все логи тестового контейнера
docker-compose logs -f investment-service-test

# Только ошибки
docker-compose logs -f investment-service-test | findstr ERROR

# Последние 50 строк
docker-compose logs --tail=50 investment-service-test
```

### Проверка статуса
```bash
# Статус контейнеров
docker-compose ps

# Информация о тестовом контейнере
docker inspect investment-data-loader-service-test
```

### Подключение к контейнеру
```bash
# Войти в тестовый контейнер
docker exec -it investment-data-loader-service-test sh

# Выполнить команду
docker exec -it investment-data-loader-service-test ls -la /app
```

## ⚡ Производительность

### Время перезапуска
- **Локальный запуск**: 5-10 секунд
- **Docker с DevTools**: 1-2 секунды
- **Docker без DevTools**: 30-60 секунд

### Оптимизация
- Используйте `.dockerignore` для исключения ненужных файлов
- Volume mapping работает только для файлов, которые можно заменить без перекомпиляции
- Для изменений в зависимостях нужна полная пересборка

## 🔧 Настройки

### application-dev.properties
```properties
# DevTools настройки
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true
spring.devtools.restart.poll-interval=1000
spring.devtools.restart.quiet-period=400
```

### docker-compose.yml
```yaml
investment-service-test:
  volumes:
    - ./src:/app/src                    # Исходный код
    - ./target:/app/target              # Скомпилированные классы
    - ./src/main/resources:/app/src/main/resources  # Конфигурация
    - ./.env.test:/app/.env:ro          # Переменные окружения
  ports:
    - "8087:8087"                       # Тестовый порт
  environment:
    SPRING_PROFILES_ACTIVE: test        # Тестовый профиль
    SPRING_DEVTOOLS_RESTART_ENABLED: "true"  # Hot reload
```

## 🚨 Устранение проблем

### DevTools не работает
1. Проверьте, что зависимость добавлена в pom.xml
2. Убедитесь, что scope=runtime
3. Проверьте переменные окружения в docker-compose.yml
4. Убедитесь, что SPRING_DEVTOOLS_RESTART_ENABLED=true

### Volume mapping не работает
1. Проверьте права доступа к папкам
2. Убедитесь, что пути указаны правильно
3. Перезапустите контейнер: `docker-compose restart investment-service-test`

### Медленная пересборка
1. Используйте .dockerignore
2. Проверьте, что кэширование работает
3. Рассмотрите локальный запуск для отладки

### Проблемы с базой данных
1. Убедитесь, что PostgreSQL запущен на localhost:5434
2. Проверьте пароль в .env.test файле
3. Используйте host.docker.internal вместо localhost для подключения из контейнера

## 📊 Мониторинг

### Health Check
```bash
# Проверка здоровья тестового приложения
curl http://localhost:8087/actuator/health

# Детальная информация
curl http://localhost:8087/actuator/info
```

### Метрики
```bash
# Метрики JVM
curl http://localhost:8087/actuator/metrics

# Использование памяти
curl http://localhost:8087/actuator/metrics/jvm.memory.used
```
