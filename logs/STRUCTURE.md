# Структура логов InvestmentDataLoaderService

## Обзор
Система логирования организована по функциональным группам с автоматическим архивированием по датам.

## Структура директорий

```
logs/
├── current/                    # Текущие логи
│   ├── system.log             # Системные логи
│   ├── errors.log             # Ошибки (только ERROR уровень)
│   ├── evening-session.log    # Вечерняя сессия
│   ├── morning-session.log    # Утренняя сессия
│   ├── main-session.log       # Основная сессия
│   ├── instruments.log        # Инструменты
│   ├── candles.log            # Свечи (дневные и минутные)
│   ├── trading.log            # Торговля
│   ├── dividends.log          # Дивиденды
│   ├── fundamentals.log      # Фундаментальные показатели
│   ├── last-trades.log        # Последние сделки
│   ├── cache.log              # Кэш
│   ├── schedulers.log         # Планировщики
│   └── api-clients.log        # Клиенты API
└── archive/                   # Архивные логи
    └── YYYY-MM-DD/           # Логи по датам
        ├── system.log
        ├── errors.log
        ├── evening-session.log
        ├── morning-session.log
        ├── main-session.log
        ├── instruments.log
        ├── candles.log
        ├── trading.log
        ├── dividends.log
        ├── fundamentals.log
        ├── last-trades.log
        ├── cache.log
        ├── schedulers.log
        └── api-clients.log
```

## Функциональные группы логов

### 1. Системные логи (`system.log`)
- **Контроллеры**: SystemController, StatusController, RateLimitController, VolumeAggregationController
- **Сервисы**: RateLimitService, RetryService
- **Приложение**: InvestmentDataLoaderService (главный класс)

### 2. Вечерняя сессия (`evening-session.log`)
- **Контроллер**: EveningSessionController
- **Сервис**: EveningSessionService

### 3. Утренняя сессия (`morning-session.log`)
- **Контроллер**: MorningSessionController
- **Сервис**: MorningSessionService

### 4. Основная сессия (`main-session.log`)
- **Контроллер**: MainSessionPricesController
- **Сервис**: MainSessionPriceService

### 5. Инструменты (`instruments.log`)
- **Контроллер**: InstrumentsController
- **Сервисы**: InstrumentService, CachedInstrumentService

### 6. Свечи (`candles.log`)
- **Контроллеры**: CandlesDailyController, CandlesMinuteController, CandlesInstrumentController
- **Сервисы**: DailyCandleService, MinuteCandleService

### 7. Торговля (`trading.log`)
- **Контроллер**: TradingController
- **Сервис**: TradingService

### 8. Дивиденды (`dividends.log`)
- **Контроллер**: DividendController
- **Сервис**: DividendService

### 9. Фундаментальные показатели (`fundamentals.log`)
- **Контроллер**: AssetFundamentalsController
- **Сервис**: AssetFundamentalService

### 10. Последние сделки (`last-trades.log`)
- **Контроллер**: LastTradesController
- **Сервисы**: LastTradesService, LastTradeService

### 11. Кэш (`cache.log`)
- **Контроллер**: CacheController
- **Сервисы**: CacheWarmupService

### 12. Планировщики (`schedulers.log`)
- **Все сервисы пакета**: `com.example.InvestmentDataLoaderService.scheduler`

### 13. Клиенты API (`api-clients.log`)
- **Все клиенты пакета**: `com.example.InvestmentDataLoaderService.client`

## Настройки архивирования

- **Период хранения**: 30 дней
- **Ротация**: Ежедневная
- **Формат архива**: `YYYY-MM-DD/`
- **Паттерн логов**: `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`

## Уровни логирования

- **INFO**: Основная информация о работе
- **DEBUG**: Детальная отладочная информация
- **ERROR**: Ошибки и исключения
- **WARN**: Предупреждения

## Мониторинг

Все логи также выводятся в консоль для разработки и отладки.

## Замена System.out.println

Все вызовы `System.out.println` и `System.err.println` заменены на соответствующие методы логирования:
- `log.info()` для информационных сообщений
- `log.error()` для ошибок
- `log.debug()` для отладочной информации
- `log.warn()` для предупреждений

## Поток данных

```
Приложение → logback-spring.xml → logs/current/ → logs/archive/YYYY-MM-DD/
```

## Автоматическая ротация

1. **Ежедневно в 00:00** - логи перемещаются из `current/` в `archive/YYYY-MM-DD/`
2. **Автоматическая очистка** - логи старше 30 дней удаляются
3. **Сжатие** - старые логи сжимаются для экономии места