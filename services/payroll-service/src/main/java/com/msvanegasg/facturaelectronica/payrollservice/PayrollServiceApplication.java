package com.msvanegasg.facturaelectronica.payrollservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.msvanegasg.facturaelectronica")
@EntityScan("com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.entity")
@EnableJpaRepositories("com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.repository")
public class PayrollServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayrollServiceApplication.class, args);
    }
}
