# Интеграция Allure с проектом

## Обзор

Allure - это фреймворк для создания красивых и информативных отчетов о тестировании. В проекте настроена интеграция с Allure для генерации детальных отчетов о выполнении тестов.

## Установка Allure

### Windows

#### Способ 1: Через Chocolatey (рекомендуемый)
```powershell
# Установка Chocolatey (если не установлен)
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Установка Allure
choco install allure
```

#### Способ 2: Ручная установка
1. Скачайте Allure с GitHub: https://github.com/allure-framework/allure2/releases
2. Распакуйте архив в папку (например, `C:\allure`)
3. Добавьте `C:\allure\bin` в переменную PATH

#### Способ 3: Через npm (если установлен Node.js)
```bash
npm install -g allure-commandline
```

### Проверка установки
```bash
allure --version
```

## Конфигурация проекта

### Зависимости Maven
В `pom.xml` добавлены следующие зависимости:

```xml
<!-- Allure JUnit 5 Integration -->
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-junit5</artifactId>
    <version>2.24.0</version>
    <scope>test</scope>
</dependency>

<!-- Allure Spring Boot Integration -->
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-spring-boot</artifactId>
    <version>2.24.0</version>
    <scope>test</scope>
</dependency>

<!-- Allure Rest Assured Integration -->
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-rest-assured</artifactId>
    <version>2.24.0</version>
    <scope>test</scope>
</dependency>
```

### Maven Plugin
Добавлен Allure Maven Plugin:

```xml
<plugin>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-maven</artifactId>
    <version>2.12.0</version>
    <configuration>
        <reportVersion>2.24.0</reportVersion>
    </configuration>
</plugin>
```

### Конфигурационные файлы

#### allure.properties
```properties
allure.results.directory=target/allure-results
allure.link.issue.pattern=https://example.com/issues/{}
allure.link.tms.pattern=https://example.com/tms/{}
allure.link.mylink.pattern=https://example.com/mylink/{}
```

#### .mvn/maven.config
```
-Dallure.results.directory=target/allure-results
```

## Использование Allure аннотаций

### Основные аннотации

```java
@Epic("Trading API")                    // Группировка по функциональности
@Feature("Trading Controller")          // Группировка по компонентам
@Story("Account Management")            // Группировка по пользовательским историям
@DisplayName("Получение списка аккаунтов")  // Человекочитаемое название
@Description("Тест проверяет корректность получения списка аккаунтов через API")
@Severity(SeverityLevel.CRITICAL)      // Уровень критичности
@Tag("api")                            // Теги для фильтрации
@Tag("accounts")
@Step("Подготовка тестовых данных")     // Шаги выполнения
```

### Уровни критичности
- `CRITICAL` - Критичные тесты
- `HIGH` - Высокий приоритет
- `NORMAL` - Обычный приоритет
- `MINOR` - Низкий приоритет
- `TRIVIAL` - Тривиальные тесты

## Запуск тестов и генерация отчетов

### 1. Запуск тестов
```bash
# Запуск всех тестов
mvnw.cmd clean test

# Запуск конкретного теста
mvnw.cmd test -Dtest=TradingControllerTest

# Запуск тестов с определенным тегом
mvnw.cmd test -Dgroups="api"
```

### 2. Генерация отчета
```bash
# Генерация отчета
mvnw.cmd allure:report

# Открытие отчета в браузере
mvnw.cmd allure:serve

# Или если Allure установлен глобально
allure serve target/allure-results
```

### 3. Просмотр отчета
После генерации отчета он будет доступен в папке `target/site/allure-maven/index.html`

## Структура отчета

### Основные разделы
1. **Overview** - Общая статистика тестов
2. **Categories** - Группировка по категориям (failed, broken, etc.)
3. **Suites** - Группировка по тестовым наборам
4. **Graphs** - Графики и метрики
5. **Timeline** - Временная шкала выполнения
6. **Behaviors** - Группировка по Epic/Feature/Story
7. **Packages** - Группировка по пакетам

### Фильтрация
- По статусу (passed, failed, broken, skipped)
- По тегам
- По Epic/Feature/Story
- По времени выполнения
- По авторам

## Дополнительные возможности

### Прикрепление файлов
```java
@Attachment(value = "Screenshot", type = "image/png")
public byte[] attachScreenshot(byte[] screenshot) {
    return screenshot;
}
```

### Параметризованные тесты
```java
@ParameterizedTest
@ValueSource(strings = {"param1", "param2"})
@DisplayName("Тест с параметром: {0}")
void testWithParameter(String param) {
    // тест
}
```

### Группировка тестов
```java
@Nested
@DisplayName("Группа тестов для аккаунтов")
class AccountTests {
    // тесты
}
```

## Интеграция с CI/CD

### GitHub Actions
```yaml
- name: Generate Allure Report
  run: |
    mvn allure:report
    allure serve target/allure-results
```

### Jenkins
```groovy
pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                sh 'mvn clean test'
            }
        }
        stage('Report') {
            steps {
                allure([
                    includeProperties: false,
                    jdk: '',
                    properties: [],
                    reportBuildPolicy: 'ALWAYS',
                    results: [[path: 'target/allure-results']]
                ])
            }
        }
    }
}
```

## Troubleshooting

### Проблемы с установкой
1. Убедитесь, что `C:\Windows\System32` в PATH
2. Перезапустите PowerShell после установки
3. Проверьте версию Java (требуется Java 8+)

### Проблемы с генерацией отчета
1. Убедитесь, что тесты выполнились успешно
2. Проверьте наличие папки `target/allure-results`
3. Убедитесь, что Allure установлен корректно

### Проблемы с аннотациями
1. Убедитесь, что импортированы правильные классы Allure
2. Проверьте версию зависимостей в pom.xml
3. Убедитесь, что используется JUnit 5

## Полезные ссылки

- [Официальная документация Allure](https://docs.qameta.io/allure/)
- [Allure JUnit 5 Integration](https://docs.qameta.io/allure/#_junit_5)
- [Allure Maven Plugin](https://docs.qameta.io/allure/#_maven)
- [Примеры аннотаций](https://github.com/allure-framework/allure-java/tree/master/allure-junit5)
