#!/usr/bin/env python3
"""
Скрипт для автоматической загрузки дневных свечей акций, индикативов и фьючерсов.

Использование:
    python scripts/load-daily-candles-2025.py [--base-url BASE_URL] [--test] [--instrument-type TYPE] [--instrument-types TYPES] [--start-date DATE] [--end-date DATE] [--year YEAR] [--exclude-weekends] [--poll-interval SECONDS] [--timeout SECONDS]

Примеры:
    # Тестовое окружение для акций за год
    python scripts/load-daily-candles-2025.py --test

    # Загрузка индикативов за произвольный период
    python scripts/load-daily-candles-2025.py --test --instrument-type indicatives --start-date 2026-01-15 --end-date 2026-01-20

    # Загрузка фьючерсов за произвольный период
    python scripts/load-daily-candles-2025.py --test --instrument-type futures --start-date 2026-01-19 --end-date 2026-01-25

    # Загрузка нескольких типов инструментов одновременно за период
    python scripts/load-daily-candles-2025.py --test --instrument-types shares,indicatives,futures --start-date 2026-01-15 --end-date 2026-01-20

    # Продакшн окружение с кастомным URL
    python scripts/load-daily-candles-2025.py --base-url http://localhost:8083

    # Исключить выходные дни
    python scripts/load-daily-candles-2025.py --exclude-weekends
"""

import requests
import time
import json
import argparse
from datetime import datetime, timedelta
from typing import List, Dict, Optional
import sys


class DailyCandlesLoader:
    """Класс для автоматической загрузки дневных свечей за период."""
    
    # Поддерживаемые типы инструментов
    INSTRUMENT_TYPES = {
        'shares': 'shares',
        'indicatives': 'indicatives',
        'futures': 'futures'
    }
    
    def __init__(self, base_url: str, instrument_types: List[str] = None, poll_interval: int = 3, timeout: int = 600):
        """
        Инициализация загрузчика.
        
        Args:
            base_url: Базовый URL API (например, http://localhost:8083)
            instrument_types: Список типов инструментов (shares, indicatives, futures). Если None, используется ['shares']
            poll_interval: Интервал проверки статуса в секундах (по умолчанию 3)
            timeout: Максимальное время ожидания завершения задачи в секундах (по умолчанию 600)
        """
        self.base_url = base_url.rstrip('/')
        if instrument_types is None:
            instrument_types = ['shares']
        # Валидация и нормализация типов инструментов
        self.instrument_types = [
            self.INSTRUMENT_TYPES.get(it.lower(), it.lower())
            for it in instrument_types
            if it.lower() in self.INSTRUMENT_TYPES
        ]
        if not self.instrument_types:
            self.instrument_types = ['shares']  # По умолчанию
        self.poll_interval = poll_interval
        self.timeout = timeout
        self.results = []
        
    def generate_dates(self, start_date: str = None, end_date: str = None, year: int = None, exclude_weekends: bool = False) -> List[str]:
        """
        Генерирует список дат за указанный период или год.
        
        Args:
            start_date: Начальная дата в формате YYYY-MM-DD (если указана, используется вместо year)
            end_date: Конечная дата в формате YYYY-MM-DD (если указана, используется вместо year)
            year: Год для генерации дат (используется если start_date и end_date не указаны)
            exclude_weekends: Исключить выходные дни (суббота и воскресенье)
            
        Returns:
            Список дат в формате YYYY-MM-DD
        """
        dates = []
        
        if start_date and end_date:
            # Произвольный период
            try:
                start = datetime.strptime(start_date, '%Y-%m-%d')
                end = datetime.strptime(end_date, '%Y-%m-%d')
            except ValueError as e:
                raise ValueError(f"Неверный формат даты: {e}")
            
            if start > end:
                raise ValueError(f"Начальная дата ({start_date}) должна быть раньше конечной ({end_date})")
        elif year:
            # Год
            start = datetime(year, 1, 1)
            end = datetime(year, 12, 31)
        else:
            raise ValueError("Необходимо указать либо start-date и end-date, либо year")
        
        current_date = start
        while current_date <= end:
            if exclude_weekends:
                # Понедельник = 0, Воскресенье = 6
                if current_date.weekday() < 5:  # Понедельник-Пятница
                    dates.append(current_date.strftime('%Y-%m-%d'))
            else:
                dates.append(current_date.strftime('%Y-%m-%d'))
            current_date += timedelta(days=1)
            
        return dates
    
    def start_loading(self, date: str, instrument_type: str) -> Optional[str]:
        """
        Запускает загрузку дневных свечей для указанной даты и типа инструмента.
        
        Args:
            date: Дата в формате YYYY-MM-DD
            instrument_type: Тип инструмента (shares, indicatives, futures)
            
        Returns:
            taskId или None в случае ошибки
        """
        url = f"{self.base_url}/api/candles/daily/{instrument_type}/{date}"
        
        try:
            instrument_name = {
                'shares': 'акций',
                'indicatives': 'индикативов',
                'futures': 'фьючерсов'
            }.get(instrument_type, 'инструментов')
            
            print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] Запуск загрузки {instrument_name} для {date}...")
            response = requests.post(url, timeout=30)
            response.raise_for_status()
            
            data = response.json()
            
            if data.get('success') and data.get('taskId'):
                task_id = data['taskId']
                print(f"  ✓ Задача запущена: taskId={task_id}")
                return task_id
            else:
                print(f"  ✗ Ошибка: {data.get('message', 'Неизвестная ошибка')}")
                return None
                
        except requests.exceptions.RequestException as e:
            print(f"  ✗ Ошибка запроса: {e}")
            return None
        except json.JSONDecodeError as e:
            print(f"  ✗ Ошибка парсинга JSON: {e}")
            return None
    
    def check_status(self, task_id: str) -> Optional[Dict]:
        """
        Проверяет статус задачи.
        
        Args:
            task_id: Идентификатор задачи
            
        Returns:
            Словарь со статусом или None в случае ошибки
        """
        url = f"{self.base_url}/api/status/{task_id}"
        
        try:
            response = requests.get(url, timeout=10)
            response.raise_for_status()
            
            data = response.json()
            
            if data.get('success'):
                return {
                    'status': data.get('status'),
                    'message': data.get('message', ''),
                    'duration_ms': data.get('durationMs'),
                    'timestamp': data.get('timestamp')
                }
            else:
                print(f"  ✗ Ошибка получения статуса: {data.get('message', 'Неизвестная ошибка')}")
                return None
                
        except requests.exceptions.RequestException as e:
            print(f"  ✗ Ошибка запроса статуса: {e}")
            return None
        except json.JSONDecodeError as e:
            print(f"  ✗ Ошибка парсинга JSON статуса: {e}")
            return None
    
    def wait_for_completion(self, task_id: str, date: str) -> bool:
        """
        Ожидает завершения задачи.
        
        Args:
            task_id: Идентификатор задачи
            date: Дата для логирования
            
        Returns:
            True если задача завершена успешно, False в случае ошибки или таймаута
        """
        start_time = time.time()
        poll_count = 0
        
        while True:
            elapsed = time.time() - start_time
            
            # Проверка таймаута
            if elapsed > self.timeout:
                print(f"  ✗ Таймаут ожидания завершения задачи (>{self.timeout}с)")
                return False
            
            # Проверка статуса
            status_data = self.check_status(task_id)
            
            if status_data is None:
                # Если не удалось получить статус, продолжаем попытки
                poll_count += 1
                if poll_count > 10:  # После 10 неудачных попыток считаем ошибкой
                    print(f"  ✗ Не удалось получить статус после {poll_count} попыток")
                    return False
                time.sleep(self.poll_interval)
                continue
            
            status = status_data.get('status')
            duration_ms = status_data.get('duration_ms')
            
            if status == 'COMPLETED':
                duration_str = f"{duration_ms}ms" if duration_ms else "N/A"
                print(f"  ✓ Задача завершена успешно за {duration_str}")
                return True
            elif status == 'FAILED':
                message = status_data.get('message', 'Неизвестная ошибка')
                print(f"  ✗ Задача завершена с ошибкой: {message}")
                return False
            elif status == 'STARTED':
                # Задача еще выполняется
                elapsed_str = f"{int(elapsed)}с"
                print(f"  ⏳ Задача выполняется... (прошло {elapsed_str})", end='\r')
                time.sleep(self.poll_interval)
            else:
                # Неизвестный статус
                print(f"  ⚠ Неизвестный статус: {status}, продолжаем ожидание...")
                time.sleep(self.poll_interval)
    
    def process_date(self, date: str, instrument_type: str) -> Dict:
        """
        Обрабатывает одну дату для указанного типа инструмента: запускает загрузку и ждет завершения.
        
        Args:
            date: Дата в формате YYYY-MM-DD
            instrument_type: Тип инструмента (shares, indicatives, futures)
            
        Returns:
            Словарь с результатами обработки
        """
        result = {
            'date': date,
            'instrument_type': instrument_type,
            'success': False,
            'task_id': None,
            'error': None,
            'duration_seconds': 0
        }
        
        start_time = time.time()
        
        # Запуск загрузки
        task_id = self.start_loading(date, instrument_type)
        
        if task_id is None:
            result['error'] = 'Не удалось запустить загрузку'
            result['duration_seconds'] = time.time() - start_time
            return result
        
        result['task_id'] = task_id
        
        # Ожидание завершения
        success = self.wait_for_completion(task_id, date)
        
        result['success'] = success
        result['duration_seconds'] = time.time() - start_time
        
        if not success:
            result['error'] = 'Задача не завершилась успешно'
        
        return result
    
    def process_period(self, start_date: str = None, end_date: str = None, year: int = None, exclude_weekends: bool = False) -> None:
        """
        Обрабатывает все даты за указанный период или год для всех указанных типов инструментов.
        
        Args:
            start_date: Начальная дата в формате YYYY-MM-DD
            end_date: Конечная дата в формате YYYY-MM-DD
            year: Год для обработки (используется если start_date и end_date не указаны)
            exclude_weekends: Исключить выходные дни
        """
        dates = self.generate_dates(start_date=start_date, end_date=end_date, year=year, exclude_weekends=exclude_weekends)
        total_dates = len(dates)
        total_tasks = total_dates * len(self.instrument_types)
        
        instrument_names = {
            'shares': 'акций',
            'indicatives': 'индикативов',
            'futures': 'фьючерсов'
        }
        
        instrument_types_str = ', '.join([instrument_names.get(it, it) for it in self.instrument_types])
        
        period_str = f"{start_date} - {end_date}" if start_date and end_date else f"{year} год"
        
        print(f"\n{'='*60}")
        print(f"Начало загрузки дневных свечей: {instrument_types_str}")
        print(f"Период: {period_str}")
        print(f"Типы инструментов: {', '.join(self.instrument_types)}")
        print(f"Всего дат: {total_dates}")
        print(f"Всего задач: {total_tasks}")
        print(f"Базовый URL: {self.base_url}")
        print(f"Интервал проверки статуса: {self.poll_interval}с")
        print(f"Таймаут на задачу: {self.timeout}с")
        print(f"{'='*60}\n")
        
        successful = 0
        failed = 0
        task_idx = 0
        
        for date in dates:
            for instrument_type in self.instrument_types:
                task_idx += 1
                print(f"\n[{task_idx}/{total_tasks}] Обработка: {date} ({instrument_type})")
                
                result = self.process_date(date, instrument_type)
                self.results.append(result)
                
                if result['success']:
                    successful += 1
                else:
                    failed += 1
                
                # Небольшая пауза между запросами
                if task_idx < total_tasks:
                    time.sleep(1)
        
        # Итоговый отчет
        period_label = period_str
        self.print_summary(period_label, successful, failed)
    
    def print_summary(self, period_label: str, successful: int, failed: int) -> None:
        """
        Выводит итоговый отчет о выполнении.
        
        Args:
            period_label: Метка периода (год или диапазон дат)
            successful: Количество успешных операций
            failed: Количество неудачных операций
        """
        total = successful + failed
        total_duration = sum(r['duration_seconds'] for r in self.results)
        avg_duration = total_duration / total if total > 0 else 0
        
        # Статистика по типам инструментов
        stats_by_type = {}
        for result in self.results:
            inst_type = result.get('instrument_type', 'unknown')
            if inst_type not in stats_by_type:
                stats_by_type[inst_type] = {'success': 0, 'failed': 0}
            if result['success']:
                stats_by_type[inst_type]['success'] += 1
            else:
                stats_by_type[inst_type]['failed'] += 1
        
        print(f"\n{'='*60}")
        print(f"ИТОГОВЫЙ ОТЧЕТ за период: {period_label}")
        print(f"{'='*60}")
        print(f"Всего обработано: {total}")
        print(f"Успешно: {successful} ({successful/total*100:.1f}%)")
        print(f"Ошибок: {failed} ({failed/total*100:.1f}%)")
        print(f"Общее время: {int(total_duration)}с ({total_duration/60:.1f} мин)")
        print(f"Среднее время на задачу: {avg_duration:.1f}с")
        
        if len(stats_by_type) > 1:
            print(f"\nСтатистика по типам инструментов:")
            for inst_type, stats in stats_by_type.items():
                type_total = stats['success'] + stats['failed']
                print(f"  {inst_type}: успешно {stats['success']}/{type_total} ({stats['success']/type_total*100:.1f}%)")
        
        if failed > 0:
            print(f"\nЗадачи с ошибками:")
            for result in self.results:
                if not result['success']:
                    error_msg = result.get('error', 'Неизвестная ошибка')
                    inst_type = result.get('instrument_type', 'unknown')
                    print(f"  - {result['date']} ({inst_type}): {error_msg}")
        
        print(f"{'='*60}\n")


def main():
    """Основная функция."""
    parser = argparse.ArgumentParser(
        description='Автоматическая загрузка дневных свечей акций, индикативов и фьючерсов',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__
    )
    
    parser.add_argument(
        '--base-url',
        type=str,
        default='http://localhost:8083',
        help='Базовый URL API (по умолчанию: http://localhost:8083)'
    )
    
    parser.add_argument(
        '--test',
        action='store_true',
        help='Использовать тестовое окружение (http://localhost:8087)'
    )
    
    parser.add_argument(
        '--instrument-type',
        type=str,
        default=None,
        choices=['shares', 'indicatives', 'futures'],
        help='Тип инструмента: shares (акции), indicatives (индикативы), futures (фьючерсы). Используется если --instrument-types не указан (по умолчанию: shares)'
    )
    
    parser.add_argument(
        '--instrument-types',
        type=str,
        default=None,
        help='Список типов инструментов через запятую: shares,indicatives,futures (например: shares,indicatives,futures). Если указан, переопределяет --instrument-type'
    )
    
    parser.add_argument(
        '--start-date',
        type=str,
        default=None,
        help='Начальная дата периода в формате YYYY-MM-DD (например: 2026-01-15)'
    )
    
    parser.add_argument(
        '--end-date',
        type=str,
        default=None,
        help='Конечная дата периода в формате YYYY-MM-DD (например: 2026-01-20)'
    )
    
    parser.add_argument(
        '--exclude-weekends',
        action='store_true',
        help='Исключить выходные дни (суббота и воскресенье)'
    )
    
    parser.add_argument(
        '--poll-interval',
        type=int,
        default=3,
        help='Интервал проверки статуса в секундах (по умолчанию: 3)'
    )
    
    parser.add_argument(
        '--timeout',
        type=int,
        default=600,
        help='Максимальное время ожидания завершения задачи в секундах (по умолчанию: 600)'
    )
    
    parser.add_argument(
        '--year',
        type=int,
        default=2025,
        help='Год для обработки (по умолчанию: 2025). Используется если --start-date и --end-date не указаны'
    )
    
    args = parser.parse_args()
    
    # Определяем базовый URL
    if args.test:
        base_url = 'http://localhost:8087'
    else:
        base_url = args.base_url
    
    # Определяем типы инструментов
    if args.instrument_types:
        # Парсим список типов инструментов
        instrument_types = [it.strip() for it in args.instrument_types.split(',')]
    elif args.instrument_type:
        instrument_types = [args.instrument_type]
    else:
        instrument_types = ['shares']  # По умолчанию
    
    # Валидация периода
    if args.start_date and args.end_date:
        # Произвольный период
        start_date = args.start_date
        end_date = args.end_date
        year = None
        period_label = f"{start_date} - {end_date}"
    elif args.start_date or args.end_date:
        print("Ошибка: необходимо указать как --start-date, так и --end-date")
        sys.exit(1)
    else:
        # Год
        start_date = None
        end_date = None
        year = args.year
        period_label = f"{year} год"
    
    # Создаем загрузчик и запускаем обработку
    loader = DailyCandlesLoader(
        base_url=base_url,
        instrument_types=instrument_types,
        poll_interval=args.poll_interval,
        timeout=args.timeout
    )
    
    try:
        loader.process_period(
            start_date=start_date,
            end_date=end_date,
            year=year,
            exclude_weekends=args.exclude_weekends
        )
    except KeyboardInterrupt:
        print("\n\nПрервано пользователем")
        loader.print_summary(
            period_label,
            sum(1 for r in loader.results if r['success']),
            sum(1 for r in loader.results if not r['success'])
        )
        sys.exit(1)
    except Exception as e:
        print(f"\n\nКритическая ошибка: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == '__main__':
    main()
