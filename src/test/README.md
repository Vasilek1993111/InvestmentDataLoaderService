## Типы тестов

### 1. Unit-тесты (`unit/`)

**Назначение**: Тестирование отдельных компонентов в изоляции

**Покрытие**:
- **InstrumentsControllerTest**: Тестирование REST API endpoints
- **InstrumentServiceTest**: Тестирование бизнес-логики сервиса
- **TInvestServiceInstrumentsTest**: Тестирование делегирующих методов

**Особенности**:
- Используют Mockito для мокирования зависимостей
- Быстрые в выполнении
- Изолированы от внешних систем
- Покрывают все основные сценарии использования

### 2. Интеграционные тесты (`integration/`)

**Назначение**: Тестирование взаимодействия между компонентами

**Покрытие**:
- **InstrumentsIntegrationTest**: Проверка интеграции workflow для инструментов

**Особенности**:
- Тестируют реальное взаимодействие компонентов
- Проверяют консистентность данных
- Валидируют обработку ошибок
- Используют TestRestTemplate для HTTP запросов

### 3. Конфигурация тестов (`config/`)

**Назначение**: Настройка тестового окружения

**Содержит**:
- **TestConfig**: Конфигурация для тестов (часы, ObjectMapper)

### 4. Тестовые данные (`fixtures/`)

**Назначение**: Централизованное управление тестовыми данными

**Содержит**:
- **TestDataFactory**: Фабрика для создания тестовых объектов

## Запуск тестов

### Все тесты

```bash
# Запуск всех тестов
mvn test

# Запуск с подробным выводом
mvn test -X

# Запуск с отчетами
mvn test -Dmaven.test.failure.ignore=true
```

### По типам

```bash
# Только unit-тесты
mvn test -Dtest="*Unit*Test"

# Только интеграционные тесты
mvn test -Dtest="*Integration*Test"

# Только тесты контроллеров
mvn test -Dtest="*Controller*Test"

# Только тесты сервисов
mvn test -Dtest="*Service*Test"
```

### По пакетам

```bash
# Тесты instruments
mvn test -Dtest="*Instruments*Test"

# Тесты unit пакета
mvn test -Dtest="com.example.InvestmentDataLoaderService.unit.*"

# Тесты integration пакета
mvn test -Dtest="com.example.InvestmentDataLoaderService.integration.*"
```

### По классам

```bash
# Конкретный класс
mvn test -Dtest="InstrumentsControllerTest"

# Несколько классов
mvn test -Dtest="InstrumentsControllerTest,InstrumentServiceTest"
```

### С профилями

```bash
# Тесты с профилем dev
mvn test -Pdev

# Тесты с профилем test
mvn test -Ptest
```

## Параметры запуска

### Maven параметры

```bash
# Параллельное выполнение
mvn test -T 4

# Пропуск тестов
mvn test -DskipTests

# Пропуск компиляции тестов
mvn test -Dmaven.test.skip=true

# Запуск только failed тестов
mvn test -Dmaven.test.failure.ignore=true
```

### JUnit параметры

```bash
# Запуск с тегами
mvn test -Dgroups="unit,integration"

# Исключение тегов
mvn test -DexcludedGroups="slow"

# Запуск с параметрами
mvn test -Dtest="*Test" -Dgroups="unit"
```

## Отчеты

### Maven Surefire

```bash
# Генерация отчетов
mvn test -Dmaven.test.failure.ignore=true

# Отчеты в target/surefire-reports/
```

### JaCoCo (покрытие кода)

```bash
# Запуск с анализом покрытия
mvn test jacoco:report

# Отчет в target/site/jacoco/
```

## Конфигурация

### application-test.properties

```properties
# Тестовая конфигурация
spring.profiles.active=test
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

### Maven профили

```xml
<profiles>
    <profile>
        <id>test</id>
        <properties>
            <spring.profiles.active>test</spring.profiles.active>
        </properties>
    </profile>
</profiles>
```

## Отладка

### Запуск в IDE

1. **IntelliJ IDEA**:
   - Правый клик на классе → Run
   - Правый клик на методе → Run
   - Debug режим для пошаговой отладки

2. **Eclipse**:
   - Правый клик на классе → Run As → JUnit Test
   - Debug As → JUnit Test для отладки

### Логирование

```bash
# Включение debug логов
mvn test -Dlogging.level.com.example=DEBUG

# Логи в файл
mvn test -Dlogging.file=target/test.log
```

## Troubleshooting

### Частые проблемы

1. **Тесты не запускаются**:
   ```bash
   # Очистка и пересборка
   mvn clean test
   ```

2. **Ошибки компиляции**:
   ```bash
   # Проверка зависимостей
   mvn dependency:tree
   ```

3. **Проблемы с базой данных**:
   ```bash
   # Проверка конфигурации
   mvn test -Dspring.profiles.active=test
   ```

### Полезные команды

```bash
# Проверка структуры тестов
find src/test -name "*.java" | head -20

# Поиск тестов по имени
find src/test -name "*Test.java" | grep -i instruments

# Проверка покрытия
mvn test jacoco:report
```

## Лучшие практики

### Написание тестов

1. **Именование**: `shouldReturnExpectedResult_whenConditionIsMet`
2. **Структура**: Given-When-Then
3. **Изоляция**: Каждый тест независим
4. **Данные**: Используйте TestDataFactory

### Организация

1. **Пакеты**: Группируйте по функциональности
2. **Классы**: Один тест на класс
3. **Методы**: Один сценарий на метод
4. **Документация**: Комментируйте сложные тесты

### Производительность

1. **Unit-тесты**: Должны быть быстрыми (< 1 сек)
2. **Интеграционные**: Могут быть медленнее
3. **Параллелизация**: Используйте `@TestMethodOrder`
4. **Моки**: Минимизируйте внешние вызовы

## Контакты

- **Команда тестирования**: test-team@example.com
- **Документация**: [Внутренняя вики](http://wiki.example.com/testing)
- **Issues**: [GitHub Issues](https://github.com/example/investment-data-loader/issues)

---

*Последнее обновление: 2024-01-01*
*Версия: 1.0*