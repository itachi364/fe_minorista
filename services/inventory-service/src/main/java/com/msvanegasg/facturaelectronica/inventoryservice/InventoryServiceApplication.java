package com.msvanegasg.facturaelectronica.inventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.msvanegasg.facturaelectronica.inventory")
@EntityScan("com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity")
@EnableJpaRepositories("com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository")
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
