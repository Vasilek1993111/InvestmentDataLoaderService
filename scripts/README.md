# 🚀 Скрипты запуска приложения

Эта папка содержит все скрипты для запуска приложения в разных окружениях.

## 📁 Структура файлов

### 🔧 **Основные скрипты запуска**
- `run-test.bat` / `run-test.sh` - запуск тестового окружения
- `run-prod.bat` / `run-prod.sh` - запуск продакшн окружения

### 🐳 **Docker скрипты**
- `docker-run-test.bat` / `docker-run-test.sh` - запуск тестового окружения в Docker
- `docker-run-prod.bat` / `docker-run-prod.sh` - запуск продакшн окружения в Docker

### 🐍 **Python скрипты автоматизации**
- `load-daily-candles-2025.py` - автоматическая загрузка дневных свечей за 2025 год
  - Требует: `pip install requests`
  - Использование: `python scripts/load-daily-candles-2025.py --test`
  - Подробная документация: [API Automation Guide](../docs/api-automation-guide.md)

### ⚙️ **Файлы конфигурации**
- `env.test` - переменные окружения для тестового профиля
- `env.prod` - переменные окружения для продакшн профиля

## 🚀 Быстрый старт

### **Windows:**
```cmd
# Тестовое окружение
run-test.bat

# Продакшн окружение
run-prod.bat

# Docker тестовое окружение
docker-run-test.bat

# Docker продакшн окружение
docker-run-prod.bat
```

### **Linux/Mac:**
```bash
# Сделайте скрипты исполняемыми
chmod +x *.sh

# Тестовое окружение
./run-test.sh

# Продакшн окружение
./run-prod.sh

# Docker тестовое окружение
./docker-run-test.sh

# Docker продакшн окружение
./docker-run-prod.sh

# Python скрипт автоматизации (требует установки requests)
python scripts/load-daily-candles-2025.py --test
```

## 🐍 Python скрипты автоматизации

### Установка зависимостей

```bash
pip install requests
```

### Использование

Подробная документация: [API Automation Guide](../docs/api-automation-guide.md)

**Быстрый старт:**
```bash
# Тестовое окружение
python scripts/load-daily-candles-2025.py --test

# Продакшн окружение
python scripts/load-daily-candles-2025.py

# С исключением выходных дней
python scripts/load-daily-candles-2025.py --exclude-weekends
```

## ⚙️ Настройка переменных окружения

### **1. Скопируйте файлы конфигурации:**
```bash
# Windows
copy env.test ..\.env.test
copy env.prod ..\.env.prod

# Linux/Mac
cp env.test ../.env.test
cp env.prod ../.env.prod
```

### **2. Отредактируйте файлы с реальными значениями:**
- `env.test` - настройки для тестового окружения
- `env.prod` - настройки для продакшн окружения

## 🔍 Описание окружений

### **TEST окружение:**
- **Порт**: 8087
- **База данных**: postgres (localhost:5434)
- **Токен**: T_INVEST_TEST_TOKEN
- **Логирование**: DEBUG

### **PROD окружение:**
- **Порт**: 8083
- **База данных**: postgres (45.132.176.136:5432)
- **Токен**: T_INVEST_PROD_TOKEN
- **Логирование**: INFO

## ⚠️ Важные моменты

1. **Перед запуском продакшн окружения** убедитесь, что установлены переменные окружения
2. **Для Docker** убедитесь, что Docker запущен
3. **Для тестового окружения** убедитесь, что PostgreSQL запущен на localhost:5434
4. **Для Python скриптов** установите зависимости: `pip install requests`
5. **Не коммитьте** файлы `.env.test` и `.env.prod` в Git

## 🛠️ Отладка

### **Проблемы с подключением к БД:**
1. Проверьте, что PostgreSQL запущен
2. Убедитесь, что порты доступны
3. Проверьте правильность URL и учетных данных

### **Проблемы с Docker:**
1. Убедитесь, что Docker запущен
2. Проверьте, что образ приложения собран
3. Убедитесь, что порты не заняты

### **Проблемы с Python скриптами:**
1. Убедитесь, что установлен Python 3.6+
2. Проверьте установку зависимостей: `pip list | grep requests`
3. Проверьте доступность API: `curl http://localhost:8083/api/health`

## 📞 Поддержка

При возникновении проблем:
1. Проверьте логи приложения
2. Убедитесь в правильности переменных окружения
3. Проверьте доступность внешних сервисов (БД, API)
4. Для Python скриптов см. [API Automation Guide](../docs/api-automation-guide.md)
