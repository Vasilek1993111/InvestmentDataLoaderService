@echo off
echo ===========================================
echo ЗАПУСК ТЕСТОВОГО ОКРУЖЕНИЯ В DOCKER
echo ===========================================
echo Профиль: test
echo База данных: postgres (localhost:5434)
echo Порт: 8087
echo ===========================================

REM Устанавливаем переменные окружения для тестового профиля
set T_INVEST_TEST_TOKEN=test-token-12345

echo Запускаем тестовое окружение...
docker-compose up investment-service-test --build

pause
