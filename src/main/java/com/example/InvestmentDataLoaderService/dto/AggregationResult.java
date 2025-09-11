package com.example.InvestmentDataLoaderService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregationResult {
    private String taskId;
    private int processedInstruments;
    private int successfulInstruments;
    private int errorInstruments;
    private boolean success;
    private String errorMessage;
    private String instrumentType; // "shares" или "futures"
    
    public AggregationResult(String taskId, String instrumentType) {
        this.taskId = taskId;
        this.instrumentType = instrumentType;
        this.processedInstruments = 0;
        this.successfulInstruments = 0;
        this.errorInstruments = 0;
        this.success = false;
    }
}
