package com.example.InvestmentDataLoaderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class InvestmentDataLoaderService {

    private static final Logger log = LoggerFactory.getLogger(InvestmentDataLoaderService.class);

    public static void main(String[] args) {
        log.info("Запуск InvestmentDataLoaderService...");
        SpringApplication.run(InvestmentDataLoaderService.class, args);
        log.info("InvestmentDataLoaderService успешно запущен");
    }
}

