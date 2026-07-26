package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "System infrastructure health summary")
public class SystemHealthResponse {

    @Schema(description = "Database Health", example = "UP")
    private String databaseHealth;

    @Schema(description = "Redis Cache Health", example = "UP")
    private String redisHealth;

    @Schema(description = "Kafka Messaging Health", example = "UP")
    private String kafkaHealth;

    @Schema(description = "Health check timestamp")
    private LocalDateTime timestamp;

    public SystemHealthResponse() {
    }

    public SystemHealthResponse(String databaseHealth, String redisHealth, String kafkaHealth, LocalDateTime timestamp) {
        this.databaseHealth = databaseHealth;
        this.redisHealth = redisHealth;
        this.kafkaHealth = kafkaHealth;
        this.timestamp = timestamp;
    }

    public String getDatabaseHealth() {
        return databaseHealth;
    }

    public void setDatabaseHealth(String databaseHealth) {
        this.databaseHealth = databaseHealth;
    }

    public String getRedisHealth() {
        return redisHealth;
    }

    public void setRedisHealth(String redisHealth) {
        this.redisHealth = redisHealth;
    }

    public String getKafkaHealth() {
        return kafkaHealth;
    }

    public void setKafkaHealth(String kafkaHealth) {
        this.kafkaHealth = kafkaHealth;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
