package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.service.CacheWarmupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Контроллер для управления кэшем инструментов
 * 
 * <p>Предоставляет REST API для управления кэшем финансовых инструментов:</p>
 * <ul>
 *   <li><strong>Прогрев кэша</strong> - загрузка инструментов в кэш</li>
 *   <li><strong>Просмотр кэша</strong> - получение информации о содержимом кэша</li>
 *   <li><strong>Очистка кэша</strong> - удаление данных из кэша</li>
 *   <li><strong>Статистика кэша</strong> - получение метрик кэша</li>
 * </ul>
 * 
 * <p>Поддерживает работу с четырьмя типами кэшей:</p>
 * <ul>
 *   <li>sharesCache - кэш акций</li>
 *   <li>futuresCache - кэш фьючерсов</li>
 *   <li>indicativesCache - кэш индикативных инструментов</li>
 *   <li>closePricesCache - кэш цен закрытия</li>
 * </ul>
 * 
 * @author InvestmentDataLoaderService
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private static final Logger log = LoggerFactory.getLogger(CacheController.class);
    private final CacheWarmupService cacheWarmupService;
    private final CacheManager cacheManager;

    public CacheController(CacheWarmupService cacheWarmupService, 
                          CacheManager cacheManager) {
        this.cacheWarmupService = cacheWarmupService;
        this.cacheManager = cacheManager;
    }

    /**
     * Прогрев кэша инструментов
     * 
     * <p>Принудительно загружает все основные инструменты в кэш:</p>
     * <ul>
     *   <li>Акции с биржи MOEX</li>
     *   <li>Все фьючерсы</li>
     *   <li>Все индикативные инструменты</li>
     * </ul>
     * 
     * <p>Полезно для:</p>
     * <ul>
     *   <li>Принудительного обновления кэша без перезапуска приложения</li>
     *   <li>Тестирования производительности</li>
     *   <li>Восстановления кэша после очистки</li>
     * </ul>
     * 
     * @return результат операции прогрева кэша
     */
    @PostMapping("/warmup")
    public ResponseEntity<Map<String, Object>> warmupCache() {
        log.info("=== ПРОГРЕВ КЭША ===");
        try {
            log.info("Запускаем принудительный прогрев кэша...");
            cacheWarmupService.manualWarmupCache();
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Кэш успешно прогрет",
                "timestamp", LocalDateTime.now().toString()
            );
            
            log.info("Кэш успешно прогрет");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Ошибка при прогреве кэша: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Ошибка при прогреве кэша: " + e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Просмотр содержимого кэша
     * 
     * <p>Возвращает информацию о содержимом всех кэшей или конкретного кэша.</p>
     * 
     * <p>Параметры запроса:</p>
     * <ul>
     *   <li>cacheName (опционально) - имя конкретного кэша для просмотра</li>
     *   <li>limit (опционально) - максимальное количество записей для отображения (по умолчанию 100)</li>
     * </ul>
     * 
     * @param cacheName имя кэша для просмотра (опционально)
     * @param limit максимальное количество записей (опционально, по умолчанию 100)
     * @return информация о содержимом кэша
     */
    @GetMapping("/content")
    public ResponseEntity<Map<String, Object>> getCacheContent(
            @RequestParam(required = false) String cacheName,
            @RequestParam(defaultValue = "100") int limit) {
        
        log.info("=== ПОЛУЧЕНИЕ СОДЕРЖИМОГО КЭША ===");
        log.info("Параметры: cacheName={}, limit={}", cacheName, limit);
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now().toString());
            
            if (cacheName != null && !cacheName.isEmpty()) {
                // Просмотр конкретного кэша
                log.info("Получаем содержимое кэша: {}", cacheName);
                Cache cache = cacheManager.getCache(cacheName);
                if (cache == null) {
                    log.warn("Кэш '{}' не найден", cacheName);
                    response.put("error", "Кэш '" + cacheName + "' не найден");
                    return ResponseEntity.badRequest().body(response);
                }
                
                Map<String, Object> cacheInfo = getCacheInfo(cache, limit);
                response.put("cacheName", cacheName);
                response.putAll(cacheInfo);
                log.info("Содержимое кэша {} получено успешно", cacheName);
                
            } else {
                // Просмотр всех кэшей
                log.info("Получаем содержимое всех кэшей");
                Map<String, Object> allCaches = new HashMap<>();
                String[] cacheNames = {"sharesCache", "futuresCache", "indicativesCache", "closePricesCache"};
                
                for (String name : cacheNames) {
                    Cache cache = cacheManager.getCache(name);
                    if (cache != null) {
                        Map<String, Object> cacheInfo = getCacheInfo(cache, limit);
                        allCaches.put(name, cacheInfo);
                        log.debug("Кэш {} обработан", name);
                    }
                }
                
                response.put("caches", allCaches);
                log.info("Содержимое всех кэшей получено успешно");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Ошибка при получении содержимого кэша: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "error", "Ошибка при получении содержимого кэша: " + e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Статистика кэша
     * 
     * <p>Возвращает общую статистику по всем кэшам:</p>
     * <ul>
     *   <li>Количество кэшей</li>
     *   <li>Общее количество записей во всех кэшах</li>
     *   <li>Статистика по каждому кэшу</li>
     *   <li>Информация о производительности</li>
     * </ul>
     * 
     * @return статистика кэша
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        log.info("=== ПОЛУЧЕНИЕ СТАТИСТИКИ КЭША ===");
        try {
            log.info("Собираем статистику по всем кэшам...");
            Map<String, Object> stats = new HashMap<>();
            stats.put("timestamp", LocalDateTime.now().toString());
            
            String[] cacheNames = {"sharesCache", "futuresCache", "indicativesCache", "closePricesCache"};
            Map<String, Object> cacheStats = new HashMap<>();
            int totalEntries = 0;
            int activeCaches = 0;
            
            for (String cacheName : cacheNames) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    activeCaches++;
                    Map<String, Object> cacheInfo = getCacheInfo(cache, Integer.MAX_VALUE);
                    cacheStats.put(cacheName, cacheInfo);
                    totalEntries += (Integer) cacheInfo.get("entryCount");
                    log.debug("Кэш {}: {} записей", cacheName, cacheInfo.get("entryCount"));
                }
            }
            
            stats.put("totalCaches", cacheNames.length);
            stats.put("activeCaches", activeCaches);
            stats.put("totalEntries", totalEntries);
            stats.put("cacheDetails", cacheStats);
            
            log.info("Статистика кэша: {} активных кэшей, {} записей", activeCaches, totalEntries);
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Ошибка при получении статистики кэша: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "error", "Ошибка при получении статистики кэша: " + e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Очистка кэша
     * 
     * <p>Очищает содержимое кэша. Можно очистить конкретный кэш или все кэши.</p>
     * 
     * @param cacheName имя кэша для очистки (опционально, если не указано - очищаются все кэши)
     * @return результат операции очистки
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearCache(
            @RequestParam(required = false) String cacheName) {
        
        log.info("=== ОЧИСТКА КЭША ===");
        log.info("Параметр cacheName: {}", cacheName);
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now().toString());
            
            if (cacheName != null && !cacheName.isEmpty()) {
                // Очистка конкретного кэша
                log.info("Очищаем кэш: {}", cacheName);
                Cache cache = cacheManager.getCache(cacheName);
                if (cache == null) {
                    log.warn("Кэш '{}' не найден", cacheName);
                    response.put("error", "Кэш '" + cacheName + "' не найден");
                    return ResponseEntity.badRequest().body(response);
                }
                
                cache.clear();
                response.put("success", true);
                response.put("message", "Кэш '" + cacheName + "' успешно очищен");
                response.put("clearedCache", cacheName);
                log.info("Кэш {} успешно очищен", cacheName);
                
            } else {
                // Очистка всех кэшей
                log.info("Очищаем все кэши");
                String[] cacheNames = {"sharesCache", "futuresCache", "indicativesCache", "closePricesCache"};
                List<String> clearedCaches = new ArrayList<>();
                
                for (String name : cacheNames) {
                    Cache cache = cacheManager.getCache(name);
                    if (cache != null) {
                        cache.clear();
                        clearedCaches.add(name);
                        log.debug("Кэш {} очищен", name);
                    }
                }
                
                response.put("success", true);
                response.put("message", "Все кэши успешно очищены");
                response.put("clearedCaches", clearedCaches);
                log.info("Все кэши успешно очищены: {}", clearedCaches);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Ошибка при очистке кэша: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Ошибка при очистке кэша: " + e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Получение информации о конкретном кэше
     * 
     * @param cache кэш для анализа
     * @param limit максимальное количество записей для отображения
     * @return информация о кэше
     */
    private Map<String, Object> getCacheInfo(Cache cache, int limit) {
        Map<String, Object> info = new HashMap<>();
        
        try {
            // Получаем информацию о кэше
            info.put("name", cache.getName());
            info.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
            
            // Подсчитываем записи (это может быть медленно для больших кэшей)
            int entryCount = 0;
            List<Map<String, Object>> sampleEntries = new ArrayList<>();
            
            if (cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache = 
                    (com.github.benmanes.caffeine.cache.Cache<?, ?>) cache.getNativeCache();
                
                entryCount = (int) caffeineCache.estimatedSize();
                
                // Получаем образцы записей
                int count = 0;
                for (Map.Entry<?, ?> entry : caffeineCache.asMap().entrySet()) {
                    if (count >= limit) break;
                    
                    Map<String, Object> entryInfo = new HashMap<>();
                    entryInfo.put("key", entry.getKey().toString());
                    entryInfo.put("valueType", entry.getValue().getClass().getSimpleName());
                    
                    // Для списков показываем размер
                    if (entry.getValue() instanceof List) {
                        entryInfo.put("valueSize", ((List<?>) entry.getValue()).size());
                    }
                    
                    sampleEntries.add(entryInfo);
                    count++;
                }
            }
            
            info.put("entryCount", entryCount);
            info.put("sampleEntries", sampleEntries);
            info.put("sampleLimit", Math.min(limit, entryCount));
            
        } catch (Exception e) {
            info.put("error", "Ошибка при получении информации о кэше: " + e.getMessage());
        }
        
        return info;
    }
}
