# Поток данных (ingestion & aggregation)

```mermaid
sequenceDiagram
  autonumber
  participant Client as Client
  participant API as REST API
  participant CandlesAPI as Candles Controllers
  participant Svc as Services
  participant T as Tinkoff API
  participant DB as PostgreSQL
  participant Logs as System Logs
  participant MV as Mat. Views

  Note over Client,MV: Свечи (минутные/дневные)
  
  Client->>CandlesAPI: POST /api/candles/minute/shares/{date}
  CandlesAPI->>Logs: STARTED log
  CandlesAPI->>Svc: saveMinuteCandlesAsync()
  Svc->>T: fetch candles (paged, throttled)
  T-->>Svc: candles batches
  Svc->>DB: insert into invest.minute_candles
  Svc->>Logs: per-FIGI logs (SUCCESS/NO_DATA/ERROR)
  Svc-->>CandlesAPI: taskId + results
  CandlesAPI->>Logs: COMPLETED log
  CandlesAPI-->>Client: success + statistics

  Note over Client,MV: Конкретные инструменты
  
  Client->>CandlesAPI: GET /api/candles/instrument/minute/{figi}/{date}
  CandlesAPI->>Logs: STARTED log
  CandlesAPI->>T: fetch candles for specific instrument
  T-->>CandlesAPI: candles data
  CandlesAPI->>Logs: COMPLETED log
  CandlesAPI-->>Client: candles + extended statistics

  Note over Client,MV: Дневные свечи
  
  Client->>CandlesAPI: POST /api/candles/daily/futures/{date}
  CandlesAPI->>Logs: STARTED log
  CandlesAPI->>Svc: saveDailyCandlesAsync()
  Svc->>T: fetch daily candles
  T-->>Svc: daily candles
  Svc->>DB: insert into invest.daily_candles
  Svc->>Logs: per-FIGI logs (SUCCESS/NO_DATA/ERROR)
  Svc-->>CandlesAPI: taskId + results
  CandlesAPI->>Logs: COMPLETED log
  CandlesAPI-->>Client: success + statistics

  Note over Client,MV: Цены закрытия
  
  Client->>API: POST /api/main-session-prices/
  API->>Svc: loadClosePrices()
  Svc->>DB: SELECT from minute_candles
  Svc->>DB: INSERT into invest.close_prices
  Svc-->>API: results
  API-->>Client: success + statistics

  Note over Client,MV: Вечерняя сессия
  
  Client->>API: POST /api/evening-session-prices
  API->>Svc: loadEveningSessionPrices()
  Svc->>DB: SELECT from minute_candles
  Svc->>DB: INSERT into invest.close_price_evening_session
  Svc-->>API: results
  API-->>Client: success + statistics

  Note over Svc,DB: Планировщики (01:00, 01:10, 02:00, 02:01, */30m)

  API->>Svc: POST /api/analytics/volume-aggregation/refresh-today
  Svc->>DB: REFRESH MATERIALIZED VIEW invest.today_volume_aggregation
  DB-->>API: ok

  API->>Svc: POST /api/analytics/volume-aggregation/refresh-full
  Svc->>DB: REFRESH MATERIALIZED VIEW invest.daily_volume_aggregation
  DB-->>API: ok

  Note over Client,MV: Логирование
  
  Client->>CandlesAPI: Any GET/POST request
  CandlesAPI->>Logs: STARTED log (taskId, endpoint, method)
  CandlesAPI->>Svc: process request
  Svc-->>CandlesAPI: results
  CandlesAPI->>Logs: COMPLETED/FAILED log (duration, message)
  CandlesAPI-->>Client: response
```

## Архитектура контроллеров свечей

```mermaid
graph TB
    subgraph "Candles Controllers"
        CM[CandlesMinuteController<br/>/api/candles/minute]
        CD[CandlesDailyController<br/>/api/candles/daily]
        CI[CandlesInstrumentController<br/>/api/candles/instrument]
    end
    
    subgraph "Endpoints"
        CM --> CM1[POST /minute]
        CM --> CM2[POST /minute/{date}]
        CM --> CM3[GET/POST /shares/{date}]
        CM --> CM4[GET/POST /futures/{date}]
        CM --> CM5[GET/POST /indicatives/{date}]
        
        CD --> CD1[POST /daily]
        CD --> CD2[POST /daily/{date}]
        CD --> CD3[GET/POST /shares/{date}]
        CD --> CD4[GET/POST /futures/{date}]
        CD --> CD5[GET/POST /indicatives/{date}]
        
        CI --> CI1[GET/POST /minute/{figi}/{date}]
        CI --> CI2[GET/POST /daily/{figi}/{date}]
    end
    
    subgraph "Services"
        MS[MinuteCandleService]
        DS[DailyCandleService]
        MDS[MarketDataService]
    end
    
    subgraph "Database"
        MC[invest.minute_candles]
        DC[invest.daily_candles]
        SL[invest.system_logs]
    end
    
    CM --> MS
    CD --> DS
    CI --> MS
    CI --> DS
    MS --> MDS
    DS --> MDS
    MS --> MC
    DS --> DC
    CM --> SL
    CD --> SL
    CI --> SL
```

## Система логирования

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

## Поток данных по типам инструментов

```mermaid
graph LR
    subgraph "Data Sources"
        T[Tinkoff API]
        DB[(PostgreSQL)]
    end
    
    subgraph "Controllers"
        CM[CandlesMinuteController]
        CD[CandlesDailyController]
        CI[CandlesInstrumentController]
    end
    
    subgraph "Data Types"
        SH[Shares<br/>Акции]
        FU[Futures<br/>Фьючерсы]
        IN[Indicatives<br/>Индикативы]
    end
    
    subgraph "Storage"
        MC[minute_candles]
        DC[daily_candles]
        SL[system_logs]
    end
    
    T --> CM
    T --> CD
    T --> CI
    
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
    CM --> SL
    CD --> SL
    CI --> SL
```