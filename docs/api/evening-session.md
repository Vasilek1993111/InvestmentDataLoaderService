# API — Цены вечерней сессии (`/api/evening-session-prices`)

## Обзор

API для работы с ценами закрытия вечерней торговой сессии.

Предоставляет возможности для:
- **Получения цен** - просмотр цен закрытия вечерней сессии
- **Загрузки цен** - сохранение цен в базу данных
- **Фильтрации по типам** - акции, фьючерсы, все инструменты
- **Работы с датами** - за конкретную дату или вчерашний день

**Базовый URL:** `http://localhost:8083/api/evening-session-prices` (PROD) / `http://localhost:8087/api/evening-session-prices` (TEST)

**Особенности:**
- **Источник данных** - таблица `minute_candles`
- **Цена закрытия** - close последней минутной свечи в дне
- **Транзакционность** - все операции выполняются в транзакциях
- **Валидация** - проверка валидности цен (больше 0)

---

## GET /api/evening-session-prices

Получение цен закрытия вечерней сессии за вчерашний день.

**Примеры использования:**
```bash
# Получение цен за вчера
curl "http://localhost:8083/api/evening-session-prices"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Цены закрытия вечерней сессии за вчера получены успешно",
  "data": [
    {
      "figi": "BBG004730N88",
      "ticker": "SBER",
      "name": "Сбербанк",
      "priceDate": "2024-01-14",
      "closePrice": 251.20,
      "instrumentType": "SHARE",
      "currency": "RUB",
      "exchange": "MOEX"
    }
  ],
  "count": 1,
  "date": "2024-01-14",
  "totalProcessed": 30,
  "foundPrices": 25,
  "missingData": 5,
  "timestamp": "2024-01-15T18:30:00"
}
```

---

## GET /api/evening-session-prices/{date}

Получение цен закрытия вечерней сессии за конкретную дату.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Получение цен за дату
curl "http://localhost:8083/api/evening-session-prices/2024-01-15"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Цены закрытия вечерней сессии за 2024-01-15 получены успешно",
  "data": [
    {
      "figi": "BBG004730N88",
      "ticker": "SBER",
      "name": "Сбербанк",
      "priceDate": "2024-01-15",
      "closePrice": 251.20,
      "instrumentType": "SHARE",
      "currency": "RUB",
      "exchange": "MOEX"
    }
  ],
  "count": 1,
  "date": "2024-01-15",
  "totalProcessed": 30,
  "foundPrices": 25,
  "missingData": 5,
  "timestamp": "2024-01-15T18:30:00"
}
```

---

## POST /api/evening-session-prices

Загрузка цен закрытия вечерней сессии за вчерашний день.

**Примеры использования:**
```bash
# Загрузка цен за вчера
curl -X POST "http://localhost:8083/api/evening-session-prices"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Успешно загружено 25 новых цен вечерней сессии из 30 найденных.",
  "date": "2024-01-14",
  "totalRequested": 30,
  "newItemsSaved": 25,
  "existingItemsSkipped": 5,
  "invalidItemsFiltered": 0,
  "missingFromApi": 0,
  "savedItems": [
    {
      "figi": "BBG004730N88",
      "ticker": "SBER",
      "name": "Сбербанк",
      "priceDate": "2024-01-14",
      "closePrice": 251.20,
      "instrumentType": "SHARE",
      "currency": "RUB",
      "exchange": "MOEX"
    }
  ],
  "timestamp": "2024-01-15T18:30:00"
}
```

---

## POST /api/evening-session-prices/{date}

Загрузка цен закрытия вечерней сессии за конкретную дату.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Загрузка цен за дату
curl -X POST "http://localhost:8083/api/evening-session-prices/2024-01-15"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Успешно загружено 25 новых цен вечерней сессии из 30 найденных.",
  "date": "2024-01-15",
  "totalRequested": 30,
  "newItemsSaved": 25,
  "existingItemsSkipped": 5,
  "invalidItemsFiltered": 0,
  "missingFromApi": 0,
  "savedItems": [
    {
      "figi": "BBG004730N88",
      "ticker": "SBER",
      "name": "Сбербанк",
      "priceDate": "2024-01-15",
      "closePrice": 251.20,
      "instrumentType": "SHARE",
      "currency": "RUB",
      "exchange": "MOEX"
    }
  ],
  "timestamp": "2024-01-15T18:30:00"
}
```

---

## 🔧 Технические детали

### Архитектура
- **EveningSessionController** - основной контроллер для вечерней сессии
- **ClosePriceEveningSessionRepository** - репозиторий для сохранения цен
- **MinuteCandleRepository** - источник данных о свечах
- **@Transactional** - все операции выполняются в транзакциях

### Источники данных
- **MinuteCandleRepository** - получение данных о свечах
- **ShareRepository** - список акций
- **FutureRepository** - список фьючерсов
- **ClosePriceEveningSessionRepository** - сохранение цен

### Логика определения цены
- **Цена закрытия** - close последней минутной свечи в дне
- **Валидация** - проверка валидности цен (больше 0)
- **Фильтрация** - исключение инструментов без данных

### Типы инструментов
- **SHARE** - акции
- **FUTURE** - фьючерсы
- **UNKNOWN** - неизвестный тип

---

## 💡 Примеры использования

### Получение цен
```bash
# Цены за вчера
curl "http://localhost:8083/api/evening-session-prices"

# Цены за дату
curl "http://localhost:8083/api/evening-session-prices/2024-01-15"
```

### Загрузка цен
```bash
# Загрузка за вчера
curl -X POST "http://localhost:8083/api/evening-session-prices"

# Загрузка за дату
curl -X POST "http://localhost:8083/api/evening-session-prices/2024-01-15"
```

### Мониторинг
```bash
# Проверка статистики
curl "http://localhost:8083/api/evening-session-prices/2024-01-15" | jq '.statistics'
```

---

## ⚠️ Коды ответов

### Успешные ответы
- **200 OK** - данные получены или сохранены успешно

### Ошибки
- **500 Internal Server Error** - внутренняя ошибка сервера

### Примеры ошибок
```json
{
  "success": false,
  "message": "Ошибка загрузки цен закрытия вечерней сессии за вчера: Connection timeout",
  "timestamp": "2024-01-15T18:30:00"
}
```

---

## 🔄 Жизненный цикл обработки

### Получение цен
1. **Запрос** - GET запрос с параметрами
2. **Получение инструментов** - из репозиториев
3. **Поиск свечей** - в minute_candles
4. **Фильтрация** - валидация цен
5. **Форматирование** - преобразование в DTO
6. **Ответ** - возврат данных клиенту

### Загрузка цен
1. **Запрос** - POST запрос с параметрами
2. **Получение инструментов** - из репозиториев
3. **Поиск свечей** - в minute_candles
4. **Фильтрация** - валидация цен
5. **Сохранение** - в close_price_evening_session
6. **Ответ** - возврат статистики клиенту

### Обработка ошибок
1. **Валидация** - проверка параметров запроса
2. **Логирование** - детальная информация об ошибках
3. **Уведомление** - возврат ошибки клиенту
4. **Мониторинг** - отслеживание частоты ошибок

