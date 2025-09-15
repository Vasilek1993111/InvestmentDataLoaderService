package com.example.InvestmentDataLoaderService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LastTradesRequestDto {
    private List<String> figis;
    private String tradeSource;
    
    /**
     * Проверяет, нужно ли загружать все инструменты
     */
    public boolean isLoadAll() {
        return figis != null && figis.size() == 1 && "ALL".equalsIgnoreCase(figis.get(0));
    }
    
    /**
     * Проверяет, нужно ли загружать все акции
     */
    public boolean isLoadAllShares() {
        return figis != null && figis.size() == 1 && "ALL_SHARES".equalsIgnoreCase(figis.get(0));
    }
    
    /**
     * Проверяет, нужно ли загружать все фьючерсы
     */
    public boolean isLoadAllFutures() {
        return figis != null && figis.size() == 1 && "ALL_FUTURES".equalsIgnoreCase(figis.get(0));
    }
}