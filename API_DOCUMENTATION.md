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
| `exchange` | String | Нет | Биржа | `moex_mrng_evng_e_wknd_dlr`, `MOEX`, `SPB`, `NASDAQ`, `NYSE`, и др. |
| `currency` | String | Нет | Валюта инструмента | `RUB`, `USD`, `EUR`, и др. |
| `ticker` | String | Нет | Тикер инструмента | `SBER`, `GAZP`, `LKOH`, `NVTK`, и др. |

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

#### Комбинированный запрос
```bash
curl "http://localhost:8083/shares?status=INSTRUMENT_STATUS_BASE&exchange=moex_mrng_evng_e_wknd_dlr&currency=RUB&ticker=SBER"
```

### Формат ответа

```json
[
  {
    "figi": "BBG004730N88",
    "ticker": "SBER",
    "name": "Сбербанк",
    "currency": "RUB",
    "exchange": "MOEX",
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

- `MOEX` - Московская биржа
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

## POST /shares

Сохранение акций в базу данных. Метод запрашивает список акций из T-Bank API согласно параметрам фильтрации, проверяет их наличие в таблице `invest.shares` и сохраняет только новые акции.

### Описание
Эндпоинт получает акции из T-Bank API, фильтрует их по заданным параметрам, проверяет существование в базе данных и сохраняет только те акции, которых еще нет в таблице `shares`.

### URL
```
POST http://localhost:8083/shares
```

### Параметры запроса

| Параметр | Тип | Обязательный | Описание | Возможные значения |
|----------|-----|--------------|----------|-------------------|
| `status` | String | Нет | Статус инструмента | `INSTRUMENT_STATUS_BASE`, `INSTRUMENT_STATUS_ALL` |
| `exchange` | String | Нет | Биржа | `moex_mrng_evng_e_wknd_dlr`, `MOEX`, `SPB`, `NASDAQ`, `NYSE`, и др. |
| `currency` | String | Нет | Валюта инструмента | `RUB`, `USD`, `EUR`, и др. |
| `ticker` | String | Нет | Тикер инструмента | `SBER`, `GAZP`, `LKOH`, `NVTK`, и др. |

### Примеры запросов

#### Сохранить все акции
```bash
curl -X POST "http://localhost:8083/shares"
```

#### Сохранить акции с базовым статусом
```bash
curl -X POST "http://localhost:8083/shares?status=INSTRUMENT_STATUS_BASE"
```

#### Сохранить акции с биржи MOEX
```bash
curl -X POST "http://localhost:8083/shares?exchange=moex_mrng_evng_e_wknd_dlr"
```

#### Сохранить акции в рублях
```bash
curl -X POST "http://localhost:8083/shares?currency=RUB"
```

#### Сохранить акцию по тикеру
```bash
curl -X POST "http://localhost:8083/shares?ticker=SBER"
```

#### Комбинированный запрос
```bash
curl -X POST "http://localhost:8083/shares?status=INSTRUMENT_STATUS_BASE&exchange=moex_mrng_evng_e_wknd_dlr&currency=RUB&ticker=SBER"
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

Возвращает список только тех акций, которые были сохранены в базу данных (новые акции).

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

### Связанные эндпоинты

- `GET /shares` - получение списка акций без сохранения в БД
- `GET /futures` - получение списка фьючерсов
- `GET /accounts` - получение списка счетов
- `GET /trading-schedules` - получение расписаний торгов
- `GET /trading-statuses` - получение статусов торговли
- `GET /close-prices` - получение цен закрытия
