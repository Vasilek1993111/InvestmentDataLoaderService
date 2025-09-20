#!/bin/bash

echo "==========================================="
echo "ЗАПУСК В ПРОДАКШН РЕЖИМЕ"
echo "==========================================="
echo "Профиль: prod"
echo "База данных: postgres (45.132.176.136:5432)"
echo "Порт: 8083"
echo "==========================================="
echo "ВНИМАНИЕ: Убедитесь, что установлены переменные окружения!"
echo "T_INVEST_PROD_TOKEN - токен для продакшн API"
echo "SPRING_DATASOURCE_PROD_* - настройки продакшн БД"
echo "==========================================="

# Устанавливаем переменные окружения для продакшн профиля
export SPRING_PROFILES_ACTIVE=prod

# Проверяем наличие обязательных переменных
if [ -z "$T_INVEST_PROD_TOKEN" ]; then
    echo "ОШИБКА: T_INVEST_PROD_TOKEN не установлен!"
    echo "Установите переменную окружения или создайте .env.prod файл"
    exit 1
fi

if [ -z "$SPRING_DATASOURCE_PROD_URL" ]; then
    echo "ОШИБКА: SPRING_DATASOURCE_PROD_URL не установлен!"
    echo "Установите переменную окружения или создайте .env.prod файл"
    exit 1
fi

echo "Запускаем приложение..."
mvn spring-boot:run
