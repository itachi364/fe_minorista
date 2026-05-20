package com.msvanegasg.facturaelectronica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.msvanegasg.facturaelectronica")
@EnableJpaRepositories(basePackages = {
		"com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository",
		"com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.repository",
		"com.msvanegasg.facturaelectronica.expenses.infrastructure.persistence.repository",
		"com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository",
		"com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository",
		"com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository"
})
@EntityScan(basePackages = {
		"com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity",
		"com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.entity",
		"com.msvanegasg.facturaelectronica.expenses.infrastructure.persistence.entity",
		"com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity",
		"com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity",
		"com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity"
})
public class FacturaelectronicaApplication {

	public static void main(String[] args) {
		SpringApplication.run(FacturaelectronicaApplication.class, args);
	}

}
