# Используем официальный образ OpenJDK 21
FROM openjdk:21-jre-slim

# Метаданные образа
LABEL maintainer="Ingestion Service Team"
LABEL version="0.0.1-SNAPSHOT"
LABEL description="Ingestion Service for collecting financial data from Tinkoff API"

# Создаем пользователя для безопасности
RUN groupadd -r ingestion && useradd -r -g ingestion ingestion

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR файл приложения
COPY target/ingestion-service-0.0.1-SNAPSHOT.jar app.jar

# Создаем директории для логов и конфигурации
RUN mkdir -p /var/log/ingestion-service && \
    mkdir -p /app/config && \
    chown -R ingestion:ingestion /var/log/ingestion-service && \
    chown -R ingestion:ingestion /app

# Переключаемся на пользователя ingestion
USER ingestion

# Открываем порт приложения
EXPOSE 8083

# Переменные окружения
ENV JAVA_OPTS="-Xms512m -Xmx1g"
ENV SPRING_PROFILES_ACTIVE="prod"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8083/actuator/health || exit 1

# Запуск приложения
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]