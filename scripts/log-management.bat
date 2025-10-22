@echo off
REM Скрипт для управления логами Investment Data Loader Service

echo ========================================
echo Управление логами Investment Data Loader
echo ========================================

:menu
echo.
echo Выберите действие:
echo 1. Просмотр текущих логов
echo 2. Просмотр архивных логов
echo 3. Очистка старых логов
echo 4. Статистика логов
echo 5. Поиск по логам
echo 6. Просмотр ошибок
echo 7. Выход
echo.
set /p choice="Введите номер (1-7): "

if "%choice%"=="1" goto current_logs
if "%choice%"=="2" goto archive_logs
if "%choice%"=="3" goto cleanup_logs
if "%choice%"=="4" goto log_stats
if "%choice%"=="5" goto search_logs
if "%choice%"=="6" goto view_errors
if "%choice%"=="7" goto exit
goto menu

:current_logs
echo.
echo === ТЕКУЩИЕ ЛОГИ ===
echo.
echo Размеры файлов:
dir logs\current\*.log
echo.
echo Последние 10 строк system.log:
echo ----------------------------------------
tail -n 10 logs\current\system.log
echo.
pause
goto menu

:archive_logs
echo.
echo === АРХИВНЫЕ ЛОГИ ===
echo.
if exist logs\archive (
    echo Найденные архивы:
    dir logs\archive\ /b
    echo.
    set /p date="Введите дату (YYYY-MM-DD) для просмотра: "
    if exist logs\archive\%date% (
        echo Логи за %date%:
        dir logs\archive\%date%\
    ) else (
        echo Архив за %date% не найден
    )
) else (
    echo Архивные логи не найдены
)
echo.
pause
goto menu

:cleanup_logs
echo.
echo === ОЧИСТКА СТАРЫХ ЛОГОВ ===
echo.
echo ВНИМАНИЕ: Это удалит логи старше 30 дней!
set /p confirm="Продолжить? (y/n): "
if /i "%confirm%"=="y" (
    echo Удаление старых логов...
    forfiles /p logs\archive /s /d -30 /c "cmd /c del @path" 2>nul
    echo Очистка завершена
) else (
    echo Операция отменена
)
echo.
pause
goto menu

:log_stats
echo.
echo === СТАТИСТИКА ЛОГОВ ===
echo.
echo Размеры текущих логов:
for %%f in (logs\current\*.log) do (
    echo %%~nxf: %%~zf байт
)
echo.
echo Общий размер логов:
dir logs\current\ /s | find "File(s)"
echo.
echo Количество ошибок:
findstr /c:"ERROR" logs\current\errors.log | find /c /v ""
echo.
pause
goto menu

:search_logs
echo.
echo === ПОИСК ПО ЛОГАМ ===
echo.
set /p search_term="Введите поисковый запрос: "
echo.
echo Результаты поиска в текущих логах:
findstr /i "%search_term%" logs\current\*.log
echo.
pause
goto menu

:view_errors
echo.
echo === ПРОСМОТР ОШИБОК ===
echo.
echo Последние 20 ошибок:
echo ----------------------------------------
tail -n 20 logs\current\errors.log
echo.
pause
goto menu

:exit
echo.
echo До свидания!
exit /b 0
