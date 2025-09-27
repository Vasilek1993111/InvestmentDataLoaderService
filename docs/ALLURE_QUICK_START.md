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
mvn clean test

# Запуск конкретного теста
mvn test -Dtest=TradingControllerTest
```

### 2. Генерация и просмотр отчета
```bash
# Генерация отчета
mvn allure:report

# Открытие отчета в браузере
mvn allure:serve

# Или если Allure установлен глобально
allure serve target/allure-results

# Принудительная очистка в Maven
mvn clean test allure:report allure:serve
```


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
mvn clean compile test

# Запуск только unit тестов
mvn test -Dtest="*Unit*"

# Генерация отчета
mvn allure:report

# Запуск тестов с определенным тегом
mvn test -Dgroups="api"

# Открытие отчета в браузере
allure serve target/allure-results

```

## Troubleshooting

1. **Allure не найден**: Убедитесь, что Allure установлен и добавлен в PATH
2. **Ошибки компиляции**: Проверьте версию Java (требуется Java 8+)
3. **Пустой отчет**: Убедитесь, что тесты выполнились и создалась папка `target/allure-results`

Подробная документация: [docs/allure-integration.md](docs/allure-integration.md)
