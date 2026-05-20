package com.msvanegasg.facturaelectronica.auditservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.msvanegasg.facturaelectronica.audit")
@EntityScan("com.msvanegasg.facturaelectronica.audit.infrastructure.persistence.entity")
@EnableJpaRepositories("com.msvanegasg.facturaelectronica.audit.infrastructure.persistence.repository")
public class AuditServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditServiceApplication.class, args);
    }
}
