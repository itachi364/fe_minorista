package com.msvanegasg.facturaelectronica.billingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.msvanegasg.facturaelectronica.billing")
@EntityScan("com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity")
@EnableJpaRepositories("com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository")
public class BillingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }
}
