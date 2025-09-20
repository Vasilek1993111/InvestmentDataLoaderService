@echo off
echo ===========================================
echo ЗАПУСК В ТЕСТОВОМ РЕЖИМЕ
echo ===========================================
echo Профиль: test
echo База данных: postgres (localhost:5434)
echo Порт: 8087
echo ===========================================

REM Устанавливаем переменные окружения для тестового профиля
set SPRING_PROFILES_ACTIVE=test
set T_INVEST_TEST_TOKEN=test-token-12345
set SPRING_DATASOURCE_TEST_URL=jdbc:postgresql://localhost:5434/postgres
set SPRING_DATASOURCE_TEST_USERNAME=postgres
set SPRING_DATASOURCE_TEST_PASSWORD=123password123

echo Запускаем приложение...
mvn spring-boot:run

pause
