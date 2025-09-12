# Investment Data Loader Service API Documentation

## Настройка и запуск

### Требования
- Java 21+
- PostgreSQL 12+
- Токен Tinkoff Invest API

### Настройка токена Tinkoff Invest API

#### Способ 1: Через .env файл (рекомендуется)

1. Скопируйте файл с примером конфигурации:
   ```bash
   cp env.example .env
   ```

2. Отредактируйте файл `.env` и укажите ваш токен:
   ```bash
   T_INVEST_TOKEN=ваш_реальный_токен_здесь
   ```

3. Запустите приложение:
   ```bash
   mvn spring-boot:run
   ```

#### Способ 2: Через переменные окружения системы

```bash
# Windows
set T_INVEST_TOKEN=ваш_токен_здесь
mvn spring-boot:run

# Linux/Mac
export T_INVEST_TOKEN=ваш_токен_здесь
mvn spring-boot:run
```

### Получение токена

1. Зарегистрируйтесь на [Tinkoff Invest API](https://developer.tbank.ru/invest/intro/intro)
2. Создайте приложение в личном кабинете
3. Скопируйте токен и добавьте его в `.env` файл

### ⚠️ Безопасность

**ВАЖНО**: Никогда не коммитьте файл `.env` в репозиторий! Файл уже добавлен в `.gitignore`.

---

## API Endpoints

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
Эндпоинт получает акции из T-Bank API, фильтрует их по заданным параметрам, проверяет существование в базе данных и сохраняет только те акции, которых еще нет в таблице `invest.shares`.

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
2. **Проверка существования**: Для каждой акции проверяет наличие в таблице `invest.shares` по `figi`
3. **Сохранение**: Сохраняет только те акции, которых нет в базе данных
4. **Возврат результата**: Возвращает список только сохраненных акций

### Схема таблицы shares

```sql
CREATE TABLE invest.shares (
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
Эндпоинт получает фьючерсы из T-Bank API, фильтрует их по заданным параметрам, проверяет существование в базе данных и сохраняет только те фьючерсы, которых еще нет в таблице `invest.futures`.

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
CREATE TABLE invest.futures (
    figi         varchar(255) not null primary key,
    ticker       varchar(255) not null,
    asset_type   varchar(255) not null,
    basic_asset  varchar(255),
    currency     varchar(255) not null,
    exchange     varchar(255) not null,
    stock_ticker varchar(255)
        constraint fk_stock_ticker
            references invest.shares (ticker)
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
CREATE TABLE invest.close_prices (
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

### Структура таблицы open_prices

```sql
CREATE TABLE invest.open_prices (
    price_date      DATE                                    NOT NULL,
    figi            VARCHAR(255)                            NOT NULL,
    instrument_type VARCHAR(255)                            NOT NULL,
    open_price      NUMERIC(18, 9)                          NOT NULL,
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

## POST /candles

Асинхронное получение и сохранение исторических свечей по инструментам в базу данных.

### Описание
Эндпоинт запрашивает исторические свечи для указанных инструментов за указанную дату и сохраняет их в таблицу `invest.candles`. Процесс выполняется асинхронно с соблюдением лимитов API T-Bank.

### URL
```
POST http://localhost:8083/candles
```

### Тело запроса

```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"],
  "date": "2024-01-15",
  "interval": "CANDLE_INTERVAL_1_MIN",
  "assetType": ["SHARES", "FUTURES"]
}
```

### Параметры запроса

| Параметр | Тип | Обязательный | Описание | Возможные значения |
|----------|-----|--------------|----------|-------------------|
| `instruments` | List<String> | Нет | Список идентификаторов инструментов (FIGI). Если не указан, используются все инструменты из БД | Массив строк с FIGI инструментов |
| `date` | String (LocalDate) | Нет | Дата для получения свечей (YYYY-MM-DD). Если не указана, используется вчерашний день | Дата в формате YYYY-MM-DD |
| `interval` | String | Нет | Интервал свечей. Если не указан, используется 1 минута | `CANDLE_INTERVAL_1_MIN`, `CANDLE_INTERVAL_5_MIN`, `CANDLE_INTERVAL_15_MIN`, `CANDLE_INTERVAL_HOUR`, `CANDLE_INTERVAL_DAY` и др. |
| `assetType` | List<String> | Нет | Типы активов для загрузки. Если не указан, загружаются все типы инструментов | `["SHARES"]`, `["FUTURES"]`, `["SHARES", "FUTURES"]` |

### Логика работы

1. **С параметрами**: Если переданы `instruments`, запрашиваются свечи только для указанных инструментов
2. **Без параметров**: Если `instruments` не указан, автоматически получаются FIGI из базы данных:
   - Если указан `assetType` с `"SHARES"` - загружаются только акции из таблицы `invest.shares`
   - Если указан `assetType` с `"FUTURES"` - загружаются только фьючерсы из таблицы `invest.futures`
   - Если указаны оба типа `["SHARES", "FUTURES"]` - загружаются и акции, и фьючерсы
   - Если `assetType` не указан - загружаются все инструменты (акции и фьючерсы)
3. **Дата по умолчанию**: Если `date` не указана, используется вчерашний день
4. **Интервал по умолчанию**: Если `interval` не указан, используется `CANDLE_INTERVAL_1_MIN`
5. **Соблюдение лимитов**: Между запросами к API добавляются задержки (100мс между запросами, 200мс между инструментами)
6. **Проверка дубликатов**: Свечи сохраняются только если нет записи с такой же `figi` + `time`

### Примеры запросов

#### Сохранить свечи за вчерашний день для всех инструментов из БД
```bash
curl -X POST "http://localhost:8083/candles" \
  -H "Content-Type: application/json" \
  -d '{}'
```

#### Сохранить свечи за конкретную дату для всех инструментов
```bash
curl -X POST "http://localhost:8083/candles" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-01-15"
  }'
```

#### Сохранить свечи для конкретных инструментов
```bash
curl -X POST "http://localhost:8083/candles" \
  -H "Content-Type: application/json" \
  -d '{
    "instruments": ["BBG004730N88", "BBG004730ZJ9"],
    "date": "2024-01-15",
    "interval": "CANDLE_INTERVAL_1_MIN"
  }'
```

#### Сохранить 5-минутные свечи
```bash
curl -X POST "http://localhost:8083/candles" \
  -H "Content-Type: application/json" \
  -d '{
    "instruments": ["BBG004730N88"],
    "date": "2024-01-15",
    "interval": "CANDLE_INTERVAL_5_MIN"
  }'
```

#### Сохранить свечи только для акций
```bash
curl -X POST "http://localhost:8083/candles" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-01-15",
    "assetType": ["SHARES"]
  }'
```

#### Сохранить свечи только для фьючерсов
```bash
curl -X POST "http://localhost:8083/candles" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-01-15",
    "assetType": ["FUTURES"]
  }'
```

#### Сохранить свечи для акций и фьючерсов
```bash
curl -X POST "http://localhost:8083/candles" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-01-15",
    "assetType": ["SHARES", "FUTURES"]
  }'
```

### Формат ответа

```json
{
  "success": true,
  "message": "Успешно загружено 1440 новых свечей из 1440 найденных.",
  "totalRequested": 1440,
  "newItemsSaved": 1440,
  "existingItemsSkipped": 0,
  "savedItems": [
    {
      "figi": "BBG004730N88",
      "volume": 1000000,
      "high": 250.75,
      "low": 248.50,
      "time": "2024-01-15T09:00:00Z",
      "close": 250.25,
      "open": 249.00,
      "isComplete": true
    }
  ]
}
```

### Поля ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `success` | Boolean | Успешность операции |
| `message` | String | Описательное сообщение о результате |
| `totalRequested` | Integer | Общее количество запрошенных свечей |
| `newItemsSaved` | Integer | Количество новых свечей, сохраненных в БД |
| `existingItemsSkipped` | Integer | Количество свечей, которые уже существовали в БД |
| `savedItems` | List<CandleDto> | Список сохраненных свечей |

### Поля CandleDto

| Поле | Тип | Описание |
|------|-----|----------|
| `figi` | String | Уникальный идентификатор инструмента |
| `volume` | Long | Объем торгов |
| `high` | BigDecimal | Максимальная цена за период |
| `low` | BigDecimal | Минимальная цена за период |
| `time` | Instant | Время свечи в формате ISO 8601 (московское время UTC+3) |
| `close` | BigDecimal | Цена закрытия |
| `open` | BigDecimal | Цена открытия |
| `isComplete` | Boolean | Завершена ли свеча |

### Возможные интервалы свечей

| Интервал | Описание | Максимальный лимит |
|----------|----------|-------------------|
| `CANDLE_INTERVAL_1_MIN` | 1 минута | 2400 |
| `CANDLE_INTERVAL_5_MIN` | 5 минут | 2400 |
| `CANDLE_INTERVAL_15_MIN` | 15 минут | 2400 |
| `CANDLE_INTERVAL_HOUR` | 1 час | 2400 |
| `CANDLE_INTERVAL_DAY` | 1 день | 2400 |
| `CANDLE_INTERVAL_2_MIN` | 2 минуты | 1200 |
| `CANDLE_INTERVAL_3_MIN` | 3 минуты | 750 |
| `CANDLE_INTERVAL_10_MIN` | 10 минут | 1200 |
| `CANDLE_INTERVAL_30_MIN` | 30 минут | 1200 |
| `CANDLE_INTERVAL_2_HOUR` | 2 часа | 2400 |
| `CANDLE_INTERVAL_4_HOUR` | 4 часа | 700 |
| `CANDLE_INTERVAL_WEEK` | 1 неделя | 300 |
| `CANDLE_INTERVAL_MONTH` | 1 месяц | 120 |

### Структура таблицы candles

```sql
CREATE TABLE invest.candles (
    figi        VARCHAR(255)                           NOT NULL,
    volume      BIGINT                                 NOT NULL,
    high        NUMERIC(18, 9)                         NOT NULL,
    low         NUMERIC(18, 9)                         NOT NULL,
    time        TIMESTAMP WITH TIME ZONE               NOT NULL,
    close       NUMERIC(18, 9)                         NOT NULL,
    open        NUMERIC(18, 9)                         NOT NULL,
    is_complete BOOLEAN                                NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    PRIMARY KEY (figi, time)
);
```

### Особенности

1. **Асинхронность**: Операция выполняется асинхронно, возвращается CompletableFuture
2. **Соблюдение лимитов**: Автоматические задержки между запросами для соблюдения лимитов T-Bank API
3. **Автоматическое получение инструментов**: При отсутствии параметра `instruments` система автоматически получает все FIGI из таблиц `invest.shares` и `invest.futures`
4. **Проверка дубликатов**: Свечи сохраняются только если нет записи с такой же `figi` + `time`
5. **Обработка ошибок**: Ошибки сохранения отдельных свечей не прерывают процесс
6. **Идемпотентность**: Повторные вызовы с теми же параметрами не создадут дубликаты
7. **Временная зона**: Все времена сохраняются в московском времени (UTC+3)

### Обработка временных зон

- **Входящие данные**: T-Bank API возвращает время в UTC
- **Конвертация**: Система автоматически конвертирует UTC время в московское время (UTC+3) при сохранении
- **Хранение**: Все времена в базе данных хранятся в московской временной зоне
- **Отображение**: Время в ответах API отображается в московском времени

### Коды ответов

| Код | Описание |
|-----|----------|
| 200 | Успешное выполнение запроса |
| 400 | Некорректные параметры запроса |
| 500 | Внутренняя ошибка сервера |

### Связанные эндпоинты

- `GET /shares` - получение списка акций без сохранения в БД
- `POST /shares` - сохранение акций в БД
- `GET /futures` - получение списка фьючерсов без сохранения в БД
- `POST /futures` - сохранение фьючерсов в БД
- `GET /indicatives` - получение списка индикативных инструментов без сохранения в БД
- `POST /indicatives` - сохранение индикативных инструментов в БД
- `GET /close-prices` - получение цен закрытия без сохранения в БД
- `POST /close-prices` - сохранение цен закрытия в БД (используется шедулером)
- `POST /candles` - получение и сохранение исторических свечей в БД
- `GET /accounts` - получение списка счетов
- `GET /trading-schedules` - получение расписаний торгов
- `GET /trading-statuses` - получение статусов торговли

---

## GET /indicatives

Получение списка индикативных инструментов (индексы, товары и другие) из T-Bank API без сохранения в базу данных.

### Описание
Эндпоинт получает индикативные инструменты из T-Bank API согласно документации: [https://developer.tbank.ru/invest/services/instruments/methods](https://developer.tbank.ru/invest/services/instruments/methods)

### URL
```
GET http://localhost:8083/indicatives
```

### Параметры запроса

| Параметр | Тип | Обязательный | Описание | Возможные значения |
|----------|-----|--------------|----------|-------------------|
| `exchange` | String | Нет | Биржа | `moex_mrng_evng_e_wknd_dlr`, `SPB`, `NASDAQ`, `NYSE`, и др. |
| `currency` | String | Нет | Валюта инструмента | `RUB`, `USD`, `EUR`, и др. |
| `ticker` | String | Нет | Тикер инструмента | `IMOEX`, `RTSI`, `SILV`, `GOLD`, и др. |
| `figi` | String | Нет | FIGI инструмента | `BBG004730N88`, `BBG00QPYJ5X0`, и др. |

### Примеры запросов

#### Получить все индикативные инструменты
```bash
curl "http://localhost:8083/indicatives"
```

#### Получить индикативные инструменты с биржи MOEX
```bash
curl "http://localhost:8083/indicatives?exchange=moex_mrng_evng_e_wknd_dlr"
```

#### Получить индикативные инструменты в рублях
```bash
curl "http://localhost:8083/indicatives?currency=RUB"
```

#### Получить индикативный инструмент по тикеру
```bash
curl "http://localhost:8083/indicatives?ticker=IMOEX"
```

#### Получить индикативный инструмент по FIGI
```bash
curl "http://localhost:8083/indicatives?figi=BBG00QPYJ5X0"
```

#### Комбинированный запрос
```bash
curl "http://localhost:8083/indicatives?exchange=moex_mrng_evng_e_wknd_dlr&currency=RUB&ticker=IMOEX"
```

### Примеры для Postman

#### 1. Получить все индикативные инструменты
- **Method**: `GET`
- **URL**: `http://localhost:8083/indicatives`
- **Headers**: Не требуются

#### 2. Получить индикативные инструменты с биржи MOEX
- **Method**: `GET`
- **URL**: `http://localhost:8083/indicatives`
- **Query Parameters**:
  - `exchange`: `moex_mrng_evng_e_wknd_dlr`
- **Headers**: Не требуются

#### 3. Получить индикативные инструменты в рублях
- **Method**: `GET`
- **URL**: `http://localhost:8083/indicatives`
- **Query Parameters**:
  - `currency`: `RUB`
- **Headers**: Не требуются

#### 4. Получить индикативный инструмент по тикеру
- **Method**: `GET`
- **URL**: `http://localhost:8083/indicatives`
- **Query Parameters**:
  - `ticker`: `IMOEX`
- **Headers**: Не требуются

#### 5. Получить индикативный инструмент по FIGI
- **Method**: `GET`
- **URL**: `http://localhost:8083/indicatives`
- **Query Parameters**:
  - `figi`: `BBG00QPYJ5X0`
- **Headers**: Не требуются

#### 6. Комбинированный запрос
- **Method**: `GET`
- **URL**: `http://localhost:8083/indicatives`
- **Query Parameters**:
  - `exchange`: `moex_mrng_evng_e_wknd_dlr`
  - `currency`: `RUB`
  - `ticker`: `IMOEX`
- **Headers**: Не требуются

### Формат ответа

Возвращает массив индикативных инструментов:

```json
[
  {
    "figi": "BBG00QPYJ5X0",
    "ticker": "IMOEX",
    "name": "Индекс МосБиржи",
    "currency": "RUB",
    "exchange": "moex_mrng_evng_e_wknd_dlr",
    "classCode": "SPBXM",
    "uid": "e6123145-9665-43e0-8413-cd61d8e6e372",
    "sellAvailableFlag": true,
    "buyAvailableFlag": true
  },
  {
    "figi": "BBG00QPYJ5X1",
    "ticker": "RTSI",
    "name": "Индекс РТС",
    "currency": "RUB",
    "exchange": "moex_mrng_evng_e_wknd_dlr",
    "classCode": "SPBXM",
    "uid": "e6123145-9665-43e0-8413-cd61d8e6e373",
    "sellAvailableFlag": true,
    "buyAvailableFlag": true
  }
]
```

### Описание полей ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `figi` | String | FIGI инструмента |
| `ticker` | String | Тикер инструмента |
| `name` | String | Название инструмента |
| `currency` | String | Валюта инструмента |
| `exchange` | String | Биржа |
| `classCode` | String | Класс инструмента |
| `uid` | String | Уникальный идентификатор |
| `sellAvailableFlag` | Boolean | Доступность для продажи |
| `buyAvailableFlag` | Boolean | Доступность для покупки |

### Коды ответов

| Код | Описание |
|-----|----------|
| 200 | Успешный запрос |
| 400 | Некорректные параметры запроса |
| 500 | Внутренняя ошибка сервера |

---

## POST /indicatives

Сохранение индикативных инструментов в базу данных. Метод запрашивает список индикативных инструментов из T-Bank API согласно параметрам фильтрации, проверяет их наличие в таблице `invest.indicatives` и сохраняет только новые инструменты.

### Описание
Эндпоинт получает индикативные инструменты из T-Bank API, фильтрует их по заданным параметрам, проверяет существование в базе данных и сохраняет только те инструменты, которых еще нет в таблице `invest.indicatives`.

### URL
```
POST http://localhost:8083/indicatives
```

### Тело запроса

Параметры передаются в теле запроса в формате JSON:

```json
{
  "exchange": "moex_mrng_evng_e_wknd_dlr",
  "currency": "RUB",
  "ticker": "IMOEX"
}
```

### Параметры тела запроса

| Параметр | Тип | Обязательный | Описание | Возможные значения |
|----------|-----|--------------|----------|-------------------|
| `exchange` | String | Нет | Биржа | `moex_mrng_evng_e_wknd_dlr`, `SPB`, `NASDAQ`, `NYSE`, и др. |
| `currency` | String | Нет | Валюта инструмента | `RUB`, `USD`, `EUR`, и др. |
| `ticker` | String | Нет | Тикер инструмента | `IMOEX`, `RTSI`, `SILV`, `GOLD`, и др. |
| `figi` | String | Нет | FIGI инструмента | `BBG00QPYJ5X0`, `BBG00QPYJ5X1`, и др. |

### Примеры запросов

#### Сохранить все индикативные инструменты
```bash
curl -X POST "http://localhost:8083/indicatives" \
  -H "Content-Type: application/json" \
  -d '{}'
```

#### Сохранить индикативные инструменты с биржи MOEX
```bash
curl -X POST "http://localhost:8083/indicatives" \
  -H "Content-Type: application/json" \
  -d '{
    "exchange": "moex_mrng_evng_e_wknd_dlr"
  }'
```

#### Сохранить индикативные инструменты в рублях
```bash
curl -X POST "http://localhost:8083/indicatives" \
  -H "Content-Type: application/json" \
  -d '{
    "currency": "RUB"
  }'
```

#### Сохранить индикативный инструмент по тикеру
```bash
curl -X POST "http://localhost:8083/indicatives" \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "IMOEX"
  }'
```

#### Комбинированный запрос
```bash
curl -X POST "http://localhost:8083/indicatives" \
  -H "Content-Type: application/json" \
  -d '{
    "exchange": "moex_mrng_evng_e_wknd_dlr",
    "currency": "RUB",
    "ticker": "IMOEX"
  }'
```

### Примеры для Postman

#### 1. Сохранить все индикативные инструменты
- **Method**: `POST`
- **URL**: `http://localhost:8083/indicatives`
- **Headers**: Не требуются
- **Body**: Не требуется

#### 2. Сохранить индикативные инструменты с биржи MOEX
- **Method**: `POST`
- **URL**: `http://localhost:8083/indicatives`
- **Headers**: Не требуются
- **Body**:
```json
{
  "exchange": "moex_mrng_evng_e_wknd_dlr"
}
```

#### 3. Сохранить индикативные инструменты в рублях
- **Method**: `POST`
- **URL**: `http://localhost:8083/indicatives`
- **Headers**: Не требуются
- **Body**:
```json
{
  "currency": "RUB"
}
```

#### 4. Сохранить индикативный инструмент по тикеру
- **Method**: `POST`
- **URL**: `http://localhost:8083/indicatives`
- **Headers**: Не требуются
- **Body**:
```json
{
  "ticker": "IMOEX"
}
```

#### 5. Комбинированный запрос
- **Method**: `POST`
- **URL**: `http://localhost:8083/indicatives`
- **Headers**: Не требуются
- **Body**:
```json
{
  "exchange": "moex_mrng_evng_e_wknd_dlr",
  "currency": "RUB",
  "ticker": "IMOEX"
}
```

### Формат ответа

Возвращает информативный ответ о результате сохранения:

```json
{
  "success": true,
  "message": "Успешно загружено 3 новых индикативных инструментов из 5 найденных.",
  "totalRequested": 5,
  "newItemsSaved": 3,
  "existingItemsSkipped": 2,
  "savedItems": [
    {
      "figi": "BBG00QPYJ5X0",
      "ticker": "IMOEX",
      "name": "Индекс МосБиржи",
      "currency": "RUB",
      "exchange": "moex_mrng_evng_e_wknd_dlr",
      "classCode": "SPBXM",
      "uid": "e6123145-9665-43e0-8413-cd61d8e6e372",
      "sellAvailableFlag": true,
      "buyAvailableFlag": true
    }
  ]
}
```

### Описание полей ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `success` | Boolean | Успешность операции (true, если найдены индикативные инструменты по фильтрам) |
| `message` | String | Информативное сообщение о результате операции |
| `totalRequested` | Integer | Общее количество найденных инструментов по фильтрам |
| `newItemsSaved` | Integer | Количество новых инструментов, сохраненных в БД |
| `existingItemsSkipped` | Integer | Количество инструментов, которые уже существовали в БД |
| `savedItems` | Array | Массив сохраненных индикативных инструментов |

### Коды ответов

| Код | Описание |
|-----|----------|
| 200 | Успешное сохранение |
| 400 | Некорректные параметры запроса |
| 500 | Внутренняя ошибка сервера |

### Связанные эндпоинты

- `GET /indicatives` - получение списка индикативных инструментов без сохранения в БД
- `GET /indicatives/{figi}` - получение индикативного инструмента по FIGI
- `GET /indicatives/ticker/{ticker}` - получение индикативного инструмента по тикеру
- `POST /indicatives` - сохранение индикативных инструментов в БД
- `GET /shares` - получение списка акций без сохранения в БД
- `POST /shares` - сохранение акций в БД
- `GET /futures` - получение списка фьючерсов без сохранения в БД
- `POST /futures` - сохранение фьючерсов в БД
- `GET /close-prices` - получение цен закрытия без сохранения в БД
- `POST /close-prices` - сохранение цен закрытия в БД (используется шедулером)
- `POST /candles` - получение и сохранение исторических свечей в БД
- `GET /accounts` - получение списка счетов
- `GET /trading-schedules` - получение расписаний торгов
- `GET /trading-statuses` - получение статусов торговли

---

## GET /indicatives/ticker/{ticker}

Получение индикативного инструмента по его тикеру.

### Описание
Эндпоинт получает конкретный индикативный инструмент из T-Bank API по его тикеру. Удобный метод для быстрого поиска индекса по тикеру (например, IMOEX, RTSI).

### URL
```
GET http://localhost:8083/indicatives/ticker/{ticker}
```

### Параметры пути

| Параметр | Тип | Обязательный | Описание | Пример |
|----------|-----|--------------|----------|--------|
| `ticker` | String | Да | Тикер инструмента | `IMOEX` |

### Примеры запросов

#### Получить индекс МосБиржи по тикеру
```bash
curl "http://localhost:8083/indicatives/ticker/IMOEX"
```

#### Получить индекс РТС по тикеру
```bash
curl "http://localhost:8083/indicatives/ticker/RTSI"
```

### Примеры для Postman

#### 1. Получить индекс МосБиржи
- **Method**: `GET`
- **URL**: `http://localhost:8083/indicatives/ticker/IMOEX`
- **Headers**: Не требуются

#### 2. Получить индекс РТС
- **Method**: `GET`
- **URL**: `http://localhost:8083/indicatives/ticker/RTSI`
- **Headers**: Не требуются

### Формат ответа

Возвращает объект индикативного инструмента:

```json
{
  "figi": "BBG00QPYJ5X0",
  "ticker": "IMOEX",
  "name": "Индекс МосБиржи",
  "currency": "RUB",
  "exchange": "moex_mrng_evng_e_wknd_dlr",
  "classCode": "SPBXM",
  "uid": "e6123145-9665-43e0-8413-cd61d8e6e372",
  "sellAvailableFlag": true,
  "buyAvailableFlag": true
}
```

### Описание полей ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `figi` | String | FIGI инструмента |
| `ticker` | String | Тикер инструмента |
| `name` | String | Название инструмента |
| `currency` | String | Валюта инструмента |
| `exchange` | String | Биржа |
| `classCode` | String | Код класса инструмента |
| `uid` | String | Уникальный идентификатор |
| `sellAvailableFlag` | Boolean | Доступность для продажи |
| `buyAvailableFlag` | Boolean | Доступность для покупки |

### Коды ответов

| Код | Описание |
|-----|----------|
| 200 | Успешный запрос, инструмент найден |
| 404 | Инструмент с таким тикером не найден |
| 400 | Некорректный тикер |
| 500 | Внутренняя ошибка сервера |

### Особенности

- Поиск выполняется без учета регистра (case-insensitive)
- Возвращается первый найденный инструмент с указанным тикером
- Если найдено несколько инструментов с одинаковым тикером, возвращается первый

---

## GET /indicatives/{figi}

Получение индикативного инструмента по его FIGI идентификатору.

### Описание
Эндпоинт получает конкретный индикативный инструмент из T-Bank API по его FIGI идентификатору. Аналог метода `ShareBy` для индикативных инструментов.

### URL
```
GET http://localhost:8083/indicatives/{figi}
```

### Параметры пути

| Параметр | Тип | Обязательный | Описание | Пример |
|----------|-----|--------------|----------|--------|
| `figi` | String | Да | FIGI идентификатор инструмента | `BBG00QPYJ5X0` |

### Примеры запросов

#### Получить индикативный инструмент по FIGI
```bash
curl "http://localhost:8083/indicatives/BBG00QPYJ5X0"
```

### Примеры для Postman

#### 1. Получить индикативный инструмент по FIGI
- **Method**: `GET`
- **URL**: `http://localhost:8083/indicatives/BBG00QPYJ5X0`
- **Headers**: Не требуются

### Формат ответа

Возвращает объект индикативного инструмента:

```json
{
  "figi": "BBG00QPYJ5X0",
  "ticker": "IMOEX",
  "name": "Индекс МосБиржи",
  "currency": "RUB",
  "exchange": "moex_mrng_evng_e_wknd_dlr",
  "classCode": "SPBXM",
  "uid": "e6123145-9665-43e0-8413-cd61d8e6e372",
  "sellAvailableFlag": true,
  "buyAvailableFlag": true
}
```

### Описание полей ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `figi` | String | FIGI инструмента |
| `ticker` | String | Тикер инструмента |
| `name` | String | Название инструмента |
| `currency` | String | Валюта инструмента |
| `exchange` | String | Биржа |
| `classCode` | String | Код класса инструмента |
| `uid` | String | Уникальный идентификатор |
| `sellAvailableFlag` | Boolean | Доступность для продажи |
| `buyAvailableFlag` | Boolean | Доступность для покупки |

### Коды ответов

| Код | Описание |
|-----|----------|
| 200 | Успешный запрос, инструмент найден |
| 404 | Инструмент не найден |
| 400 | Некорректный FIGI |
| 500 | Внутренняя ошибка сервера |

---

## Админские эндпоинты

Эндпоинты для административного управления загрузкой данных.

### POST /admin/load-close-prices

Ручной запуск загрузки цен закрытия за вчерашний день.

#### URL
```
POST http://localhost:8083/admin/load-close-prices
```

#### Ответ
```json
"Close prices loaded for today"
```

### POST /admin/load-candles

Ручной запуск загрузки свечей за вчерашний день (акции + фьючерсы).

#### URL
```
POST http://localhost:8083/admin/load-candles
```

#### Ответ
```json
"Candles loading started for today"
```

### POST /admin/load-candles/{date}

Ручной запуск загрузки свечей за указанную дату (акции + фьючерсы).

#### URL
```
POST http://localhost:8083/admin/load-candles/2024-01-15
```

#### Параметры
- `date` - дата в формате YYYY-MM-DD

#### Ответ
```json
"Candles loading started for 2024-01-15"
```

### POST /admin/load-candles/shares/{date}

Ручной запуск загрузки свечей только для акций за указанную дату.

#### URL
```
POST http://localhost:8083/admin/load-candles/shares/2024-01-15
```

#### Параметры
- `date` - дата в формате YYYY-MM-DD

#### Ответ
```json
"Shares candles loading started for 2024-01-15"
```

### POST /admin/load-candles/futures/{date}

Ручной запуск загрузки свечей только для фьючерсов за указанную дату.

#### URL
```
POST http://localhost:8083/admin/load-candles/futures/2024-01-15
```

#### Параметры
- `date` - дата в формате YYYY-MM-DD

#### Ответ
```json
"Futures candles loading started for 2024-01-15"
```

### POST /admin/load-evening-session-prices

Ручной запуск загрузки цен закрытия вечерней сессии за вчерашний день.

#### URL
```
POST http://localhost:8083/admin/load-evening-session-prices
```

#### Ответ
```json
"Evening session prices loading started for today"
```

### POST /admin/load-evening-session-prices/{date}

Ручной запуск загрузки цен закрытия вечерней сессии за указанную дату.

#### URL
```
POST http://localhost:8083/admin/load-evening-session-prices/2024-01-15
```

#### Параметры
- `date` - дата в формате YYYY-MM-DD

### POST /admin/load-morning-session-prices

Ручной запуск загрузки цен открытия утренней сессии за вчерашний день.

#### URL
```
POST http://localhost:8083/admin/load-morning-session-prices
```

#### Ответ
```json
"Morning session prices loading started for today"
```

### POST /admin/load-morning-session-prices/{date}

Ручной запуск загрузки цен открытия утренней сессии за указанную дату.

#### URL
```
POST http://localhost:8083/admin/load-morning-session-prices/2024-01-15
```

#### Параметры
- `date` - дата в формате YYYY-MM-DD

## Система агрегации данных

Система агрегации данных предназначена для расчета и хранения статистических показателей по объемам торгов для акций и фьючерсов. Агрегированные данные помогают анализировать торговую активность инструментов в различные временные периоды.

### Принципы работы

1. **Источник данных**: Анализируются свечи из таблицы `invest.candles`
2. **Временные периоды**:
   - **Утренняя сессия**: 06:50:00 - 09:59:59 (рабочие дни)
   - **Вечерняя сессия**: 19:00:00 - 23:59:59 (только для фьючерсов, рабочие дни)
   - **Выходные дни**: Суббота и воскресенье
3. **Расчет показателей**: Средние объемы торгов, количество дней
4. **Хранение**: Отдельные таблицы для акций и фьючерсов

### Доступные эндпоинты

- `POST /admin/recalculate-shares-aggregation` - Пересчет данных для акций
- `POST /admin/recalculate-futures-aggregation` - Пересчет данных для фьючерсов  
- `GET /admin/shares-aggregation` - Получение данных по акциям
- `GET /admin/futures-aggregation` - Получение данных по фьючерсам

### POST /admin/recalculate-shares-aggregation

Пересчет агрегированных данных для всех акций.

#### URL
```
POST http://localhost:8083/admin/recalculate-shares-aggregation
```

#### Описание
Рассчитывает и сохраняет агрегированные данные по объемам торгов для всех акций:
- Средний объем торгов с 06:50:00 по 09:59:59 (утренняя сессия)
- Средний объем торгов в выходные дни (суббота и воскресенье)
- Количество торговых и выходных дней

#### Логика работы
1. **Получение данных**: Загружает все акции из таблицы `invest.shares`
2. **Анализ свечей**: Для каждой акции анализирует все свечи из таблицы `invest.candles`
3. **Группировка по дням**: Группирует свечи по торговым дням
4. **Расчет утренних объемов**: Суммирует объемы с 06:50:00 по 09:59:59 (рабочие дни)
5. **Расчет выходных объемов**: Суммирует объемы в субботу и воскресенье
6. **Сохранение**: Обновляет или создает записи в таблице `invest.shares_aggregated_data`

#### Ответ
```json
{
  "taskId": "SHARES_AGGREGATION_12345678",
  "processedInstruments": 150,
  "successfulInstruments": 148,
  "errorInstruments": 2,
  "success": true,
  "errorMessage": null,
  "instrumentType": "shares"
}
```

#### Поля ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `taskId` | String | Уникальный идентификатор задачи |
| `processedInstruments` | Integer | Общее количество обработанных инструментов |
| `successfulInstruments` | Integer | Количество успешно обработанных инструментов |
| `errorInstruments` | Integer | Количество инструментов с ошибками |
| `success` | Boolean | Общий статус выполнения задачи |
| `errorMessage` | String | Сообщение об ошибке (если есть) |
| `instrumentType` | String | Тип инструментов ("shares") |

#### Примеры использования

**Запуск пересчета:**
```bash
curl -X POST "http://localhost:8083/admin/recalculate-shares-aggregation" \
  -H "Content-Type: application/json"
```

**Postman:**
- Method: `POST`
- URL: `http://localhost:8083/admin/recalculate-shares-aggregation`
- Headers: `Content-Type: application/json`

#### Особенности
- **Асинхронность**: Операция выполняется синхронно, но может занять длительное время
- **Подробное логирование**: Все этапы обработки логируются в консоль
- **Обработка ошибок**: Ошибки отдельных инструментов не прерывают общий процесс
- **Идемпотентность**: Повторные вызовы пересчитывают данные заново

### POST /admin/recalculate-futures-aggregation

Пересчет агрегированных данных для всех фьючерсов.

#### URL
```
POST http://localhost:8083/admin/recalculate-futures-aggregation
```

#### Описание
Рассчитывает и сохраняет агрегированные данные по объемам торгов для всех фьючерсов:
- Средний объем торгов с 06:50:00 по 09:59:59 (утренняя сессия)
- Средний объем торгов с 19:00:00 по 23:59:59 (вечерняя сессия)
- Средний объем торгов в выходные дни (суббота и воскресенье)
- Количество торговых и выходных дней

#### Логика работы
1. **Получение данных**: Загружает все фьючерсы из таблицы `invest.futures`
2. **Анализ свечей**: Для каждого фьючерса анализирует все свечи из таблицы `invest.candles`
3. **Группировка по дням**: Группирует свечи по торговым дням
4. **Расчет утренних объемов**: Суммирует объемы с 06:50:00 по 09:59:59 (рабочие дни)
5. **Расчет вечерних объемов**: Суммирует объемы с 19:00:00 по 23:59:59 (рабочие дни)
6. **Расчет выходных объемов**: Суммирует объемы в субботу и воскресенье
7. **Сохранение**: Обновляет или создает записи в таблице `invest.futures_aggregated_data`

#### Ответ
```json
{
  "taskId": "FUTURES_AGGREGATION_87654321",
  "processedInstruments": 50,
  "successfulInstruments": 48,
  "errorInstruments": 2,
  "success": true,
  "errorMessage": null,
  "instrumentType": "futures"
}
```

#### Поля ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `taskId` | String | Уникальный идентификатор задачи |
| `processedInstruments` | Integer | Общее количество обработанных инструментов |
| `successfulInstruments` | Integer | Количество успешно обработанных инструментов |
| `errorInstruments` | Integer | Количество инструментов с ошибками |
| `success` | Boolean | Общий статус выполнения задачи |
| `errorMessage` | String | Сообщение об ошибке (если есть) |
| `instrumentType` | String | Тип инструментов ("futures") |

#### Примеры использования

**Запуск пересчета:**
```bash
curl -X POST "http://localhost:8083/admin/recalculate-futures-aggregation" \
  -H "Content-Type: application/json"
```

**Postman:**
- Method: `POST`
- URL: `http://localhost:8083/admin/recalculate-futures-aggregation`
- Headers: `Content-Type: application/json`

#### Особенности
- **Асинхронность**: Операция выполняется синхронно, но может занять длительное время
- **Подробное логирование**: Все этапы обработки логируются в консоль
- **Обработка ошибок**: Ошибки отдельных инструментов не прерывают общий процесс
- **Идемпотентность**: Повторные вызовы пересчитывают данные заново
- **Вечерняя сессия**: Фьючерсы имеют дополнительную вечернюю сессию (19:00-23:59)

### GET /admin/shares-aggregation

Получение агрегированных данных для акций.

#### URL
```
GET http://localhost:8083/admin/shares-aggregation
```

#### Описание
Возвращает все рассчитанные агрегированные данные по объемам торгов для акций. Данные отсортированы по дате последнего обновления (новые записи первыми).

#### Ответ
```json
[
  {
    "figi": "BBG000B9XRY4",
    "avgVolumeMorning": 1250000.50,
    "avgVolumeWeekend": 85000.25,
    "totalTradingDays": 250,
    "totalWeekendDays": 115,
    "lastCalculated": "2024-01-15T10:30:00+03:00",
    "createdAt": "2024-01-15T10:30:00+03:00",
    "updatedAt": "2024-01-15T10:30:00+03:00"
  }
]
```

#### Поля ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `figi` | String | Уникальный идентификатор инструмента |
| `avgVolumeMorning` | BigDecimal | Средний объем торгов с 06:50:00 по 09:59:59 |
| `avgVolumeWeekend` | BigDecimal | Средний объем торгов в выходные дни |
| `totalTradingDays` | Integer | Общее количество торговых дней |
| `totalWeekendDays` | Integer | Общее количество выходных дней |
| `lastCalculated` | LocalDateTime | Время последнего расчета данных |
| `createdAt` | LocalDateTime | Время создания записи |
| `updatedAt` | LocalDateTime | Время последнего обновления записи |

#### Примеры использования

**Получение всех данных:**
```bash
curl "http://localhost:8083/admin/shares-aggregation"
```

**Postman:**
- Method: `GET`
- URL: `http://localhost:8083/admin/shares-aggregation`
- Headers: Не требуются

#### Особенности
- **Сортировка**: Результаты отсортированы по `updatedAt` в убывающем порядке
- **Полнота данных**: Возвращаются только записи с рассчитанными данными
- **Временная зона**: Все времена в московской временной зоне (UTC+3)

### GET /admin/futures-aggregation

Получение агрегированных данных для фьючерсов.

#### URL
```
GET http://localhost:8083/admin/futures-aggregation
```

#### Описание
Возвращает все рассчитанные агрегированные данные по объемам торгов для фьючерсов. Данные отсортированы по дате последнего обновления (новые записи первыми).

#### Ответ
```json
[
  {
    "figi": "FUTSILV0324",
    "avgVolumeMorning": 2500000.75,
    "avgVolumeEvening": 1800000.25,
    "avgVolumeWeekend": 150000.50,
    "totalTradingDays": 250,
    "totalWeekendDays": 115,
    "lastCalculated": "2024-01-15T10:30:00+03:00",
    "createdAt": "2024-01-15T10:30:00+03:00",
    "updatedAt": "2024-01-15T10:30:00+03:00"
  }
]
```

#### Поля ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `figi` | String | Уникальный идентификатор инструмента |
| `avgVolumeMorning` | BigDecimal | Средний объем торгов с 06:50:00 по 09:59:59 |
| `avgVolumeEvening` | BigDecimal | Средний объем торгов с 19:00:00 по 23:59:59 |
| `avgVolumeWeekend` | BigDecimal | Средний объем торгов в выходные дни |
| `totalTradingDays` | Integer | Общее количество торговых дней |
| `totalWeekendDays` | Integer | Общее количество выходных дней |
| `lastCalculated` | LocalDateTime | Время последнего расчета данных |
| `createdAt` | LocalDateTime | Время создания записи |
| `updatedAt` | LocalDateTime | Время последнего обновления записи |

#### Примеры использования

**Получение всех данных:**
```bash
curl "http://localhost:8083/admin/futures-aggregation"
```

**Postman:**
- Method: `GET`
- URL: `http://localhost:8083/admin/futures-aggregation`
- Headers: Не требуются

#### Особенности
- **Сортировка**: Результаты отсортированы по `updatedAt` в убывающем порядке
- **Полнота данных**: Возвращаются только записи с рассчитанными данными
- **Временная зона**: Все времена в московской временной зоне (UTC+3)
- **Вечерняя сессия**: Включает данные по вечерней торговой сессии (19:00-23:59)

### Структуры таблиц агрегации

#### Таблица shares_aggregated_data

```sql
CREATE TABLE invest.shares_aggregated_data (
    figi                VARCHAR(255)                            NOT NULL PRIMARY KEY,
    avg_volume_morning  NUMERIC(18, 2)                          NOT NULL,
    avg_volume_weekend  NUMERIC(18, 2)                          NOT NULL,
    total_trading_days  INTEGER                                 NOT NULL,
    total_weekend_days  INTEGER                                 NOT NULL,
    last_calculated     TIMESTAMP WITH TIME ZONE                NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW()  NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW()  NOT NULL
);
```

#### Таблица futures_aggregated_data

```sql
CREATE TABLE invest.futures_aggregated_data (
    figi                VARCHAR(255)                            NOT NULL PRIMARY KEY,
    avg_volume_morning  NUMERIC(18, 2)                          NOT NULL,
    avg_volume_evening  NUMERIC(18, 2)                          NOT NULL,
    avg_volume_weekend  NUMERIC(18, 2)                          NOT NULL,
    total_trading_days  INTEGER                                 NOT NULL,
    total_weekend_days  INTEGER                                 NOT NULL,
    last_calculated     TIMESTAMP WITH TIME ZONE                NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW()  NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW()  NOT NULL
);
```

### POST /admin/load-last-trades

Асинхронная загрузка обезличенных сделок за последний час.

#### URL
```
POST http://localhost:8083/admin/load-last-trades
```

#### Тело запроса
```json
{
  "figis": ["ALL_SHARES"],
  "tradeSource": "TRADE_SOURCE_ALL"
}
```

#### Параметры
- `figis` - массив FIGI инструментов или ключевых слов:
  - `["ALL"]` - все инструменты (акции + фьючерсы)
  - `["ALL_SHARES"]` - все акции
  - `["ALL_FUTURES"]` - все фьючерсы
  - `["BBG004730N88", "BBG004730ZJ9"]` - конкретные FIGI
- `tradeSource` - источник сделок (обычно `"TRADE_SOURCE_ALL"`)

#### Ответ
```
"Загрузка обезличенных сделок запущена в фоновом режиме"
```

#### Примеры использования

**Загрузка всех акций:**
```bash
curl -X POST http://localhost:8083/admin/load-last-trades \
  -H "Content-Type: application/json" \
  -d '{"figis": ["ALL_SHARES"], "tradeSource": "TRADE_SOURCE_ALL"}'
```

**Загрузка всех фьючерсов:**
```bash
curl -X POST http://localhost:8083/admin/load-last-trades \
  -H "Content-Type: application/json" \
  -d '{"figis": ["ALL_FUTURES"], "tradeSource": "TRADE_SOURCE_ALL"}'
```

**Загрузка всех инструментов:**
```bash
curl -X POST http://localhost:8083/admin/load-last-trades \
  -H "Content-Type: application/json" \
  -d '{"figis": ["ALL"], "tradeSource": "TRADE_SOURCE_ALL"}'
```

**Загрузка конкретных инструментов:**
```bash
curl -X POST http://localhost:8083/admin/load-last-trades \
  -H "Content-Type: application/json" \
  -d '{"figis": ["BBG004730N88", "BBG004730ZJ9"], "tradeSource": "TRADE_SOURCE_ALL"}'
```

---

## Автоматические шедулеры

### Шедулер цен закрытия

- **Время запуска**: 01:00 по московскому времени ежедневно
- **Функция**: Загружает цены закрытия за предыдущий день для всех RUB инструментов
- **Cron**: `0 0 1 * * *`

### Шедулер свечей

- **Время запуска**: 01:10 по московскому времени ежедневно
- **Функция**: Загружает минутные свечи за предыдущий день
- **Последовательность**: Сначала акции, затем фьючерсы (с паузой 5 секунд между ними)
- **Cron**: `0 10 1 * * *`

### Шедулер цен закрытия вечерней сессии

- **Время запуска**: 02:00 по московскому времени ежедневно
- **Функция**: Загружает цены закрытия вечерней сессии за предыдущий день
- **Логика работы**:
  1. Получает список всех акций из таблицы `invest.shares`
  2. Для каждой акции находит последнюю свечу за предыдущий день в таблице `invest.candles`
  3. Извлекает цену закрытия (`close`) из последней свечи
  4. Сохраняет данные в таблицу `invest.close_prices_evening_session`
- **Cron**: `0 0 2 * * *`

### Шедулер цен открытия утренней сессии

- **Время запуска**: 02:01 по московскому времени ежедневно
- **Функция**: Загружает цены открытия утренней сессии за предыдущий день
- **Логика работы**:
  1. Получает список всех акций и фьючерсов из таблиц `invest.shares` и `invest.futures`
  2. **Для акций**: находит первую свечу за предыдущий день в таблице `invest.candles` до 06:59:59 включительно
  3. **Для фьючерсов**: находит первую свечу за предыдущий день в таблице `invest.candles` до 08:59:59 включительно
  4. Извлекает цену открытия (`open`) из первой свечи
  5. Сохраняет данные в таблицу `invest.open_prices`
- **Особенности**: В выходные дни (суббота и воскресенье) цены открытия не загружаются
- **Cron**: `0 1 2 * * *`

### Шедулер обезличенных сделок

- **Время запуска**: Каждые 30 минут с 2:00 до 00:00 по московскому времени ежедневно
- **Функция**: Загружает обезличенные сделки за последний час
- **Логика работы**:
  1. **Этап 1**: Загрузка обезличенных сделок по всем акциям
     - Использует ключевое слово `"ALL_SHARES"`
     - Загружает сделки за последний час
     - Сохраняет в таблицу `invest.last_prices`
  2. **Пауза**: 5 секунд между этапами
  3. **Этап 2**: Загрузка обезличенных сделок по всем фьючерсам
     - Использует ключевое слово `"ALL_FUTURES"`
     - Загружает сделки за последний час
     - Сохраняет в таблицу `invest.last_prices`
- **Cron**: `0 0,30 2-23 * * *`
- **Часовой пояс**: `Europe/Moscow`
- **Выполнение**: Асинхронное (в фоновом режиме)
- **Время запуска**: 2:00, 2:30, 3:00, 3:30, ..., 23:00, 23:30

---

## Примеры использования системы агрегации

### Типичный рабочий процесс

1. **Загрузка данных**: Сначала загружаются свечи через эндпоинт `POST /candles`
2. **Расчет агрегации**: Запускается пересчет агрегированных данных
3. **Анализ результатов**: Получение и анализ рассчитанных показателей

### Пример полного цикла

```bash
# 1. Загрузка свечей за последний месяц
curl -X POST "http://localhost:8083/candles" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-01-15",
    "assetType": ["SHARES", "FUTURES"]
  }'

# 2. Пересчет агрегированных данных для акций
curl -X POST "http://localhost:8083/admin/recalculate-shares-aggregation" \
  -H "Content-Type: application/json"

# 3. Пересчет агрегированных данных для фьючерсов
curl -X POST "http://localhost:8083/admin/recalculate-futures-aggregation" \
  -H "Content-Type: application/json"

# 4. Получение результатов по акциям
curl "http://localhost:8083/admin/shares-aggregation"

# 5. Получение результатов по фьючерсам
curl "http://localhost:8083/admin/futures-aggregation"
```

### Анализ торговой активности

Агрегированные данные позволяют:

- **Сравнивать активность**: Сравнивать объемы торгов в разные периоды
- **Выявлять паттерны**: Находить инструменты с высокой активностью в определенное время
- **Планировать торговлю**: Использовать данные для принятия торговых решений
- **Мониторинг**: Отслеживать изменения торговой активности

### Мониторинг системы

```bash
# Проверка статуса последнего пересчета акций
curl "http://localhost:8083/admin/shares-aggregation" | jq '.[0] | {figi, lastCalculated, avgVolumeMorning}'

# Проверка статуса последнего пересчета фьючерсов  
curl "http://localhost:8083/admin/futures-aggregation" | jq '.[0] | {figi, lastCalculated, avgVolumeMorning, avgVolumeEvening}'
```

### Автоматизация

Для автоматизации процесса можно создать скрипт:

```bash
#!/bin/bash
# Скрипт для ежедневного пересчета агрегации

echo "Начинаем пересчет агрегированных данных..."

# Пересчет акций
echo "Пересчет данных для акций..."
curl -X POST "http://localhost:8083/admin/recalculate-shares-aggregation" \
  -H "Content-Type: application/json"

# Пересчет фьючерсов
echo "Пересчет данных для фьючерсов..."
curl -X POST "http://localhost:8083/admin/recalculate-futures-aggregation" \
  -H "Content-Type: application/json"

echo "Пересчет завершен!"
```

---

## Эндпоинты индикативных инструментов

### GET /indicatives

Получение списка индикативных инструментов (индексы, товары и другие) с возможностью фильтрации.

#### Описание
Эндпоинт возвращает список индикативных инструментов, полученных из базы данных с применением фильтров. Индикативные инструменты включают индексы, товары и другие инструменты, которые не торгуются напрямую на бирже.

#### URL
```
GET http://localhost:8083/indicatives
```

#### Параметры запроса

| Параметр | Тип | Обязательный | Описание | Возможные значения |
|----------|-----|--------------|----------|-------------------|
| `exchange` | String | Нет | Биржа | `MOEX`, `SPB`, и др. |
| `currency` | String | Нет | Валюта инструмента | `RUB`, `USD`, `EUR`, и др. |
| `ticker` | String | Нет | Тикер инструмента | `IMOEX`, `RTSI`, и др. |
| `classCode` | String | Нет | Код класса инструмента | `SPBXM`, и др. |

#### Примеры запросов

##### Получить все индикативные инструменты
```bash
curl "http://localhost:8083/indicatives"
```

##### Получить индикативные инструменты с биржи MOEX
```bash
curl "http://localhost:8083/indicatives?exchange=MOEX"
```

##### Получить индикативные инструменты в рублях
```bash
curl "http://localhost:8083/indicatives?currency=RUB"
```

##### Получить индикативный инструмент по тикеру
```bash
curl "http://localhost:8083/indicatives?ticker=IMOEX"
```

##### Получить индикативные инструменты по коду класса
```bash
curl "http://localhost:8083/indicatives?classCode=SPBXM"
```

##### Комбинированный запрос
```bash
curl "http://localhost:8083/indicatives?exchange=MOEX&currency=RUB&ticker=IMOEX&classCode=SPBXM"
```

#### Формат ответа

```json
[
  {
    "figi": "BBG00M8XQPY9",
    "ticker": "IMOEX",
    "name": "Индекс МосБиржи",
    "currency": "RUB",
    "exchange": "MOEX",
    "classCode": "SPBXM",
    "uid": "6afa6f98-7b65-4c5a-ae5e-3f9d62f4ac07",
    "sellAvailableFlag": true,
    "buyAvailableFlag": true
  }
]
```

#### Поля ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `figi` | String | Уникальный идентификатор инструмента (FIGI) |
| `ticker` | String | Тикер инструмента |
| `name` | String | Название инструмента |
| `currency` | String | Валюта инструмента |
| `exchange` | String | Биржа |
| `classCode` | String | Код класса инструмента |
| `uid` | String | Уникальный идентификатор инструмента |
| `sellAvailableFlag` | Boolean | Флаг доступности для продажи |
| `buyAvailableFlag` | Boolean | Флаг доступности для покупки |

#### Особенности

1. **Источник данных**: Данные получаются из таблицы `invest.indicatives` в базе данных
2. **Сортировка**: Результаты автоматически сортируются по тикеру в алфавитном порядке
3. **Фильтрация**: Фильтры применяются регистронезависимо
4. **Готовность к API**: Код подготовлен для интеграции с T-Bank API, когда метод `indicatives` станет доступен

### POST /indicatives

Сохранение индикативных инструментов в базу данных.

#### Описание
Эндпоинт сохраняет индикативные инструменты в таблицу `invest.indicatives`. В текущей реализации используется заглушка с примером данных из документации T-Bank API.

#### URL
```
POST http://localhost:8083/indicatives
```

#### Тело запроса

Параметры передаются в теле запроса в формате JSON:

```json
{
  "exchange": "MOEX",
  "currency": "RUB",
  "ticker": "IMOEX",
  "classCode": "SPBXM"
}
```

#### Параметры тела запроса

| Параметр | Тип | Обязательный | Описание | Возможные значения |
|----------|-----|--------------|----------|-------------------|
| `exchange` | String | Нет | Биржа | `MOEX`, `SPB`, и др. |
| `currency` | String | Нет | Валюта инструмента | `RUB`, `USD`, `EUR`, и др. |
| `ticker` | String | Нет | Тикер инструмента | `IMOEX`, `RTSI`, и др. |
| `classCode` | String | Нет | Код класса инструмента | `SPBXM`, и др. |

#### Примеры запросов

##### Сохранить все индикативные инструменты
```bash
curl -X POST "http://localhost:8083/indicatives" \
  -H "Content-Type: application/json" \
  -d '{}'
```

##### Сохранить индикативные инструменты с биржи MOEX
```bash
curl -X POST "http://localhost:8083/indicatives" \
  -H "Content-Type: application/json" \
  -d '{
    "exchange": "MOEX"
  }'
```

##### Сохранить индикативные инструменты в рублях
```bash
curl -X POST "http://localhost:8083/indicatives" \
  -H "Content-Type: application/json" \
  -d '{
    "currency": "RUB"
  }'
```

##### Сохранить индикативный инструмент по тикеру
```bash
curl -X POST "http://localhost:8083/indicatives" \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "IMOEX"
  }'
```

##### Комбинированный запрос
```bash
curl -X POST "http://localhost:8083/indicatives" \
  -H "Content-Type: application/json" \
  -d '{
    "exchange": "MOEX",
    "currency": "RUB",
    "ticker": "IMOEX",
    "classCode": "SPBXM"
  }'
```

#### Формат ответа

```json
{
  "success": true,
  "message": "Успешно загружено 2 новых индикативных инструмента из 2 найденных.",
  "totalRequested": 2,
  "newItemsSaved": 2,
  "existingItemsSkipped": 0,
  "savedItems": [
    {
      "figi": "BBG00M8XQPY9",
      "ticker": "IMOEX",
      "name": "Индекс МосБиржи",
      "currency": "RUB",
      "exchange": "MOEX",
      "classCode": "SPBXM",
      "uid": "6afa6f98-7b65-4c5a-ae5e-3f9d62f4ac07",
      "sellAvailableFlag": true,
      "buyAvailableFlag": true
    },
    {
      "figi": "BBG00N9JX0P0",
      "ticker": "RTSI",
      "name": "Индекс РТС",
      "currency": "RUB",
      "exchange": "MOEX",
      "classCode": "SPBXM",
      "uid": "7c5e6d4f-3a2b-4c1d-9e8f-1a2b3c4d5e6f",
      "sellAvailableFlag": true,
      "buyAvailableFlag": true
    }
  ]
}
```

#### Поля ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `success` | Boolean | Успешность операции |
| `message` | String | Информативное сообщение о результате |
| `totalRequested` | Integer | Общее количество найденных инструментов |
| `newItemsSaved` | Integer | Количество новых инструментов, сохраненных в БД |
| `existingItemsSkipped` | Integer | Количество инструментов, которые уже существовали в БД |
| `savedItems` | Array | Массив сохраненных индикативных инструментов |

#### Структура таблицы indicatives

```sql
CREATE TABLE invest.indicatives (
    figi            VARCHAR(255)    PRIMARY KEY,
    ticker          VARCHAR(255)    NOT NULL,
    name            VARCHAR(500)    NOT NULL,
    currency        VARCHAR(10)     NOT NULL,
    exchange        VARCHAR(255)    NOT NULL,
    class_code      VARCHAR(255),
    uid             VARCHAR(255),
    sell_available_flag BOOLEAN,
    buy_available_flag  BOOLEAN,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);
```

#### Особенности

1. **Заглушка данных**: В текущей реализации используются примеры данных из документации T-Bank API
2. **Готовность к API**: Код подготовлен для интеграции с реальным API T-Bank
3. **Идемпотентность**: Повторные вызовы с теми же параметрами не создадут дубликаты
4. **Обработка ошибок**: Ошибки сохранения отдельных инструментов не прерывают процесс

#### Интеграция с T-Bank API

Когда метод `indicatives` станет доступен в T-Bank API, код будет автоматически переключен на реальные данные. В коде есть закомментированный блок с правильной реализацией:

```java
// Когда API метод будет доступен, раскомментируйте код ниже:
/*
var response = instrumentsService.indicatives(InstrumentsRequest.newBuilder().build());
// ... обработка реальных данных из API
*/
```

#### Коды ответов

| Код | Описание |
|-----|----------|
| 200 | Успешное выполнение запроса |
| 400 | Некорректные параметры запроса |
| 500 | Внутренняя ошибка сервера |