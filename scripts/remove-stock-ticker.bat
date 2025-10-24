@echo off
REM Скрипт для удаления лишней колонки stock_ticker из таблицы futures

echo ============================================
echo Удаление колонки stock_ticker из таблицы futures
echo ============================================
echo.

REM Получаем путь к корневой директории проекта (на уровень выше от scripts)
set PROJECT_ROOT=%~dp0..

REM Выполняем SQL-скрипт
set PGPASSWORD=vaniam2101
psql -h localhost -p 5434 -U postgres -d postgres -f "%PROJECT_ROOT%\db\21-remove-stock-ticker-column.sql"

echo.
echo ============================================
echo Готово!
echo ============================================
pause

