@echo off
echo ===========================================
echo ЗАПУСК ПРОДАКШН ОКРУЖЕНИЯ В DOCKER
echo ===========================================
echo Профиль: prod
echo База данных: postgres (45.132.176.136:5432)
echo Порт: 8083
echo ===========================================
echo ВНИМАНИЕ: Убедитесь, что установлены переменные окружения!
echo T_INVEST_PROD_TOKEN - токен для продакшн API
echo SPRING_DATASOURCE_PROD_* - настройки продакшн БД
echo ===========================================

REM Проверяем наличие обязательных переменных
if "%T_INVEST_PROD_TOKEN%"=="" (
    echo ОШИБКА: T_INVEST_PROD_TOKEN не установлен!
    echo Установите переменную окружения или создайте .env.prod файл
    pause
    exit /b 1
)

if "%SPRING_DATASOURCE_PROD_URL%"=="" (
    echo ОШИБКА: SPRING_DATASOURCE_PROD_URL не установлен!
    echo Установите переменную окружения или создайте .env.prod файл
    pause
    exit /b 1
)

echo Запускаем продакшн окружение...
docker-compose --profile production up investment-service-prod --build

pause
