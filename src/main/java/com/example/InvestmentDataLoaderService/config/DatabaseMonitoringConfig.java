package com.example.InvestmentDataLoaderService.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Конфигурация для мониторинга подключений к базе данных
 * 
 * <p>Предоставляет мониторинг состояния пула подключений HikariCP
 * и автоматическую проверку утечек подключений.</p>
 * 
 * @author InvestmentDataLoaderService
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableScheduling
public class DatabaseMonitoringConfig {

    @Autowired
    private DataSource dataSource;

    /**
     * Health indicator для мониторинга состояния пула подключений
     */
    @Bean
    public HealthIndicator databaseConnectionPoolHealth() {
        return new HealthIndicator() {
            @Override
            public Health health() {
                try {
                    if (dataSource instanceof HikariDataSource) {
                        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                        
                        int activeConnections = hikariDataSource.getHikariPoolMXBean().getActiveConnections();
                        int idleConnections = hikariDataSource.getHikariPoolMXBean().getIdleConnections();
                        int totalConnections = hikariDataSource.getHikariPoolMXBean().getTotalConnections();
                        int threadsAwaitingConnection = hikariDataSource.getHikariPoolMXBean().getThreadsAwaitingConnection();
                        
                        // Проверяем на утечки подключений
                        boolean hasLeaks = threadsAwaitingConnection > 5 || 
                                         (activeConnections > 0 && activeConnections == totalConnections);
                        
                        if (hasLeaks) {
                            return Health.down()
                                .withDetail("activeConnections", activeConnections)
                                .withDetail("idleConnections", idleConnections)
                                .withDetail("totalConnections", totalConnections)
                                .withDetail("threadsAwaitingConnection", threadsAwaitingConnection)
                                .withDetail("status", "Potential connection leak detected")
                                .build();
                        }
                        
                        return Health.up()
                            .withDetail("activeConnections", activeConnections)
                            .withDetail("idleConnections", idleConnections)
                            .withDetail("totalConnections", totalConnections)
                            .withDetail("threadsAwaitingConnection", threadsAwaitingConnection)
                            .withDetail("status", "Connection pool healthy")
                            .build();
                    }
                    
                    // Простая проверка подключения для других типов DataSource
                    try (Connection connection = dataSource.getConnection()) {
                        return Health.up()
                            .withDetail("status", "Database connection successful")
                            .build();
                    }
                    
                } catch (SQLException e) {
                    return Health.down()
                        .withDetail("error", e.getMessage())
                        .withDetail("status", "Database connection failed")
                        .build();
                }
            }
        };
    }

    /**
     * Периодическая проверка состояния пула подключений
     * Запускается каждые 5 минут
     */
    @Scheduled(fixedRate = 300000) // 5 минут
    public void monitorConnectionPool() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            
            int activeConnections = hikariDataSource.getHikariPoolMXBean().getActiveConnections();
            int idleConnections = hikariDataSource.getHikariPoolMXBean().getIdleConnections();
            int totalConnections = hikariDataSource.getHikariPoolMXBean().getTotalConnections();
            int threadsAwaitingConnection = hikariDataSource.getHikariPoolMXBean().getThreadsAwaitingConnection();
            
            // Логируем состояние пула подключений
            System.out.println("=== СОСТОЯНИЕ ПУЛА ПОДКЛЮЧЕНИЙ ===");
            System.out.println("Активных подключений: " + activeConnections);
            System.out.println("Неактивных подключений: " + idleConnections);
            System.out.println("Всего подключений: " + totalConnections);
            System.out.println("Потоков ожидающих подключение: " + threadsAwaitingConnection);
            
            // Предупреждение о потенциальных утечках
            if (threadsAwaitingConnection > 5) {
                System.err.println("⚠️  ПРЕДУПРЕЖДЕНИЕ: Много потоков ожидают подключение (" + threadsAwaitingConnection + ")");
            }
            
            if (activeConnections > 0 && activeConnections == totalConnections) {
                System.err.println("⚠️  ПРЕДУПРЕЖДЕНИЕ: Все подключения заняты, возможна утечка");
            }
            
            if (activeConnections == 0 && idleConnections > 0) {
                System.out.println("✅ Пул подключений в норме");
            }
            
            System.out.println("=================================");
        }
    }

    /**
     * Принудительная очистка неактивных подключений
     * Запускается каждые 30 минут
     */
    @Scheduled(fixedRate = 1800000) // 30 минут
    public void cleanupIdleConnections() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            
            // Принудительно закрываем неактивные подключения
            hikariDataSource.getHikariPoolMXBean().softEvictConnections();
            
            System.out.println("🧹 Выполнена очистка неактивных подключений");
        }
    }
}
