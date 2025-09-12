#!/bin/bash

# Скрипт для сборки и запуска Docker контейнера
# Использование: ./docker-build.sh [build|start|stop|restart|logs]

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Функция для вывода сообщений
log() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Проверка наличия Docker
check_docker() {
    if ! command -v docker &> /dev/null; then
        error "Docker не установлен. Пожалуйста, установите Docker."
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose не установлен. Пожалуйста, установите Docker Compose."
        exit 1
    fi
}

# Проверка переменных окружения
check_env() {
    if [ ! -f .env ]; then
        warn "Файл .env не найден. Создайте его на основе env.example"
        if [ -f env.example ]; then
            log "Копирую env.example в .env..."
            cp env.example .env
            warn "Пожалуйста, отредактируйте .env файл и укажите реальный T_INVEST_TOKEN"
        fi
    fi
}

# Сборка образа
build_image() {
    log "Сборка Docker образа..."
    docker-compose build --no-cache
    log "Образ успешно собран!"
}

# Запуск контейнеров
start_containers() {
    log "Запуск контейнеров..."
    docker-compose up -d
    log "Контейнеры запущены!"
    log "Приложение доступно по адресу: http://localhost:8083"
    log "PostgreSQL доступна по адресу: localhost:5434"
}

# Остановка контейнеров
stop_containers() {
    log "Остановка контейнеров..."
    docker-compose down
    log "Контейнеры остановлены!"
}

# Перезапуск контейнеров
restart_containers() {
    log "Перезапуск контейнеров..."
    docker-compose restart
    log "Контейнеры перезапущены!"
}

# Просмотр логов
show_logs() {
    log "Показ логов..."
    docker-compose logs -f
}

# Очистка
cleanup() {
    log "Очистка неиспользуемых Docker ресурсов..."
    docker system prune -f
    log "Очистка завершена!"
}

# Основная логика
main() {
    check_docker
    check_env
    
    case "${1:-start}" in
        "build")
            build_image
            ;;
        "start")
            build_image
            start_containers
            ;;
        "stop")
            stop_containers
            ;;
        "restart")
            restart_containers
            ;;
        "logs")
            show_logs
            ;;
        "cleanup")
            cleanup
            ;;
        *)
            echo "Использование: $0 [build|start|stop|restart|logs|cleanup]"
            echo ""
            echo "Команды:"
            echo "  build   - Собрать Docker образ"
            echo "  start   - Собрать и запустить контейнеры (по умолчанию)"
            echo "  stop    - Остановить контейнеры"
            echo "  restart - Перезапустить контейнеры"
            echo "  logs    - Показать логи"
            echo "  cleanup - Очистить неиспользуемые Docker ресурсы"
            exit 1
            ;;
    esac
}

main "$@"
