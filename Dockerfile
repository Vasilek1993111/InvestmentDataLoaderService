# Используем официальный образ OpenJDK 21 для сборки
FROM openjdk:21-jdk-slim AS builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы pom.xml и mvnw для кэширования зависимостей
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Делаем mvnw исполняемым
RUN chmod +x mvnw

# Скачиваем зависимости (этот слой будет кэшироваться если pom.xml не изменился)
RUN ./mvnw dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN ./mvnw clean package -DskipTests

# Финальный образ для запуска
FROM openjdk:21-jre-slim

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

# Команда запуска
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
