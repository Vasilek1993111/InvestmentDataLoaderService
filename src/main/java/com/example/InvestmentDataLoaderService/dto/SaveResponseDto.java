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
    private List<?> savedItems;
}