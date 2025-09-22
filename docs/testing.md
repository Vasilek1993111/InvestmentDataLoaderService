# Тестирование

## Обзор

Investment Data Loader Service использует многоуровневую стратегию тестирования для обеспечения качества и надежности кода.

## 🧪 Типы тестов

### 1. Unit Tests (Модульные тесты)
Тестирование отдельных компонентов в изоляции.

**Расположение**: `src/test/java/com/example/InvestmentDataLoaderService/unit/`

**Примеры**:
- `InstrumentServiceTest` - тестирование бизнес-логики инструментов
- `MarketDataServiceTest` - тестирование рыночных данных
- `TradingServiceTest` - тестирование торговых данных

**Особенности**:
- Используют моки для внешних зависимостей
- Быстрые и изолированные
- Покрывают основную бизнес-логику

### 2. Integration Tests (Интеграционные тесты)
Тестирование взаимодействия между компонентами.

**Расположение**: `src/test/java/com/example/InvestmentDataLoaderService/integration/`

**Примеры**:
- `InstrumentsControllerIntegrationTest` - тестирование REST API
- `DatabaseIntegrationTest` - тестирование работы с БД
- `TinkoffApiIntegrationTest` - тестирование внешних API

**Особенности**:
- Используют реальные компоненты Spring
- Тестируют полный поток данных
- Используют TestContainers для БД

### 3. Component Tests (Компонентные тесты)
Тестирование API эндпоинтов с реальной БД.

**Расположение**: `src/test/java/com/example/InvestmentDataLoaderService/component/`

**Примеры**:
- `InstrumentsComponentTest` - тестирование API инструментов
- `MarketDataComponentTest` - тестирование API рыночных данных

**Особенности**:
- Используют TestContainers
- Тестируют полный стек приложения
- Проверяют реальное поведение API

## 🚀 Запуск тестов

### Все тесты
```bash
mvn test
```

### Только unit тесты
```bash
mvn test -Dtest="**/unit/**"
```

### Только integration тесты
```bash
mvn test -Dtest="**/integration/**"
```

### Только component тесты
```bash
mvn test -Dtest="**/component/**"
```

### Конкретный тест
```bash
mvn test -Dtest="InstrumentServiceTest"
```

## 📊 Покрытие кода

### Генерация отчета
```bash
mvn jacoco:report
```

### Просмотр отчета
Откройте `target/site/jacoco/index.html` в браузере.

### Цели покрытия
- **Unit тесты**: 80%+
- **Integration тесты**: 70%+
- **Общее покрытие**: 75%+

## 🐳 TestContainers

### Настройка
```java
@Testcontainers
@SpringBootTest
public class DatabaseIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

### Преимущества
- Изолированная среда для каждого теста
- Реальная БД PostgreSQL
- Автоматическая очистка после тестов
- Параллельное выполнение тестов

## 🎯 Примеры тестов

### Unit Test
```java
@ExtendWith(MockitoExtension.class)
class InstrumentServiceTest {
    
    @Mock
    private ShareRepository shareRepository;
    
    @Mock
    private InstrumentsServiceBlockingStub instrumentsService;
    
    @InjectMocks
    private InstrumentService instrumentService;
    
    @Test
    void shouldReturnSharesWhenApiCallSucceeds() {
        // Given
        when(instrumentsService.shares(any())).thenReturn(mockResponse());
        
        // When
        List<ShareDto> result = instrumentService.getShares("ACTIVE", "MOEX", "RUB", null, null);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).ticker()).isEqualTo("SBER");
    }
}
```

### Integration Test
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class InstrumentsControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldReturnSharesWhenRequested() {
        // When
        ResponseEntity<List<ShareDto>> response = restTemplate.exchange(
            "/api/instruments/shares?exchange=MOEX",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ShareDto>>() {}
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }
}
```

### Component Test
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class InstrumentsComponentTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldSaveSharesToDatabase() {
        // Given
        ShareFilterDto filter = new ShareFilterDto();
        filter.setExchange("MOEX");
        filter.setCurrency("RUB");
        
        // When
        ResponseEntity<SaveResponseDto> response = restTemplate.postForEntity(
            "/api/instruments/shares",
            filter,
            SaveResponseDto.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getNewItemsSaved()).isGreaterThan(0);
    }
}
```

## 🔧 Настройка тестов

### application-test.properties
```properties
# Test database configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/testdb
spring.datasource.username=test
spring.datasource.password=test

# Disable caching in tests
spring.cache.type=none

# Logging for tests
logging.level.com.example.InvestmentDataLoaderService=DEBUG
logging.level.org.springframework.web=DEBUG

# Test profiles
spring.profiles.active=test
```

### Test Configuration
```java
@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public TinkoffRestClient mockTinkoffRestClient() {
        return Mockito.mock(TinkoffRestClient.class);
    }
    
    @Bean
    @Primary
    public InstrumentsServiceBlockingStub mockInstrumentsService() {
        return Mockito.mock(InstrumentsServiceBlockingStub.class);
    }
}
```

## 📈 Метрики тестирования

### Ключевые показатели
- **Покрытие кода**: 75%+
- **Время выполнения тестов**: < 5 минут
- **Успешность тестов**: 100%
- **Количество тестов**: 50+

### Отчеты
- **Jacoco** - покрытие кода
- **Surefire** - результаты unit тестов
- **Failsafe** - результаты integration тестов
- **Allure** - детальные отчеты

## 🚨 Troubleshooting

### Частые проблемы

#### 1. Тесты падают из-за БД
```bash
# Проверьте, что PostgreSQL запущен
docker ps | grep postgres

# Очистите тестовую БД
mvn clean test
```

#### 2. Медленные тесты
```bash
# Запустите только unit тесты
mvn test -Dtest="**/unit/**"

# Используйте параллельное выполнение
mvn test -T 4
```

#### 3. Проблемы с TestContainers
```bash
# Очистите Docker
docker system prune -f

# Перезапустите тесты
mvn clean test
```

## 🔄 CI/CD Integration

### GitHub Actions
```yaml
name: Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run tests
        run: mvn test
        env:
          T_INVEST_TOKEN: ${{ secrets.T_INVEST_TOKEN }}
```

### Jenkins Pipeline
```groovy
pipeline {
    agent any
    
    stages {
        stage('Test') {
            steps {
                sh 'mvn clean test'
            }
        }
        
        stage('Coverage') {
            steps {
                sh 'mvn jacoco:report'
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/site/jacoco',
                    reportFiles: 'index.html',
                    reportName: 'Coverage Report'
                ])
            }
        }
    }
}
```

## 📚 Дополнительные ресурсы

- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [TestContainers](https://www.testcontainers.org/)
- [Mockito](https://site.mockito.org/)
- [JUnit 5](https://junit.org/junit5/)
- [Jacoco](https://www.jacoco.org/jacoco/)
