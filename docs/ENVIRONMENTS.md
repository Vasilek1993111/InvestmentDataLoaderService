# 🚀 Настройка окружений

Проект поддерживает 2 основных окружения: **test** и **prod** с разными настройками баз данных и токенами API.

## 📋 Структура окружений

### 🧪 **TEST окружение** (`application-test.properties`)
- **Порт**: 8087
- **База данных**: `postgres` (localhost:5434)
- **Токен**: `T_INVEST_TEST_TOKEN` (тестовый)
- **Логирование**: DEBUG (подробное)
- **DevTools**: включены
- **DDL**: `create-drop` (пересоздает схему при каждом запуске)

### 🏭 **PROD окружение** (`application-prod.properties`)
- **Порт**: 8083
- **База данных**: `postgres` (45.132.176.136:5432)
- **Токен**: `T_INVEST_PROD_TOKEN` (продакшн)
- **Логирование**: INFO (минимальное)
- **DevTools**: отключены
- **DDL**: `validate` (проверяет схему без изменений)

## 🔧 Настройка переменных окружения

### 1. **Создайте файлы с переменными**

```bash
# Скопируйте примеры и заполните реальными значениями
cp env.test.example .env.test
cp env.prod.example .env.prod
```

### 2. **Заполните переменные в .env файлах**

#### `.env.test`:
```env
T_INVEST_TEST_TOKEN=your-test-token-here
SPRING_DATASOURCE_TEST_URL=jdbc:postgresql://localhost:5434/postgres
SPRING_DATASOURCE_TEST_USERNAME=postgres
SPRING_DATASOURCE_TEST_PASSWORD=123password123
```

#### `.env.prod`:
```env
T_INVEST_PROD_TOKEN=your-production-token-here
SPRING_DATASOURCE_PROD_URL=jdbc:postgresql://45.132.176.136:5432/postgres
SPRING_DATASOURCE_PROD_USERNAME=postgres
SPRING_DATASOURCE_PROD_PASSWORD=your-production-password
```

## 🚀 Способы запуска

### **1. Через скрипты в папке scripts (рекомендуется)**

#### Windows:
```cmd
# Тестовое окружение
scripts\run-test.bat

# Продакшн окружение
scripts\run-prod.bat

# Docker тестовое окружение
scripts\docker-run-test.bat

# Docker продакшн окружение
scripts\docker-run-prod.bat
```

#### Linux/Mac:
```bash
# Сделайте скрипты исполняемыми
chmod +x scripts/*.sh

# Тестовое окружение
./scripts/run-test.sh

# Продакшн окружение
./scripts/run-prod.sh

# Docker тестовое окружение
./scripts/docker-run-test.sh

# Docker продакшн окружение
./scripts/docker-run-prod.sh
```

### **2. Через Maven**

```bash
# Тестовое окружение
mvn spring-boot:run -Dspring-boot.run.profiles=test

# Продакшн окружение
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### **3. Через переменные окружения**

```bash
# Windows
set SPRING_PROFILES_ACTIVE=test
mvn spring-boot:run

# Linux/Mac
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
```

### **4. Через IDE Cursor**

#### **4.1. Настройка конфигурации запуска**

1. **Откройте панель Run and Debug** (Ctrl+Shift+D)
2. **Создайте новую конфигурацию**:
   - Нажмите "Create a launch.json file"
   - Выберите "Java" из списка

3. **Настройте конфигурации для каждого окружения**:

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Investment Service - TEST",
            "request": "launch",
            "mainClass": "com.example.InvestmentDataLoaderService.InvestmentDataLoaderService",
            "projectName": "InvestmentDataLoaderService",
            "args": "",
            "vmArgs": "-Dspring.profiles.active=test -Dspring.config.location=classpath:/application-test.properties",
            "env": {
                "T_INVEST_TEST_TOKEN": "your-test-token-here",
                "SPRING_DATASOURCE_TEST_URL": "jdbc:postgresql://localhost:5434/postgres",
                "SPRING_DATASOURCE_TEST_USERNAME": "postgres",
                "SPRING_DATASOURCE_TEST_PASSWORD": "123password123"
            },
            "console": "integratedTerminal"
        },
        {
            "type": "java",
            "name": "Investment Service - PROD",
            "request": "launch",
            "mainClass": "com.example.InvestmentDataLoaderService.InvestmentDataLoaderService",
            "projectName": "InvestmentDataLoaderService",
            "args": "",
            "vmArgs": "-Dspring.profiles.active=prod -Dspring.config.location=classpath:/application-prod.properties",
            "env": {
                "T_INVEST_PROD_TOKEN": "your-production-token-here",
                "SPRING_DATASOURCE_PROD_URL": "jdbc:postgresql://45.132.176.136:5432/postgres",
                "SPRING_DATASOURCE_PROD_USERNAME": "postgres",
                "SPRING_DATASOURCE_PROD_PASSWORD": "your-production-password"
            },
            "console": "integratedTerminal"
        }
    ]
}
```

#### **4.2. Альтернативный способ через .vscode/settings.json**

Создайте файл `.vscode/settings.json` в корне проекта:

```json
{
    "java.configuration.runtimes": [
        {
            "name": "JavaSE-17",
            "path": "C:\\Program Files\\Java\\jdk-17"
        }
    ],
    "java.debug.settings.onBuildFailureProceed": true,
    "java.debug.settings.hotCodeReplace": "auto"
}
```

#### **4.3. Запуск через Command Palette**

1. **Откройте Command Palette** (Ctrl+Shift+P)
2. **Выберите команду**: "Java: Run Java"
3. **Выберите main класс**: `InvestmentDataLoaderService`
4. **Добавьте VM аргументы**:
   ```
   -Dspring.profiles.active=test
   ```

#### **4.4. Запуск через Maven в Cursor**

1. **Откройте терминал** (Ctrl+`)
2. **Выполните команду**:
   ```bash
   # Тестовое окружение
   mvn spring-boot:run -Dspring-boot.run.profiles=test
   
   # Продакшн окружение
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

#### **4.5. Настройка переменных окружения в Cursor**

1. **Создайте файл `.env` в корне проекта**:
   ```env
   # Тестовое окружение
   SPRING_PROFILES_ACTIVE=test
   T_INVEST_TEST_TOKEN=your-test-token-here
   SPRING_DATASOURCE_TEST_URL=jdbc:postgresql://localhost:5434/postgres
   SPRING_DATASOURCE_TEST_USERNAME=postgres
   SPRING_DATASOURCE_TEST_PASSWORD=123password123
   ```

2. **Установите расширение "DotENV"** для автоматической загрузки переменных

#### **4.6. Отладка в Cursor**

1. **Установите breakpoints** в коде
2. **Запустите конфигурацию** в режиме отладки (F5)
3. **Используйте панель Debug** для просмотра переменных и стека вызовов

#### **4.7. Полезные расширения для Cursor**

- **Extension Pack for Java** - основной набор для Java разработки
- **Spring Boot Extension Pack** - поддержка Spring Boot
- **DotENV** - поддержка .env файлов
- **Docker** - поддержка Docker контейнеров
- **PostgreSQL** - поддержка PostgreSQL

### **5. Через другие IDE**

#### **IntelliJ IDEA:**
1. Откройте Run/Debug Configurations
2. Добавьте новую конфигурацию "Application"
3. В VM options добавьте: `-Dspring.profiles.active=test`

#### **Eclipse:**
1. Правый клик на проекте → Run As → Run Configurations
2. Создайте новую конфигурацию "Java Application"
3. В Arguments → VM arguments добавьте: `-Dspring.profiles.active=test`

## 🐳 Docker запуск

### **Тестовое окружение:**
```bash
docker-compose up --build
```

### **Продакшн окружение:**
```bash
# Установите переменные окружения
export T_INVEST_PROD_TOKEN=your-production-token
export SPRING_DATASOURCE_PROD_URL=jdbc:postgresql://45.132.176.136:5432/postgres
export SPRING_DATASOURCE_PROD_USERNAME=postgres
export SPRING_DATASOURCE_PROD_PASSWORD=your-password

# Запустите с продакшн профилем
docker run -e SPRING_PROFILES_ACTIVE=prod \
           -e T_INVEST_PROD_TOKEN=$T_INVEST_PROD_TOKEN \
           -e SPRING_DATASOURCE_PROD_URL=$SPRING_DATASOURCE_PROD_URL \
           -e SPRING_DATASOURCE_PROD_USERNAME=$SPRING_DATASOURCE_PROD_USERNAME \
           -e SPRING_DATASOURCE_PROD_PASSWORD=$SPRING_DATASOURCE_PROD_PASSWORD \
           -p 8083:8083 \
           investment-data-loader-service
```

## 🔍 Проверка активного профиля

### **1. Через API:**
```bash
curl http://localhost:8083/actuator/info
```

### **2. Через логи:**
Ищите в логах строку:
```
The following profiles are active: test
```

### **3. Через переменную окружения:**
```bash
echo $SPRING_PROFILES_ACTIVE
```

## ⚠️ Важные моменты

### **Безопасность:**
- ❌ **НЕ** коммитьте файлы `.env.test` и `.env.prod` в Git
- ✅ Используйте `.env.example` файлы как шаблоны
- ✅ Храните секреты в переменных окружения системы

### **База данных:**
- **TEST**: Создается автоматически при первом запуске
- **PROD**: Должна существовать заранее, приложение только проверяет схему

### **Токены API:**
- **TEST**: Используйте тестовый токен Tinkoff
- **PROD**: Используйте продакшн токен Tinkoff

## 🛠️ Отладка

### **Проблемы с подключением к БД:**
1. Проверьте, что PostgreSQL запущен
2. Убедитесь, что порты доступны
3. Проверьте правильность URL и учетных данных

### **Проблемы с API:**
1. Проверьте правильность токена
2. Убедитесь, что токен имеет нужные права
3. Проверьте доступность API Tinkoff

### **Проблемы с профилями:**
1. Проверьте переменную `SPRING_PROFILES_ACTIVE`
2. Убедитесь, что файлы `application-{profile}.properties` существуют
3. Проверьте синтаксис в файлах конфигурации

## 📞 Поддержка

При возникновении проблем:
1. Проверьте логи приложения
2. Убедитесь в правильности переменных окружения
3. Проверьте доступность внешних сервисов (БД, API)
