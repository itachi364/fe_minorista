package com.msvanegasg.facturaelectronica.billing.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "billing")
public record BillingProperties(String inventoryServiceUrl, String providerServiceUrl, String accountingServiceUrl,
        String auditServiceUrl, String mockProviderDefaultStatus) {
}
