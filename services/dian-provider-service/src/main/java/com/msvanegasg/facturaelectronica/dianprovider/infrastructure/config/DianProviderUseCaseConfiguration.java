package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.ProviderSubmissionRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.usecase.MockProviderSubmissionService;

@Configuration
public class DianProviderUseCaseConfiguration {

    @Bean
    MockProviderSubmissionService mockProviderSubmissionService(ProviderSubmissionRepositoryPort repository,
            IdGeneratorPort idGenerator, ClockPort clock, DianProviderProperties properties) {
        return new MockProviderSubmissionService(repository, idGenerator, clock, properties);
    }
}
