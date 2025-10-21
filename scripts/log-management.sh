#!/bin/bash

# Скрипт для управления логами Investment Data Loader Service

echo "========================================"
echo "Управление логами Investment Data Loader"
echo "========================================"

show_menu() {
    echo ""
    echo "Выберите действие:"
    echo "1. Просмотр текущих логов"
    echo "2. Просмотр архивных логов"
    echo "3. Очистка старых логов"
    echo "4. Статистика логов"
    echo "5. Поиск по логам"
    echo "6. Просмотр ошибок"
    echo "7. Выход"
    echo ""
    read -p "Введите номер (1-7): " choice
}

current_logs() {
    echo ""
    echo "=== ТЕКУЩИЕ ЛОГИ ==="
    echo ""
    echo "Размеры файлов:"
    ls -lh logs/current/*.log 2>/dev/null || echo "Логи не найдены"
    echo ""
    echo "Последние 10 строк system.log:"
    echo "----------------------------------------"
    tail -n 10 logs/current/system.log 2>/dev/null || echo "Файл не найден"
    echo ""
    read -p "Нажмите Enter для продолжения..."
}

archive_logs() {
    echo ""
    echo "=== АРХИВНЫЕ ЛОГИ ==="
    echo ""
    if [ -d "logs/archive" ]; then
        echo "Найденные архивы:"
        ls -1 logs/archive/ 2>/dev/null || echo "Архивы не найдены"
        echo ""
        read -p "Введите дату (YYYY-MM-DD) для просмотра: " date
        if [ -d "logs/archive/$date" ]; then
            echo "Логи за $date:"
            ls -lh logs/archive/$date/
        else
            echo "Архив за $date не найден"
        fi
    else
        echo "Архивные логи не найдены"
    fi
    echo ""
    read -p "Нажмите Enter для продолжения..."
}

cleanup_logs() {
    echo ""
    echo "=== ОЧИСТКА СТАРЫХ ЛОГОВ ==="
    echo ""
    echo "ВНИМАНИЕ: Это удалит логи старше 30 дней!"
    read -p "Продолжить? (y/n): " confirm
    if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
        echo "Удаление старых логов..."
        find logs/archive -type f -mtime +30 -delete 2>/dev/null
        find logs/archive -type d -empty -delete 2>/dev/null
        echo "Очистка завершена"
    else
        echo "Операция отменена"
    fi
    echo ""
    read -p "Нажмите Enter для продолжения..."
}

log_stats() {
    echo ""
    echo "=== СТАТИСТИКА ЛОГОВ ==="
    echo ""
    echo "Размеры текущих логов:"
    for file in logs/current/*.log; do
        if [ -f "$file" ]; then
            size=$(du -h "$file" | cut -f1)
            name=$(basename "$file")
            echo "$name: $size"
        fi
    done
    echo ""
    echo "Общий размер логов:"
    du -sh logs/current/ 2>/dev/null || echo "Директория не найдена"
    echo ""
    echo "Количество ошибок:"
    grep -c "ERROR" logs/current/errors.log 2>/dev/null || echo "0"
    echo ""
    read -p "Нажмите Enter для продолжения..."
}

search_logs() {
    echo ""
    echo "=== ПОИСК ПО ЛОГАМ ==="
    echo ""
    read -p "Введите поисковый запрос: " search_term
    echo ""
    echo "Результаты поиска в текущих логах:"
    grep -i "$search_term" logs/current/*.log 2>/dev/null || echo "Ничего не найдено"
    echo ""
    read -p "Нажмите Enter для продолжения..."
}

view_errors() {
    echo ""
    echo "=== ПРОСМОТР ОШИБОК ==="
    echo ""
    echo "Последние 20 ошибок:"
    echo "----------------------------------------"
    tail -n 20 logs/current/errors.log 2>/dev/null || echo "Файл не найден"
    echo ""
    read -p "Нажмите Enter для продолжения..."
}

# Основной цикл
while true; do
    show_menu
    case $choice in
        1) current_logs ;;
        2) archive_logs ;;
        3) cleanup_logs ;;
        4) log_stats ;;
        5) search_logs ;;
        6) view_errors ;;
        7) echo ""; echo "До свидания!"; exit 0 ;;
        *) echo "Неверный выбор. Попробуйте снова." ;;
    esac
done
