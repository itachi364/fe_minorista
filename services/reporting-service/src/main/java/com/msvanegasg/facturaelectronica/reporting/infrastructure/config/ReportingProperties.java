package com.msvanegasg.facturaelectronica.reporting.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reporting.services")
public record ReportingProperties(
        String identityUrl,
        String inventoryUrl,
        String billingUrl,
        String accountingUrl,
        String payrollUrl,
        String tenantUrl) {
}
