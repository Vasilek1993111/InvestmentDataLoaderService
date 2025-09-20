# Allure - Быстрый старт

## Установка Allure

### Windows (рекомендуемый способ)
```powershell
# Установка через Chocolatey
choco install allure

# Проверка установки
allure --version
```

## Запуск тестов и генерация отчетов

### 1. Запуск тестов
```bash
# Запуск всех тестов
mvnw.cmd clean test

# Запуск конкретного теста
mvnw.cmd test -Dtest=TradingControllerTest
```

### 2. Генерация и просмотр отчета
```bash
# Генерация отчета
mvnw.cmd allure:report

# Открытие отчета в браузере
mvnw.cmd allure:serve

# Или если Allure установлен глобально
allure serve target/allure-results
```

# Принудительная очистка в Maven
mvn clean test allure:report allure:serve
```

## Структура отчета

- **Overview** - Общая статистика
- **Categories** - Группировка по статусам
- **Suites** - Группировка по тестовым наборам
- **Behaviors** - Группировка по Epic/Feature/Story
- **Timeline** - Временная шкала выполнения

## Основные аннотации

```java
@Epic("Trading API")                    // Группировка по функциональности
@Feature("Trading Controller")          // Группировка по компонентам
@Story("Account Management")            // Группировка по пользовательским историям
@DisplayName("Получение списка аккаунтов")  // Человекочитаемое название
@Description("Описание теста")
@Severity(SeverityLevel.CRITICAL)      // Уровень критичности
@Tag("api")                            // Теги для фильтрации
@Step("Подготовка данных")             // Шаги выполнения
```

## Уровни критичности
- `CRITICAL` - Критичные тесты
- `NORMAL` - Обычный приоритет
- `MINOR` - Низкий приоритет
- `TRIVIAL` - Тривиальные тесты

## Полезные команды

```bash
# Очистка и пересборка
mvnw.cmd clean compile test

# Запуск только unit тестов
mvnw.cmd test -Dtest="*Unit*"

# Запуск тестов с определенным тегом
mvnw.cmd test -Dgroups="api"

# Генерация отчета без запуска тестов
mvnw.cmd allure:report -Dallure.results.directory=target/allure-results
```

## Troubleshooting

1. **Allure не найден**: Убедитесь, что Allure установлен и добавлен в PATH
2. **Ошибки компиляции**: Проверьте версию Java (требуется Java 8+)
3. **Пустой отчет**: Убедитесь, что тесты выполнились и создалась папка `target/allure-results`

Подробная документация: [docs/allure-integration.md](docs/allure-integration.md)
