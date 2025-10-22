package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.entity.DividendEntity;
import com.example.InvestmentDataLoaderService.repository.DividendRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;


import com.example.InvestmentDataLoaderService.client.TinkoffApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DividendService {
    
    private static final Logger log = LoggerFactory.getLogger(DividendService.class);
    
    @Autowired
    private DividendRepository dividendRepository;
    
    @Autowired
    private ShareRepository shareRepository;
    
    @Autowired
    private TinkoffApiClient tinkoffApiClient;
    
    @Transactional
    public Map<String, Object> loadDividendsForInstruments(List<String> instruments, LocalDate from, LocalDate to) {
        Map<String, Object> result = new HashMap<>();
        int totalLoaded = 0;
        int totalFromApi = 0;
        int alreadyExists = 0;
        int processedInstruments = 0;
        int errorInstruments = 0;

        // Обрабатываем список инструментов
        List<String> figisToProcess = new ArrayList<>();

        for (String instrument : instruments) {
            if ("SHARES".equalsIgnoreCase(instrument)) {
                // Если передано ключевое слово SHARES, загружаем все акции
                List<String> allShareFigis = shareRepository.findAllFigis();
                figisToProcess.addAll(allShareFigis);
                log.info("Загружаем дивиденды для всех акций ({} инструментов)", allShareFigis.size());
            } else {
                // Обычный FIGI инструмента
                figisToProcess.add(instrument);
            }
        }

        // Загружаем дивиденды для всех FIGI
        for (String figi : figisToProcess) {
            try {
                // Задержка для соблюдения лимитов API
                Thread.sleep(200);
                
                // Получаем дивиденды из T-Bank API
                List<DividendEntity> dividends = tinkoffApiClient.getDividends(figi, from, to);
                totalFromApi += dividends.size();

                for (DividendEntity dividend : dividends) {
                    // Проверяем, не существует ли уже такая запись
                    if (!dividendRepository.existsByFigiAndRecordDate(dividend.getFigi(), dividend.getRecordDate())) {
                        dividendRepository.save(dividend);
                        totalLoaded++;
                    } else {
                        alreadyExists++;
                    }
                }
                
                processedInstruments++;
            } catch (Exception e) {
                log.error("Ошибка загрузки дивидендов для {}", figi, e);
                errorInstruments++;
            }
        }

        result.put("success", true);
        result.put("from", from.toString());
        result.put("to", to.toString());
        result.put("processedInstruments", processedInstruments);
        result.put("errorInstruments", errorInstruments);
        result.put("totalFromApi", totalFromApi);
        result.put("totalLoaded", totalLoaded);
        result.put("alreadyExists", alreadyExists);

        if (totalLoaded > 0) {
            result.put("message", "Успешно загружено " + totalLoaded + " новых записей о дивидендах для " + processedInstruments + " инструментов");
        } else if (alreadyExists > 0) {
            result.put("message", "Дивиденды уже существуют в БД (" + alreadyExists + " записей для " + processedInstruments + " инструментов)");
        } else {
            result.put("message", "Дивиденды не найдены в указанном периоде для " + processedInstruments + " инструментов");
        }

        return result;
    }
    
    /**
     * Получение статистики по дивидендам для инструмента
     */
    public Map<String, Object> getDividendStats(String figi) {
        Map<String, Object> stats = new HashMap<>();
        
        List<DividendEntity> allDividends = dividendRepository.findByFigiOrderByRecordDateDesc(figi);
        
        if (allDividends.isEmpty()) {
            stats.put("totalDividends", 0);
            stats.put("message", "Дивиденды не найдены");
            return stats;
        }
        
        // Общая статистика
        stats.put("totalDividends", allDividends.size());
        
        // Последний дивиденд
        DividendEntity lastDividend = allDividends.get(0);
        stats.put("lastDividend", Map.of(
            "recordDate", lastDividend.getRecordDate(),
            "paymentDate", lastDividend.getPaymentDate(),
            "value", lastDividend.getDividendValue(),
            "currency", lastDividend.getCurrency()
        ));
        
        // Статистика по годам
        Map<Integer, Integer> dividendsByYear = new HashMap<>();
        Map<Integer, BigDecimal> totalValueByYear = new HashMap<>();
        
        for (DividendEntity dividend : allDividends) {
            int year = dividend.getRecordDate().getYear();
            dividendsByYear.put(year, dividendsByYear.getOrDefault(year, 0) + 1);
            
            if (dividend.getDividendValue() != null) {
                BigDecimal currentTotal = totalValueByYear.getOrDefault(year, BigDecimal.ZERO);
                totalValueByYear.put(year, currentTotal.add(dividend.getDividendValue()));
            }
        }
        
        stats.put("dividendsByYear", dividendsByYear);
        stats.put("totalValueByYear", totalValueByYear);
        
        // Средний размер дивиденда
        BigDecimal totalValue = allDividends.stream()
            .filter(d -> d.getDividendValue() != null)
            .map(DividendEntity::getDividendValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int countWithValue = (int) allDividends.stream()
            .filter(d -> d.getDividendValue() != null)
            .count();
        
        if (countWithValue > 0) {
            BigDecimal averageDividend = totalValue.divide(BigDecimal.valueOf(countWithValue), 2, java.math.RoundingMode.HALF_UP);
            stats.put("averageDividend", averageDividend);
        }
        
        return stats;
    }
    
    /**
     * Загрузка дивидендов для всех акций
     */
    @Transactional
    public int loadDividendsForAllShares(LocalDate from, LocalDate to) {
        // Получаем все FIGI акций из базы
        // Это нужно будет реализовать через ShareRepository
        // List<String> allShareFigis = shareRepository.findAllFigis();
        // return loadDividendsForInstruments(allShareFigis, from, to);
        
        // Пока возвращаем 0, пока не подключим ShareRepository
        return 0;
    }
    
    /**
     * Загрузка дивидендов для одного инструмента с подробной статистикой
     */
    public Map<String, Object> loadDividendsForSingleInstrument(String figi, LocalDate from, LocalDate to) {
        Map<String, Object> result = new HashMap<>();
        int totalLoaded = 0;
        int totalFromApi = 0;
        int alreadyExists = 0;
        
        try {
            // Получаем дивиденды из T-Bank API
            List<DividendEntity> dividends = tinkoffApiClient.getDividends(figi, from, to);
            totalFromApi = dividends.size();
            
            for (DividendEntity dividend : dividends) {
                // Проверяем, не существует ли уже такая запись
                if (!dividendRepository.existsByFigiAndRecordDate(dividend.getFigi(), dividend.getRecordDate())) {
                    dividendRepository.save(dividend);
                    totalLoaded++;
                } else {
                    alreadyExists++;
                }
            }
            
            result.put("success", true);
            result.put("figi", figi);
            result.put("from", from.toString());
            result.put("to", to.toString());
            result.put("totalFromApi", totalFromApi);
            result.put("totalLoaded", totalLoaded);
            result.put("alreadyExists", alreadyExists);
            
            if (totalLoaded > 0) {
                result.put("message", "Успешно загружено " + totalLoaded + " новых записей о дивидендах для " + figi);
            } else if (alreadyExists > 0) {
                result.put("message", "Дивиденды для " + figi + " уже существуют в БД (" + alreadyExists + " записей)");
            } else {
                result.put("message", "Дивиденды для " + figi + " не найдены в указанном периоде");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Ошибка загрузки дивидендов для " + figi + ": " + e.getMessage());
            result.put("figi", figi);
            result.put("from", from.toString());
            result.put("to", to.toString());
            result.put("totalFromApi", 0);
            result.put("totalLoaded", 0);
            result.put("alreadyExists", 0);
        }
        
        return result;
    }
    
   /**
 * Загрузка дивидендов для указанных инструментов в БД
 */
public Map<String, Object> loadDividendsForAllSharesToDb(List<String> instruments, LocalDate from, LocalDate to) {
    Map<String, Object> result = new HashMap<>();
    int totalLoaded = 0;
    int totalFromApi = 0;
    int alreadyExists = 0;
    int processedInstruments = 0;
    int errorInstruments = 0;

    // Обрабатываем список инструментов
    List<String> figisToProcess = new ArrayList<>();

    for (String instrument : instruments) {
        if ("SHARES".equalsIgnoreCase(instrument)) {
            // Если передано ключевое слово SHARES, загружаем все акции
            List<String> allShareFigis = shareRepository.findAllFigis();
            figisToProcess.addAll(allShareFigis);
            System.out.println("Загружаем дивиденды для всех акций (" + allShareFigis.size() + " инструментов)");
        } else {
            // Обычный FIGI инструмента
            figisToProcess.add(instrument);
        }
    }

    System.out.println("Всего инструментов для обработки: " + figisToProcess.size());

    // Загружаем дивиденды для всех FIGI
    for (String figi : figisToProcess) {
        try {
            // Каждый инструмент в отдельной транзакции
            Map<String, Object> instrumentResult = loadDividendsForSingleInstrument(figi, from, to);
            
            if ((Boolean) instrumentResult.get("success")) {
                totalFromApi += (Integer) instrumentResult.get("totalFromApi");
                totalLoaded += (Integer) instrumentResult.get("totalLoaded");
                alreadyExists += (Integer) instrumentResult.get("alreadyExists");
                processedInstruments++;
            } else {
                errorInstruments++;
            }
            
            // Логируем прогресс каждые 50 инструментов
            if (processedInstruments % 50 == 0) {
                System.out.println("Обработано " + processedInstruments + " из " + figisToProcess.size() + " инструментов");
            }
            
        } catch (Exception e) {
            System.err.println("Ошибка загрузки дивидендов для " + figi + ": " + e.getMessage());
            errorInstruments++;
        }
    }

    result.put("success", true);
    result.put("from", from.toString());
    result.put("to", to.toString());
    result.put("requestedInstruments", instruments);
    result.put("processedInstruments", processedInstruments);
    result.put("errorInstruments", errorInstruments);
    result.put("totalFromApi", totalFromApi);
    result.put("totalLoaded", totalLoaded);
    result.put("alreadyExists", alreadyExists);

    if (totalLoaded > 0) {
        result.put("message", "Успешно загружено " + totalLoaded + " новых записей о дивидендах для " + processedInstruments + " инструментов");
    } else if (alreadyExists > 0) {
        result.put("message", "Дивиденды уже существуют в БД (" + alreadyExists + " записей для " + processedInstruments + " инструментов)");
    } else {
        result.put("message", "Дивиденды не найдены в указанном периоде для " + processedInstruments + " инструментов");
    }

    return result;
}
   
    /**
     * Получение дивидендов для всех акций от T-API без сохранения в БД
     */
    public List<DividendEntity> getDividendsForAllSharesFromApi(LocalDate from, LocalDate to) {
        List<DividendEntity> allDividends = new ArrayList<>();
        
        try {
            // Получаем все FIGI акций
            List<String> allShareFigis = shareRepository.findAllFigis();
            log.info("Получаем дивиденды для всех акций ({} инструментов)", allShareFigis.size());
            
            // Получаем дивиденды для каждого FIGI
            for (String figi : allShareFigis) {
                try {
                    // Задержка для соблюдения лимитов API
                    Thread.sleep(300);
                    
                    List<DividendEntity> dividends = tinkoffApiClient.getDividends(figi, from, to);
                    allDividends.addAll(dividends);
                } catch (Exception e) {
                    log.error("Ошибка получения дивидендов от T-API для {}", figi, e);
                }
            }
            
        } catch (Exception e) {
            log.error("Ошибка получения списка акций", e);
        }
        
        return allDividends;
    }
    
    /**
     * Получение дивидендов напрямую от T-API без сохранения в БД
     */
    public List<DividendEntity> getDividendsFromApi(String figi, LocalDate from, LocalDate to) {
        try {
            return tinkoffApiClient.getDividends(figi, from, to);
        } catch (Exception e) {
            log.error("Ошибка получения дивидендов от T-API для {}", figi, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Проверка существования дивидендов для инструмента
     */
    public boolean hasDividends(String figi) {
        return !dividendRepository.findByFigiOrderByRecordDateDesc(figi).isEmpty();
    }
    
}
