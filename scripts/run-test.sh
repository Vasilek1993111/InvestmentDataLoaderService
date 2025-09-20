#!/bin/bash

echo "==========================================="
echo "ЗАПУСК В ТЕСТОВОМ РЕЖИМЕ"
echo "==========================================="
echo "Профиль: test"
echo "База данных: postgres (localhost:5434)"
echo "Порт: 8087"
echo "==========================================="

# Устанавливаем переменные окружения для тестового профиля
export SPRING_PROFILES_ACTIVE=test
export T_INVEST_TEST_TOKEN=test-token-12345
export SPRING_DATASOURCE_TEST_URL=jdbc:postgresql://localhost:5434/postgres
export SPRING_DATASOURCE_TEST_USERNAME=postgres
export SPRING_DATASOURCE_TEST_PASSWORD=123password123

echo "Запускаем приложение..."
mvn spring-boot:run
