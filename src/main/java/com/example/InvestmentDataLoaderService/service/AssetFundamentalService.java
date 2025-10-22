package com.example.InvestmentDataLoaderService.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.InvestmentDataLoaderService.client.TinkoffApiClient;
import com.example.InvestmentDataLoaderService.dto.AssetFundamentalDto;
import com.example.InvestmentDataLoaderService.entity.AssetFundamentalEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.repository.AssetFundamentalsRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.util.AssetFundamentalMapper;

@Service
public class AssetFundamentalService {
    private static final Logger log = LoggerFactory.getLogger(AssetFundamentalService.class);
    private final AssetFundamentalsRepository assetFundamentalsRepository;
    private final TinkoffApiClient tinkoffApiClient;
    private final AssetFundamentalMapper mapper;
    private final ShareRepository sharesRepo;

    public AssetFundamentalService(AssetFundamentalsRepository assetFundamentalsRepository, 
                                 TinkoffApiClient tinkoffApiClient,
                                 AssetFundamentalMapper mapper,
                                 ShareRepository sharesRepo) {
        this.assetFundamentalsRepository = assetFundamentalsRepository;
        this.tinkoffApiClient = tinkoffApiClient;
        this.mapper = mapper;
        this.sharesRepo = sharesRepo;
    }


    /**
     * Получить фундаментальные показатели для одного актива
     * @param assetUid идентификатор актива
     * @return список фундаментальных показателей
     * @throws IllegalArgumentException если assetUid некорректный
     */
    public List<AssetFundamentalDto> getFundamentalsForAsset(String assetUid) {
        // Валидация входного параметра
        if (!StringUtils.hasText(assetUid)) {
            throw new IllegalArgumentException("AssetUid не может быть пустым или null");
        }
        
        try {
            log.info("Запрос фундаментальных показателей для актива: " + assetUid);
            List<AssetFundamentalDto> result = tinkoffApiClient.getAssetFundamentals(List.of(assetUid));
            
            if (result == null) {
                log.info("API вернул null для актива: " + assetUid);
                return new ArrayList<>();
            }
            
            log.info("Получено " + result.size() + " записей для актива: " + assetUid);
            return result;
            
        } catch (Exception e) {
            log.error("Ошибка при получении фундаментальных показателей для актива " + assetUid + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Получить фундаментальные показатели для списка активов
     * @param assetUids список идентификаторов активов
     * @return список фундаментальных показателей
     */
    public List<AssetFundamentalDto> getFundamentalsForAssets(List<String> assetUids) {
        // Валидация входных параметров
        if (assetUids == null || assetUids.isEmpty()) {
            log.info("Получен пустой список активов");
            return new ArrayList<>();
        }
        
        List<String> allAssetIds = new ArrayList<>();
        
        try {
            // Обработка специального случая "shares"
            for (String assetUid : assetUids) {
                if (assetUid == null) {
                    log.info("Пропущен null assetUid");
                    continue;
                }
                
                if ("shares".equalsIgnoreCase(assetUid.trim())) {
                    log.info("Обработка специального случая 'shares'");
                    List<ShareEntity> shares = sharesRepo.findAll();
                    
                    if (shares == null || shares.isEmpty()) {
                        log.info("Не найдено акций в базе данных");
                        continue;
                    }
                    
                    for (ShareEntity share : shares) {
                        String assetId = share.getAssetUid();
                        if (StringUtils.hasText(assetId)) {
                            allAssetIds.add(assetId.trim());
                        }
                    }
                    log.info("Добавлено " + shares.size() + " акций для обработки");
                } else if (StringUtils.hasText(assetUid)) {
                    allAssetIds.add(assetUid.trim());
                }
            }
            
            if (allAssetIds.isEmpty()) {
                log.info("Не найдено активов для обработки");
                return new ArrayList<>();
            }
            
            log.info("Всего активов для обработки: " + allAssetIds.size());
            
            List<AssetFundamentalDto> allFundamentals = new ArrayList<>();
            
            // Разбиваем на батчи по 100 элементов
            int batchSize = 100;
            int totalBatches = (allAssetIds.size() + batchSize - 1) / batchSize;
            
            for (int i = 0; i < allAssetIds.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, allAssetIds.size());
                List<String> batch = allAssetIds.subList(i, endIndex);
                int batchNumber = (i / batchSize) + 1;
                
                try {
                    log.info("Обработка батча " + batchNumber + " из " + totalBatches + 
                        " (элементов: " + batch.size() + ")");
                    
                    List<AssetFundamentalDto> batchFundamentals = tinkoffApiClient.getAssetFundamentals(batch);
                    
                    if (batchFundamentals != null && !batchFundamentals.isEmpty()) {
                        allFundamentals.addAll(batchFundamentals);
                        log.info("Батч " + batchNumber + " успешно обработан: получено " + 
                            batchFundamentals.size() + " записей");
                    } else {
                        log.info("Батч " + batchNumber + " не вернул данных");
                    }
                    
                    // Небольшая задержка между батчами для соблюдения лимитов API
                    if (batchNumber < totalBatches) {
                        Thread.sleep(500);
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Прервана обработка батча " + batchNumber);
                    break;
                } catch (Exception e) {
                    log.error("Ошибка при обработке батча " + batchNumber + ": " + e.getMessage());
                    e.printStackTrace();
                    // Продолжаем обработку следующих батчей
                }
            }
            
            log.info("Обработка завершена. Всего получено записей: " + allFundamentals.size());
            return allFundamentals;
            
        } catch (Exception e) {
            log.error("Критическая ошибка в getFundamentalsForAssets: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Сохранить фундаментальные показатели с принудительным обновлением
     * @param allFundamentals список DTO для сохранения
     * @throws IllegalArgumentException если список некорректный
     */
    @Transactional
    public void saveAssetFundamentals(List<AssetFundamentalDto> allFundamentals) {
        // Валидация входных параметров
        if (allFundamentals == null || allFundamentals.isEmpty()) {
            log.info("Получен пустой список фундаментальных показателей для сохранения");
            return;
        }
        
        log.info("Начало сохранения " + allFundamentals.size() + " записей фундаментальных показателей");
        
        try {
            // Конвертируем DTO в Entity
            List<AssetFundamentalEntity> listEntities = mapper.toEntityList(allFundamentals);
            
            if (listEntities == null || listEntities.isEmpty()) {
                log.info("Не удалось конвертировать DTO в Entity");
                return;
            }
            
            int updatedCount = 0;
            int createdCount = 0;
            int errorCount = 0;
            
            // Обрабатываем каждую запись
            for (AssetFundamentalEntity entity : listEntities) {
                try {
                    String assetUid = entity.getAssetUid();
                    
                    // Валидация assetUid
                    if (!StringUtils.hasText(assetUid)) {
                        log.error("Пропущена запись с пустым assetUid");
                        errorCount++;
                        continue;
                    }
                    
                    // Проверяем, существует ли запись
                    Optional<AssetFundamentalEntity> existingEntity = assetFundamentalsRepository.findByAssetUid(assetUid);
                    
                    if (existingEntity.isPresent()) {
                        // Обновляем существующую запись
                        AssetFundamentalEntity existing = existingEntity.get();
                        updateEntityFields(existing, entity);
                        
                        assetFundamentalsRepository.save(existing);
                        updatedCount++;
                        
                        if (updatedCount % 100 == 0) {
                            log.info("Обновлено записей: " + updatedCount);
                        }
                    } else {
                        // Создаем новую запись
                        assetFundamentalsRepository.save(entity);
                        createdCount++;
                        
                        if (createdCount % 100 == 0) {
                            log.info("Создано записей: " + createdCount);
                        }
                    }
                    
                } catch (DataIntegrityViolationException e) {
                    log.error("Ошибка целостности данных для актива " + entity.getAssetUid() + ": " + e.getMessage());
                    errorCount++;
                } catch (DataAccessException e) {
                    log.error("Ошибка доступа к данным для актива " + entity.getAssetUid() + ": " + e.getMessage());   
                    errorCount++;
                } catch (Exception e) {
                    log.error("Неожиданная ошибка для актива " + entity.getAssetUid() + ": " + e.getMessage());
                    errorCount++;
                }
            }
            
            log.info("Сохранение завершено:");
            log.info("- Обновлено записей: " + updatedCount);
            log.info("- Создано записей: " + createdCount);
            log.info("- Ошибок: " + errorCount);
            log.info("- Всего обработано: " + (updatedCount + createdCount + errorCount));
            
        } catch (Exception e) {
            log.error("Критическая ошибка при сохранении фундаментальных показателей: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Не удалось сохранить фундаментальные показатели", e);
        }
    }
    
    /**
     * Обновляет поля существующей сущности данными из новой сущности
     * @param existing существующая сущность
     * @param newEntity новая сущность с обновленными данными
     */
    private void updateEntityFields(AssetFundamentalEntity existing, AssetFundamentalEntity newEntity) {
        try {
            // Основные поля
            existing.setDomicileIndicatorCode(newEntity.getDomicileIndicatorCode());
            existing.setCurrency(newEntity.getCurrency());
            
            // Рыночные данные
            existing.setMarketCapitalization(newEntity.getMarketCapitalization());
            existing.setHighPriceLast52Weeks(newEntity.getHighPriceLast52Weeks());
            existing.setLowPriceLast52Weeks(newEntity.getLowPriceLast52Weeks());
            existing.setAverageDailyVolumeLast10Days(newEntity.getAverageDailyVolumeLast10Days());
            existing.setAverageDailyVolumeLast4Weeks(newEntity.getAverageDailyVolumeLast4Weeks());
            existing.setBeta(newEntity.getBeta());
            existing.setFreeFloat(newEntity.getFreeFloat());
            existing.setForwardAnnualDividendYield(newEntity.getForwardAnnualDividendYield());
            existing.setSharesOutstanding(newEntity.getSharesOutstanding());
            
            // Финансовые показатели
            existing.setRevenueTtm(newEntity.getRevenueTtm());
            existing.setEbitdaTtm(newEntity.getEbitdaTtm());
            existing.setNetIncomeTtm(newEntity.getNetIncomeTtm());
            existing.setEpsTtm(newEntity.getEpsTtm());
            existing.setDilutedEpsTtm(newEntity.getDilutedEpsTtm());
            existing.setFreeCashFlowTtm(newEntity.getFreeCashFlowTtm());
            
            // Ростовые показатели
            existing.setFiveYearAnnualRevenueGrowthRate(newEntity.getFiveYearAnnualRevenueGrowthRate());
            existing.setThreeYearAnnualRevenueGrowthRate(newEntity.getThreeYearAnnualRevenueGrowthRate());
            existing.setOneYearAnnualRevenueGrowthRate(newEntity.getOneYearAnnualRevenueGrowthRate());
            
            // Финансовые коэффициенты
            existing.setPeRatioTtm(newEntity.getPeRatioTtm());
            existing.setPriceToSalesTtm(newEntity.getPriceToSalesTtm());
            existing.setPriceToBookTtm(newEntity.getPriceToBookTtm());
            existing.setPriceToFreeCashFlowTtm(newEntity.getPriceToFreeCashFlowTtm());
            existing.setTotalEnterpriseValueMrq(newEntity.getTotalEnterpriseValueMrq());
            existing.setEvToEbitdaMrq(newEntity.getEvToEbitdaMrq());
            existing.setEvToSales(newEntity.getEvToSales());
            
            // Показатели рентабельности
            existing.setNetMarginMrq(newEntity.getNetMarginMrq());
            existing.setNetInterestMarginMrq(newEntity.getNetInterestMarginMrq());
            existing.setRoe(newEntity.getRoe());
            existing.setRoa(newEntity.getRoa());
            existing.setRoic(newEntity.getRoic());
            
            // Долговые показатели
            existing.setTotalDebtMrq(newEntity.getTotalDebtMrq());
            existing.setTotalDebtToEquityMrq(newEntity.getTotalDebtToEquityMrq());
            existing.setTotalDebtToEbitdaMrq(newEntity.getTotalDebtToEbitdaMrq());
            existing.setFreeCashFlowToPrice(newEntity.getFreeCashFlowToPrice());
            existing.setNetDebtToEbitda(newEntity.getNetDebtToEbitda());
            existing.setCurrentRatioMrq(newEntity.getCurrentRatioMrq());
            existing.setFixedChargeCoverageRatioFy(newEntity.getFixedChargeCoverageRatioFy());
            
            // Дивидендные показатели
            existing.setDividendYieldDailyTtm(newEntity.getDividendYieldDailyTtm());
            existing.setDividendRateTtm(newEntity.getDividendRateTtm());
            existing.setDividendsPerShare(newEntity.getDividendsPerShare());
            existing.setFiveYearsAverageDividendYield(newEntity.getFiveYearsAverageDividendYield());
            existing.setFiveYearAnnualDividendGrowthRate(newEntity.getFiveYearAnnualDividendGrowthRate());
            existing.setDividendPayoutRatioFy(newEntity.getDividendPayoutRatioFy());
            
            // Другие показатели
            existing.setBuyBackTtm(newEntity.getBuyBackTtm());
            existing.setAdrToCommonShareRatio(newEntity.getAdrToCommonShareRatio());
            existing.setNumberOfEmployees(newEntity.getNumberOfEmployees());
            
            // Даты
            existing.setExDividendDate(newEntity.getExDividendDate());
            existing.setFiscalPeriodStartDate(newEntity.getFiscalPeriodStartDate());
            existing.setFiscalPeriodEndDate(newEntity.getFiscalPeriodEndDate());
            
            // Показатели изменений
            existing.setRevenueChangeFiveYears(newEntity.getRevenueChangeFiveYears());
            existing.setEpsChangeFiveYears(newEntity.getEpsChangeFiveYears());
            existing.setEbitdaChangeFiveYears(newEntity.getEbitdaChangeFiveYears());
            existing.setTotalDebtChangeFiveYears(newEntity.getTotalDebtChangeFiveYears());
            
        } catch (Exception e) {
            log.error("Ошибка при обновлении полей для актива " + existing.getAssetUid() + ": " + e.getMessage());
            throw e;
        }
    }
    
    
}
