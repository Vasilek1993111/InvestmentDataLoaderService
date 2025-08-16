# Руководство по участию в разработке

Спасибо за интерес к проекту Ingestion Service! Мы приветствуем вклад от сообщества.

## Как внести свой вклад

### Сообщение об ошибках

Если вы нашли ошибку, пожалуйста:

1. **Проверьте существующие Issues** - возможно, проблема уже известна
2. **Создайте новый Issue** с подробным описанием:
   - Краткое описание проблемы
   - Шаги для воспроизведения
   - Ожидаемое и фактическое поведение
   - Версия приложения и окружение
   - Логи ошибок (если есть)

### Предложение новых функций

Для предложения новых функций:

1. **Создайте Issue** с описанием функции
2. **Обсудите** с командой разработки
3. **Создайте Pull Request** с реализацией

### Внесение изменений в код

#### Подготовка окружения

1. **Форкните репозиторий**
2. **Клонируйте** ваш форк:
   ```bash
   git clone https://github.com/your-username/ingestion-service.git
   cd ingestion-service
   ```
3. **Создайте ветку** для ваших изменений:
   ```bash
   git checkout -b feature/your-feature-name
   ```

#### Разработка

1. **Убедитесь, что код компилируется**:
   ```bash
   mvn clean compile
   ```
2. **Запустите тесты**:
   ```bash
   mvn test
   ```
3. **Следуйте стилю кода** (см. раздел "Стиль кода")
4. **Добавьте тесты** для новой функциональности
5. **Обновите документацию** при необходимости

#### Создание Pull Request

1. **Закоммитьте изменения**:
   ```bash
   git add .
   git commit -m "feat: add new feature description"
   ```
2. **Отправьте изменения**:
   ```bash
   git push origin feature/your-feature-name
   ```
3. **Создайте Pull Request** на GitHub
4. **Опишите изменения** в PR:
   - Что изменилось
   - Почему это нужно
   - Как тестировать

## Стиль кода

### Java

- Используйте **Java 21** синтаксис
- Следуйте **Google Java Style Guide**
- Используйте **Lombok** для уменьшения boilerplate кода
- Добавляйте **JavaDoc** для публичных методов

### Spring Boot

- Используйте **Spring Boot best practices**
- Следуйте **dependency injection** принципам
- Используйте **Spring Data JPA** для работы с БД
- Применяйте **Spring Security** для аутентификации

### Архитектура

- Следуйте **многослойной архитектуре**:
  - Controllers → Services → Repositories → Entities
- Используйте **DTO** для передачи данных
- Разделяйте **бизнес-логику** и **представление**

### Тестирование

- Пишите **unit тесты** для сервисов
- Используйте **integration тесты** для API
- Достигайте **покрытия кода** не менее 80%
- Используйте **Mockito** для мокирования

## Структура проекта

```
src/main/java/com/example/ingestionservice/
├── config/          # Конфигурация
├── controller/      # REST контроллеры
├── dto/            # Объекты передачи данных
├── entity/         # Модели данных
├── repository/     # Репозитории для доступа к БД
├── service/        # Бизнес-логика
└── IngestionServiceApplication.java

src/test/java/
├── controller/     # Тесты контроллеров
├── service/        # Тесты сервисов
└── integration/    # Интеграционные тесты
```

## Коммиты

Используйте **Conventional Commits**:

- `feat:` - новые функции
- `fix:` - исправления ошибок
- `docs:` - изменения в документации
- `style:` - форматирование кода
- `refactor:` - рефакторинг
- `test:` - добавление тестов
- `chore:` - обновление зависимостей, конфигурации

Примеры:
```bash
git commit -m "feat: add support for bonds"
git commit -m "fix: resolve memory leak in price scheduler"
git commit -m "docs: update API documentation"
```

## Pull Request процесс

### Требования к PR

1. **Описание изменений** - что и зачем изменилось
2. **Тесты** - новые тесты или обновление существующих
3. **Документация** - обновление README, API docs
4. **Совместимость** - не ломает существующую функциональность

### Review процесс

1. **Автоматические проверки** должны пройти
2. **Code review** от команды разработки
3. **Тесты** должны проходить
4. **Документация** обновлена

### После одобрения

1. **Squash commits** при необходимости
2. **Merge** в основную ветку
3. **Удаление** feature ветки

## Документация

### Обновление документации

При внесении изменений обновите:

- **README.md** - если изменилась основная функциональность
- **docs/API.md** - при изменении API
- **docs/DEVELOPMENT.md** - при изменении архитектуры
- **docs/EXAMPLES.md** - при добавлении новых примеров
- **CHANGELOG.md** - опишите изменения

### Стиль документации

- Используйте **Markdown**
- Добавляйте **примеры кода**
- Включайте **диаграммы** при необходимости
- Обновляйте **версии** в примерах

## Тестирование

### Unit тесты

```java
@ExtendWith(MockitoExtension.class)
class TInvestServiceTest {
    
    @Mock
    private ShareRepository shareRepository;
    
    @InjectMocks
    private TInvestService service;
    
    @Test
    void shouldGetShares() {
        // given
        when(shareRepository.findAll()).thenReturn(Arrays.asList(share1, share2));
        
        // when
        List<ShareDto> result = service.getShares(null, null, null);
        
        // then
        assertThat(result).hasSize(2);
    }
}
```

### Integration тесты

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class TInvestControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldReturnShares() {
        ResponseEntity<List<ShareDto>> response = restTemplate.exchange(
            "/shares",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ShareDto>>() {}
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }
}
```

## Отладка

### Логирование

```java
@Slf4j
@Service
public class TInvestService {
    
    public List<ShareDto> getShares(String status, String exchange, String currency) {
        log.debug("Getting shares with status={}, exchange={}, currency={}", 
                 status, exchange, currency);
        
        // implementation
        
        log.info("Retrieved {} shares", shares.size());
        return shares;
    }
}
```

### Профилирование

```bash
# Запуск с профилированием
java -agentlib:hprof=cpu=samples,interval=20,depth=3 -jar app.jar

# Анализ heap dump
jmap -dump:format=b,file=heap.hprof <pid>
```

## Безопасность

### Проверка безопасности

1. **Сканирование зависимостей**:
   ```bash
   mvn dependency:check
   ```
2. **Проверка уязвимостей**:
   ```bash
   mvn org.owasp:dependency-check-maven:check
   ```
3. **Аудит кода**:
   ```bash
   mvn spotbugs:check
   ```

### Рекомендации

- Не коммитьте **секреты** в код
- Используйте **HTTPS** в продакшн
- Валидируйте **входные данные**
- Логируйте **безопасностные события**

## Производительность

### Бенчмарки

```java
@Benchmark
public void testGetShares() {
    service.getShares(null, null, null);
}
```

### Мониторинг

- Используйте **Micrometer** для метрик
- Интегрируйте с **Prometheus**
- Настройте **Grafana** дашборды

## Контакты

### Команда разработки

- **Lead Developer**: [@username](https://github.com/username)
- **Architect**: [@username](https://github.com/username)
- **DevOps**: [@username](https://github.com/username)

### Каналы связи

- **Issues**: [GitHub Issues](https://github.com/your-username/ingestion-service/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-username/ingestion-service/discussions)
- **Email**: team@ingestion-service.com

## Благодарности

Спасибо всем участникам проекта за их вклад!

### Contributors

- [@username](https://github.com/username) - основная разработка
- [@username](https://github.com/username) - документация
- [@username](https://github.com/username) - тестирование

---

Следуя этим руководствам, вы поможете сделать проект лучше для всех!