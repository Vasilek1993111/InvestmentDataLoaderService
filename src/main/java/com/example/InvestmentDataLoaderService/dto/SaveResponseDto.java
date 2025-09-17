package com.example.InvestmentDataLoaderService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveResponseDto {
    private boolean success;
    private String message;
    private int totalRequested;
    private int newItemsSaved;
    private int existingItemsSkipped;
    private int invalidItemsFiltered;
    private int missingFromApi; // Цены, которые не были получены из API
    private List<?> savedItems;
}