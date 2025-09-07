# Investment Data Loader Service API Documentation

## GET /shares

Получение списка акций из T-Bank API с возможностью фильтрации.

### Описание
Эндпоинт возвращает список акций, полученных напрямую из T-Bank API без сохранения в базу данных. Данные фильтруются на основе переданных параметров и возвращаются в отсортированном по тикеру виде.

### URL
```
GET http://localhost:8083/shares
```

### Параметры запроса

| Параметр | Тип | Обязательный | Описание | Возможные значения |
|----------|-----|--------------|----------|-------------------|
| `status` | String | Нет | Статус инструмента | `INSTRUMENT_STATUS_BASE`, `INSTRUMENT_STATUS_ALL` |
| `exchange` | String | Нет | Биржа | `moex_mrng_evng_e_wknd_dlr`, `SPB`, `NASDAQ`, `NYSE`, и др. |
| `currency` | String | Нет | Валюта инструмента | `RUB`, `USD`, `EUR`, и др. |
| `ticker` | String | Нет | Тикер инструмента | `SBER`, `GAZP`, `LKOH`, `NVTK`, и др. |
| `figi` | String | Нет | Уникальный идентификатор инструмента | `BBG004730N88`, `BBG004730ZJ9`, и др. |

### Примеры запросов

#### Получить все акции
```bash
curl "http://localhost:8083/shares"
```

#### Получить акции с базовым статусом
```bash
curl "http://localhost:8083/shares?status=INSTRUMENT_STATUS_BASE"
```

#### Получить акции с биржи MOEX
```bash
curl "http://localhost:8083/shares?exchange=moex_mrng_evng_e_wknd_dlr"
```

#### Получить акции в рублях
```bash
curl "http://localhost:8083/shares?currency=RUB"
```

#### Получить акцию по тикеру
```bash
curl "http://localhost:8083/shares?ticker=SBER"
```

#### Получить акцию по FIGI
```bash
curl "http://localhost:8083/shares?figi=BBG004730N88"
```

#### Комбинированный запрос
```bash
curl "http://localhost:8083/shares?status=INSTRUMENT_STATUS_BASE&exchange=moex_mrng_evng_e_wknd_dlr&currency=RUB&ticker=SBER&figi=BBG004730N88"
```

### Формат ответа

```json
[
  {
    "figi": "BBG004730N88",
    "ticker": "SBER",
    "name": "Сбербанк",
    "currency": "RUB",
    "exchange": "moex_mrng_evng_e_wknd_dlr",
    "sector": "Financial Services",
    "tradingStatus": "SECURITY_TRADING_STATUS_NORMAL_TRADING"
  }
]
```

### Описание полей ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `figi` | String | Уникальный идентификатор инструмента (FIGI) |
| `ticker` | String | Тикер инструмента |
| `name` | String | Название инструмента |
| `currency` | String | Валюта инструмента |
| `exchange` | String | Биржа, на которой торгуется инструмент |
| `sector` | String | Сектор экономики |
| `tradingStatus` | String | Статус торговли |

### Возможные статусы инструментов

Согласно [документации T-Bank API](https://developer.tbank.ru/invest/api/instruments-service):

- `INSTRUMENT_STATUS_BASE` - базовый статус (по умолчанию)
- `INSTRUMENT_STATUS_ALL` - все статусы

### Возможные биржи

- `moex_mrng_evng_e_wknd_dlr` - Московская биржа
- `SPB` - Санкт-Петербургская биржа
- `NASDAQ` - NASDAQ
- `NYSE` - Нью-Йоркская фондовая биржа
- `moex_mrng_evng_e_wknd_dlr` - Московская биржа все торгуемые инструменты
- И другие биржи, поддерживаемые T-Bank API

### Возможные валюты

- `RUB` - Российский рубль
- `USD` - Доллар США
- `EUR` - Евро
- И другие валюты, поддерживаемые T-Bank API

### Коды ответов

| Код | Описание |
|-----|----------|
| 200 | Успешный запрос |
| 400 | Некорректные параметры запроса |
| 500 | Внутренняя ошибка сервера |

### Особенности

1. **Без сохранения в БД**: Данные получаются напрямую из T-Bank API и не сохраняются в базу данных
2. **Сортировка**: Результаты автоматически сортируются по тикеру в алфавитном порядке
3. **Фильтрация**: Фильтры применяются на стороне приложения после получения данных из API
4. **Регистронезависимость**: Фильтры по бирже и валюте работают без учета регистра

### Ограничения

- Максимальное количество возвращаемых инструментов ограничено возможностями T-Bank API
- При некорректном значении параметра `status` используется значение по умолчанию `INSTRUMENT_STATUS_BASE`
- Все параметры фильтрации являются необязательными

### Примеры для Postman

#### 1. Получить все акции
- **Method**: `GET`
- **URL**: `http://localhost:8083/shares`
- **Headers**: Не требуются

#### 2. Получить акции с базовым статусом
- **Method**: `GET`
- **URL**: `http://localhost:8083/shares`
- **Query Parameters**:
  - `status`: `INSTRUMENT_STATUS_BASE`
- **Headers**: Не требуются

#### 3. Получить акции с биржи MOEX
- **Method**: `GET`
- **URL**: `http://localhost:8083/shares`
- **Query Parameters**:
  - `exchange`: `moex_mrng_evng_e_wknd_dlr`
- **Headers**: Не требуются

#### 4. Получить акции в рублях
- **Method**: `GET`
- **URL**: `http://localhost:8083/shares`
- **Query Parameters**:
  - `currency`: `RUB`
- **Headers**: Не требуются

#### 5. Получить акцию по тикеру
- **Method**: `GET`
- **URL**: `http://localhost:8083/shares`
- **Query Parameters**:
  - `ticker`: `SBER`
- **Headers**: Не требуются

#### 6. Получить акцию по FIGI
- **Method**: `GET`
- **URL**: `http://localhost:8083/shares`
- **Query Parameters**:
  - `figi`: `BBG004730N88`
- **Headers**: Не требуются

#### 7. Комбинированный запрос
- **Method**: `GET`
- **URL**: `http://localhost:8083/shares`
- **Query Parameters**:
  - `status`: `INSTRUMENT_STATUS_BASE`
  - `exchange`: `moex_mrng_evng_e_wknd_dlr`
  - `currency`: `RUB`
  - `ticker`: `SBER`
  - `figi`: `BBG004730N88`
- **Headers**: Не требуются

## POST /shares

Сохранение акций в базу данных. Метод запрашивает список акций из T-Bank API согласно параметрам фильтрации, проверяет их наличие в таблице `invest.shares` и сохраняет только новые акции.

### Описание
Эндпоинт получает акции из T-Bank API, фильтрует их по заданным параметрам, проверяет существование в базе данных и сохраняет только те акции, которых еще нет в таблице `shares`.

### URL
```
POST http://localhost:8083/shares
```

### Тело запроса

Параметры передаются в теле запроса в формате JSON:

```json
{
  "status": "INSTRUMENT_STATUS_BASE",
  "exchange": "moex_mrng_evng_e_wknd_dlr",
  "currency": "RUB",
  "ticker": "SBER"
}
```

### Параметры тела запроса

| Параметр | Тип | Обязательный | Описание | Возможные значения |
|----------|-----|--------------|----------|-------------------|
| `status` | String | Нет | Статус инструмента | `INSTRUMENT_STATUS_BASE`, `INSTRUMENT_STATUS_ALL` |
| `exchange` | String | Нет | Биржа | `moex_mrng_evng_e_wknd_dlr`, `SPB`, `NASDAQ`, `NYSE`, и др. |
| `currency` | String | Нет | Валюта инструмента | `RUB`, `USD`, `EUR`, и др. |
| `ticker` | String | Нет | Тикер инструмента | `SBER`, `GAZP`, `LKOH`, `NVTK`, и др. |

### Примеры запросов

#### Сохранить все акции
```bash
curl -X POST "http://localhost:8083/shares" \
  -H "Content-Type: application/json" \
  -d '{}'
```

#### Сохранить акции с базовым статусом
```bash
curl -X POST "http://localhost:8083/shares" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "INSTRUMENT_STATUS_BASE"
  }'
```

#### Сохранить акции с биржи MOEX
```bash
curl -X POST "http://localhost:8083/shares" \
  -H "Content-Type: application/json" \
  -d '{
    "exchange": "moex_mrng_evng_e_wknd_dlr"
  }'
```

#### Сохранить акции в рублях
```bash
curl -X POST "http://localhost:8083/shares" \
  -H "Content-Type: application/json" \
  -d '{
    "currency": "RUB"
  }'
```

#### Сохранить акцию по тикеру
```bash
curl -X POST "http://localhost:8083/shares" \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "SBER"
  }'
```

#### Комбинированный запрос
```bash
curl -X POST "http://localhost:8083/shares" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "INSTRUMENT_STATUS_BASE",
    "exchange": "moex_mrng_evng_e_wknd_dlr",
    "currency": "RUB",
    "ticker": "SBER"
  }'
```

### Примеры для Postman

#### 1. Сохранить все акции
- **Method**: `POST`
- **URL**: `http://localhost:8083/shares`
- **Headers**: Не требуются
- **Body**: Не требуется

#### 2. Сохранить акции с базовым статусом
- **Method**: `POST`
- **URL**: `http://localhost:8083/shares`
- **Query Parameters**:
  - `status`: `INSTRUMENT_STATUS_BASE`
- **Headers**: Не требуются
- **Body**: Не требуется

#### 3. Сохранить акции с биржи MOEX
- **Method**: `POST`
- **URL**: `http://localhost:8083/shares`
- **Query Parameters**:
  - `exchange`: `moex_mrng_evng_e_wknd_dlr`
- **Headers**: Не требуются
- **Body**: Не требуется

#### 4. Сохранить акции в рублях
- **Method**: `POST`
- **URL**: `http://localhost:8083/shares`
- **Query Parameters**:
  - `currency`: `RUB`
- **Headers**: Не требуются
- **Body**: Не требуется

#### 5. Комбинированный запрос
- **Method**: `POST`
- **URL**: `http://localhost:8083/shares`
- **Query Parameters**:
  - `status`: `INSTRUMENT_STATUS_BASE`
  - `exchange`: `moex_mrng_evng_e_wknd_dlr`
  - `currency`: `RUB`
- **Headers**: Не требуются
- **Body**: Не требуется

### Формат ответа

Возвращает информативный ответ о результате сохранения:

```json
{
  "success": true,
  "message": "Успешно загружено 5 новых акций из 10 найденных.",
  "totalRequested": 10,
  "newItemsSaved": 5,
  "existingItemsSkipped": 5,
  "savedItems": [
    {
      "figi": "BBG004730N88",
      "ticker": "SBER",
      "name": "Сбербанк",
      "currency": "RUB",
      "exchange": "moex_mrng_evng_e_wknd_dlr",
      "sector": "Financial Services",
      "tradingStatus": "SECURITY_TRADING_STATUS_NORMAL_TRADING"
    }
  ]
}
```

### Описание полей ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `success` | Boolean | Успешность операции (true, если найдены акции по фильтрам) |
| `message` | String | Информативное сообщение о результате |
| `totalRequested` | Integer | Общее количество найденных акций по фильтрам |
| `newItemsSaved` | Integer | Количество новых акций, сохраненных в БД |
| `existingItemsSkipped` | Integer | Количество акций, которые уже существовали в БД |
| `savedItems` | Array | Массив сохраненных акций (пустой, если новых акций нет) |

### Возможные сообщения

- **Новые акции найдены**: `"Успешно загружено X новых акций из Y найденных."`
- **Нет новых акций**: `"Новых акций не обнаружено. Все найденные акции уже существуют в базе данных."`
- **Акции не найдены**: `"Новых акций не обнаружено. По заданным фильтрам акции не найдены."`

### Логика работы

1. **Получение данных**: Запрашивает акции из T-Bank API с применением фильтров
2. **Проверка существования**: Для каждой акции проверяет наличие в таблице `shares` по `figi`
3. **Сохранение**: Сохраняет только те акции, которых нет в базе данных
4. **Возврат результата**: Возвращает список только сохраненных акций

### Схема таблицы shares

```sql
CREATE TABLE shares (
    figi     VARCHAR(50)  NOT NULL PRIMARY KEY,
    ticker   VARCHAR(20)  NOT NULL UNIQUE,
    name     VARCHAR(100) NOT NULL,
    currency VARCHAR(10)  NOT NULL,
    exchange VARCHAR(50)  NOT NULL
);
```

### Коды ответов

| Код | Описание |
|-----|----------|
| 200 | Успешное сохранение |
| 400 | Некорректные параметры запроса |
| 500 | Внутренняя ошибка сервера или ошибка базы данных |

### Особенности

1. **Идемпотентность**: Повторные вызовы с теми же параметрами не создадут дубликаты
2. **Частичное сохранение**: Если часть акций уже существует, сохранятся только новые
3. **Обработка ошибок**: Ошибки сохранения отдельных акций не прерывают процесс
4. **Фильтрация**: Применяются те же фильтры, что и в GET /shares

## GET /futures

Получение списка фьючерсов из T-Bank API с возможностью фильтрации.

### Описание
Эндпоинт возвращает список фьючерсов, полученных напрямую из T-Bank API без сохранения в базу данных. Данные фильтруются на основе переданных параметров и возвращаются в отсортированном по тикеру виде.

### URL
```
GET http://localhost:8083/futures
```

### Параметры запроса

| Параметр | Тип | Обязательный | Описание | Возможные значения |
|----------|-----|--------------|----------|-------------------|
| `status` | String | Нет | Статус инструмента | `INSTRUMENT_STATUS_BASE`, `INSTRUMENT_STATUS_ALL` |
| `exchange` | String | Нет | Биржа | `FORTS_EVENING`, `SPB`, и др. |
| `currency` | String | Нет | Валюта инструмента | `RUB`, `USD`, `EUR`, и др. |
| `ticker` | String | Нет | Тикер инструмента | `Si-12.24`, `RTS-12.24`, и др. |
| `assetType` | String | Нет | Тип базового актива | `TYPE_CURRENCY`, `TYPE_COMMODITY`, `TYPE_INDEX`, `TYPE_SECURITY` |

### Примеры запросов

#### Получить все фьючерсы
```bash
curl "http://localhost:8083/futures"
```

#### Получить фьючерсы с базовым статусом
```bash
curl "http://localhost:8083/futures?status=INSTRUMENT_STATUS_BASE"
```

#### Получить фьючерсы с биржи FORTS
```bash
curl "http://localhost:8083/futures?exchange=FORTS_EVENING"
```

#### Получить фьючерсы в рублях
```bash
curl "http://localhost:8083/futures?currency=RUB"
```

#### Получить фьючерс по тикеру
```bash
curl "http://localhost:8083/futures?ticker=Si-12.24"
```

#### Получить фьючерсы по типу актива
```bash
curl "http://localhost:8083/futures?assetType=CURRENCY"
```

#### Комбинированный запрос
```bash
curl "http://localhost:8083/futures?status=INSTRUMENT_STATUS_BASE&exchange=FORTS_EVENING&currency=RUB&ticker=Si-12.24&assetType=TYPE_CURRENCY"
```

### Формат ответа

```json
[
  {
    "figi": "FUTSI1224000",
    "ticker": "Si-12.24",
    "assetType": "TYPE_CURRENCY",
    "basicAsset": "USD/RUB",
    "currency": "RUB",
    "exchange": "FORTS_EVENING",
    "stockTicker": null
  }
]
```

### Возможные значения assetType

| Значение | Описание | Примеры тикеров |
|----------|----------|-----------------|
| `TYPE_CURRENCY` | Валютные фьючерсы | `Si-12.24`, `Eu-12.24` |
| `TYPE_COMMODITY` | Товарные фьючерсы | `BR-12.24`, `GZ-12.24` |
| `TYPE_INDEX` | Фьючерсы на индексы | `RTS-12.24`, `MX-12.24` |
| `TYPE_SECURITY` | Фьючерсы на акции | - |

### Особенности

- Максимальное количество возвращаемых инструментов ограничено возможностями T-Bank API
- При некорректном значении параметра `status` используется значение по умолчанию `INSTRUMENT_STATUS_BASE`
- Все параметры фильтрации являются необязательными
- Данные не сохраняются в базу данных
- Фильтрация по `assetType` происходит регистронезависимо

## POST /futures

Сохранение фьючерсов в базу данных. Метод запрашивает список фьючерсов из T-Bank API согласно параметрам фильтрации, проверяет их наличие в таблице `invest.futures` и сохраняет только новые фьючерсы.

### Описание
Эндпоинт получает фьючерсы из T-Bank API, фильтрует их по заданным параметрам, проверяет существование в базе данных и сохраняет только те фьючерсы, которых еще нет в таблице `futures`.

### URL
```
POST http://localhost:8083/futures
```

### Тело запроса

Параметры передаются в теле запроса в формате JSON:

```json
{
  "status": "INSTRUMENT_STATUS_BASE",
  "exchange": "FORTS_EVENING",
  "currency": "RUB",
  "ticker": "Si-12.24",
  "assetType": "TYPE_CURRENCY"
}
```

### Параметры тела запроса

| Параметр | Тип | Обязательный | Описание | Возможные значения |
|----------|-----|--------------|----------|-------------------|
| `status` | String | Нет | Статус инструмента | `INSTRUMENT_STATUS_BASE`, `INSTRUMENT_STATUS_ALL` |
| `exchange` | String | Нет | Биржа | `FORTS_EVENING`, `forts_futures_weekend`, и др. |
| `currency` | String | Нет | Валюта инструмента | `RUB`, `USD`, `EUR`, и др. |
| `ticker` | String | Нет | Тикер инструмента | `Si-12.24`, `RTS-12.24`, и др. |
| `assetType` | String | Нет | Тип базового актива | `TYPE_SECURITY`, `TYPE_COMMODITY`, `TYPE_CURRENCY`, `TYPE_INDEX` |

### Примеры запросов

#### Сохранить все фьючерсы
```bash
curl -X POST "http://localhost:8083/futures" \
  -H "Content-Type: application/json" \
  -d '{}'
```

#### Сохранить фьючерсы с базовым статусом
```bash
curl -X POST "http://localhost:8083/futures" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "INSTRUMENT_STATUS_BASE"
  }'
```

#### Сохранить фьючерсы с биржи FORTS
```bash
curl -X POST "http://localhost:8083/futures" \
  -H "Content-Type: application/json" \
  -d '{
    "exchange": "FORTS_EVENING"
  }'
```

#### Сохранить фьючерсы в рублях
```bash
curl -X POST "http://localhost:8083/futures" \
  -H "Content-Type: application/json" \
  -d '{
    "currency": "RUB"
  }'
```

#### Сохранить фьючерс по тикеру
```bash
curl -X POST "http://localhost:8083/futures" \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "Si-12.24"
  }'
```

#### Сохранить фьючерсы по типу актива
```bash
curl -X POST "http://localhost:8083/futures" \
  -H "Content-Type: application/json" \
  -d '{
    "assetType": "TYPE_CURRENCY"
  }'
```

#### Комбинированный запрос
```bash
curl -X POST "http://localhost:8083/futures" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "INSTRUMENT_STATUS_BASE",
    "exchange": "FORTS_EVENING",
    "currency": "RUB",
    "ticker": "Si-12.24",
    "assetType": "TYPE_CURRENCY"
  }'
```

### Формат ответа

Возвращает информативный ответ о результате сохранения:

```json
{
  "success": true,
  "message": "Успешно загружено 3 новых фьючерса из 8 найденных.",
  "totalRequested": 8,
  "newItemsSaved": 3,
  "existingItemsSkipped": 5,
  "savedItems": [
    {
      "figi": "FUTSI1224000",
      "ticker": "Si-12.24",
      "assetType": "TYPE_CURRENCY",
      "basicAsset": "USD/RUB",
      "currency": "RUB",
      "exchange": "FORTS_EVENING",
      "stockTicker": null
    }
  ]
}
```

### Описание полей ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `success` | Boolean | Успешность операции (true, если найдены фьючерсы по фильтрам) |
| `message` | String | Информативное сообщение о результате |
| `totalRequested` | Integer | Общее количество найденных фьючерсов по фильтрам |
| `newItemsSaved` | Integer | Количество новых фьючерсов, сохраненных в БД |
| `existingItemsSkipped` | Integer | Количество фьючерсов, которые уже существовали в БД |
| `savedItems` | Array | Массив сохраненных фьючерсов (пустой, если новых фьючерсов нет) |

### Возможные сообщения

- **Новые фьючерсы найдены**: `"Успешно загружено X новых фьючерсов из Y найденных."`
- **Нет новых фьючерсов**: `"Новых фьючерсов не обнаружено. Все найденные фьючерсы уже существуют в базе данных."`
- **Фьючерсы не найдены**: `"Новых фьючерсов не обнаружено. По заданным фильтрам фьючерсы не найдены."`

### Структура таблицы futures

```sql
CREATE TABLE futures (
    figi         varchar(255) not null primary key,
    ticker       varchar(255) not null,
    asset_type   varchar(255) not null,
    basic_asset  varchar(255),
    currency     varchar(255) not null,
    exchange     varchar(255) not null,
    stock_ticker varchar(255)
        constraint fk_stock_ticker
            references shares (ticker)
            on delete set null
);
```

### Коды ответов

- `200 OK` - Успешное выполнение запроса
- `400 Bad Request` - Некорректные параметры запроса
- `500 Internal Server Error` - Внутренняя ошибка сервера

### Особенности

1. **Идемпотентность**: Повторные вызовы с теми же параметрами не создадут дубликаты
2. **Частичное сохранение**: Если часть фьючерсов уже существует, сохранятся только новые
3. **Обработка ошибок**: Ошибки сохранения отдельных фьючерсов не прерывают процесс
4. **Фильтрация**: Применяются те же фильтры, что и в GET /futures

## GET /close-prices

Получение цен закрытия торговой сессии по инструментам из T-Bank API.

### Описание
Эндпоинт возвращает цены закрытия торговой сессии для указанных инструментов. Если инструменты не указаны, автоматически запрашиваются цены для всех инструментов, сохраненных в базе данных (акции и фьючерсы).

### URL
```
GET http://localhost:8083/close-prices
```

### Параметры запроса

| Параметр | Тип | Обязательный | Описание | Возможные значения |
|----------|-----|--------------|----------|-------------------|
| `instrumentId` | List<String> | Нет | Список идентификаторов инструментов (FIGI) | Массив строк с FIGI инструментов |
| `instrumentStatus` | String | Нет | Статус запрашиваемых инструментов | `INSTRUMENT_STATUS_UNSPECIFIED`, `INSTRUMENT_STATUS_BASE`, `INSTRUMENT_STATUS_ALL` |

### Логика работы

1. **С параметрами**: Если переданы `instrumentId`, запрашиваются цены закрытия только для указанных инструментов
2. **Без параметров**: Если `instrumentId` не указан, автоматически получаются все FIGI из таблиц `shares` и `futures` в базе данных
3. **Пустая БД**: Если в базе данных нет инструментов, возвращается пустой список

### Примеры запросов

#### Получить цены закрытия для всех инструментов из БД
```bash
curl "http://localhost:8083/close-prices"
```

#### Получить цены закрытия для конкретных инструментов
```bash
curl "http://localhost:8083/close-prices?instrumentId=BBG004730N88&instrumentId=BBG004730ZJ9"
```

#### Получить цены закрытия с определенным статусом
```bash
curl "http://localhost:8083/close-prices?instrumentStatus=INSTRUMENT_STATUS_BASE"
```

#### Postman примеры

**Запрос 1: Все инструменты из БД**
- Method: `GET`
- URL: `http://localhost:8083/close-prices`
- Headers: `Content-Type: application/json`

**Запрос 2: Конкретные инструменты**
- Method: `GET`
- URL: `http://localhost:8083/close-prices?instrumentId=BBG004730N88&instrumentId=BBG004730ZJ9`
- Headers: `Content-Type: application/json`

**Запрос 3: С фильтром по статусу**
- Method: `GET`
- URL: `http://localhost:8083/close-prices?instrumentStatus=INSTRUMENT_STATUS_BASE`
- Headers: `Content-Type: application/json`

### Формат ответа

```json
[
  {
    "figi": "BBG004730N88",
    "tradingDate": "2024-01-15",
    "closePrice": 250.75,
    "eveningSessionPrice": 251.20
  },
  {
    "figi": "BBG004730ZJ9", 
    "tradingDate": "2024-01-15",
    "closePrice": 125.50,
    "eveningSessionPrice": null
  }
]
```

### Поля ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `figi` | String | Уникальный идентификатор инструмента |
| `tradingDate` | String | Дата торговой сессии (YYYY-MM-DD) |
| `closePrice` | BigDecimal | Цена закрытия торговой сессии |
| `eveningSessionPrice` | BigDecimal | Цена закрытия вечерней сессии (может быть null) |

### Возможные статусы инструментов

| Статус | Описание |
|--------|----------|
| `INSTRUMENT_STATUS_UNSPECIFIED` | Значение не определено (по умолчанию) |
| `INSTRUMENT_STATUS_BASE` | Базовый список инструментов, доступных для торговли через T-Invest API |
| `INSTRUMENT_STATUS_ALL` | Список всех инструментов |

### Особенности

1. **Автоматическое получение инструментов**: При отсутствии параметра `instrumentId` система автоматически получает все FIGI из БД
2. **Объединение данных**: Запрашиваются цены как для акций, так и для фьючерсов
3. **Обработка пустых результатов**: Если в БД нет инструментов, возвращается пустой массив
4. **Валидация статуса**: Некорректные значения статуса заменяются на `INSTRUMENT_STATUS_BASE`

## POST /close-prices

Сохранение цен закрытия торговой сессии в базу данных.

### Описание
Эндпоинт запрашивает цены закрытия для указанных инструментов (или всех инструментов в рублях из БД) и сохраняет их в таблицу `invest.close_prices`. Цены обновляются только если нет аналогичной записи с такой же `figi` + `price_date`.

### URL
```
POST http://localhost:8083/close-prices
```

### Тело запроса

```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"]
}
```

### Параметры запроса

| Параметр | Тип | Обязательный | Описание |
|----------|-----|--------------|----------|
| `instruments` | List<String> | Нет | Список идентификаторов инструментов (FIGI). Если не указан, используются все инструменты в рублях из БД |

### Логика работы

1. **С параметрами**: Если передан массив `instruments`, запрашиваются цены закрытия только для указанных инструментов
2. **Без параметров**: Если `instruments` не указан или пуст, автоматически получаются только FIGI инструментов в рублях из таблиц `shares` и `futures` в базе данных
3. **Пустая БД**: Если в базе данных нет инструментов в рублях, возвращается сообщение об ошибке
4. **Проверка дубликатов**: Цены сохраняются только если нет записи с такой же `figi` + `price_date`
5. **Определение типа**: Автоматически определяется тип инструмента (`SHARE` или `FUTURE`) и получается дополнительная информация (валюта, биржа)

### Примеры запросов

#### Сохранить цены закрытия для всех инструментов в рублях из БД
```bash
curl -X POST "http://localhost:8083/close-prices" \
  -H "Content-Type: application/json" \
  -d '{}'
```

Или без тела запроса:
```bash
curl -X POST "http://localhost:8083/close-prices" \
  -H "Content-Type: application/json"
```

#### Сохранить цены закрытия для конкретных инструментов
```bash
curl -X POST "http://localhost:8083/close-prices" \
  -H "Content-Type: application/json" \
  -d '{
    "instruments": ["BBG004730N88", "BBG004730ZJ9"]
  }'
```

#### Postman примеры

**Запрос 1: Все инструменты в рублях из БД**
- Method: `POST`
- URL: `http://localhost:8083/close-prices`
- Headers: `Content-Type: application/json`
- Body (raw JSON) - можно оставить пустым или указать:
```json
{}
```

**Запрос 2: Конкретные инструменты**
- Method: `POST`
- URL: `http://localhost:8083/close-prices`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"]
}
```

### Формат ответа

```json
{
  "success": true,
  "message": "Успешно загружено 15 новых цен закрытия из 20 найденных.",
  "totalRequested": 20,
  "newItemsSaved": 15,
  "existingItemsSkipped": 5,
  "savedItems": [
    {
      "figi": "BBG004730N88",
      "tradingDate": "2024-01-15",
      "closePrice": 250.75,
      "eveningSessionPrice": 251.20
    },
    {
      "figi": "BBG004730ZJ9",
      "tradingDate": "2024-01-15", 
      "closePrice": 125.50,
      "eveningSessionPrice": null
    }
  ]
}
```

### Поля ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `success` | Boolean | Успешность операции |
| `message` | String | Описательное сообщение о результате |
| `totalRequested` | Integer | Общее количество запрошенных цен |
| `newItemsSaved` | Integer | Количество новых цен, сохраненных в БД |
| `existingItemsSkipped` | Integer | Количество цен, которые уже существовали в БД |
| `savedItems` | List<ClosePriceDto> | Список сохраненных цен закрытия |

### Возможные сообщения

| Сообщение | Описание |
|-----------|----------|
| `"Успешно загружено X новых цен закрытия из Y найденных."` | Успешная загрузка с указанием количества |
| `"Новых цен закрытия не обнаружено. Все найденные цены уже существуют в базе данных."` | Все цены уже были в БД |
| `"Новых цен закрытия не обнаружено. По заданным инструментам цены не найдены."` | API не вернул цены для указанных инструментов |
| `"Нет инструментов в рублях для загрузки цен закрытия. В базе данных нет акций или фьючерсов с валютой RUB."` | В БД нет инструментов в рублях для загрузки |

### Структура таблицы close_prices

```sql
CREATE TABLE close_prices (
    price_date      DATE                                    NOT NULL,
    figi            VARCHAR(255)                            NOT NULL,
    instrument_type VARCHAR(255)                            NOT NULL,
    close_price     NUMERIC(18, 9)                          NOT NULL,
    currency        VARCHAR(255)                            NOT NULL,
    exchange        VARCHAR(255)                            NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()  NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()  NOT NULL,
    PRIMARY KEY (price_date, figi)
);
```

### Особенности

1. **Автоматическое получение инструментов**: При отсутствии параметра `instruments` система автоматически получает только FIGI инструментов в рублях из БД
2. **Объединение данных**: Запрашиваются цены как для акций, так и для фьючерсов
3. **Проверка дубликатов**: Цены сохраняются только если нет записи с такой же `figi` + `price_date`
4. **Автоматическое определение типа**: Система автоматически определяет `instrument_type` (`SHARE` или `FUTURE`)
5. **Обработка ошибок**: Ошибки сохранения отдельных цен не прерывают процесс
6. **Идемпотентность**: Повторные вызовы с теми же параметрами не создадут дубликаты

## Автоматический шедулер цен закрытия

### Описание
Сервис включает в себя автоматический шедулер, который ежедневно загружает цены закрытия для всех инструментов в рублях.

### Расписание
- **Время запуска**: 01:00 по московскому времени (MSK)
- **Частота**: Ежедневно
- **Cron выражение**: `0 0 1 * * *` (Europe/Moscow)

### Логика работы

1. **Автоматический запуск**: Шедулер запускается каждый день в 01:00 МСК
2. **Загрузка за предыдущий день**: Запрашиваются цены закрытия за предыдущий торговый день
3. **Фильтрация по валюте**: Загружаются только инструменты, торгующиеся в рублях (RUB)
4. **Проверка дубликатов**: Цены сохраняются только если нет записи с такой же `figi` + `price_date`
5. **Подробное логирование**: Все операции логируются с детальной информацией

### Логи шедулера

#### Успешное выполнение:
```
Starting scheduled close prices fetch for 2024-01-15
Scheduled close prices fetch completed successfully:
- Date: 2024-01-15
- Message: Успешно загружено 150 новых цен закрытия из 200 найденных.
- Total requested: 200
- New items saved: 150
- Existing items skipped: 50
```

#### Ошибка выполнения:
```
Starting scheduled close prices fetch for 2024-01-15
Scheduled close prices fetch failed: Нет инструментов в рублях для загрузки цен закрытия. В базе данных нет акций или фьючерсов с валютой RUB.
```

### Ручное управление

Для ручного запуска загрузки цен закрытия доступен метод:
```java
public void fetchAndUpdateClosePricesForDate(LocalDate date)
```

### Особенности

1. **Временная зона**: Все операции выполняются в московском времени
2. **Обработка ошибок**: Ошибки не прерывают работу приложения, только логируются
3. **Идемпотентность**: Повторные запуски не создают дубликаты
4. **Автоматическая фильтрация**: Загружаются только RUB инструменты
5. **Интеграция с API**: Использует тот же эндпоинт `POST /close-prices`

### Связанные эндпоинты

- `GET /shares` - получение списка акций без сохранения в БД
- `POST /shares` - сохранение акций в БД
- `GET /futures` - получение списка фьючерсов без сохранения в БД
- `POST /futures` - сохранение фьючерсов в БД
- `GET /close-prices` - получение цен закрытия без сохранения в БД
- `POST /close-prices` - сохранение цен закрытия в БД (используется шедулером)
- `GET /accounts` - получение списка счетов
- `GET /trading-schedules` - получение расписаний торгов
- `GET /trading-statuses` - получение статусов торговли
