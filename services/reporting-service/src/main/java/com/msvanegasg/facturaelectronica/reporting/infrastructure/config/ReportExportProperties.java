package com.msvanegasg.facturaelectronica.reporting.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "reporting.exports")
public record ReportExportProperties(
        @NotBlank String appPublicBaseUrl,
        @NotBlank String localStoragePath,
        @Min(1) int downloadPresignedTtlSeconds,
        @Min(1) int linkTokenTtlHours,
        @Min(1) int retentionDays,
        boolean workerEnabled,
        @Min(1000) long workerFixedDelayMs,
        @Min(1) int batchSize) {

    public Duration linkTokenTtl() {
        return Duration.ofHours(linkTokenTtlHours);
    }

    public Duration retentionDuration() {
        return Duration.ofDays(retentionDays);
    }
}
