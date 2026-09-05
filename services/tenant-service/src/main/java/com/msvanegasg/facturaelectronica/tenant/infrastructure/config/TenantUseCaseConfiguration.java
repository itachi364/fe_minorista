package com.msvanegasg.facturaelectronica.tenant.infrastructure.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyLicenseUseCase;
import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyUseCase;
import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyBrandingUseCase;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.BrandingAssetStoragePort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyBrandingRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyFileAssetUseCase;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyFileAssetRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyFileStoragePort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyLicenseRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.tenant.application.usecase.CompanyFileAssetManagementService;
import com.msvanegasg.facturaelectronica.tenant.application.usecase.CompanyBrandingManagementService;
import com.msvanegasg.facturaelectronica.tenant.application.usecase.CompanyLicenseManagementService;
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

    @Bean
    ManageCompanyLicenseUseCase manageCompanyLicenseUseCase(
            CompanyRepositoryPort companyRepository,
            CompanyLicenseRepositoryPort licenseRepository,
            IdGeneratorPort idGenerator,
            ClockPort clock) {
        return new CompanyLicenseManagementService(companyRepository, licenseRepository, idGenerator, clock);
    }

    @Bean
    ManageCompanyBrandingUseCase manageCompanyBrandingUseCase(
            CompanyRepositoryPort companyRepository,
            CompanyBrandingRepositoryPort brandingRepository,
            BrandingAssetStoragePort storage,
            ClockPort clock) {
        return new CompanyBrandingManagementService(companyRepository, brandingRepository, storage, clock);
    }

    @Bean
    ManageCompanyFileAssetUseCase manageCompanyFileAssetUseCase(
            CompanyRepositoryPort companyRepository,
            CompanyFileAssetRepositoryPort fileAssetRepository,
            CompanyFileStoragePort storage,
            IdGeneratorPort idGenerator,
            ClockPort clock,
            @Value("${tenant.files.download-ttl-seconds:300}") long downloadTtlSeconds,
            @Value("${tenant.files.download-token-secret:local-development-download-token-secret-change-me}") String downloadTokenSecret) {
        return new CompanyFileAssetManagementService(companyRepository, fileAssetRepository, storage, idGenerator,
                clock, Duration.ofSeconds(downloadTtlSeconds), downloadTokenSecret);
    }
}
