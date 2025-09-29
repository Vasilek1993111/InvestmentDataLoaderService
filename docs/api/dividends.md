# API Дивидендов

API для работы с дивидендами по инструментам через T-Bank T-Invest API.

## Обзор

API предоставляет возможность:
- Загружать дивиденды по инструментам за период
- Получать дивиденды по FIGI
- Фильтровать дивиденды по датам
- Получать статистику по дивидендам
- Находить ближайшие дивиденды к выплате

## Эндпоинты

### 1. Получение дивидендов по всем акциям

**GET** `/api/dividends/load?from={from}&to={to}`

Получает дивиденды по всем акциям напрямую от T-Bank API.

> **Источник данных**: T-Bank T-Invest API (реальное время)

### 2. Загрузка дивидендов по всем акциям

**POST** `/api/dividends/load?from={from}&to={to}`

Загружает дивиденды по всем акциям в базу данных.

> **Источник данных**: T-Bank T-Invest API → База данных

#### Параметры

- `from` (query, optional) - начальная дата (YYYY-MM-DD). По умолчанию: 2024-01-01
- `to` (query, optional) - конечная дата (YYYY-MM-DD). По умолчанию: 2026-12-31

#### Примеры запросов

**GET - получение дивидендов (без сохранения):**
```bash
curl "http://localhost:8080/api/dividends/load"
curl "http://localhost:8080/api/dividends/load?from=2024-01-01&to=2024-12-31"
```

**POST - загрузка дивидендов в БД:**
```bash
curl -X POST "http://localhost:8080/api/dividends/load"
curl -X POST "http://localhost:8080/api/dividends/load?from=2024-01-01&to=2024-12-31"
```

#### Ответы

**GET ответ (список дивидендов):**
```json
[
  {
    "figi": "BBG004730N88",
    "declaredDate": "2024-01-15",
    "recordDate": "2024-01-20",
    "paymentDate": "2024-01-25",
    "dividendValue": 10.50,
    "currency": "RUB",
    "dividendType": "ORDINARY"
  }
]
```

**POST ответ (статистика загрузки):**
```json
{
  "success": true,
  "message": "Успешно загружено 150 новых записей о дивидендах для 25 инструментов",
  "from": "2024-01-01",
  "to": "2024-12-31",
  "processedInstruments": 25,
  "errorInstruments": 0,
  "totalFromApi": 150,
  "totalLoaded": 150,
  "alreadyExists": 0,
  "timestamp": "2024-01-15"
}
```

### 3. Загрузка дивидендов по инструментам

**POST** `/api/dividends/load-instruments`

Загружает дивиденды по указанным инструментам в базу данных.

> **Источник данных**: T-Bank T-Invest API → База данных

#### Параметры запроса

```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"],
  "from": "2024-01-01",
  "to": "2024-12-31"
}
```

**Специальные ключевые слова:**
- `"SHARES"` - загружает дивиденды для всех акций из базы данных

**Пример с ключевым словом:**
```json
{
  "instruments": ["SHARES"],
  "from": "2024-01-01",
  "to": "2024-12-31"
}
```

#### Пример запроса

```bash
curl -X POST "http://localhost:8080/api/dividends/load-instruments" \
  -H "Content-Type: application/json" \
  -d '{
    "instruments": ["SHARES"],
    "from": "2024-01-01",
    "to": "2024-12-31"
  }'
```

#### Ответ

```json
{
  "success": true,
  "message": "Успешно загружено 150 новых записей о дивидендах для 25 инструментов",
  "from": "2024-01-01",
  "to": "2024-12-31",
  "processedInstruments": 25,
  "errorInstruments": 0,
  "totalFromApi": 150,
  "totalLoaded": 150,
  "alreadyExists": 0,
  "timestamp": "2024-01-15"
}
```

### 4. Загрузка дивидендов по FIGI

**POST** `/api/dividends/{figi}`

Загружает дивиденды по указанному FIGI в базу данных.

> **Источник данных**: T-Bank T-Invest API → База данных

#### Параметры

- `figi` (path) - FIGI инструмента
- Тело запроса (optional) - JSON с датами:
  ```json
  {
    "from": "2024-01-01",
    "to": "2024-12-31"
  }
  ```

#### Примеры запросов

**Без тела запроса (использует даты по умолчанию):**
```bash
curl -X POST "http://localhost:8080/api/dividends/BBG004730N88"
```

**С указанием дат в теле:**
```bash
curl -X POST "http://localhost:8080/api/dividends/BBG004730N88" \
  -H "Content-Type: application/json" \
  -d '{
    "from": "2024-01-01",
    "to": "2024-12-31"
  }'
```

**Только с начальной датой:**
```bash
curl -X POST "http://localhost:8080/api/dividends/BBG004730N88" \
  -H "Content-Type: application/json" \
  -d '{
    "from": "2024-06-01"
  }'
```

#### Ответ

**При загрузке новых дивидендов:**
```json
{
  "success": true,
  "message": "Успешно загружено 5 новых записей о дивидендах для BBG004730N88",
  "figi": "BBG004730N88",
  "from": "2024-01-01",
  "to": "2024-12-31",
  "totalFromApi": 5,
  "totalLoaded": 5,
  "alreadyExists": 0,
  "timestamp": "2024-01-15"
}
```

**Если дивиденды уже существуют в БД:**
```json
{
  "success": true,
  "message": "Дивиденды для BBG004730N88 уже существуют в БД (3 записи)",
  "figi": "BBG004730N88",
  "from": "2024-01-01",
  "to": "2024-12-31",
  "totalFromApi": 3,
  "totalLoaded": 0,
  "alreadyExists": 3,
  "timestamp": "2024-01-15"
}
```

**Если дивиденды не найдены:**
```json
{
  "success": true,
  "message": "Дивиденды для BBG004730N88 не найдены в указанном периоде",
  "figi": "BBG004730N88",
  "from": "2024-01-01",
  "to": "2024-12-31",
  "totalFromApi": 0,
  "totalLoaded": 0,
  "alreadyExists": 0,
  "timestamp": "2024-01-15"
}
```

### 5. Получение дивидендов по FIGI

**GET** `/api/dividends/{figi}?from={from}&to={to}`

Получает дивиденды по указанному FIGI напрямую от T-Bank API.

> **Источник данных**: T-Bank T-Invest API (реальное время)

#### Параметры

- `figi` (path) - FIGI инструмента
- `from` (query, optional) - начальная дата (YYYY-MM-DD). По умолчанию: 2024-01-01
- `to` (query, optional) - конечная дата (YYYY-MM-DD). По умолчанию: 2026-12-01

#### Примеры запросов

**Без параметров (использует даты по умолчанию):**
```bash
curl "http://localhost:8080/api/dividends/BBG004730N88"
```

**С указанием дат:**
```bash
curl "http://localhost:8080/api/dividends/BBG004730N88?from=2024-01-01&to=2024-12-31"
```

**Только с начальной датой:**
```bash
curl "http://localhost:8080/api/dividends/BBG004730N88?from=2024-06-01"
```

#### Ответ

```json
[
  {
    "figi": "BBG004730N88",
    "declaredDate": "2024-01-15",
    "recordDate": "2024-01-20",
    "paymentDate": "2024-01-25",
    "dividendValue": 10.50,
    "currency": "RUB",
    "dividendType": "ORDINARY"
  },
  {
    "figi": "BBG004730N88",
    "declaredDate": "2023-07-10",
    "recordDate": "2023-07-15",
    "paymentDate": "2023-07-20",
    "dividendValue": 8.25,
    "currency": "RUB",
    "dividendType": "ORDINARY"
  }
]
```



## Структура данных

### DividendDto

```json
{
  "figi": "string",           // FIGI инструмента
  "declaredDate": "date",     // Дата объявления дивидендов
  "recordDate": "date",       // Дата фиксации реестра
  "paymentDate": "date",      // Дата выплаты дивидендов
  "dividendValue": "decimal", // Размер дивиденда на одну акцию
  "currency": "string",       // Валюта дивиденда
  "dividendType": "string"    // Тип дивиденда
}
```

### DividendRequestDto

```json
{
  "instruments": ["string"],  // Список FIGI инструментов
  "from": "date",            // Начальная дата периода
  "to": "date"               // Конечная дата периода
}
```

## Коды ошибок

| Код | Описание |
|-----|----------|
| 200 | Успешный запрос |
| 400 | Некорректные параметры запроса |
| 404 | Инструмент не найден |
| 500 | Внутренняя ошибка сервера |

## Примеры использования

### Получение дивидендов по всем акциям

**GET - получение без сохранения:**
```bash
curl "http://localhost:8080/api/dividends/load"
curl "http://localhost:8080/api/dividends/load?from=2024-01-01&to=2024-12-31"
```

### Загрузка дивидендов по всем акциям в БД

**POST - загрузка в базу данных:**
```bash
curl -X POST "http://localhost:8080/api/dividends/load"
curl -X POST "http://localhost:8080/api/dividends/load?from=2024-01-01&to=2024-12-31"
```

### Загрузка дивидендов по инструментам

**POST /load-instruments с телом запроса:**
```bash
curl -X POST "http://localhost:8080/api/dividends/load-instruments" \
  -H "Content-Type: application/json" \
  -d '{
    "instruments": ["SHARES"],
    "from": "2024-01-01",
    "to": "2024-12-31"
  }'
```

### Загрузка дивидендов для конкретного инструмента

**Без тела запроса (даты по умолчанию):**
```bash
curl -X POST "http://localhost:8080/api/dividends/BBG004730N88"
```

**С указанием дат:**
```bash
curl -X POST "http://localhost:8080/api/dividends/BBG004730N88" \
  -H "Content-Type: application/json" \
  -d '{
    "from": "2024-01-01",
    "to": "2024-12-31"
  }'
```



## Примечания

1. **Валюты**: Все суммы дивидендов возвращаются в валюте инструмента
2. **Даты**: Все даты в формате ISO 8601 (YYYY-MM-DD) - например: "2024-01-15"
3. **Сортировка**: Дивиденды сортируются по дате фиксации реестра (новые сначала)
4. **Фильтрация**: При загрузке дивидендов дубликаты автоматически исключаются
5. **Лимиты**: API не имеет встроенных лимитов, но рекомендуется не запрашивать более 100 инструментов за раз
6. **Источник данных**: 
   - **GET /load** - получает дивиденды по всем акциям от T-Bank API (реальное время)
   - **POST /load** - загружает дивиденды по всем акциям в БД
   - **POST /load-instruments** - загружает дивиденды по указанным инструментам в БД
   - **POST /{figi}** - загружает дивиденды по конкретному FIGI в БД
   - **GET /{figi}** - получает данные напрямую от T-Bank API (реальное время)
7. **Формат дат**: Jackson автоматически сериализует LocalDate в строки формата "yyyy-MM-dd"

## Интеграция с T-Bank API

API использует метод `GetDividends` из `InstrumentsService` T-Bank T-Invest API для получения актуальных данных о дивидендах.

### Источник данных

- **API**: T-Bank T-Invest API
- **Метод**: `InstrumentsService.GetDividends`
- **Обновление**: Данные обновляются в реальном времени при каждом запросе
- **Кэширование**: Результаты сохраняются в базе данных для быстрого доступа
