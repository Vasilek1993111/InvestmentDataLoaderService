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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    public List<ShareDto> getSharesFromCache() {
        Cache cache = cacheManager.getCache("sharesCache");
        if (cache == null) {
            return new ArrayList<>();
        }

        List<ShareDto> allShares = new ArrayList<>();
        
        // Получаем все записи из кэша через ключи
        try {
            // Пробуем разные ключи для получения акций
            String[] possibleKeys = {
                "|||", // Пустые параметры
                "|moex_mrng_evng_e_wknd_dlr|||", // Ключ из CacheController
                "|moex_mrng_evng_e_wknd_dlr|||", // Другой возможный ключ
            };
            
            for (String cacheKey : possibleKeys) {
                Cache.ValueWrapper wrapper = cache.get(cacheKey);
                if (wrapper != null && wrapper.get() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<ShareDto> shares = (List<ShareDto>) wrapper.get();
                    if (shares != null && !shares.isEmpty()) {
                        allShares.addAll(shares);
                        System.out.println("Найдено " + shares.size() + " акций в кэше с ключом: " + cacheKey);
                        break; // Используем первый найденный ключ
                    }
                }
            }
            
            // Если не нашли по стандартным ключам, пробуем получить все записи из кэша
            if (allShares.isEmpty() && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache = 
                    (com.github.benmanes.caffeine.cache.Cache<?, ?>) cache.getNativeCache();
                
                for (Map.Entry<?, ?> entry : caffeineCache.asMap().entrySet()) {
                    if (entry.getValue() instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<ShareDto> shares = (List<ShareDto>) entry.getValue();
                        if (shares != null && !shares.isEmpty()) {
                            allShares.addAll(shares);
                            System.out.println("Найдено " + shares.size() + " акций в кэше с ключом: " + entry.getKey());
                            break; // Используем первый найденный список
                        }
                    }
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
    public List<FutureDto> getFuturesFromCache() {
        Cache cache = cacheManager.getCache("futuresCache");
        if (cache == null) {
            return new ArrayList<>();
        }

        List<FutureDto> allFutures = new ArrayList<>();
        
        // Получаем все записи из кэша через ключи
        try {
            // Пробуем разные ключи для получения фьючерсов
            String[] possibleKeys = {
                "||||", // Пустые параметры
                "||||", // Другой возможный ключ
            };
            
            for (String cacheKey : possibleKeys) {
                Cache.ValueWrapper wrapper = cache.get(cacheKey);
                if (wrapper != null && wrapper.get() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<FutureDto> futures = (List<FutureDto>) wrapper.get();
                    if (futures != null && !futures.isEmpty()) {
                        allFutures.addAll(futures);
                        System.out.println("Найдено " + futures.size() + " фьючерсов в кэше с ключом: " + cacheKey);
                        break; // Используем первый найденный ключ
                    }
                }
            }
            
            // Если не нашли по стандартным ключам, пробуем получить все записи из кэша
            if (allFutures.isEmpty() && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache = 
                    (com.github.benmanes.caffeine.cache.Cache<?, ?>) cache.getNativeCache();
                
                for (Map.Entry<?, ?> entry : caffeineCache.asMap().entrySet()) {
                    if (entry.getValue() instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<FutureDto> futures = (List<FutureDto>) entry.getValue();
                        if (futures != null && !futures.isEmpty()) {
                            allFutures.addAll(futures);
                            System.out.println("Найдено " + futures.size() + " фьючерсов в кэше с ключом: " + entry.getKey());
                            break; // Используем первый найденный список
                        }
                    }
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
                    entity.setShortEnabled(dto.shortEnabled());
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
                    dto.exchange(),
                    dto.shortEnabled()
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
            List<ShareDto> shares = getSharesFromCache();
            List<FutureDto> futures = getFuturesFromCache();
            
            int sharesCount = shares.size();
            int futuresCount = futures.size();
            
            String info = String.format("Кэш: %d акций, %d фьючерсов", sharesCount, futuresCount);
            
            // Добавляем отладочную информацию
            if (sharesCount > 0) {
                System.out.println("DEBUG: Найдено " + sharesCount + " акций в кэше");
            } else {
                System.out.println("DEBUG: Акции в кэше не найдены");
            }
            
            if (futuresCount > 0) {
                System.out.println("DEBUG: Найдено " + futuresCount + " фьючерсов в кэше");
            } else {
                System.out.println("DEBUG: Фьючерсы в кэше не найдены");
            }
            
            return info;
        } catch (Exception e) {
            System.err.println("Ошибка получения информации о кэше: " + e.getMessage());
            e.printStackTrace();
            return "Ошибка получения информации о кэше: " + e.getMessage();
        }
    }

	public void warmUpCache() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'warmUpCache'");
	}
}
