package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.FutureDto;
import com.example.InvestmentDataLoaderService.dto.ShareDto;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для получения инструментов из кэша с fallback на БД
 * 
 * <p>Предоставляет методы для получения акций и фьючерсов из кэша.
 * В случае отсутствия данных в кэше, обращается к базе данных.</p>
 * 
 * @author InvestmentDataLoaderService
 * @version 1.0
 * @since 2024
 */
@Service
public class CachedInstrumentService {

    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final CacheManager cacheManager;

    public CachedInstrumentService(ShareRepository shareRepository,
                                 FutureRepository futureRepository,
                                 CacheManager cacheManager) {
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.cacheManager = cacheManager;
    }

    /**
     * Получает все акции из кэша или БД
     * 
     * @return список всех акций
     */
    public List<ShareEntity> getAllShares() {
        try {
            // Пытаемся получить из кэша
            List<ShareDto> cachedShares = getSharesFromCache();
            
            if (cachedShares != null && !cachedShares.isEmpty()) {
                System.out.println("Получено " + cachedShares.size() + " акций из кэша");
                return convertSharesDtoToEntity(cachedShares);
            }
            
            // Fallback на БД
            System.out.println("Кэш акций пуст, загружаем из БД");
            return shareRepository.findAll();
            
        } catch (Exception e) {
            System.err.println("Ошибка при получении акций из кэша, используем БД: " + e.getMessage());
            return shareRepository.findAll();
        }
    }

    /**
     * Получает все фьючерсы из кэша или БД
     * 
     * @return список всех фьючерсов
     */
    public List<FutureEntity> getAllFutures() {
        try {
            // Пытаемся получить из кэша
            List<FutureDto> cachedFutures = getFuturesFromCache();
            
            if (cachedFutures != null && !cachedFutures.isEmpty()) {
                System.out.println("Получено " + cachedFutures.size() + " фьючерсов из кэша");
                return convertFuturesDtoToEntity(cachedFutures);
            }
            
            // Fallback на БД
            System.out.println("Кэш фьючерсов пуст, загружаем из БД");
            return futureRepository.findAll();
            
        } catch (Exception e) {
            System.err.println("Ошибка при получении фьючерсов из кэша, используем БД: " + e.getMessage());
            return futureRepository.findAll();
        }
    }

    /**
     * Получает акции из кэша
     */
    private List<ShareDto> getSharesFromCache() {
        Cache cache = cacheManager.getCache("sharesCache");
        if (cache == null) {
            return new ArrayList<>();
        }

        List<ShareDto> allShares = new ArrayList<>();
        
        // Получаем все записи из кэша через ключи
        try {
            // Используем стандартный ключ для получения всех акций
            String cacheKey = "|||"; // Пустые параметры для получения всех акций
            Cache.ValueWrapper wrapper = cache.get(cacheKey);
            if (wrapper != null && wrapper.get() instanceof List) {
                @SuppressWarnings("unchecked")
                List<ShareDto> shares = (List<ShareDto>) wrapper.get();
                if (shares != null) {
                    allShares.addAll(shares);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка получения акций из кэша: " + e.getMessage());
        }
        
        return allShares;
    }

    /**
     * Получает фьючерсы из кэша
     */
    private List<FutureDto> getFuturesFromCache() {
        Cache cache = cacheManager.getCache("futuresCache");
        if (cache == null) {
            return new ArrayList<>();
        }

        List<FutureDto> allFutures = new ArrayList<>();
        
        // Получаем все записи из кэша через ключи
        try {
            // Используем стандартный ключ для получения всех фьючерсов
            String cacheKey = "||||"; // Пустые параметры для получения всех фьючерсов
            Cache.ValueWrapper wrapper = cache.get(cacheKey);
            if (wrapper != null && wrapper.get() instanceof List) {
                @SuppressWarnings("unchecked")
                List<FutureDto> futures = (List<FutureDto>) wrapper.get();
                if (futures != null) {
                    allFutures.addAll(futures);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка получения фьючерсов из кэша: " + e.getMessage());
        }
        
        return allFutures;
    }

    /**
     * Конвертирует ShareDto в ShareEntity
     */
    private List<ShareEntity> convertSharesDtoToEntity(List<ShareDto> sharesDto) {
        return sharesDto.stream()
                .map(dto -> {
                    ShareEntity entity = new ShareEntity();
                    entity.setFigi(dto.figi());
                    entity.setTicker(dto.ticker());
                    entity.setName(dto.name());
                    entity.setCurrency(dto.currency());
                    entity.setExchange(dto.exchange());
                    entity.setSector(dto.sector());
                    entity.setTradingStatus(dto.tradingStatus());
                    return entity;
                })
                .collect(Collectors.toList());
    }

    /**
     * Конвертирует FutureDto в FutureEntity
     */
    private List<FutureEntity> convertFuturesDtoToEntity(List<FutureDto> futuresDto) {
        return futuresDto.stream()
                .map(dto -> new FutureEntity(
                    dto.figi(),
                    dto.ticker(),
                    dto.assetType(),
                    dto.basicAsset(),
                    dto.currency(),
                    dto.exchange()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Проверяет, есть ли инструмент в кэше
     * 
     * @param figi идентификатор инструмента
     * @return true если инструмент найден в кэше
     */
    public boolean isInstrumentInCache(String figi) {
        try {
            // Проверяем в кэше акций
            List<ShareDto> shares = getSharesFromCache();
            boolean foundInShares = shares.stream()
                    .anyMatch(share -> share.figi().equals(figi));
            
            if (foundInShares) {
                return true;
            }
            
            // Проверяем в кэше фьючерсов
            List<FutureDto> futures = getFuturesFromCache();
            return futures.stream()
                    .anyMatch(future -> future.figi().equals(figi));
            
        } catch (Exception e) {
            System.err.println("Ошибка при проверке инструмента в кэше: " + e.getMessage());
            return false;
        }
    }

    /**
     * Получает информацию о размере кэша
     * 
     * @return информация о кэше
     */
    public String getCacheInfo() {
        try {
            int sharesCount = getSharesFromCache().size();
            int futuresCount = getFuturesFromCache().size();
            
            return String.format("Кэш: %d акций, %d фьючерсов", sharesCount, futuresCount);
        } catch (Exception e) {
            return "Ошибка получения информации о кэше: " + e.getMessage();
        }
    }
}
