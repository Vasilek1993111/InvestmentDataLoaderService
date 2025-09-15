package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.dto.OpenPriceDto;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.entity.CandleEntity;
import com.example.InvestmentDataLoaderService.entity.OpenPriceEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.repository.OpenPriceRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.CandleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MorningSessionService {

    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final CandleRepository candleRepository;
    private final OpenPriceRepository openPriceRepository;

    public MorningSessionService(ShareRepository shareRepository, 
                               FutureRepository futureRepository,
                               CandleRepository candleRepository,
                               OpenPriceRepository openPriceRepository) {
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.candleRepository = candleRepository;
        this.openPriceRepository = openPriceRepository;
    }

    /**
     * Ежедневная загрузка цен открытия утренней сессии
     * Запускается в 2:01 по московскому времени
     */
    @Scheduled(cron = "0 1 2 * * *", zone = "Europe/Moscow")
    public void fetchAndStoreMorningSessionPrices() {
        try {
            LocalDate previousDay = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(1);
            String taskId = "MORNING_" + UUID.randomUUID().toString().substring(0, 8);
            
            System.out.println("=== НАЧАЛО ЗАГРУЗКИ ЦЕН ОТКРЫТИЯ УТРЕННЕЙ СЕССИИ ===");
            System.out.println("[" + taskId + "] Дата: " + previousDay);
            System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Проверяем, является ли предыдущий день выходным
            if (isWeekend(previousDay)) {
                String message = "Предыдущий день является выходным (" + previousDay + "). В выходные дни (суббота и воскресенье) нет цен открытия.";
                System.out.println("[" + taskId + "] " + message);
                System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ОТКРЫТИЯ УТРЕННЕЙ СЕССИИ ===");
                return;
            }
            
            SaveResponseDto response = processMorningSessionPrices(previousDay, taskId);
            
            System.out.println("[" + taskId + "] Загрузка завершена:");
            System.out.println("[" + taskId + "] - Успех: " + response.isSuccess());
            System.out.println("[" + taskId + "] - Сообщение: " + response.getMessage());
            System.out.println("[" + taskId + "] - Всего запрошено: " + response.getTotalRequested());
            System.out.println("[" + taskId + "] - Сохранено новых: " + response.getNewItemsSaved());
            System.out.println("[" + taskId + "] - Пропущено существующих: " + response.getExistingItemsSkipped());
            System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ОТКРЫТИЯ УТРЕННЕЙ СЕССИИ ===");
            
        } catch (Exception e) {
            System.err.println("Критическая ошибка в загрузке цен открытия утренней сессии: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Обрабатывает цены открытия утренней сессии для указанной даты
     */
    public SaveResponseDto processMorningSessionPrices(LocalDate date, String taskId) {
        try {
            System.out.println("[" + taskId + "] Начало обработки цен открытия утренней сессии за " + date);
            
            // Получаем все акции и фьючерсы из БД
            List<ShareEntity> shares = shareRepository.findAll();
            List<FutureEntity> futures = futureRepository.findAll();
            System.out.println("[" + taskId + "] Найдено " + shares.size() + " акций и " + futures.size() + " фьючерсов для обработки");
            
            List<OpenPriceDto> savedItems = new ArrayList<>();
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
                    if (openPriceRepository.existsByPriceDateAndFigi(date, share.getFigi())) {
                        existingCount++;
                        System.out.println("[" + taskId + "] Запись уже существует для " + share.getTicker() + " за " + date);
                        continue;
                    }
                    
                    // Ищем первую свечу за указанную дату до 06:59:59
                    BigDecimal firstOpenPrice = findFirstOpenPriceForDate(share.getFigi(), date, taskId, "share");
                    
                    if (firstOpenPrice != null) {
                        totalRequested++;
                        
                        // Создаем DTO для сохранения
                        OpenPriceDto dto = new OpenPriceDto(
                            share.getFigi(),
                            date,
                            firstOpenPrice,
                            "share",
                            "RUB",
                            "moex_mrng_evng_e_wknd_dlr"
                        );
                        
                        // Сохраняем в БД
                        OpenPriceEntity entity = new OpenPriceEntity();
                        entity.setId(new com.example.InvestmentDataLoaderService.entity.OpenPriceKey(date, share.getFigi()));
                        entity.setInstrumentType(dto.instrumentType());
                        entity.setOpenPrice(dto.openPrice());
                        entity.setCurrency(dto.currency());
                        entity.setExchange(dto.exchange());
                        
                        openPriceRepository.save(entity);
                        savedItems.add(dto);
                        savedCount++;
                        
                        System.out.println("[" + taskId + "] Сохранена цена открытия для " + share.getTicker() + ": " + firstOpenPrice);
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
                    if (openPriceRepository.existsByPriceDateAndFigi(date, future.getFigi())) {
                        existingCount++;
                        System.out.println("[" + taskId + "] Запись уже существует для " + future.getTicker() + " за " + date);
                        continue;
                    }
                    
                    // Ищем первую свечу за указанную дату до 08:59:59
                    BigDecimal firstOpenPrice = findFirstOpenPriceForDate(future.getFigi(), date, taskId, "future");
                    
                    if (firstOpenPrice != null) {
                        totalRequested++;
                        
                        // Создаем DTO для сохранения
                        OpenPriceDto dto = new OpenPriceDto(
                            future.getFigi(),
                            date,
                            firstOpenPrice,
                            "future",
                            "RUB",
                            "FORTS_EVENING"
                        );
                        
                        // Сохраняем в БД
                        OpenPriceEntity entity = new OpenPriceEntity();
                        entity.setId(new com.example.InvestmentDataLoaderService.entity.OpenPriceKey(date, future.getFigi()));
                        entity.setInstrumentType(dto.instrumentType());
                        entity.setOpenPrice(dto.openPrice());
                        entity.setCurrency(dto.currency());
                        entity.setExchange(dto.exchange());
                        
                        openPriceRepository.save(entity);
                        savedItems.add(dto);
                        savedCount++;
                        
                        System.out.println("[" + taskId + "] Сохранена цена открытия для " + future.getTicker() + ": " + firstOpenPrice);
                    } else {
                        System.out.println("[" + taskId + "] Не найдена свеча для " + future.getTicker() + " за " + date);
                    }
                    
                } catch (Exception e) {
                    System.err.println("[" + taskId + "] Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                }
            }
            
            System.out.println("[" + taskId + "] Обработка завершена:");
            System.out.println("[" + taskId + "] - Обработано инструментов: " + processedInstruments + " (акций: " + shares.size() + ", фьючерсов: " + futures.size() + ")");
            System.out.println("[" + taskId + "] - Запрошено цен: " + totalRequested);
            System.out.println("[" + taskId + "] - Сохранено новых: " + savedCount);
            System.out.println("[" + taskId + "] - Пропущено существующих: " + existingCount);
            
            return new SaveResponseDto(
                true,
                "Цены открытия утренней сессии загружены. Сохранено: " + savedCount + ", пропущено: " + existingCount,
                totalRequested,
                savedCount,
                existingCount,
                savedItems
            );
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Критическая ошибка в обработке цен открытия утренней сессии: " + e.getMessage());
            e.printStackTrace();
            return new SaveResponseDto(
                false,
                "Ошибка загрузки цен открытия утренней сессии: " + e.getMessage(),
                0,
                0,
                0,
                new ArrayList<>()
            );
        }
    }

    /**
     * Находит первую цену открытия для указанной даты и FIGI
     * Для акций: до 06:59:59 включительно
     * Для фьючерсов: до 08:59:59 включительно
     */
    private BigDecimal findFirstOpenPriceForDate(String figi, LocalDate date, String taskId, String instrumentType) {
        try {
            LocalTime cutoffTime = instrumentType.equals("share") ? 
                LocalTime.of(6, 59, 59) : LocalTime.of(8, 59, 59);
            
            System.out.println("[" + taskId + "] Поиск первой свечи для " + figi + " (" + instrumentType + ") за дату: " + date + " до " + cutoffTime);
            
            // Вычисляем следующий день для диапазона
            LocalDate nextDate = date.plusDays(1);
            
            // Ищем все свечи за указанную дату и берем первую (самую раннюю)
            List<CandleEntity> candles = candleRepository.findByFigiAndDateOrderByTimeDesc(figi, date, nextDate);
            
            if (!candles.isEmpty()) {
                // Фильтруем свечи до cutoffTime и берем самую раннюю
                CandleEntity firstCandle = null;
                
                for (CandleEntity candle : candles) {
                    LocalTime candleTime = candle.getTime().atZone(ZoneId.of("Europe/Moscow")).toLocalTime();
                    if (candleTime.isBefore(cutoffTime) || candleTime.equals(cutoffTime)) {
                        firstCandle = candle;
                        break; // Берем первую найденную свечу до cutoffTime
                    }
                }
                
                if (firstCandle != null) {
                    System.out.println("[" + taskId + "] Найдена первая свеча для " + figi + " с ценой открытия: " + firstCandle.getOpen());
                    return firstCandle.getOpen();
                } else {
                    System.out.println("[" + taskId + "] Не найдена свеча до " + cutoffTime + " для " + figi + " за дату: " + date);
                }
            } else {
                System.out.println("[" + taskId + "] Свечи не найдены для " + figi + " за дату: " + date);
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка поиска первой цены открытия для " + figi + ": " + e.getMessage());
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
     * Ручная загрузка цен открытия утренней сессии за конкретную дату
     */
    public SaveResponseDto fetchAndStoreMorningSessionPricesForDate(LocalDate date) {
        String taskId = "MANUAL_MORNING_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Проверяем, является ли дата выходным днем
        if (isWeekend(date)) {
            String message = "В выходные дни (суббота и воскресенье) нет цен открытия. Дата: " + date;
            System.out.println("[" + taskId + "] " + message);
            return new SaveResponseDto(
                false,
                message,
                0,
                0,
                0,
                new ArrayList<>()
            );
        }
        
        return processMorningSessionPrices(date, taskId);
    }
}
