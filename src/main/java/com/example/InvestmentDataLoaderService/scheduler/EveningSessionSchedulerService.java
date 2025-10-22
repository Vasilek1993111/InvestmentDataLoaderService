package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.dto.ClosePriceEveningSessionDto;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.entity.ClosePriceEveningSessionEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.IndicativeEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.entity.MinuteCandleEntity;
import com.example.InvestmentDataLoaderService.repository.ClosePriceEveningSessionRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.MinuteCandleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(EveningSessionSchedulerService.class);
    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final IndicativeRepository indicativeRepository;
    private final MinuteCandleRepository minuteCandleRepository;
    private final ClosePriceEveningSessionRepository closePriceEveningSessionRepository;

    public EveningSessionSchedulerService(ShareRepository shareRepository, 
                               FutureRepository futureRepository,
                               IndicativeRepository indicativeRepository,
                               MinuteCandleRepository minuteCandleRepository,
                               ClosePriceEveningSessionRepository closePriceEveningSessionRepository) {
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.indicativeRepository = indicativeRepository;
        this.minuteCandleRepository = minuteCandleRepository;
        this.closePriceEveningSessionRepository = closePriceEveningSessionRepository;
    }

    /**
     * Ежедневная загрузка цен закрытия вечерней сессии
     * Запускается в 1:40 по московскому времени
     */
    @Scheduled(cron = "0 40 1 * * *", zone = "Europe/Moscow")
    public void fetchAndStoreEveningSessionPrices() {
        try {
            LocalDate previousDay = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(1);
            String taskId = "EVENING_" + UUID.randomUUID().toString().substring(0, 8);
            
            log.info("=== НАЧАЛО ЗАГРУЗКИ ЦЕН ЗАКРЫТИЯ ВЕЧЕРНЕЙ СЕССИИ ===");
            log.info("[{}] Дата: {}", taskId, previousDay);
            log.info("[{}] Время запуска: {}", taskId, LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Проверяем, является ли предыдущий день выходным
            if (isWeekend(previousDay)) {
                String message = "Предыдущий день является выходным (" + previousDay + "). Вечерняя сессия не проводится в выходные дни.";
                log.info("[{}] {}", taskId, message);
                log.info("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ЗАКРЫТИЯ ВЕЧЕРНЕЙ СЕССИИ ===");
                return;
            }
            
            SaveResponseDto response = processEveningSessionPrices(previousDay, taskId);
            
            log.info("[{}] Загрузка завершена:", taskId);
            log.info("[{}] - Успех: {}", taskId, response.isSuccess());
            log.info("[{}] - Сообщение: {}", taskId, response.getMessage());
            log.info("[{}] - Всего запрошено: {}", taskId, response.getTotalRequested());
            log.info("[{}] - Сохранено новых: {}", taskId, response.getNewItemsSaved());
            log.info("[{}] - Пропущено существующих: {}", taskId, response.getExistingItemsSkipped());
            log.info("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ЗАКРЫТИЯ ВЕЧЕРНЕЙ СЕССИИ ===");
            
        } catch (Exception e) {
            log.error("Критическая ошибка в загрузке цен закрытия вечерней сессии", e);
        }
    }

    /**
     * Обрабатывает цены закрытия вечерней сессии для указанной даты
     */
    public SaveResponseDto processEveningSessionPrices(LocalDate date, String taskId) {
        try {
            log.info("[{}] Начало обработки цен закрытия вечерней сессии за {}", taskId, date);
            
            // Получаем все акции, фьючерсы и индикативные инструменты из БД
            List<ShareEntity> shares = shareRepository.findAll();
            List<FutureEntity> futures = futureRepository.findAll();
            List<IndicativeEntity> indicatives = indicativeRepository.findAll();
            log.info("[{}] Найдено {} акций, {} фьючерсов и {} индикативных инструментов для обработки", taskId, shares.size(), futures.size(), indicatives.size());
            
            List<ClosePriceEveningSessionDto> savedItems = new ArrayList<>();
            int totalRequested = 0;
            int savedCount = 0;
            int existingCount = 0;
            int processedInstruments = 0;
            
            // Обрабатываем акции
            for (ShareEntity share : shares) {
                try {
                    processedInstruments++;
                    log.info("[{}] Обработка акции {}/{}: {} ({})", taskId, processedInstruments, (shares.size() + futures.size()), share.getTicker(), share.getFigi());
                    
                    // Проверяем, есть ли уже запись для этой даты и FIGI
                    if (closePriceEveningSessionRepository.existsByPriceDateAndFigi(date, share.getFigi())) {
                        existingCount++;
                        log.info("[{}] Запись уже существует для {} за {}", taskId, share.getTicker(), date);
                        continue;
                    }
                    
            // Ищем последнюю минутную свечу за указанную дату
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
                        
                        log.info("[{}] Сохранена цена закрытия для {}: {}", taskId, share.getTicker(), lastClosePrice);
                    } else {
                        log.info("[{}] Не найдена свеча для {} за {}", taskId, share.getTicker(), date);
                    }
                    
                } catch (Exception e) {
                    log.error("[{}] Ошибка обработки акции {}", taskId, share.getTicker(), e);
                }
            }
            
            // Обрабатываем фьючерсы
            for (FutureEntity future : futures) {
                try {
                    processedInstruments++;
                    log.info("[{}] Обработка фьючерса {}/{}: {} ({})", taskId, processedInstruments, (shares.size() + futures.size()), future.getTicker(), future.getFigi());
                    
                    // Проверяем, есть ли уже запись для этой даты и FIGI
                    if (closePriceEveningSessionRepository.existsByPriceDateAndFigi(date, future.getFigi())) {
                        existingCount++;
                        log.info("[{}] Запись уже существует для {} за {}", taskId, future.getTicker(), date);
                        continue;
                    }
                    
            // Ищем последнюю минутную свечу за указанную дату
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
                        
                        log.info("[{}] Сохранена цена закрытия для {}: {}", taskId, future.getTicker(), lastClosePrice);
                    } else {
                        log.info("[{}] Не найдена свеча для {} за {}", taskId, future.getTicker(), date);
                    }
                    
                } catch (Exception e) {
                    log.error("[{}] Ошибка обработки фьючерса {}", taskId, future.getTicker(), e);
                }
            }
            
            // Обрабатываем индикативные инструменты
            for (IndicativeEntity indicative : indicatives) {
                // Пропускаем пустые или null FIGI
                if (indicative.getFigi() == null || indicative.getFigi().trim().isEmpty()) {
                    log.warn("[{}] Skipping empty or null FIGI for indicative: {}", taskId, indicative.getTicker());
                    continue;
                }
                
                try {
                    processedInstruments++;
                    log.info("[{}] Обработка индикативного инструмента {}/{}: {} ({})", taskId, processedInstruments, (shares.size() + futures.size() + indicatives.size()), indicative.getTicker(), indicative.getFigi());
                    
                    // Проверяем, есть ли уже запись для этой даты и FIGI
                    if (closePriceEveningSessionRepository.existsByPriceDateAndFigi(date, indicative.getFigi())) {
                        existingCount++;
                        log.info("[{}] Запись уже существует для {} за {}", taskId, indicative.getTicker(), date);
                        continue;
                    }
                    
            // Ищем последнюю минутную свечу за указанную дату
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
                        
                        log.info("[{}] Сохранена цена закрытия для {}: {}", taskId, indicative.getTicker(), lastClosePrice);
                    } else {
                        log.info("[{}] Не найдена свеча для {} за {}", taskId, indicative.getTicker(), date);
                    }
                    
                } catch (Exception e) {
                    log.error("[{}] Ошибка обработки индикативного инструмента {}", taskId, indicative.getTicker(), e);
                }
            }
            
            log.info("[{}] Обработка завершена:", taskId);
            log.info("[{}] - Обработано инструментов: {} (акций: {}, фьючерсов: {}, индикативных: {})", taskId, processedInstruments, shares.size(), futures.size(), indicatives.size());
            log.info("[{}] - Запрошено цен: {}", taskId, totalRequested);
            log.info("[{}] - Сохранено новых: {}", taskId, savedCount);
            log.info("[{}] - Пропущено существующих: {}", taskId, existingCount);
            
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
            log.error("[{}] Критическая ошибка в обработке цен закрытия вечерней сессии", taskId, e);
            return new SaveResponseDto(
                false,
                "Ошибка загрузки цен закрытия вечерней сессии: " + e.getMessage(),
                0,
                0,
                0,
                0,
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
            log.info("[{}] Поиск свечи для {} за дату (MSK): {}", taskId, figi, date);

            // Формируем точные границы суток в часовом поясе Europe/Moscow
            var mskZone = ZoneId.of("Europe/Moscow");
            var startInstant = date.atStartOfDay(mskZone).toInstant();
            var endInstant = date.plusDays(1).atStartOfDay(mskZone).toInstant();

            // Ищем минутные свечи по точному временному диапазону и берем последнюю по времени
            List<MinuteCandleEntity> candles = minuteCandleRepository.findByFigiAndTimeBetween(figi, startInstant, endInstant);

            if (!candles.isEmpty()) {
                MinuteCandleEntity lastCandle = candles.get(candles.size() - 1);
                log.info("[{}] Найдена минутная свеча ({}) для {} с ценой закрытия: {}", taskId, lastCandle.getTime(), figi, lastCandle.getClose());
                return lastCandle.getClose();
            } else {
                log.info("[{}] Свечи не найдены для {} за дату: {}", taskId, figi, date);
            }

            return null;

        } catch (Exception e) {
            log.error("[{}] Ошибка поиска последней цены закрытия для {}", taskId, figi, e);
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
            log.info("[{}] {}", taskId, message);
            return new SaveResponseDto(
                false,
                message,
                0,
                0,
                0,
                0,
                0, // missingFromApi
                new ArrayList<>()
            );
        }
        
        return processEveningSessionPrices(date, taskId);
    }
}