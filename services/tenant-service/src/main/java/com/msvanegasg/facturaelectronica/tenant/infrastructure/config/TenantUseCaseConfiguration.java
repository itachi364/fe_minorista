package com.msvanegasg.facturaelectronica.tenant.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyUseCase;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.tenant.application.usecase.CompanyManagementService;

@Configuration
public class TenantUseCaseConfiguration {

    @Bean
    ManageCompanyUseCase manageCompanyUseCase(
            CompanyRepositoryPort companyRepository,
            IdGeneratorPort idGenerator,
            ClockPort clock) {
        return new CompanyManagementService(companyRepository, idGenerator, clock);
    }
}
