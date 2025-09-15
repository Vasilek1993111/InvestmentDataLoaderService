package com.example.InvestmentDataLoaderService.exception;

/**
 * Исключение для ошибок планировщиков
 */
public class SchedulerException extends DataLoadException {
    
    private final String taskId;
    
    public SchedulerException(String message, String taskId) {
        super(message);
        this.taskId = taskId;
    }
    
    public SchedulerException(String message, String taskId, Throwable cause) {
        super(message, cause);
        this.taskId = taskId;
    }
    
    public String getTaskId() {
        return taskId;
    }
}
