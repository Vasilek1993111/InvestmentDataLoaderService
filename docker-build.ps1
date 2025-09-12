# PowerShell скрипт для сборки и запуска Docker контейнера
# Использование: .\docker-build.ps1 [build|start|stop|restart|logs]

param(
    [string]$Action = "start"
)

# Функции для вывода сообщений
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Проверка наличия Docker
function Test-Docker {
    try {
        docker --version | Out-Null
        docker-compose --version | Out-Null
        return $true
    }
    catch {
        Write-Error "Docker или Docker Compose не установлены. Пожалуйста, установите их."
        return $false
    }
}

# Проверка переменных окружения
function Test-Environment {
    if (-not (Test-Path ".env")) {
        Write-Warning "Файл .env не найден. Создаю его на основе env.example"
        if (Test-Path "env.example") {
            Copy-Item "env.example" ".env"
            Write-Warning "Пожалуйста, отредактируйте .env файл и укажите реальный T_INVEST_TOKEN"
        }
    }
}

# Сборка образа
function Build-Image {
    Write-Info "Сборка Docker образа..."
    docker-compose build --no-cache
    if ($LASTEXITCODE -eq 0) {
        Write-Info "Образ успешно собран!"
    } else {
        Write-Error "Ошибка при сборке образа"
        exit 1
    }
}

# Запуск контейнеров
function Start-Containers {
    Write-Info "Запуск контейнеров..."
    docker-compose up -d
    if ($LASTEXITCODE -eq 0) {
        Write-Info "Контейнеры запущены!"
        Write-Info "Приложение доступно по адресу: http://localhost:8083"
        Write-Info "PostgreSQL доступна по адресу: localhost:5434"
    } else {
        Write-Error "Ошибка при запуске контейнеров"
        exit 1
    }
}

# Остановка контейнеров
function Stop-Containers {
    Write-Info "Остановка контейнеров..."
    docker-compose down
    if ($LASTEXITCODE -eq 0) {
        Write-Info "Контейнеры остановлены!"
    } else {
        Write-Error "Ошибка при остановке контейнеров"
        exit 1
    }
}

# Перезапуск контейнеров
function Restart-Containers {
    Write-Info "Перезапуск контейнеров..."
    docker-compose restart
    if ($LASTEXITCODE -eq 0) {
        Write-Info "Контейнеры перезапущены!"
    } else {
        Write-Error "Ошибка при перезапуске контейнеров"
        exit 1
    }
}

# Просмотр логов
function Show-Logs {
    Write-Info "Показ логов..."
    docker-compose logs -f
}

# Очистка
function Invoke-Cleanup {
    Write-Info "Очистка неиспользуемых Docker ресурсов..."
    docker system prune -f
    if ($LASTEXITCODE -eq 0) {
        Write-Info "Очистка завершена!"
    } else {
        Write-Error "Ошибка при очистке"
        exit 1
    }
}

# Основная логика
function Main {
    if (-not (Test-Docker)) {
        exit 1
    }
    
    Test-Environment
    
    switch ($Action.ToLower()) {
        "build" {
            Build-Image
        }
        "start" {
            Build-Image
            Start-Containers
        }
        "stop" {
            Stop-Containers
        }
        "restart" {
            Restart-Containers
        }
        "logs" {
            Show-Logs
        }
        "cleanup" {
            Invoke-Cleanup
        }
        default {
            Write-Host "Использование: .\docker-build.ps1 [build|start|stop|restart|logs|cleanup]"
            Write-Host ""
            Write-Host "Команды:"
            Write-Host "  build   - Собрать Docker образ"
            Write-Host "  start   - Собрать и запустить контейнеры (по умолчанию)"
            Write-Host "  stop    - Остановить контейнеры"
            Write-Host "  restart - Перезапустить контейнеры"
            Write-Host "  logs    - Показать логи"
            Write-Host "  cleanup - Очистить неиспользуемые Docker ресурсы"
            exit 1
        }
    }
}

Main
