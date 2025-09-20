# 🔧 Устранение проблем с Cursor IDE

## ❌ Ошибка: "The project 'InvestmentDataLoaderService' is not a valid java project"

### 🚀 **Быстрое решение:**

1. **Перезагрузите окно Cursor:**
   - `Ctrl+Shift+P` → "Developer: Reload Window"

2. **Очистите кеш Java:**
   - `Ctrl+Shift+P` → "Java: Clean Workspace"
   - Выберите "Restart and delete"

3. **Пересоберите проект:**
   - `Ctrl+Shift+P` → "Tasks: Run Task"
   - Выберите "Maven: Clean"
   - Затем "Maven: Compile"

### 🔄 **Альтернативные способы запуска:**

#### **1. Через Maven конфигурации:**
- `F5` → выберите "Maven: Spring Boot Run (TEST)" или "Maven: Spring Boot Run (PROD)"

#### **2. Через терминал:**
```bash
# Тестовое окружение
mvn spring-boot:run -Dspring-boot.run.profiles=test

# Продакшн окружение
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

#### **3. Через скрипты:**
```bash
# Windows
scripts\run-test.bat
scripts\run-prod.bat

# Linux/Mac
./scripts/run-test.sh
./scripts/run-prod.sh
```

### 🛠️ **Дополнительные шаги:**

1. **Убедитесь, что установлены расширения:**
   - Extension Pack for Java
   - Spring Boot Extension Pack

2. **Проверьте Java версию:**
   - `Ctrl+Shift+P` → "Java: Configure Java Runtime"
   - Убедитесь, что Java 17 установлена

3. **Проверьте Maven:**
   - `Ctrl+Shift+P` → "Java: Reload Projects"

### 📋 **Проверочный список:**

- [ ] Cursor перезагружен
- [ ] Java кеш очищен
- [ ] Проект пересобран
- [ ] Расширения установлены
- [ ] Java 17 настроена
- [ ] Maven работает

### 🆘 **Если ничего не помогает:**

1. **Удалите папку `.vscode`** и пересоздайте конфигурации
2. **Используйте терминал** для запуска через Maven
3. **Проверьте логи** в Output → Java

## ✅ **Успешный запуск:**

После исправления вы должны увидеть:
```
Started InvestmentDataLoaderService in X.XXX seconds
```
