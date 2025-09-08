package com.example.InvestmentDataLoaderService.dto;

import java.util.List;

public class LastTradesRequestDto {
    private List<String> figis;
    private String tradeSource;

    public LastTradesRequestDto() {}

    public LastTradesRequestDto(List<String> figis, String tradeSource) {
        this.figis = figis;
        this.tradeSource = tradeSource;
    }

    public List<String> getFigis() {
        return figis;
    }

    public void setFigis(List<String> figis) {
        this.figis = figis;
    }

    public String getTradeSource() {
        return tradeSource;
    }

    public void setTradeSource(String tradeSource) {
        this.tradeSource = tradeSource;
    }
    
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
