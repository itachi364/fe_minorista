package com.msvanegasg.facturaelectronica.bff.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bff.services")
public record BffProperties(String tenantUrl, String identityUrl, String catalogUrl, String thirdpartyUrl,
        String inventoryUrl, String billingUrl, String accountingUrl, String payrollUrl, String reportingUrl,
        String auditUrl) {
}
