package com.example.InvestmentDataLoaderService.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "system_logs", schema = "invest")
public class SystemLogEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "task_id", nullable = false)
    private String taskId;
    
    @Column(name = "endpoint", nullable = false)
    private String endpoint;
    
    @Column(name = "method", nullable = false)
    private String method;
    
    @Column(name = "status", nullable = false)
    private String status;
    
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Column(name = "start_time", nullable = false)
    private Instant startTime;
    
    @Column(name = "end_time")
    private Instant endTime;
    
    @Column(name = "duration_ms")
    private Long durationMs;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public SystemLogEntity() {
        this.createdAt = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).toInstant();
    }

    public SystemLogEntity(String taskId, String endpoint, String method, String status, String message) {
        this();
        this.taskId = taskId;
        this.endpoint = endpoint;
        this.method = method;
        this.status = status;
        this.message = message;
        this.startTime = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).toInstant();
    }

    @PreUpdate
    public void preUpdate() {
        if (this.endTime != null && this.startTime != null) {
            this.durationMs = this.endTime.toEpochMilli() - this.startTime.toEpochMilli();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}