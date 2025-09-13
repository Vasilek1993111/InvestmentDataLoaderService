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

# Собираем приложение
RUN mvn clean package -DskipTests

# Финальный образ для запуска
FROM eclipse-temurin:21-jre

# Устанавливаем рабочую директорию
WORKDIR /app

# Создаем пользователя для безопасности
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Копируем собранный JAR файл из builder образа
COPY --from=builder /app/target/*.jar app.jar

# Меняем владельца файла
RUN chown appuser:appuser app.jar

# Переключаемся на непривилегированного пользователя
USER appuser

# Открываем порт
EXPOSE 8083

# Настраиваем JVM для контейнера
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Команда запуска с диагностикой
ENTRYPOINT ["sh", "-c", "echo 'Starting application with JAVA_OPTS: $JAVA_OPTS' && echo 'Database URL: $SPRING_DATASOURCE_URL' && java $JAVA_OPTS -jar app.jar"]
