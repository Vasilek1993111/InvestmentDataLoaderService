# Примеры использования

## Обзор

Данный документ содержит практические примеры использования Ingestion Service API для различных сценариев.

## Базовые примеры

### 1. Получение списка акций

#### Python
```python
import requests
import json

# Получение всех акций
response = requests.get('http://localhost:8083/shares')
shares = response.json()

print(f"Найдено {len(shares)} акций")
for share in shares[:5]:  # Первые 5 акций
    print(f"{share['ticker']}: {share['name']}")

# Фильтрация по бирже
moex_shares = requests.get('http://localhost:8083/shares?exchange=MOEX').json()
print(f"Акций на MOEX: {len(moex_shares)}")

# Фильтрация по валюте
rub_shares = requests.get('http://localhost:8083/shares?currency=RUB').json()
print(f"Акций в рублях: {len(rub_shares)}")
```

#### JavaScript (Node.js)
```javascript
const axios = require('axios');

async function getShares() {
    try {
        // Получение всех акций
        const response = await axios.get('http://localhost:8083/shares');
        const shares = response.data;
        
        console.log(`Найдено ${shares.length} акций`);
        
        // Фильтрация по тикеру
        const sber = shares.find(share => share.ticker === 'SBER');
        if (sber) {
            console.log('Сбербанк:', sber);
        }
        
        // Группировка по бирже
        const byExchange = shares.reduce((acc, share) => {
            acc[share.exchange] = (acc[share.exchange] || 0) + 1;
            return acc;
        }, {});
        
        console.log('Распределение по биржам:', byExchange);
        
    } catch (error) {
        console.error('Ошибка:', error.message);
    }
}

getShares();
```

#### Java
```java
import org.springframework.web.client.RestTemplate;
import java.util.List;

public class SharesClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = "http://localhost:8083";
    
    public List<ShareDto> getAllShares() {
        return restTemplate.getForObject(baseUrl + "/shares", List.class);
    }
    
    public List<ShareDto> getSharesByExchange(String exchange) {
        return restTemplate.getForObject(
            baseUrl + "/shares?exchange=" + exchange, 
            List.class
        );
    }
    
    public List<ShareDto> getSharesByCurrency(String currency) {
        return restTemplate.getForObject(
            baseUrl + "/shares?currency=" + currency, 
            List.class
        );
    }
}
```

### 2. Получение цен закрытия

#### Python
```python
import requests
from datetime import datetime, timedelta

def get_close_prices(figi_list):
    """Получение цен закрытия для списка инструментов"""
    
    # Формируем параметры запроса
    params = {'instrumentId': figi_list}
    
    response = requests.get('http://localhost:8083/close-prices', params=params)
    prices = response.json()
    
    return prices

# Пример использования
figi_list = ['BBG000B9XRY4', 'BBG000B9XRY5']  # SBER, GAZP
prices = get_close_prices(figi_list)

for price in prices:
    print(f"{price['figi']}: {price['closePrice']} на {price['date']}")

# Создание словаря для быстрого доступа
price_dict = {p['figi']: p['closePrice'] for p in prices}
print("Цена Сбербанка:", price_dict.get('BBG000B9XRY4'))
```

#### JavaScript
```javascript
async function getClosePrices(figiList) {
    try {
        const params = new URLSearchParams();
        figiList.forEach(figi => params.append('instrumentId', figi));
        
        const response = await axios.get(
            `http://localhost:8083/close-prices?${params.toString()}`
        );
        
        return response.data;
    } catch (error) {
        console.error('Ошибка получения цен:', error.message);
        return [];
    }
}

// Пример использования
const figiList = ['BBG000B9XRY4', 'BBG000B9XRY5'];
getClosePrices(figiList).then(prices => {
    console.log('Цены закрытия:', prices);
    
    // Создание карты цен
    const priceMap = new Map(
        prices.map(p => [p.figi, p.closePrice])
    );
    
    console.log('Цена Сбербанка:', priceMap.get('BBG000B9XRY4'));
});
```

### 3. Получение торговых расписаний

#### Python
```python
import requests
from datetime import datetime, timedelta

def get_trading_schedule(exchange, start_date, end_date):
    """Получение торгового расписания"""
    
    # Форматируем даты в ISO 8601
    from_iso = start_date.isoformat() + 'Z'
    to_iso = end_date.isoformat() + 'Z'
    
    params = {
        'exchange': exchange,
        'from': from_iso,
        'to': to_iso
    }
    
    response = requests.get('http://localhost:8083/trading-schedules', params=params)
    return response.json()

# Пример использования
start_date = datetime.now()
end_date = start_date + timedelta(days=7)

schedule = get_trading_schedule('MOEX', start_date, end_date)

for exchange_data in schedule:
    print(f"Биржа: {exchange_data['exchange']}")
    for day in exchange_data['days']:
        status = "Торговый день" if day['isTradingDay'] else "Выходной"
        print(f"  {day['date']}: {status}")
        if day['isTradingDay']:
            print(f"    Время торгов: {day['startTime']} - {day['endTime']}")
```

## Продвинутые примеры

### 1. Мониторинг цен акций

#### Python
```python
import requests
import time
import pandas as pd
from datetime import datetime

class PriceMonitor:
    def __init__(self, base_url='http://localhost:8083'):
        self.base_url = base_url
        self.price_history = {}
    
    def get_shares(self):
        """Получение списка акций"""
        response = requests.get(f'{self.base_url}/shares')
        return response.json()
    
    def get_prices(self, figi_list):
        """Получение цен закрытия"""
        params = {'instrumentId': figi_list}
        response = requests.get(f'{self.base_url}/close-prices', params=params)
        return response.json()
    
    def monitor_prices(self, tickers, interval=3600):
        """Мониторинг цен с заданным интервалом"""
        shares = self.get_shares()
        
        # Создаем словарь тикер -> figi
        ticker_to_figi = {share['ticker']: share['figi'] for share in shares}
        
        # Получаем figi для запрошенных тикеров
        figi_list = [ticker_to_figi[ticker] for ticker in tickers if ticker in ticker_to_figi]
        
        print(f"Мониторинг {len(figi_list)} инструментов...")
        
        while True:
            try:
                prices = self.get_prices(figi_list)
                timestamp = datetime.now()
                
                for price in prices:
                    ticker = next(t for t, f in ticker_to_figi.items() if f == price['figi'])
                    
                    if ticker not in self.price_history:
                        self.price_history[ticker] = []
                    
                    self.price_history[ticker].append({
                        'timestamp': timestamp,
                        'price': price['closePrice'],
                        'date': price['date']
                    })
                    
                    print(f"{timestamp.strftime('%H:%M:%S')} {ticker}: {price['closePrice']}")
                
                time.sleep(interval)
                
            except KeyboardInterrupt:
                print("\nМониторинг остановлен")
                break
            except Exception as e:
                print(f"Ошибка: {e}")
                time.sleep(60)  # Пауза при ошибке
    
    def get_price_changes(self, ticker, days=7):
        """Получение изменения цены за период"""
        if ticker not in self.price_history:
            return None
        
        history = self.price_history[ticker]
        if len(history) < 2:
            return None
        
        latest = history[-1]['price']
        oldest = history[0]['price']
        change = ((latest - oldest) / oldest) * 100
        
        return {
            'ticker': ticker,
            'start_price': oldest,
            'end_price': latest,
            'change_percent': change,
            'period_days': days
        }

# Пример использования
monitor = PriceMonitor()
monitor.monitor_prices(['SBER', 'GAZP', 'LKOH'], interval=1800)  # Каждые 30 минут
```

### 2. Анализ волатильности

#### Python
```python
import numpy as np
import pandas as pd
from datetime import datetime, timedelta

class VolatilityAnalyzer:
    def __init__(self, base_url='http://localhost:8083'):
        self.base_url = base_url
    
    def get_historical_prices(self, figi, days=30):
        """Получение исторических цен (требует расширения API)"""
        # Здесь нужно будет добавить endpoint для исторических данных
        pass
    
    def calculate_volatility(self, prices):
        """Расчет волатильности"""
        if len(prices) < 2:
            return 0
        
        returns = np.diff(np.log(prices))
        volatility = np.std(returns) * np.sqrt(252)  # Годовая волатильность
        
        return volatility
    
    def analyze_market_volatility(self, top_n=10):
        """Анализ волатильности топ акций"""
        shares = requests.get(f'{self.base_url}/shares').json()
        
        # Получаем топ акций по объему (пример)
        top_shares = shares[:top_n]
        
        volatility_data = []
        
        for share in top_shares:
            # Получаем цены (упрощенный пример)
            prices = self.get_historical_prices(share['figi'])
            if prices:
                volatility = self.calculate_volatility(prices)
                volatility_data.append({
                    'ticker': share['ticker'],
                    'name': share['name'],
                    'volatility': volatility
                })
        
        # Сортируем по волатильности
        volatility_data.sort(key=lambda x: x['volatility'], reverse=True)
        
        return volatility_data

# Пример использования
analyzer = VolatilityAnalyzer()
volatility_ranking = analyzer.analyze_market_volatility()

print("Рейтинг волатильности:")
for i, item in enumerate(volatility_ranking, 1):
    print(f"{i}. {item['ticker']}: {item['volatility']:.2%}")
```

### 3. Интеграция с торговыми системами

#### Python
```python
import asyncio
import aiohttp
from typing import List, Dict

class TradingSystemIntegration:
    def __init__(self, base_url='http://localhost:8083'):
        self.base_url = base_url
        self.session = None
    
    async def __aenter__(self):
        self.session = aiohttp.ClientSession()
        return self
    
    async def __aexit__(self, exc_type, exc_val, exc_tb):
        if self.session:
            await self.session.close()
    
    async def get_market_data(self, figi_list: List[str]) -> Dict:
        """Получение рыночных данных для торговой системы"""
        tasks = [
            self.get_close_prices(figi_list),
            self.get_trading_statuses(figi_list)
        ]
        
        results = await asyncio.gather(*tasks)
        
        return {
            'close_prices': results[0],
            'trading_statuses': results[1]
        }
    
    async def get_close_prices(self, figi_list: List[str]):
        """Асинхронное получение цен закрытия"""
        params = {'instrumentId': figi_list}
        async with self.session.get(f'{self.base_url}/close-prices', params=params) as response:
            return await response.json()
    
    async def get_trading_statuses(self, figi_list: List[str]):
        """Асинхронное получение статусов торговли"""
        params = {'instrumentId': figi_list}
        async with self.session.get(f'{self.base_url}/trading-statuses', params=params) as response:
            return await response.json()
    
    def process_market_data(self, market_data: Dict) -> Dict:
        """Обработка рыночных данных для торговых сигналов"""
        processed_data = {}
        
        # Создаем словарь цен
        price_dict = {p['figi']: p['closePrice'] for p in market_data['close_prices']}
        
        # Создаем словарь статусов
        status_dict = {s['figi']: s['tradingStatus'] for s in market_data['trading_statuses']}
        
        for figi in price_dict.keys():
            processed_data[figi] = {
                'price': price_dict[figi],
                'trading_status': status_dict.get(figi, 'UNKNOWN'),
                'is_tradable': status_dict.get(figi) == 'SECURITY_TRADING_STATUS_NORMAL_TRADING'
            }
        
        return processed_data

# Пример использования
async def main():
    figi_list = ['BBG000B9XRY4', 'BBG000B9XRY5', 'BBG000B9XRY6']
    
    async with TradingSystemIntegration() as integration:
        market_data = await integration.get_market_data(figi_list)
        processed_data = integration.process_market_data(market_data)
        
        print("Обработанные рыночные данные:")
        for figi, data in processed_data.items():
            print(f"{figi}: {data}")

# Запуск
asyncio.run(main())
```

### 4. Веб-интерфейс для мониторинга

#### HTML + JavaScript
```html
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Мониторинг цен акций</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .price-card {
            border: 1px solid #ddd;
            padding: 15px;
            margin: 10px 0;
            border-radius: 4px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .price-up { color: green; }
        .price-down { color: red; }
        .controls {
            margin-bottom: 20px;
        }
        button {
            padding: 10px 20px;
            margin: 5px;
            border: none;
            border-radius: 4px;
            background-color: #007bff;
            color: white;
            cursor: pointer;
        }
        button:hover {
            background-color: #0056b3;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Мониторинг цен акций</h1>
        
        <div class="controls">
            <button onclick="loadShares()">Загрузить акции</button>
            <button onclick="startMonitoring()">Начать мониторинг</button>
            <button onclick="stopMonitoring()">Остановить мониторинг</button>
        </div>
        
        <div id="shares-list"></div>
        <div id="prices-container"></div>
    </div>

    <script>
        const API_BASE = 'http://localhost:8083';
        let monitoringInterval = null;
        let shares = [];
        let priceHistory = {};

        async function loadShares() {
            try {
                const response = await fetch(`${API_BASE}/shares`);
                shares = await response.json();
                
                displayShares(shares);
            } catch (error) {
                console.error('Ошибка загрузки акций:', error);
            }
        }

        function displayShares(sharesList) {
            const container = document.getElementById('shares-list');
            container.innerHTML = '<h2>Список акций</h2>';
            
            const topShares = sharesList.slice(0, 20); // Показываем топ 20
            
            topShares.forEach(share => {
                const shareDiv = document.createElement('div');
                shareDiv.className = 'price-card';
                shareDiv.innerHTML = `
                    <div>
                        <strong>${share.ticker}</strong> - ${share.name}
                        <br><small>${share.exchange} | ${share.currency}</small>
                    </div>
                    <div>
                        <button onclick="addToMonitoring('${share.figi}', '${share.ticker}')">
                            Добавить к мониторингу
                        </button>
                    </div>
                `;
                container.appendChild(shareDiv);
            });
        }

        function addToMonitoring(figi, ticker) {
            if (!priceHistory[ticker]) {
                priceHistory[ticker] = [];
            }
            
            // Получаем текущую цену
            getClosePrice([figi], ticker);
        }

        async function getClosePrice(figiList, ticker) {
            try {
                const params = new URLSearchParams();
                figiList.forEach(figi => params.append('instrumentId', figi));
                
                const response = await fetch(`${API_BASE}/close-prices?${params}`);
                const prices = await response.json();
                
                if (prices.length > 0) {
                    const price = prices[0];
                    updatePriceDisplay(ticker, price.closePrice);
                }
            } catch (error) {
                console.error('Ошибка получения цены:', error);
            }
        }

        function updatePriceDisplay(ticker, price) {
            const container = document.getElementById('prices-container');
            
            // Добавляем цену в историю
            if (!priceHistory[ticker]) {
                priceHistory[ticker] = [];
            }
            
            const timestamp = new Date();
            priceHistory[ticker].push({ price, timestamp });
            
            // Ограничиваем историю последними 10 записями
            if (priceHistory[ticker].length > 10) {
                priceHistory[ticker].shift();
            }
            
            // Находим или создаем элемент для отображения цены
            let priceElement = document.getElementById(`price-${ticker}`);
            if (!priceElement) {
                priceElement = document.createElement('div');
                priceElement.id = `price-${ticker}`;
                priceElement.className = 'price-card';
                container.appendChild(priceElement);
            }
            
            // Определяем изменение цены
            let priceChange = '';
            let priceClass = '';
            
            if (priceHistory[ticker].length > 1) {
                const previousPrice = priceHistory[ticker][priceHistory[ticker].length - 2].price;
                const change = price - previousPrice;
                const changePercent = (change / previousPrice) * 100;
                
                if (change > 0) {
                    priceChange = `+${change.toFixed(2)} (+${changePercent.toFixed(2)}%)`;
                    priceClass = 'price-up';
                } else if (change < 0) {
                    priceChange = `${change.toFixed(2)} (${changePercent.toFixed(2)}%)`;
                    priceClass = 'price-down';
                }
            }
            
            priceElement.innerHTML = `
                <div>
                    <strong>${ticker}</strong>
                    <br><small>${timestamp.toLocaleTimeString()}</small>
                </div>
                <div class="${priceClass}">
                    <strong>${price.toFixed(2)}</strong>
                    ${priceChange ? `<br><small>${priceChange}</small>` : ''}
                </div>
            `;
        }

        function startMonitoring() {
            if (monitoringInterval) {
                clearInterval(monitoringInterval);
            }
            
            // Обновляем цены каждые 30 секунд
            monitoringInterval = setInterval(() => {
                Object.keys(priceHistory).forEach(ticker => {
                    const share = shares.find(s => s.ticker === ticker);
                    if (share) {
                        getClosePrice([share.figi], ticker);
                    }
                });
            }, 30000);
            
            console.log('Мониторинг запущен');
        }

        function stopMonitoring() {
            if (monitoringInterval) {
                clearInterval(monitoringInterval);
                monitoringInterval = null;
                console.log('Мониторинг остановлен');
            }
        }

        // Загружаем акции при загрузке страницы
        window.onload = loadShares;
    </script>
</body>
</html>
```

## Интеграция с внешними системами

### 1. Интеграция с Telegram ботом

#### Python
```python
import asyncio
import aiohttp
from telegram import Bot, Update
from telegram.ext import Application, CommandHandler, ContextTypes

class StockBot:
    def __init__(self, token, api_base_url='http://localhost:8083'):
        self.bot = Bot(token=token)
        self.api_base_url = api_base_url
    
    async def start_command(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        """Обработчик команды /start"""
        await update.message.reply_text(
            "Привет! Я бот для мониторинга акций.\n"
            "Доступные команды:\n"
            "/shares - список акций\n"
            "/price <тикер> - цена акции\n"
            "/monitor <тикер> - начать мониторинг"
        )
    
    async def shares_command(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        """Обработчик команды /shares"""
        async with aiohttp.ClientSession() as session:
            async with session.get(f'{self.api_base_url}/shares') as response:
                shares = await response.json()
        
        # Отправляем топ 10 акций
        message = "Топ 10 акций:\n\n"
        for i, share in enumerate(shares[:10], 1):
            message += f"{i}. {share['ticker']} - {share['name']}\n"
        
        await update.message.reply_text(message)
    
    async def price_command(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        """Обработчик команды /price"""
        if not context.args:
            await update.message.reply_text("Укажите тикер: /price SBER")
            return
        
        ticker = context.args[0].upper()
        
        # Получаем figi для тикера
        async with aiohttp.ClientSession() as session:
            async with session.get(f'{self.api_base_url}/shares') as response:
                shares = await response.json()
        
        share = next((s for s in shares if s['ticker'] == ticker), None)
        if not share:
            await update.message.reply_text(f"Акция {ticker} не найдена")
            return
        
        # Получаем цену
        params = {'instrumentId': [share['figi']]}
        async with aiohttp.ClientSession() as session:
            async with session.get(f'{self.api_base_url}/close-prices', params=params) as response:
                prices = await response.json()
        
        if prices:
            price = prices[0]
            message = f"💰 {ticker}\n"
            message += f"Цена: {price['closePrice']} {share['currency']}\n"
            message += f"Дата: {price['date']}\n"
            message += f"Биржа: {share['exchange']}"
        else:
            message = f"Цена для {ticker} не найдена"
        
        await update.message.reply_text(message)

# Пример использования
async def main():
    bot = StockBot('YOUR_TELEGRAM_BOT_TOKEN')
    
    app = Application.builder().token('YOUR_TELEGRAM_BOT_TOKEN').build()
    
    app.add_handler(CommandHandler("start", bot.start_command))
    app.add_handler(CommandHandler("shares", bot.shares_command))
    app.add_handler(CommandHandler("price", bot.price_command))
    
    await app.run_polling()

if __name__ == '__main__':
    asyncio.run(main())
```

### 2. Интеграция с Excel

#### Python
```python
import pandas as pd
import requests
from openpyxl import Workbook
from openpyxl.styles import Font, PatternFill

class ExcelExporter:
    def __init__(self, base_url='http://localhost:8083'):
        self.base_url = base_url
    
    def export_shares_to_excel(self, filename='shares_report.xlsx'):
        """Экспорт списка акций в Excel"""
        # Получаем данные
        shares = requests.get(f'{self.base_url}/shares').json()
        
        # Создаем DataFrame
        df = pd.DataFrame(shares)
        
        # Создаем Excel файл
        wb = Workbook()
        ws = wb.active
        ws.title = "Акции"
        
        # Заголовки
        headers = ['Тикер', 'Название', 'Валюта', 'Биржа', 'FIGI']
        for col, header in enumerate(headers, 1):
            cell = ws.cell(row=1, column=col, value=header)
            cell.font = Font(bold=True)
            cell.fill = PatternFill(start_color="CCCCCC", end_color="CCCCCC", fill_type="solid")
        
        # Данные
        for row, share in enumerate(shares, 2):
            ws.cell(row=row, column=1, value=share['ticker'])
            ws.cell(row=row, column=2, value=share['name'])
            ws.cell(row=row, column=3, value=share['currency'])
            ws.cell(row=row, column=4, value=share['exchange'])
            ws.cell(row=row, column=5, value=share['figi'])
        
        # Автоматическая ширина колонок
        for column in ws.columns:
            max_length = 0
            column_letter = column[0].column_letter
            for cell in column:
                try:
                    if len(str(cell.value)) > max_length:
                        max_length = len(str(cell.value))
                except:
                    pass
            adjusted_width = min(max_length + 2, 50)
            ws.column_dimensions[column_letter].width = adjusted_width
        
        wb.save(filename)
        print(f"Отчет сохранен в {filename}")
    
    def export_prices_to_excel(self, figi_list, filename='prices_report.xlsx'):
        """Экспорт цен в Excel"""
        # Получаем цены
        params = {'instrumentId': figi_list}
        prices = requests.get(f'{self.base_url}/close-prices', params=params).json()
        
        # Получаем информацию об акциях
        shares = requests.get(f'{self.base_url}/shares').json()
        shares_dict = {s['figi']: s for s in shares}
        
        # Создаем DataFrame
        data = []
        for price in prices:
            share = shares_dict.get(price['figi'], {})
            data.append({
                'FIGI': price['figi'],
                'Тикер': share.get('ticker', ''),
                'Название': share.get('name', ''),
                'Цена': price['closePrice'],
                'Дата': price['date'],
                'Валюта': share.get('currency', ''),
                'Биржа': share.get('exchange', '')
            })
        
        df = pd.DataFrame(data)
        
        # Сохраняем в Excel
        with pd.ExcelWriter(filename, engine='openpyxl') as writer:
            df.to_excel(writer, sheet_name='Цены', index=False)
            
            # Получаем лист для форматирования
            worksheet = writer.sheets['Цены']
            
            # Форматирование заголовков
            for cell in worksheet[1]:
                cell.font = Font(bold=True)
                cell.fill = PatternFill(start_color="CCCCCC", end_color="CCCCCC", fill_type="solid")
        
        print(f"Отчет по ценам сохранен в {filename}")

# Пример использования
exporter = ExcelExporter()

# Экспорт списка акций
exporter.export_shares_to_excel()

# Экспорт цен для конкретных акций
figi_list = ['BBG000B9XRY4', 'BBG000B9XRY5']
exporter.export_prices_to_excel(figi_list)
```

Эти примеры демонстрируют различные способы использования API Ingestion Service для создания полезных приложений и интеграций.