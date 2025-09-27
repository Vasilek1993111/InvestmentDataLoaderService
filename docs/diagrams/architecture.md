# Архитектура системы

## 🏗️ Высокоуровневая архитектура

```mermaid
flowchart TB
  subgraph Client[Клиенты]
    Web[Веб-интерфейс]
    Mobile[Мобильное приложение]
    API_Client[API клиенты]
    Postman[Postman/Insomnia]
  end

  subgraph Service[Investment Data Loader Service]
    subgraph Controllers[REST Controllers - 12 шт.]
      IC[InstrumentsController<br/>/api/instruments]
      CM[CandlesMinuteController<br/>/api/candles/minute]
      CD[CandlesDailyController<br/>/api/candles/daily]
      CI[CandlesInstrumentController<br/>/api/candles/instrument]
      MSP[MainSessionPricesController<br/>/api/main-session-prices]
      ESP[EveningSessionController<br/>/api/evening-session-prices]
      MSC[MorningSessionController<br/>/api/morning-session-prices]
      LT[LastTradesController<br/>/api/last-trades]
      TC[TradingController<br/>/api/trading]
      CC[CacheController<br/>/api/cache]
      VA[VolumeAggregationController<br/>/api/aggregation]
      SC[SystemController<br/>/api/system]
    end
    
    subgraph Services[Business Services - 9 шт.]
      IS[InstrumentService]
      MS[MinuteCandleService]
      DS[DailyCandleService]
      MDS[MainSessionPriceService]
      ESS[EveningSessionService]
      MSS[MorningSessionService]
      LTS[LastTradesService]
      TS[TradingService]
      CWS[CacheWarmupService]
    end
    
    subgraph Repositories[Data Layer - 13 шт.]
      SR[ShareRepository]
      FR[FutureRepository]
      IR[IndicativeRepository]
      MCR[MinuteCandleRepository]
      DCR[DailyCandleRepository]
      CPR[ClosePriceRepository]
      OPR[OpenPriceRepository]
      LPR[LastPriceRepository]
      SLR[SystemLogRepository]
    end
    
    subgraph External[External Clients]
      GRPC[Tinkoff gRPC Client]
      REST[Tinkoff REST Client]
      TAPI[TinkoffApiClient]
    end
    
    subgraph ThreadPools[Thread Pools]
      MCE[minuteCandleExecutor<br/>10 threads]
      DCE[dailyCandleExecutor<br/>5 threads]
      ADE[apiDataExecutor<br/>20 threads]
      BWE[batchWriteExecutor<br/>5 threads]
    end
    
    Cache[(Spring Cache<br/>Caffeine)]
    Scheduler[Schedulers<br/>7 планировщиков]
  end

  subgraph Database[PostgreSQL Database]
    subgraph Tables[Tables - 10+ шт.]
      S_TBL[(shares)]
      F_TBL[(futures)]
      I_TBL[(indicatives)]
      MC_TBL[(minute_candles)]
      DC_TBL[(daily_candles)]
      CP_TBL[(close_prices)]
      OPE_TBL[(close_prices_evening_session)]
      OP_TBL[(open_prices)]
      LP_TBL[(last_prices)]
      SL_TBL[(system_logs)]
    end
    
    subgraph Views[Materialized Views]
      MV_TODAY[[today_volume_aggregation]]
      MV_DAILY[[daily_volume_aggregation]]
    end
    
    subgraph Functions[Functions & Triggers]
      FUNC[PL/pgSQL Functions]
      TRIG[Database Triggers]
    end
  end

  subgraph External_APIs[Внешние API]
    TINKOFF[Tinkoff Invest API<br/>gRPC + REST]
  end

  %% Client connections
  Web --> IC
  Mobile --> IC
  API_Client --> IC
  Postman --> IC
  Web --> CM
  Mobile --> CM
  API_Client --> CM
  Postman --> CM

  %% Controller to Service
  IC --> IS
  CM --> MS
  CD --> DS
  CI --> MS
  CI --> DS
  MSP --> MDS
  ESP --> ESS
  MSC --> MSS
  LT --> LTS
  TC --> TS
  CC --> CWS
  VA --> MDS

  %% Service to Repository
  IS --> SR
  IS --> FR
  IS --> IR
  MS --> MCR
  DS --> DCR
  MDS --> CPR
  ESS --> OPE_TBL
  MSS --> OP_TBL
  LTS --> LPR

  %% Service to External
  IS --> GRPC
  IS --> REST
  MS --> TAPI
  DS --> TAPI
  LTS --> TAPI
  TS --> GRPC

  %% Repository to Database
  SR --> S_TBL
  FR --> F_TBL
  IR --> I_TBL
  MCR --> MC_TBL
  DCR --> DC_TBL
  CPR --> CP_TBL
  LPR --> LP_TBL
  SLR --> SL_TBL

  %% External to APIs
  GRPC --> TINKOFF
  REST --> TINKOFF
  TAPI --> TINKOFF

  %% Caching
  IS --> Cache
  CWS --> Cache

  %% Thread Pools
  MS --> MCE
  DS --> DCE
  MS --> ADE
  DS --> ADE
  MS --> BWE
  DS --> BWE

  %% Scheduling
  Scheduler --> IS
  Scheduler --> MS
  Scheduler --> DS
  Scheduler --> MDS
```

## 🔧 Детальная архитектура свечей

```mermaid
graph TB
  subgraph "Candles Controllers"
    CM[CandlesMinuteController<br/>/api/candles/minute]
    CD[CandlesDailyController<br/>/api/candles/daily]
    CI[CandlesInstrumentController<br/>/api/candles/instrument]
  end
  
  subgraph "Services Layer"
    MS[MinuteCandleService]
    DS[DailyCandleService]
  end
  
  subgraph "Thread Pools"
    MCE[minuteCandleExecutor<br/>10 threads]
    DCE[dailyCandleExecutor<br/>5 threads]
    ADE[apiDataExecutor<br/>20 threads]
    BWE[batchWriteExecutor<br/>5 threads]
  end
  
  subgraph "External APIs"
    TAPI[TinkoffApiClient<br/>gRPC + REST]
  end
  
  subgraph "Database Tables"
    MC_TBL[(minute_candles<br/>partitioned by day)]
    DC_TBL[(daily_candles<br/>partitioned by month)]
    SL_TBL[(system_logs)]
  end
  
  subgraph "Materialized Views"
    MV_TODAY[[today_volume_aggregation]]
    MV_DAILY[[daily_volume_aggregation]]
  end

  CM --> MS
  CD --> DS
  CI --> MS
  CI --> DS
  
  MS --> MCE
  MS --> ADE
  MS --> BWE
  DS --> DCE
  DS --> ADE
  DS --> BWE
  
  MS --> TAPI
  DS --> TAPI
  
  MS --> MC_TBL
  DS --> DC_TBL
  MS --> SL_TBL
  DS --> SL_TBL
  
  MC_TBL --> MV_TODAY
  DC_TBL --> MV_DAILY
```

## 🔄 Поток данных для свечей

```mermaid
sequenceDiagram
  participant C as Client
  participant CM as CandlesMinuteController
  participant MS as MinuteCandleService
  participant TAPI as TinkoffApiClient
  participant DB as Database
  participant SL as SystemLogs
  participant TP as ThreadPool

  Note over C,TP: Асинхронная загрузка минутных свечей
  
  C->>CM: POST /api/candles/minute
  CM->>SL: STARTED log (taskId)
  CM->>MS: saveMinuteCandlesAsync()
  CM-->>C: 202 Accepted + taskId
  
  Note over MS,DB: Асинхронная обработка
  
  MS->>TP: Submit to minuteCandleExecutor
  TP->>MS: Process async
  MS->>TAPI: fetch candles (paged, throttled)
  TAPI-->>MS: candles batches
  
  loop For each batch
    MS->>TP: Submit to batchWriteExecutor
    TP->>DB: INSERT into minute_candles
    MS->>SL: per-FIGI logs (SUCCESS/NO_DATA/ERROR)
  end
  
  MS->>SL: COMPLETED log
  
  Note over C: Клиент может проверить статус
  
  C->>CM: GET /api/system/logs?taskId=xxx
  CM->>DB: SELECT from system_logs
  DB-->>CM: log entries
  CM-->>C: task status + details
```

## 💾 Архитектура кэширования

```mermaid
graph TB
  subgraph "Cache Implementation"
    CAFFEINE[Caffeine Cache<br/>Local In-Memory]
  end
  
  subgraph "Cache Names"
    SHARES_CACHE[sharesCache<br/>TTL: 1 hour]
    FUTURES_CACHE[futuresCache<br/>TTL: 1 hour]
    INDICATIVES_CACHE[indicativesCache<br/>TTL: 1 hour]
    CLOSE_PRICES_CACHE[closePricesCache<br/>TTL: 15 minutes]
  end
  
  subgraph "Cache Operations"
    GET[GET - Read from cache]
    PUT[PUT - Store in cache]
    EVICT[EVICT - Remove from cache]
    CLEAR[CLEAR - Clear all cache]
  end
  
  subgraph "Cache Management"
    WARMUP[Cache Warmup<br/>POST /api/cache/warmup]
    INFO[Cache Info<br/>GET /api/cache/info]
    STATS[Cache Stats<br/>GET /api/cache/stats]
  end
  
  CAFFEINE --> SHARES_CACHE
  CAFFEINE --> FUTURES_CACHE
  CAFFEINE --> INDICATIVES_CACHE
  CAFFEINE --> CLOSE_PRICES_CACHE
  
  SHARES_CACHE --> GET
  FUTURES_CACHE --> GET
  INDICATIVES_CACHE --> GET
  CLOSE_PRICES_CACHE --> GET
  
  GET --> PUT
  PUT --> EVICT
  EVICT --> CLEAR
  
  WARMUP --> CAFFEINE
  INFO --> CAFFEINE
  STATS --> CAFFEINE
```

## ⚠️ Система обработки ошибок

```mermaid
graph TB
  subgraph "Error Handling Layers"
    Controller[Controller Layer<br/>@Transactional]
    Service[Service Layer<br/>Business Logic]
    Repository[Repository Layer<br/>JPA/Hibernate]
    External[External API Layer<br/>Tinkoff API]
  end
  
  subgraph "Error Types"
    Validation[Validation Errors<br/>400 Bad Request<br/>Invalid parameters]
    NotFound[Not Found Errors<br/>404 Not Found<br/>Resource not found]
    External[External API Errors<br/>503 Service Unavailable<br/>Tinkoff API down]
    Database[Database Errors<br/>500 Internal Server Error<br/>Connection leaks fixed]
    Timeout[Timeout Errors<br/>408 Request Timeout<br/>Async operations]
  end
  
  subgraph "Error Responses"
    JSON[JSON Error Response<br/>Standardized format]
    Logs[System Logs<br/>invest.system_logs]
    Metrics[Error Metrics<br/>Prometheus compatible]
  end
  
  subgraph "Error Recovery"
    Retry[Retry Logic<br/>Exponential backoff]
    Fallback[Fallback Strategy<br/>Cache fallback]
    Circuit[Circuit Breaker<br/>External API protection]
  end
  
  Controller --> Validation
  Service --> NotFound
  Repository --> Database
  External --> External
  Service --> Timeout
  
  Validation --> JSON
  NotFound --> JSON
  Database --> JSON
  External --> JSON
  Timeout --> JSON
  
  Validation --> Logs
  NotFound --> Logs
  Database --> Logs
  External --> Logs
  Timeout --> Logs
  
  Validation --> Metrics
  NotFound --> Metrics
  Database --> Metrics
  External --> Metrics
  Timeout --> Metrics
  
  External --> Retry
  Retry --> Fallback
  Fallback --> Circuit
```

## 📈 Масштабируемость

```mermaid
graph TB
  subgraph "Load Balancing"
    LB[Load Balancer<br/>nginx/HAProxy]
  end
  
  subgraph "Application Instances"
    APP1[App Instance 1<br/>Port 8083 (PROD)]
    APP2[App Instance 2<br/>Port 8087 (TEST)]
    APP3[App Instance N<br/>Port 8088 (DEV)]
  end
  
  subgraph "Database Cluster"
    MASTER[(PostgreSQL Master<br/>Read/Write<br/>Partitioned Tables)]
    REPLICA1[(PostgreSQL Replica 1<br/>Read Only<br/>Analytics)]
    REPLICA2[(PostgreSQL Replica 2<br/>Read Only<br/>Reporting)]
  end
  
  subgraph "Caching Infrastructure"
    CAFFEINE[Caffeine Local Cache<br/>Per Instance]
    CACHE_MANAGER[Cache Manager<br/>Spring Cache]
  end
  
  subgraph "Thread Pools"
    TP1[minuteCandleExecutor<br/>10 threads per instance]
    TP2[dailyCandleExecutor<br/>5 threads per instance]
    TP3[apiDataExecutor<br/>20 threads per instance]
    TP4[batchWriteExecutor<br/>5 threads per instance]
  end
  
  subgraph "External Services"
    TINKOFF[Tinkoff Invest API<br/>Rate Limited]
    MONITORING[Monitoring & Alerting<br/>Prometheus + Grafana]
  end
  
  LB --> APP1
  LB --> APP2
  LB --> APP3
  
  APP1 --> MASTER
  APP1 --> REPLICA1
  APP2 --> MASTER
  APP2 --> REPLICA2
  APP3 --> MASTER
  APP3 --> REPLICA1
  
  APP1 --> CAFFEINE
  APP2 --> CAFFEINE
  APP3 --> CAFFEINE
  
  APP1 --> CACHE_MANAGER
  APP2 --> CACHE_MANAGER
  APP3 --> CACHE_MANAGER
  
  APP1 --> TP1
  APP1 --> TP2
  APP1 --> TP3
  APP1 --> TP4
  
  APP1 --> TINKOFF
  APP2 --> TINKOFF
  APP3 --> TINKOFF
  
  APP1 --> MONITORING
  APP2 --> MONITORING
  APP3 --> MONITORING
```

## 📊 Мониторинг и логирование

```mermaid
graph TB
  subgraph "Application"
    APP[Spring Boot App<br/>Investment Data Loader]
    ACTUATOR[Spring Actuator<br/>Health & Metrics]
  end
  
  subgraph "System Logging"
    SYS_LOGS[System Logs<br/>invest.system_logs]
    TASK_LOGS[Task Logs<br/>per-FIGI tracking]
    ERROR_LOGS[Error Logs<br/>Exception handling]
  end
  
  subgraph "Application Logging"
    APP_LOGS[Application Logs<br/>SLF4J + Logback]
    STRUCTURED[Structured Logging<br/>JSON format]
  end
  
  subgraph "Metrics"
    APP_METRICS[Application Metrics<br/>Custom counters]
    CACHE_METRICS[Cache Metrics<br/>Hit/Miss ratios]
    DB_METRICS[Database Metrics<br/>Connection pool]
    API_METRICS[API Metrics<br/>Response times]
  end
  
  subgraph "Health Checks"
    HEALTH[Health Endpoints<br/>/api/system/health]
    DB_CHECK[Database Health<br/>Connection status]
    API_CHECK[External API Health<br/>Tinkoff API status]
    CACHE_CHECK[Cache Health<br/>Cache status]
  end
  
  subgraph "Monitoring Stack"
    PROMETHEUS[Prometheus Server<br/>Metrics collection]
    GRAFANA[Grafana Dashboard<br/>Visualization]
    ALERTMANAGER[AlertManager<br/>Alerting]
    ALLURE[Allure Reports<br/>Test reporting]
  end
  
  APP --> ACTUATOR
  APP --> APP_LOGS
  APP --> APP_METRICS
  
  ACTUATOR --> HEALTH
  ACTUATOR --> DB_CHECK
  ACTUATOR --> API_CHECK
  ACTUATOR --> CACHE_CHECK
  
  APP_LOGS --> STRUCTURED
  STRUCTURED --> SYS_LOGS
  SYS_LOGS --> TASK_LOGS
  SYS_LOGS --> ERROR_LOGS
  
  APP_METRICS --> CACHE_METRICS
  APP_METRICS --> DB_METRICS
  APP_METRICS --> API_METRICS
  
  HEALTH --> PROMETHEUS
  SYS_LOGS --> PROMETHEUS
  APP_METRICS --> PROMETHEUS
  
  PROMETHEUS --> GRAFANA
  PROMETHEUS --> ALERTMANAGER
  
  APP --> ALLURE
```

## 🐳 Развертывание в Docker

```mermaid
graph TB
  subgraph "Docker Compose"
    APP_CONTAINER[investment-data-loader:latest<br/>Spring Boot App]
    DB_CONTAINER[postgres:15<br/>PostgreSQL Database]
  end
  
  subgraph "Docker Network"
    NETWORK[investment-network<br/>Internal communication]
  end
  
  subgraph "Volumes"
    DB_VOLUME[postgres_data<br/>Persistent storage]
    APP_LOGS[app_logs<br/>Application logs]
  end
  
  subgraph "Environment Profiles"
    PROD_ENV[Production<br/>application-prod.properties<br/>Port 8083]
    TEST_ENV[Test<br/>application-test.properties<br/>Port 8087]
    DOCKER_ENV[Docker<br/>application-docker.properties<br/>Port 8083]
  end
  
  subgraph "External Services"
    TINKOFF_API[Tinkoff Invest API<br/>External dependency]
    MONITORING[Monitoring Stack<br/>Prometheus + Grafana]
  end
  
  APP_CONTAINER --> NETWORK
  DB_CONTAINER --> NETWORK
  
  APP_CONTAINER --> PROD_ENV
  APP_CONTAINER --> TEST_ENV
  APP_CONTAINER --> DOCKER_ENV
  
  DB_CONTAINER --> DB_VOLUME
  APP_CONTAINER --> APP_LOGS
  
  APP_CONTAINER --> DB_CONTAINER
  APP_CONTAINER --> TINKOFF_API
  APP_CONTAINER --> MONITORING
```