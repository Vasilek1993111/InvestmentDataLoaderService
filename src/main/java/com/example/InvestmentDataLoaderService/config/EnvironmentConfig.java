package com.example.InvestmentDataLoaderService.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Конфигурация для загрузки переменных окружения из .env файла.
 * Обеспечивает безопасное хранение чувствительных данных.
 */
@Slf4j
@Configuration
public class EnvironmentConfig {

    /**
     * Загружает переменные окружения из .env файла при старте приложения.
     * Если .env файл не найден, логирует предупреждение и продолжает работу.
     */
    @PostConstruct
    public void loadEnvironmentVariables() {
        File envFile = new File(".env");
        
        if (!envFile.exists()) {
            log.info(".env файл не найден. Используются системные переменные окружения.");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            int loadedCount = 0;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Пропускаем пустые строки и комментарии
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Парсим строку формата KEY=VALUE
                int equalIndex = line.indexOf('=');
                if (equalIndex > 0) {
                    String key = line.substring(0, equalIndex).trim();
                    String value = line.substring(equalIndex + 1).trim();
                    
                    // Убираем кавычки если есть
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    
                    // Устанавливаем переменную только если она еще не установлена
                    if (System.getenv(key) == null) {
                        System.setProperty(key, value);
                        loadedCount++;
                        log.debug("Загружена переменная окружения: {}", key);
                    }
                }
            }
            
            log.info("Успешно загружено {} переменных окружения из .env файла", loadedCount);
            
        } catch (IOException e) {
            log.warn("Ошибка при чтении .env файла: {}. Используются системные переменные окружения.", e.getMessage());
        }
    }
}
