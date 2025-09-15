package com.example.InvestmentDataLoaderService.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    public static final String SHARES_CACHE = "sharesCache";
    public static final String FUTURES_CACHE = "futuresCache";
    public static final String INDICATIVES_CACHE = "indicativesCache";

    @Bean
    public Caffeine<Object, Object> caffeineSpec() {
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(1, TimeUnit.DAYS);
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                SHARES_CACHE,
                FUTURES_CACHE,
                INDICATIVES_CACHE
        );
        manager.setCaffeine(caffeine);
        return manager;
    }
}


