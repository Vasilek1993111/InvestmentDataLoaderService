# Архитектура системы

## Высокоуровневая архитектура

```mermaid
flowchart TB
  subgraph Client[Клиенты]
    Web[Веб-интерфейс]
    Mobile[Мобильное приложение]
    API_Client[API клиенты]
  end

  subgraph Service[Investment Data Loader Service]
    subgraph Controllers[REST Controllers]
      IC[InstrumentsController<br/>/api/instruments]
      CC[CandlesInstrumentController<br/>/api/candles]
      SC[SystemController<br/>/api/system]
      TC[TradingController<br/>/api/trading]
    end
    
    subgraph Services[Business Services]
      IS[InstrumentService]
      MDS[MarketDataService]
      TS[TradingService]
      TIS[TInvestService]
    end
    
    subgraph Repositories[Data Layer]
      SR[ShareRepository]
      FR[FutureRepository]
      IR[IndicativeRepository]
      CPR[ClosePriceRepository]
    end
    
    subgraph External[External Clients]
      GRPC[Tinkoff gRPC Client]
      REST[Tinkoff REST Client]
    end
    
    Cache[(Spring Cache)]
    Scheduler[Schedulers]
  end

  subgraph Database[PostgreSQL Database]
    subgraph Tables[Tables]
      S_TBL[(shares)]
      F_TBL[(futures)]
      I_TBL[(indicatives)]
      CP_TBL[(close_prices)]
      C_TBL[(candles)]
    end
    
    subgraph Views[Materialized Views]
      MV_TODAY[[today_aggregation]]
      MV_DAILY[[daily_aggregation]]
    end
  end

  subgraph External_APIs[Внешние API]
    TINKOFF[Tinkoff Invest API]
  end

  %% Client connections
  Web --> IC
  Mobile --> IC
  API_Client --> IC
  Web --> CC
  Mobile --> CC
  API_Client --> CC

  %% Controller to Service
  IC --> IS
  CC --> MDS
  SC --> TIS
  TC --> TS

  %% Service to Repository
  IS --> SR
  IS --> FR
  IS --> IR
  MDS --> CPR

  %% Service to External
  IS --> GRPC
  IS --> REST
  MDS --> GRPC
  TS --> GRPC

  %% Repository to Database
  SR --> S_TBL
  FR --> F_TBL
  IR --> I_TBL
  CPR --> CP_TBL

  %% External to APIs
  GRPC --> TINKOFF
  REST --> TINKOFF

  %% Caching
  IS --> Cache
  MDS --> Cache

  %% Scheduling
  Scheduler --> IS
  Scheduler --> MDS
```

## Детальная архитектура инструментов

```mermaid
graph TB
  subgraph "Instruments Layer"
    IC[InstrumentsController]
  end
  
  subgraph "Service Layer"
    IS[InstrumentService]
  end
  
  subgraph "Repository Layer"
    SR[ShareRepository]
    FR[FutureRepository]
    IR[IndicativeRepository]
  end
  
  subgraph "External APIs"
    TINKOFF_GRPC[Tinkoff gRPC API]
    TINKOFF_REST[Tinkoff REST API]
  end
  
  subgraph "Database"
    S_TBL[(shares)]
    F_TBL[(futures)]
    I_TBL[(indicatives)]
  end
  
  subgraph "Caching"
    CACHE[(Spring Cache)]
  end

  IC --> IS
  IS --> SR
  IS --> FR
  IS --> IR
  IS --> TINKOFF_GRPC
  IS --> TINKOFF_REST
  IS --> CACHE
  
  SR --> S_TBL
  FR --> F_TBL
  IR --> I_TBL
```

## Поток данных для инструментов

```mermaid
sequenceDiagram
  participant C as Client
  participant IC as InstrumentsController
  participant IS as InstrumentService
  participant API as Tinkoff API
  participant DB as Database
  participant Cache as Cache

  Note over C,Cache: Получение инструментов из API
  
  C->>IC: GET /api/instruments/shares?exchange=MOEX
  IC->>IS: getShares(exchange, currency, ...)
  
  alt Cache Hit
    IS->>Cache: Check cache
    Cache-->>IS: Return cached data
  else Cache Miss
    IS->>API: gRPC call to Tinkoff
    API-->>IS: Return instruments
    IS->>Cache: Store in cache
  end
  
  IS-->>IC: Return instruments
  IC-->>C: JSON response

  Note over C,Cache: Сохранение инструментов в БД
  
  C->>IC: POST /api/instruments/shares
  IC->>IS: saveShares(filter)
  IS->>API: getShares(filter)
  API-->>IS: Return instruments
  
  loop For each instrument
    IS->>DB: Check if exists
    alt Not exists
      IS->>DB: Save new instrument
    else Exists
      Note over IS: Skip duplicate
    end
  end
  
  IS-->>IC: Return save result
  IC-->>C: JSON response with statistics
```

## Архитектура кэширования

```mermaid
graph TB
  subgraph "Cache Layers"
    L1[L1: Caffeine Local Cache]
    L2[L2: Redis Distributed Cache]
  end
  
  subgraph "Cache Keys"
    SHARES[shares:status|exchange|currency|ticker|figi]
    FUTURES[futures:status|exchange|currency|ticker|assetType]
    INDICATIVES[indicatives:exchange|currency|ticker|figi]
  end
  
  subgraph "Cache TTL"
    TTL_SHORT[Short TTL: 5 minutes]
    TTL_MEDIUM[Medium TTL: 1 hour]
    TTL_LONG[Long TTL: 24 hours]
  end
  
  L1 --> SHARES
  L1 --> FUTURES
  L1 --> INDICATIVES
  
  L2 --> SHARES
  L2 --> FUTURES
  L2 --> INDICATIVES
  
  SHARES --> TTL_MEDIUM
  FUTURES --> TTL_MEDIUM
  INDICATIVES --> TTL_LONG
```

## Система обработки ошибок

```mermaid
graph TB
  subgraph "Error Handling Layers"
    Controller[Controller Layer]
    Service[Service Layer]
    Repository[Repository Layer]
    External[External API Layer]
  end
  
  subgraph "Error Types"
    Validation[Validation Errors<br/>400 Bad Request]
    NotFound[Not Found Errors<br/>404 Not Found]
    External[External API Errors<br/>503 Service Unavailable]
    Database[Database Errors<br/>500 Internal Server Error]
  end
  
  subgraph "Error Responses"
    JSON[JSON Error Response]
    Logs[Error Logging]
    Metrics[Error Metrics]
  end
  
  Controller --> Validation
  Service --> NotFound
  Repository --> Database
  External --> External
  
  Validation --> JSON
  NotFound --> JSON
  Database --> JSON
  External --> JSON
  
  Validation --> Logs
  NotFound --> Logs
  Database --> Logs
  External --> Logs
  
  Validation --> Metrics
  NotFound --> Metrics
  Database --> Metrics
  External --> Metrics
```

## Масштабируемость

```mermaid
graph TB
  subgraph "Load Balancing"
    LB[Load Balancer<br/>nginx/HAProxy]
  end
  
  subgraph "Application Instances"
    APP1[App Instance 1<br/>Port 8083]
    APP2[App Instance 2<br/>Port 8084]
    APP3[App Instance N<br/>Port 8085]
  end
  
  subgraph "Database Cluster"
    MASTER[(PostgreSQL Master<br/>Read/Write)]
    REPLICA1[(PostgreSQL Replica 1<br/>Read Only)]
    REPLICA2[(PostgreSQL Replica 2<br/>Read Only)]
  end
  
  subgraph "Caching Infrastructure"
    REDIS[(Redis Cluster)]
    CAFFEINE[Caffeine Local Cache]
  end
  
  subgraph "External Services"
    TINKOFF[Tinkoff Invest API]
    MONITORING[Monitoring & Alerting]
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
  
  APP1 --> REDIS
  APP2 --> REDIS
  APP3 --> REDIS
  
  APP1 --> CAFFEINE
  APP2 --> CAFFEINE
  APP3 --> CAFFEINE
  
  APP1 --> TINKOFF
  APP2 --> TINKOFF
  APP3 --> TINKOFF
  
  APP1 --> MONITORING
  APP2 --> MONITORING
  APP3 --> MONITORING
```

## Мониторинг и логирование

```mermaid
graph TB
  subgraph "Application"
    APP[Spring Boot App]
    ACTUATOR[Spring Actuator]
  end
  
  subgraph "Logging"
    LOGS[Application Logs]
    SLF4J[SLF4J + Logback]
  end
  
  subgraph "Metrics"
    METRICS[Application Metrics]
    PROMETHEUS[Prometheus Metrics]
  end
  
  subgraph "Health Checks"
    HEALTH[Health Endpoints]
    DB_CHECK[Database Health]
    API_CHECK[External API Health]
  end
  
  subgraph "Monitoring Stack"
    PROMETHEUS_SERVER[Prometheus Server]
    GRAFANA[Grafana Dashboard]
    ALERTMANAGER[AlertManager]
  end
  
  APP --> ACTUATOR
  APP --> SLF4J
  APP --> METRICS
  
  ACTUATOR --> HEALTH
  ACTUATOR --> DB_CHECK
  ACTUATOR --> API_CHECK
  
  SLF4J --> LOGS
  METRICS --> PROMETHEUS
  
  HEALTH --> PROMETHEUS_SERVER
  LOGS --> PROMETHEUS_SERVER
  PROMETHEUS --> PROMETHEUS_SERVER
  
  PROMETHEUS_SERVER --> GRAFANA
  PROMETHEUS_SERVER --> ALERTMANAGER
```

## Развертывание в Docker

```mermaid
graph TB
  subgraph "Docker Compose"
    APP_CONTAINER[investment-data-loader:latest]
    DB_CONTAINER[postgres:15]
    REDIS_CONTAINER[redis:7]
  end
  
  subgraph "Docker Network"
    NETWORK[investment-network]
  end
  
  subgraph "Volumes"
    DB_VOLUME[postgres_data]
    REDIS_VOLUME[redis_data]
  end
  
  subgraph "Environment"
    ENV_VARS[Environment Variables<br/>T_INVEST_TOKEN<br/>DB_HOST<br/>DB_PASSWORD]
  end
  
  APP_CONTAINER --> NETWORK
  DB_CONTAINER --> NETWORK
  REDIS_CONTAINER --> NETWORK
  
  APP_CONTAINER --> ENV_VARS
  DB_CONTAINER --> DB_VOLUME
  REDIS_CONTAINER --> REDIS_VOLUME
  
  APP_CONTAINER --> DB_CONTAINER
  APP_CONTAINER --> REDIS_CONTAINER
```