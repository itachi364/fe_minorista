package com.msvanegasg.facturaelectronica.catalogservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.msvanegasg.facturaelectronica.catalogservice",
        "com.msvanegasg.facturaelectronica.catalog.infrastructure",
        "com.msvanegasg.facturaelectronica.catalog.interfaces",
        "com.msvanegasg.facturaelectronica.controller",
        "com.msvanegasg.facturaelectronica.exception",
        "com.msvanegasg.facturaelectronica.observability"
})
@EntityScan("com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity")
@EnableJpaRepositories("com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository")
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
