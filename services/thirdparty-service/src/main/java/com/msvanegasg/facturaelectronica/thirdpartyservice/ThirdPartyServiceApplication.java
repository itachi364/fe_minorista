package com.msvanegasg.facturaelectronica.thirdpartyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.msvanegasg.facturaelectronica")
@EntityScan("com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.entity")
@EnableJpaRepositories("com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.repository")
public class ThirdPartyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThirdPartyServiceApplication.class, args);
    }
}
