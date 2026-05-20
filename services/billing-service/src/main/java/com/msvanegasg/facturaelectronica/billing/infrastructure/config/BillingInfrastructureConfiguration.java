package com.msvanegasg.facturaelectronica.billing.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BillingProperties.class)
public class BillingInfrastructureConfiguration {
}
