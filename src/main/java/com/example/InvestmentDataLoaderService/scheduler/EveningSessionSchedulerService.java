package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.dto.ClosePriceEveningSessionDto;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.entity.CandleEntity;
import com.example.InvestmentDataLoaderService.entity.ClosePriceEveningSessionEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.IndicativeEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.repository.ClosePriceEveningSessionRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.CandleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EveningSessionSchedulerService {

    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final IndicativeRepository indicativeRepository;
    private final CandleRepository candleRepository;
    private final ClosePriceEveningSessionRepository closePriceEveningSessionRepository;

    public EveningSessionSchedulerService(ShareRepository shareRepository, 
                               FutureRepository futureRepository,
                               IndicativeRepository indicativeRepository,
                               CandleRepository candleRepository,
                               ClosePriceEveningSessionRepository closePriceEveningSessionRepository) {
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.indicativeRepository = indicativeRepository;
        this.candleRepository = candleRepository;
        this.closePriceEveningSessionRepository = closePriceEveningSessionRepository;
    }

    /**
     * Ежедневная загрузка цен закрытия вечерней сессии
     * Запускается в 2:00 по московскому времени
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Europe/Moscow")
    public void fetchAndStoreEveningSessionPrices() {
        try {
            LocalDate previousDay = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(1);
            String taskId = "EVENING_" + UUID.randomUUID().toString().substring(0, 8);
            
            System.out.println("=== НАЧАЛО ЗАГРУЗКИ ЦЕН ЗАКРЫТИЯ ВЕЧЕРНЕЙ СЕССИИ ===");
            System.out.println("[" + taskId + "] Дата: " + previousDay);
            System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Проверяем, является ли предыдущий день выходным
            if (isWeekend(previousDay)) {
                String message = "Предыдущий день является выходным (" + previousDay + "). Вечерняя сессия не проводится в выходные дни.";
                System.out.println("[" + taskId + "] " + message);
                System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ЗАКРЫТИЯ ВЕЧЕРНЕЙ СЕССИИ ===");
                return;
            }
            
            SaveResponseDto response = processEveningSessionPrices(previousDay, taskId);
            
            System.out.println("[" + taskId + "] Загрузка завершена:");
            System.out.println("[" + taskId + "] - Успех: " + response.isSuccess());
            System.out.println("[" + taskId + "] - Сообщение: " + response.getMessage());
            System.out.println("[" + taskId + "] - Всего запрошено: " + response.getTotalRequested());
            System.out.println("[" + taskId + "] - Сохранено новых: " + response.getNewItemsSaved());
            System.out.println("[" + taskId + "] - Пропущено существующих: " + response.getExistingItemsSkipped());
            System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ЗАКРЫТИЯ ВЕЧЕРНЕЙ СЕССИИ ===");
            
        } catch (Exception e) {
            System.err.println("Критическая ошибка в загрузке цен закрытия вечерней сессии: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Обрабатывает цены закрытия вечерней сессии для указанной даты
     */
    public SaveResponseDto processEveningSessionPrices(LocalDate date, String taskId) {
        try {
            System.out.println("[" + taskId + "] Начало обработки цен закрытия вечерней сессии за " + date);
            
            // Получаем все акции, фьючерсы и индикативные инструменты из БД
            List<ShareEntity> shares = shareRepository.findAll();
            List<FutureEntity> futures = futureRepository.findAll();
            List<IndicativeEntity> indicatives = indicativeRepository.findAll();
            System.out.println("[" + taskId + "] Найдено " + shares.size() + " акций, " + futures.size() + " фьючерсов и " + indicatives.size() + " индикативных инструментов для обработки");
            
            List<ClosePriceEveningSessionDto> savedItems = new ArrayList<>();
            int totalRequested = 0;
            int savedCount = 0;
            int existingCount = 0;
            int processedInstruments = 0;
            
            // Обрабатываем акции
            for (ShareEntity share : shares) {
                try {
                    processedInstruments++;
                    System.out.println("[" + taskId + "] Обработка акции " + processedInstruments + "/" + (shares.size() + futures.size()) + ": " + share.getTicker() + " (" + share.getFigi() + ")");
                    
                    // Проверяем, есть ли уже запись для этой даты и FIGI
                    if (closePriceEveningSessionRepository.existsByPriceDateAndFigi(date, share.getFigi())) {
                        existingCount++;
                        System.out.println("[" + taskId + "] Запись уже существует для " + share.getTicker() + " за " + date);
                        continue;
                    }
                    
                    // Ищем последнюю свечу за указанную дату
                    BigDecimal lastClosePrice = findLastClosePriceForDate(share.getFigi(), date, taskId);
                    
                    if (lastClosePrice != null) {
                        totalRequested++;
                        
                        // Создаем DTO для сохранения
                        ClosePriceEveningSessionDto dto = new ClosePriceEveningSessionDto(
                            date,
                            share.getFigi(),
                            lastClosePrice,
                            "share",
                            "RUB",
                            "moex_mrng_evng_e_wknd_dlr"
                        );
                        
                        // Сохраняем в БД
                        ClosePriceEveningSessionEntity entity = new ClosePriceEveningSessionEntity();
                        entity.setFigi(dto.figi());
                        entity.setPriceDate(dto.priceDate());
                        entity.setClosePrice(dto.closePrice());
                        entity.setInstrumentType(dto.instrumentType());
                        entity.setCurrency(dto.currency());
                        entity.setExchange(dto.exchange());
                        
                        closePriceEveningSessionRepository.save(entity);
                        savedItems.add(dto);
                        savedCount++;
                        
                        System.out.println("[" + taskId + "] Сохранена цена закрытия для " + share.getTicker() + ": " + lastClosePrice);
                    } else {
                        System.out.println("[" + taskId + "] Не найдена свеча для " + share.getTicker() + " за " + date);
                    }
                    
                } catch (Exception e) {
                    System.err.println("[" + taskId + "] Ошибка обработки акции " + share.getTicker() + ": " + e.getMessage());
                }
            }
            
            // Обрабатываем фьючерсы
            for (FutureEntity future : futures) {
                try {
                    processedInstruments++;
                    System.out.println("[" + taskId + "] Обработка фьючерса " + processedInstruments + "/" + (shares.size() + futures.size()) + ": " + future.getTicker() + " (" + future.getFigi() + ")");
                    
                    // Проверяем, есть ли уже запись для этой даты и FIGI
                    if (closePriceEveningSessionRepository.existsByPriceDateAndFigi(date, future.getFigi())) {
                        existingCount++;
                        System.out.println("[" + taskId + "] Запись уже существует для " + future.getTicker() + " за " + date);
                        continue;
                    }
                    
                    // Ищем последнюю свечу за указанную дату
                    BigDecimal lastClosePrice = findLastClosePriceForDate(future.getFigi(), date, taskId);
                    
                    if (lastClosePrice != null) {
                        totalRequested++;
                        
                        // Создаем DTO для сохранения
                        ClosePriceEveningSessionDto dto = new ClosePriceEveningSessionDto(
                            date,
                            future.getFigi(),
                            lastClosePrice,
                            "future",
                            "RUB",
                            "FORTS_EVENING"
                        );
                        
                        // Сохраняем в БД
                        ClosePriceEveningSessionEntity entity = new ClosePriceEveningSessionEntity();
                        entity.setFigi(dto.figi());
                        entity.setPriceDate(dto.priceDate());
                        entity.setClosePrice(dto.closePrice());
                        entity.setInstrumentType(dto.instrumentType());
                        entity.setCurrency(dto.currency());
                        entity.setExchange(dto.exchange());
                        
                        closePriceEveningSessionRepository.save(entity);
                        savedItems.add(dto);
                        savedCount++;
                        
                        System.out.println("[" + taskId + "] Сохранена цена закрытия для " + future.getTicker() + ": " + lastClosePrice);
                    } else {
                        System.out.println("[" + taskId + "] Не найдена свеча для " + future.getTicker() + " за " + date);
                    }
                    
                } catch (Exception e) {
                    System.err.println("[" + taskId + "] Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                }
            }
            
            // Обрабатываем индикативные инструменты
            for (IndicativeEntity indicative : indicatives) {
                // Пропускаем пустые или null FIGI
                if (indicative.getFigi() == null || indicative.getFigi().trim().isEmpty()) {
                    System.err.println("[" + taskId + "] Skipping empty or null FIGI for indicative: " + indicative.getTicker());
                    continue;
                }
                
                try {
                    processedInstruments++;
                    System.out.println("[" + taskId + "] Обработка индикативного инструмента " + processedInstruments + "/" + (shares.size() + futures.size() + indicatives.size()) + ": " + indicative.getTicker() + " (" + indicative.getFigi() + ")");
                    
                    // Проверяем, есть ли уже запись для этой даты и FIGI
                    if (closePriceEveningSessionRepository.existsByPriceDateAndFigi(date, indicative.getFigi())) {
                        existingCount++;
                        System.out.println("[" + taskId + "] Запись уже существует для " + indicative.getTicker() + " за " + date);
                        continue;
                    }
                    
                    // Ищем последнюю свечу за указанную дату
                    BigDecimal lastClosePrice = findLastClosePriceForDate(indicative.getFigi(), date, taskId);
                    
                    if (lastClosePrice != null) {
                        totalRequested++;
                        
                        // Создаем DTO для сохранения
                        ClosePriceEveningSessionDto dto = new ClosePriceEveningSessionDto(
                            date,
                            indicative.getFigi(),
                            lastClosePrice,
                            "indicative",
                            indicative.getCurrency() != null ? indicative.getCurrency() : "USD",
                            "MOEX"
                        );
                        
                        // Сохраняем в БД
                        ClosePriceEveningSessionEntity entity = new ClosePriceEveningSessionEntity();
                        entity.setFigi(dto.figi());
                        entity.setPriceDate(dto.priceDate());
                        entity.setClosePrice(dto.closePrice());
                        entity.setInstrumentType(dto.instrumentType());
                        entity.setCurrency(dto.currency());
                        entity.setExchange(dto.exchange());
                        
                        closePriceEveningSessionRepository.save(entity);
                        savedItems.add(dto);
                        savedCount++;
                        
                        System.out.println("[" + taskId + "] Сохранена цена закрытия для " + indicative.getTicker() + ": " + lastClosePrice);
                    } else {
                        System.out.println("[" + taskId + "] Не найдена свеча для " + indicative.getTicker() + " за " + date);
                    }
                    
                } catch (Exception e) {
                    System.err.println("[" + taskId + "] Ошибка обработки индикативного инструмента " + indicative.getTicker() + ": " + e.getMessage());
                }
            }
            
            System.out.println("[" + taskId + "] Обработка завершена:");
            System.out.println("[" + taskId + "] - Обработано инструментов: " + processedInstruments + " (акций: " + shares.size() + ", фьючерсов: " + futures.size() + ", индикативных: " + indicatives.size() + ")");
            System.out.println("[" + taskId + "] - Запрошено цен: " + totalRequested);
            System.out.println("[" + taskId + "] - Сохранено новых: " + savedCount);
            System.out.println("[" + taskId + "] - Пропущено существующих: " + existingCount);
            
            return new SaveResponseDto(
                true,
                "Цены закрытия вечерней сессии загружены. Сохранено: " + savedCount + ", пропущено: " + existingCount,
                totalRequested,
                savedCount,
                existingCount,
                0, // invalidItemsFiltered
                0, // missingFromApi
                savedItems
            );
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Критическая ошибка в обработке цен закрытия вечерней сессии: " + e.getMessage());
            e.printStackTrace();
            return new SaveResponseDto(
                false,
                "Ошибка загрузки цен закрытия вечерней сессии: " + e.getMessage(),
                0,
                0,
                0,
                0, // invalidItemsFiltered
                0, // missingFromApi
                new ArrayList<>()
            );
        }
    }

    /**
     * Находит последнюю цену закрытия для указанной даты и FIGI
     */
    private BigDecimal findLastClosePriceForDate(String figi, LocalDate date, String taskId) {
        try {
            System.out.println("[" + taskId + "] Поиск свечи для " + figi + " за дату (MSK): " + date);

            // Формируем точные границы суток в часовом поясе Europe/Moscow
            var mskZone = ZoneId.of("Europe/Moscow");
            var startInstant = date.atStartOfDay(mskZone).toInstant();
            var endInstant = date.plusDays(1).atStartOfDay(mskZone).toInstant();

            // Ищем свечи по точному временному диапазону и берем последнюю по времени
            List<CandleEntity> candles = candleRepository.findByFigiAndTimeBetween(figi, startInstant, endInstant);

            if (!candles.isEmpty()) {
                CandleEntity lastCandle = candles.get(candles.size() - 1);
                System.out.println("[" + taskId + "] Найдена свеча (" + lastCandle.getTime() + ") для " + figi + " с ценой закрытия: " + lastCandle.getClose());
                return lastCandle.getClose();
            } else {
                System.out.println("[" + taskId + "] Свечи не найдены для " + figi + " за дату: " + date);
            }

            return null;

        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка поиска последней цены закрытия для " + figi + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Проверяет, является ли дата выходным днем
     */
    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * Ручная загрузка цен закрытия вечерней сессии за конкретную дату
     */
    public SaveResponseDto fetchAndStoreEveningSessionPricesForDate(LocalDate date) {
        String taskId = "MANUAL_EVENING_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Проверяем, является ли дата выходным днем
        if (isWeekend(date)) {
            String message = "В выходные дни (суббота и воскресенье) вечерняя сессия не проводится. Дата: " + date;
            System.out.println("[" + taskId + "] " + message);
            return new SaveResponseDto(
                false,
                message,
                0,
                0,
                0,
                0, // invalidItemsFiltered
                0, // missingFromApi
                new ArrayList<>()
            );
        }
        
        return processEveningSessionPrices(date, taskId);
    }
}