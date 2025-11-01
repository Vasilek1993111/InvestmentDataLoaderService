# Используем официальный образ Eclipse Temurin 21 для сборки
FROM eclipse-temurin:21-jdk AS builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Устанавливаем Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Копируем файлы pom.xml для кэширования зависимостей
COPY pom.xml .

# Скачиваем зависимости (этот слой будет кэшироваться если pom.xml не изменился)
RUN mvn dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение без тестов и их компиляции
RUN mvn clean package -Dmaven.test.skip=true -DskipTests=true -Dmaven.main.skip=false

# Финальный образ для запуска
FROM eclipse-temurin:21-jre

# Устанавливаем рабочую директорию
WORKDIR /app

# Создаем пользователя для безопасности
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Копируем собранный JAR файл из builder образа
COPY --from=builder /app/target/*.jar app.jar

# Создаем директории для логов и меняем владельца
RUN mkdir -p /app/logs/current /app/logs/archive && \
    chown -R appuser:appuser /app/logs

# Меняем владельца файла
RUN chown appuser:appuser app.jar

# Переключаемся на непривилегированного пользователя
USER appuser

# Открываем порт
EXPOSE 8083

# Настраиваем JVM для контейнера
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# DevTools настройки для hot reload
ENV SPRING_DEVTOOLS_RESTART_ENABLED=true
ENV SPRING_DEVTOOLS_LIVERELOAD_ENABLED=true

# Команда запуска с диагностикой
ENTRYPOINT ["sh", "-c", "echo 'Starting application with JAVA_OPTS: $JAVA_OPTS' && echo 'Database URL: $SPRING_DATASOURCE_URL' && echo 'DevTools enabled: $SPRING_DEVTOOLS_RESTART_ENABLED' && java $JAVA_OPTS -jar app.jar"]
