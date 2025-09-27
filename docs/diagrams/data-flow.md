# Поток данных (ingestion & aggregation)

## 🔄 Основной поток данных

```mermaid
sequenceDiagram
  autonumber
  participant Client as Client
  participant API as REST API
  participant Controllers as Controllers
  participant Services as Services
  participant T as Tinkoff API
  participant DB as PostgreSQL
  participant Cache as Cache
  participant Logs as System Logs
  participant MV as Mat. Views

  Note over Client,MV: Инструменты
  
  Client->>Controllers: GET /api/instruments/shares
  Controllers->>Cache: Check cache
  alt Cache Miss
    Controllers->>Services: getShares()
    Services->>T: fetch shares (gRPC)
    T-->>Services: shares data
    Services->>Cache: Store in cache
    Services->>DB: INSERT into invest.shares
  else Cache Hit
    Cache-->>Controllers: cached data
  end
  Controllers-->>Client: shares list

  Note over Client,MV: Минутные свечи
  
  Client->>Controllers: POST /api/candles/minute
  Controllers->>Logs: STARTED log (taskId)
  Controllers->>Services: saveMinuteCandlesAsync()
  Services->>T: fetch candles (paged, throttled)
  T-->>Services: candles batches
  Services->>DB: INSERT into invest.minute_candles
  Services->>Logs: per-FIGI logs (SUCCESS/NO_DATA/ERROR)
  Services-->>Controllers: taskId + results
  Controllers->>Logs: COMPLETED log
  Controllers-->>Client: success + statistics

  Note over Client,MV: Дневные свечи
  
  Client->>Controllers: POST /api/candles/daily
  Controllers->>Logs: STARTED log (taskId)
  Controllers->>Services: saveDailyCandlesAsync()
  Services->>T: fetch daily candles
  T-->>Services: daily candles
  Services->>DB: INSERT into invest.daily_candles
  Services->>Logs: per-FIGI logs (SUCCESS/NO_DATA/ERROR)
  Services-->>Controllers: taskId + results
  Controllers->>Logs: COMPLETED log
  Controllers-->>Client: success + statistics

  Note over Client,MV: Цены сессий
  
  Client->>Controllers: POST /api/main-session-prices/
  Controllers->>Services: loadClosePrices()
  Services->>DB: SELECT from minute_candles
  Services->>DB: INSERT into invest.close_prices
  Services-->>Controllers: results
  Controllers-->>Client: success + statistics

  Client->>Controllers: POST /api/evening-session-prices
  Controllers->>Services: loadEveningSessionPrices()
  Services->>DB: SELECT from minute_candles
  Services->>DB: INSERT into invest.close_prices_evening_session
  Services-->>Controllers: results
  Controllers-->>Client: success + statistics

  Note over Client,MV: Последние сделки
  
  Client->>Controllers: POST /api/last-trades
  Controllers->>Services: loadLastTrades()
  Services->>T: fetch last trades
  T-->>Services: trades data
  Services->>DB: INSERT into invest.last_prices
  Services-->>Controllers: results
  Controllers-->>Client: success + statistics

  Note over Services,DB: Агрегация данных

  Controllers->>Services: POST /api/aggregation/refresh
  Services->>DB: REFRESH MATERIALIZED VIEW invest.today_volume_aggregation
  Services->>DB: REFRESH MATERIALIZED VIEW invest.daily_volume_aggregation
  DB-->>Services: ok
  Services-->>Controllers: success
  Controllers-->>Client: aggregation completed

  Note over Client,MV: Кэширование
  
  Client->>Controllers: POST /api/cache/warmup
  Controllers->>Services: warmupCache()
  Services->>T: fetch all instruments
  T-->>Services: instruments data
  Services->>Cache: Store all in cache
  Services-->>Controllers: cache warmed up
  Controllers-->>Client: cache status

  Note over Client,MV: Системные логи
  
  Client->>Controllers: Any GET/POST request
  Controllers->>Logs: STARTED log (taskId, endpoint, method)
  Controllers->>Services: process request
  Services-->>Controllers: results
  Controllers->>Logs: COMPLETED/FAILED log (duration, message)
  Controllers-->>Client: response
```

## 🏗️ Архитектура контроллеров

```mermaid
graph TB
    subgraph "API Controllers"
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
    
    subgraph "Services"
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
    
    subgraph "External APIs"
        TAPI[Tinkoff API<br/>gRPC + REST]
    end
    
    subgraph "Database"
        SH[invest.shares]
        FU[invest.futures]
        IND[invest.indicatives]
        MC[invest.minute_candles]
        DC[invest.daily_candles]
        CP[invest.close_prices]
        OPE[invest.close_prices_evening_session]
        OP[invest.open_prices]
        LP[invest.last_prices]
        SL[invest.system_logs]
        MV1[invest.today_volume_aggregation]
        MV2[invest.daily_volume_aggregation]
    end
    
    subgraph "Cache"
        CACHE[Spring Cache<br/>Caffeine]
    end
    
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
    
    IS --> TAPI
    IS --> CACHE
    IS --> SH
    IS --> FU
    IS --> IND
    
    MS --> TAPI
    MS --> MC
    MS --> SL
    DS --> TAPI
    DS --> DC
    DS --> SL
    
    MDS --> MC
    MDS --> CP
    ESS --> MC
    ESS --> OPE
    MSS --> MC
    MSS --> OP
    LTS --> TAPI
    LTS --> LP
    TS --> TAPI
    
    CWS --> CACHE
    CWS --> TAPI
    
    CM --> SL
    CD --> SL
    CI --> SL
    MSP --> SL
    ESP --> SL
    MSC --> SL
    LT --> SL
```

## 📊 Система логирования

```mermaid
sequenceDiagram
  participant API as Controller
  participant Logs as System Logs
  participant DB as Database

  API->>Logs: STARTED log
  Note over Logs: taskId, endpoint, method, startTime
  
  API->>DB: Process request
  DB-->>API: Results
  
  alt Success
    API->>Logs: COMPLETED log
    Note over Logs: duration, message, statistics
  else Error
    API->>Logs: FAILED log
    Note over Logs: error message, duration
  end
  
  Note over Logs: All logs include:<br/>- taskId (UUID)<br/>- endpoint path<br/>- HTTP method<br/>- status (STARTED/COMPLETED/FAILED)<br/>- message with details<br/>- startTime/endTime<br/>- durationMs
```

## 🔄 Асинхронная обработка

```mermaid
sequenceDiagram
  participant Client as Client
  participant API as Controller
  participant Executor as Thread Pool
  participant Service as Service
  participant TAPI as Tinkoff API
  participant DB as Database
  participant Logs as System Logs

  Client->>API: POST /api/candles/minute
  API->>Logs: STARTED log (taskId)
  API->>Executor: submit async task
  API-->>Client: 202 Accepted + taskId
  
  Note over Executor,DB: Async processing
  
  Executor->>Service: saveMinuteCandlesAsync()
  Service->>TAPI: fetch candles (paged)
  TAPI-->>Service: candles batches
  Service->>DB: batch insert
  Service->>Logs: per-FIGI logs
  Service->>Logs: COMPLETED log
  
  Note over Client: Client can check status via taskId
  
  Client->>API: GET /api/system/logs?taskId=xxx
  API->>DB: SELECT from system_logs
  DB-->>API: log entries
  API-->>Client: task status + details
```

## 📈 Поток данных по типам инструментов

```mermaid
graph LR
    subgraph "Data Sources"
        T[Tinkoff API<br/>gRPC + REST]
        DB[(PostgreSQL)]
        CACHE[Spring Cache<br/>Caffeine]
    end
    
    subgraph "Controllers"
        IC[InstrumentsController]
        CM[CandlesMinuteController]
        CD[CandlesDailyController]
        CI[CandlesInstrumentController]
        MSP[MainSessionPricesController]
        ESP[EveningSessionController]
        MSC[MorningSessionController]
        LT[LastTradesController]
        TC[TradingController]
    end
    
    subgraph "Data Types"
        SH[Shares<br/>Акции]
        FU[Futures<br/>Фьючерсы]
        IN[Indicatives<br/>Индикативы]
    end
    
    subgraph "Storage"
        ST[invest.shares]
        FT[invest.futures]
        IT[invest.indicatives]
        MC[invest.minute_candles]
        DC[invest.daily_candles]
        CP[invest.close_prices]
        OPE[invest.close_prices_evening_session]
        OP[invest.open_prices]
        LP[invest.last_prices]
        SL[invest.system_logs]
        MV[invest.today_volume_aggregation<br/>invest.daily_volume_aggregation]
    end
    
    T --> IC
    T --> CM
    T --> CD
    T --> CI
    T --> LT
    T --> TC
    
    IC --> CACHE
    IC --> ST
    IC --> FT
    IC --> IT
    
    CM --> SH
    CM --> FU
    CM --> IN
    CD --> SH
    CD --> FU
    CD --> IN
    CI --> SH
    CI --> FU
    CI --> IN
    
    CM --> MC
    CD --> DC
    MSP --> CP
    ESP --> OPE
    MSC --> OP
    LT --> LP
    
    CM --> SL
    CD --> SL
    CI --> SL
    MSP --> SL
    ESP --> SL
    MSC --> SL
    LT --> SL
    
    MC --> MV
    DC --> MV
```

## 🔄 Кэширование и производительность

```mermaid
graph TB
    subgraph "Cache Strategy"
        CACHE[Spring Cache<br/>Caffeine]
        CACHE --> C1[sharesCache<br/>TTL: 1 hour]
        CACHE --> C2[futuresCache<br/>TTL: 1 hour]
        CACHE --> C3[indicativesCache<br/>TTL: 1 hour]
        CACHE --> C4[closePricesCache<br/>TTL: 15 min]
    end
    
    subgraph "Database Optimization"
        DB[(PostgreSQL)]
        DB --> P1[Partitioning<br/>by date/time]
        DB --> P2[Indexes<br/>composite keys]
        DB --> P3[Materialized Views<br/>pre-computed]
    end
    
    subgraph "Async Processing"
        EXEC[Thread Pools]
        EXEC --> E1[minuteCandleExecutor<br/>10 threads]
        EXEC --> E2[dailyCandleExecutor<br/>5 threads]
        EXEC --> E3[apiDataExecutor<br/>20 threads]
        EXEC --> E4[batchWriteExecutor<br/>5 threads]
    end
    
    subgraph "External API"
        TAPI[Tinkoff API]
        TAPI --> T1[Rate Limiting<br/>100 req/min]
        TAPI --> T2[Throttling<br/>paged requests]
        TAPI --> T3[Retry Logic<br/>exponential backoff]
    end
    
    CACHE --> DB
    EXEC --> TAPI
    EXEC --> DB
```