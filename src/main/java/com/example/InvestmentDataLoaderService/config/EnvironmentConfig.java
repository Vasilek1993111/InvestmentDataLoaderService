package com.example.InvestmentDataLoaderService.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * Конфигурация для загрузки переменных окружения из .env файла.
 * Использует dotenv-java для надежной загрузки переменных.
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
        try {
            // Загружаем .env файл
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")  // Ищем .env в корне проекта
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();
            
            // Получаем все переменные из .env
            Map<String, String> envVars = dotenv.entries().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            entry -> entry.getKey(),
                            entry -> entry.getValue()
                    ));
            
            if (envVars.isEmpty()) {
                log.info(".env файл не найден или пуст. Используются системные переменные окружения.");
                return;
            }
            
            int loadedCount = 0;
            
            // Устанавливаем переменные только если они еще не установлены в системе
            for (Map.Entry<String, String> entry : envVars.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // Проверяем, не установлена ли уже переменная в системе
                if (System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                    loadedCount++;
                    log.debug("Загружена переменная окружения: {} = {}", key, 
                            key.contains("PASSWORD") || key.contains("TOKEN") ? "***" : value);
                }
            }
            
            log.info("Успешно загружено {} переменных окружения из .env файла", loadedCount);
            
            // Логируем основные переменные (без чувствительных данных)
            String token = dotenv.get("T_INVEST_TOKEN");
            if (token != null && !token.isEmpty()) {
                log.info("T_INVEST_TOKEN загружен из .env файла");
            } else {
                log.warn("T_INVEST_TOKEN не найден в .env файле. Убедитесь, что токен установлен.");
            }
            
        } catch (DotenvException e) {
            log.warn("Ошибка при загрузке .env файла: {}. Используются системные переменные окружения.", e.getMessage());
        } catch (Exception e) {
            log.error("Неожиданная ошибка при загрузке переменных окружения: {}", e.getMessage(), e);
        }
    }
}
