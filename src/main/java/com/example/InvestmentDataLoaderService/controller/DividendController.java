package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.DividendDto;
import com.example.InvestmentDataLoaderService.dto.DividendRequestDto;
import com.example.InvestmentDataLoaderService.entity.DividendEntity;
import com.example.InvestmentDataLoaderService.service.DividendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dividends")
public class DividendController {
    
    private static final Logger log = LoggerFactory.getLogger(DividendController.class);
    
    @Autowired
    private DividendService dividendService;
    
    /**
     * Получение дивидендов от T-API по всем акциям
     */
    @GetMapping("/load")
    public ResponseEntity<List<DividendDto>> getDividendsForAllShares(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        try {
            // Устанавливаем значения по умолчанию
            LocalDate defaultFrom = from != null ? from : LocalDate.of(2024, 1, 1);
            LocalDate defaultTo = to != null ? to : LocalDate.of(2026, 12, 31);
            
            // Получаем дивиденды для всех акций от T-API
            List<DividendEntity> entities = dividendService.getDividendsForAllSharesFromApi(defaultFrom, defaultTo);
            List<DividendDto> dividends = entities.stream()
                .map(this::convertToDto)
                .toList();
            
            return ResponseEntity.ok(dividends);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }
    
   /**
 * Загрузка дивидендов по инструментам в БД
 */
@PostMapping("/load")
public ResponseEntity<Map<String, Object>> loadDividends(
        @RequestBody DividendRequestDto request) {
    
    Map<String, Object> response = new HashMap<>();
    
    try {
        // Валидация входных параметров
        if (request.getInstruments() == null || request.getInstruments().isEmpty()) {
            response.put("success", false);
            response.put("message", "Список инструментов не может быть пустым");
            response.put("timestamp", LocalDate.now().toString());
            return ResponseEntity.badRequest().body(response);
        }
        
        // Устанавливаем значения по умолчанию для дат
        LocalDate from = request.getFrom() != null ? request.getFrom() : LocalDate.of(2024, 1, 1);
        LocalDate to = request.getTo() != null ? request.getTo() : LocalDate.of(2026, 12, 31);
        
        log.info("Начинаем загрузку дивидендов для инструментов: {}", request.getInstruments());
        log.info("Период: {} - {}", from, to);
        
        // Загружаем дивиденды для указанных инструментов
        Map<String, Object> result = dividendService.loadDividendsForAllSharesToDb(request.getInstruments(), from, to);
        result.put("timestamp", LocalDate.now().toString());
        
        return ResponseEntity.ok(result);
        
    } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Ошибка загрузки дивидендов: " + e.getMessage());
        response.put("timestamp", LocalDate.now().toString());
        return ResponseEntity.status(500).body(response);
    }
}
    
    /**
     * Получение дивидендов по FIGI напрямую от T-API
     */
    @GetMapping("/by-figi/{figi}")
    public ResponseEntity<List<DividendDto>> getDividendsByFigi(
            @PathVariable String figi,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        try {
            // Устанавливаем значения по умолчанию, если параметры не переданы
            LocalDate defaultFrom = from != null ? from : LocalDate.of(2024, 1, 1);
            LocalDate defaultTo = to != null ? to : LocalDate.of(2026, 12, 1);
            
            // Получаем дивиденды напрямую от T-API
            List<DividendEntity> entities = dividendService.getDividendsFromApi(figi, defaultFrom, defaultTo);
            List<DividendDto> dividends = entities.stream()
                .map(this::convertToDto)
                .toList();
            
            return ResponseEntity.ok(dividends);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }
    
    /**
     * Загрузка дивидендов по FIGI с возможностью передачи дат в теле запроса
     */
    @PostMapping("/by-figi/{figi}")
    public ResponseEntity<Map<String, Object>> loadDividendsByFigi(
            @PathVariable String figi,
            @RequestBody(required = false) Map<String, String> requestBody) {
        
        try {
            // Устанавливаем значения по умолчанию
            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate to = LocalDate.of(2026, 12, 1);
            
            // Если переданы даты в теле запроса, используем их
            if (requestBody != null) {
                if (requestBody.containsKey("from")) {
                    from = LocalDate.parse(requestBody.get("from"));
                }
                if (requestBody.containsKey("to")) {
                    to = LocalDate.parse(requestBody.get("to"));
                }
            }
            
            // Загружаем дивиденды для конкретного FIGI с подробной статистикой
            Map<String, Object> result = dividendService.loadDividendsForSingleInstrument(figi, from, to);
            result.put("timestamp", LocalDate.now().toString());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка загрузки дивидендов для " + figi + ": " + e.getMessage());
            response.put("figi", figi);
            response.put("timestamp", LocalDate.now().toString());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    
    
    private DividendDto convertToDto(DividendEntity entity) {
        return new DividendDto(
            entity.getFigi(),
            entity.getDeclaredDate(),
            entity.getRecordDate(),
            entity.getPaymentDate(),
            entity.getDividendValue(),
            entity.getCurrency(),
            entity.getDividendType()
        );
    }
}
