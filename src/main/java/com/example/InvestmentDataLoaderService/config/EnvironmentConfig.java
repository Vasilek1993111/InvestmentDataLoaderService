package com.example.InvestmentDataLoaderService.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    /**
     * Загружает переменные окружения из .env файла при старте приложения.
     * Выбирает правильный .env файл в зависимости от активного профиля.
     * Если .env файл не найден, логирует предупреждение и продолжает работу.
     */
    @PostConstruct
    public void loadEnvironmentVariables() {
        try {
            // Определяем имя .env файла в зависимости от профиля
            String envFileName = getEnvFileName();
            log.info("Загружаем переменные окружения из файла: {}", envFileName);
            
            // Загружаем соответствующий .env файл
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")  // Ищем в корне проекта
                    .filename(envFileName)  // Указываем конкретный файл
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
                log.info("{} файл не найден или пуст. Используются системные переменные окружения.", envFileName);
                return;
            }
            
            int loadedCount = 0;
            
            // Принудительно загружаем переменные из .env файла (переопределяем системные)
            for (Map.Entry<String, String> entry : envVars.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // Всегда устанавливаем переменные из .env файла
                System.setProperty(key, value);
                loadedCount++;
                log.debug("Загружена переменная окружения: {} = {}", key, 
                        key.contains("PASSWORD") || key.contains("TOKEN") ? "***" : value);
            }
            
            log.info("Успешно загружено {} переменных окружения из {} файла", loadedCount, envFileName);
            
            // Логируем основные переменные (без чувствительных данных)
            String token = dotenv.get("T_INVEST_TOKEN");
            if (token != null && !token.isEmpty()) {
                log.info("T_INVEST_TOKEN загружен из {} файла", envFileName);
            } else {
                log.warn("T_INVEST_TOKEN не найден в {} файле. Убедитесь, что токен установлен.", envFileName);
            }
            
        } catch (DotenvException e) {
            log.warn("Ошибка при загрузке .env файла: {}. Используются системные переменные окружения.", e.getMessage());
        } catch (Exception e) {
            log.error("Неожиданная ошибка при загрузке переменных окружения: {}", e.getMessage(), e);
        }
    }

    /**
     * Определяет имя .env файла в зависимости от активного профиля Spring.
     * 
     * @return имя .env файла для загрузки
     */
    private String getEnvFileName() {
        if (activeProfile == null || activeProfile.isEmpty() || "default".equals(activeProfile)) {
            return ".env.test";  // По умолчанию используем .env.test
        }
        
        // Для профилей test и prod используем соответствующие .env файлы
        if ("test".equals(activeProfile)) {
            return ".env.test";
        } else if ("prod".equals(activeProfile)) {
            return ".env.prod";
        }
        
        // Для других профилей используем .env.{profile}
        return ".env." + activeProfile;
    }
}
