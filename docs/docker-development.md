# Режим разработки - Hot Reload

## 🚀 Быстрый старт

### Вариант 1: Docker с Hot Reload (рекомендуется)
```bash
# Запуск в режиме разработки
dev-start.bat

# Или вручную:
docker-compose up -d --build
```

### Вариант 2: Локальный запуск (самый быстрый)
```bash
# 1. Создайте .env файл на основе env.example
copy env.example .env

# 2. Отредактируйте .env файл и добавьте ваш токен
# T_INVEST_TOKEN=your_actual_token_here

# 3. Запустите локально
dev-local.bat

# Или вручную:
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 🔧 Как работает Hot Reload

### Volume Mapping
- `./src` → `/app/src` - исходный код
- `./target` → `/app/target` - скомпилированные классы
- `./src/main/resources` → `/app/src/main/resources` - конфигурация

### Spring Boot DevTools
- Автоматически перезапускает приложение при изменении Java файлов
- Время перезапуска: 1-2 секунды
- Отслеживает изменения в classpath

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
docker-compose up -d --build
```

## 🐛 Отладка

### Просмотр логов
```bash
# Все логи
docker-compose logs -f

# Только ошибки
docker-compose logs -f | findstr ERROR

# Последние 50 строк
docker-compose logs --tail=50
```

### Проверка статуса
```bash
# Статус контейнеров
docker-compose ps

# Информация о контейнере
docker inspect investment-data-loader-service
```

### Подключение к контейнеру
```bash
# Войти в контейнер
docker exec -it investment-data-loader-service sh

# Выполнить команду
docker exec -it investment-data-loader-service ls -la /app
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
volumes:
  - ./src:/app/src                    # Исходный код
  - ./target:/app/target              # Скомпилированные классы
  - ./src/main/resources:/app/src/main/resources  # Конфигурация
```

## 🚨 Устранение проблем

### DevTools не работает
1. Проверьте, что зависимость добавлена в pom.xml
2. Убедитесь, что scope=runtime
3. Проверьте переменные окружения в docker-compose.yml

### Volume mapping не работает
1. Проверьте права доступа к папкам
2. Убедитесь, что пути указаны правильно
3. Перезапустите контейнер: `docker-compose restart`

### Медленная пересборка
1. Используйте .dockerignore
2. Проверьте, что кэширование работает
3. Рассмотрите локальный запуск для отладки

## 📊 Мониторинг

### Health Check
```bash
# Проверка здоровья приложения
curl http://localhost:8083/actuator/health

# Детальная информация
curl http://localhost:8083/actuator/info
```

### Метрики
```bash
# Метрики JVM
curl http://localhost:8083/actuator/metrics

# Использование памяти
curl http://localhost:8083/actuator/metrics/jvm.memory.used
```
