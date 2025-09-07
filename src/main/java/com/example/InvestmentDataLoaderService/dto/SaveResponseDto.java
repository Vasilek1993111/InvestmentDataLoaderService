package com.example.InvestmentDataLoaderService.dto;

import java.util.List;

public class SaveResponseDto {
    private boolean success;
    private String message;
    private int totalRequested;
    private int newItemsSaved;
    private int existingItemsSkipped;
    private List<?> savedItems;

    public SaveResponseDto() {}

    public SaveResponseDto(boolean success, String message, int totalRequested, int newItemsSaved, int existingItemsSkipped, List<?> savedItems) {
        this.success = success;
        this.message = message;
        this.totalRequested = totalRequested;
        this.newItemsSaved = newItemsSaved;
        this.existingItemsSkipped = existingItemsSkipped;
        this.savedItems = savedItems;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getTotalRequested() { return totalRequested; }
    public void setTotalRequested(int totalRequested) { this.totalRequested = totalRequested; }

    public int getNewItemsSaved() { return newItemsSaved; }
    public void setNewItemsSaved(int newItemsSaved) { this.newItemsSaved = newItemsSaved; }

    public int getExistingItemsSkipped() { return existingItemsSkipped; }
    public void setExistingItemsSkipped(int existingItemsSkipped) { this.existingItemsSkipped = existingItemsSkipped; }

    public List<?> getSavedItems() { return savedItems; }
    public void setSavedItems(List<?> savedItems) { this.savedItems = savedItems; }
}
