package com.msvanegasg.facturaelectronica.accountingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.msvanegasg.facturaelectronica.accounting")
@EntityScan({
        "com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity",
        "com.msvanegasg.facturaelectronica.accounting.infrastructure.messaging.entity"
})
@EnableJpaRepositories({
        "com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository",
        "com.msvanegasg.facturaelectronica.accounting.infrastructure.messaging.repository"
})
public class AccountingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountingServiceApplication.class, args);
    }
}
