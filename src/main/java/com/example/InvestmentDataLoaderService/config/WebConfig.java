package com.example.InvestmentDataLoaderService.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурация веб-слоя для обработки ошибок 404
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    
    // Spring Boot автоматически включает throwExceptionIfNoHandlerFound=true
    // когда используется @EnableWebMvc
}
