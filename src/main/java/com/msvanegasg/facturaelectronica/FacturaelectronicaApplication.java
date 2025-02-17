package com.msvanegasg.facturaelectronica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.msvanegasg.facturaelectronica")
@EnableJpaRepositories(basePackages = "com.msvanegasg.facturaelectronica.repository")
@EntityScan(basePackages = "com.msvanegasg.facturaelectronica.models")
public class FacturaelectronicaApplication {

	public static void main(String[] args) {
		SpringApplication.run(FacturaelectronicaApplication.class, args);
	}

}
