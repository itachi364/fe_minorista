package com.msvanegasg.facturaelectronica.dianproviderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.msvanegasg.facturaelectronica.dianprovider")
@EntityScan("com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.entity")
@EnableJpaRepositories("com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.repository")
public class DianProviderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DianProviderServiceApplication.class, args);
    }
}
