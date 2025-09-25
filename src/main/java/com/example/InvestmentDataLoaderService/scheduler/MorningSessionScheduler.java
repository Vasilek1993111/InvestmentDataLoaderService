package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.service.MorningSessionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Планировщик для загрузки цен открытия утренней сессии
 * Управляет расписанием загрузки цен открытия
 */
@Component
public class MorningSessionScheduler {

    private final MorningSessionService morningSessionService;

    public MorningSessionScheduler(MorningSessionService morningSessionService) {
        this.morningSessionService = morningSessionService;
    }

    /**
     * Ежедневная загрузка цен открытия утренней сессии
     * Запускается в 2:01 по московскому времени
     */
    @Scheduled(cron = "0 1 2 * * *", zone = "Europe/Moscow")
    public void fetchAndStoreMorningSessionPrices() {
        try {
            LocalDate previousDay = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(1);
            String taskId = "MORNING_" + UUID.randomUUID().toString().substring(0, 8);
            
            System.out.println("=== НАЧАЛО ЗАГРУЗКИ ЦЕН ОТКРЫТИЯ УТРЕННЕЙ СЕССИИ ===");
            System.out.println("[" + taskId + "] Дата: " + previousDay);
            System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Проверяем, является ли предыдущий день выходным
            if (isWeekend(previousDay)) {
                String message = "Предыдущий день является выходным (" + previousDay + "). В выходные дни (суббота и воскресенье) нет цен открытия.";
                System.out.println("[" + taskId + "] " + message);
                System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ОТКРЫТИЯ УТРЕННЕЙ СЕССИИ ===");
                return;
            }
            
            SaveResponseDto response = morningSessionService.processMorningSessionPrices(previousDay, taskId);
            
            System.out.println("[" + taskId + "] Загрузка завершена:");
            System.out.println("[" + taskId + "] - Успех: " + response.isSuccess());
            System.out.println("[" + taskId + "] - Сообщение: " + response.getMessage());
            System.out.println("[" + taskId + "] - Всего запрошено: " + response.getTotalRequested());
            System.out.println("[" + taskId + "] - Сохранено новых: " + response.getNewItemsSaved());
            System.out.println("[" + taskId + "] - Пропущено существующих: " + response.getExistingItemsSkipped());
            System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ОТКРЫТИЯ УТРЕННЕЙ СЕССИИ ===");
            
        } catch (Exception e) {
            System.err.println("Критическая ошибка в загрузке цен открытия утренней сессии: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== НОВОЕ РАСПИСАНИЕ ЗАГРУЗКИ ЦЕН ОТКРЫТИЯ ==========

    /**
     * Загрузка цен открытия в выходные дни в 2:01
     * Запускается только в субботу и воскресенье
     */
    @Scheduled(cron = "0 1 2 * * 0,6", zone = "Europe/Moscow")
    public void fetchAndStoreWeekendOpenPrices() {
        try {
            LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));
            String taskId = "WEEKEND_OPEN_" + UUID.randomUUID().toString().substring(0, 8);
            
            System.out.println("=== НАЧАЛО ЗАГРУЗКИ ЦЕН ОТКРЫТИЯ В ВЫХОДНЫЕ ===");
            System.out.println("[" + taskId + "] Дата: " + today);
            System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            System.out.println("[" + taskId + "] Тип дня: Выходной");
            
            SaveResponseDto response = morningSessionService.processMorningSessionPrices(today, taskId, false);
            
            System.out.println("[" + taskId + "] Загрузка завершена:");
            System.out.println("[" + taskId + "] - Успех: " + response.isSuccess());
            System.out.println("[" + taskId + "] - Сообщение: " + response.getMessage());
            System.out.println("[" + taskId + "] - Всего запрошено: " + response.getTotalRequested());
            System.out.println("[" + taskId + "] - Сохранено новых: " + response.getNewItemsSaved());
            System.out.println("[" + taskId + "] - Пропущено существующих: " + response.getExistingItemsSkipped());
            System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ОТКРЫТИЯ В ВЫХОДНЫЕ ===");
            
        } catch (Exception e) {
            System.err.println("Критическая ошибка в загрузке цен открытия в выходные: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Загрузка цен открытия в рабочие дни в 7:01
     * Запускается с понедельника по пятницу
     */
    @Scheduled(cron = "0 1 7 * * 1-5", zone = "Europe/Moscow")
    public void fetchAndStoreWorkdayOpenPrices7AM() {
        try {
            LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));
            String taskId = "WORKDAY_7AM_" + UUID.randomUUID().toString().substring(0, 8);
            
            System.out.println("=== НАЧАЛО ЗАГРУЗКИ ЦЕН ОТКРЫТИЯ В РАБОЧИЕ ДНИ (7:01) ===");
            System.out.println("[" + taskId + "] Дата: " + today);
            System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            System.out.println("[" + taskId + "] Тип дня: Рабочий");
            
            SaveResponseDto response = morningSessionService.processMorningSessionPrices(today, taskId, false);
            
            System.out.println("[" + taskId + "] Загрузка завершена:");
            System.out.println("[" + taskId + "] - Успех: " + response.isSuccess());
            System.out.println("[" + taskId + "] - Сообщение: " + response.getMessage());
            System.out.println("[" + taskId + "] - Всего запрошено: " + response.getTotalRequested());
            System.out.println("[" + taskId + "] - Сохранено новых: " + response.getNewItemsSaved());
            System.out.println("[" + taskId + "] - Пропущено существующих: " + response.getExistingItemsSkipped());
            System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ОТКРЫТИЯ В РАБОЧИЕ ДНИ (7:01) ===");
            
        } catch (Exception e) {
            System.err.println("Критическая ошибка в загрузке цен открытия в рабочие дни (7:01): " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Загрузка цен открытия в рабочие дни в 9:01
     * Запускается с понедельника по пятницу
     */
    @Scheduled(cron = "0 1 9 * * 1-5", zone = "Europe/Moscow")
    public void fetchAndStoreWorkdayOpenPrices9AM() {
        try {
            LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));
            String taskId = "WORKDAY_9AM_" + UUID.randomUUID().toString().substring(0, 8);
            
            System.out.println("=== НАЧАЛО ЗАГРУЗКИ ЦЕН ОТКРЫТИЯ В РАБОЧИЕ ДНИ (9:01) ===");
            System.out.println("[" + taskId + "] Дата: " + today);
            System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            System.out.println("[" + taskId + "] Тип дня: Рабочий");
            
            SaveResponseDto response = morningSessionService.processMorningSessionPrices(today, taskId, false);
            
            System.out.println("[" + taskId + "] Загрузка завершена:");
            System.out.println("[" + taskId + "] - Успех: " + response.isSuccess());
            System.out.println("[" + taskId + "] - Сообщение: " + response.getMessage());
            System.out.println("[" + taskId + "] - Всего запрошено: " + response.getTotalRequested());
            System.out.println("[" + taskId + "] - Сохранено новых: " + response.getNewItemsSaved());
            System.out.println("[" + taskId + "] - Пропущено существующих: " + response.getExistingItemsSkipped());
            System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ОТКРЫТИЯ В РАБОЧИЕ ДНИ (9:01) ===");
            
        } catch (Exception e) {
            System.err.println("Критическая ошибка в загрузке цен открытия в рабочие дни (9:01): " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Загрузка цен открытия в рабочие дни в 10:01
     * Запускается с понедельника по пятницу
     */
    @Scheduled(cron = "0 1 10 * * 1-5", zone = "Europe/Moscow")
    public void fetchAndStoreWorkdayOpenPrices10AM() {
        try {
            LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));
            String taskId = "WORKDAY_10AM_" + UUID.randomUUID().toString().substring(0, 8);
            
            System.out.println("=== НАЧАЛО ЗАГРУЗКИ ЦЕН ОТКРЫТИЯ В РАБОЧИЕ ДНИ (10:01) ===");
            System.out.println("[" + taskId + "] Дата: " + today);
            System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            System.out.println("[" + taskId + "] Тип дня: Рабочий");
            
            SaveResponseDto response = morningSessionService.processMorningSessionPrices(today, taskId, false);
            
            System.out.println("[" + taskId + "] Загрузка завершена:");
            System.out.println("[" + taskId + "] - Успех: " + response.isSuccess());
            System.out.println("[" + taskId + "] - Сообщение: " + response.getMessage());
            System.out.println("[" + taskId + "] - Всего запрошено: " + response.getTotalRequested());
            System.out.println("[" + taskId + "] - Сохранено новых: " + response.getNewItemsSaved());
            System.out.println("[" + taskId + "] - Пропущено существующих: " + response.getExistingItemsSkipped());
            System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ОТКРЫТИЯ В РАБОЧИЕ ДНИ (10:01) ===");
            
        } catch (Exception e) {
            System.err.println("Критическая ошибка в загрузке цен открытия в рабочие дни (10:01): " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Проверяет, является ли дата выходным днем
     */
    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek().getValue() >= 6; // 6=Сб, 7=Вс
    }
}
