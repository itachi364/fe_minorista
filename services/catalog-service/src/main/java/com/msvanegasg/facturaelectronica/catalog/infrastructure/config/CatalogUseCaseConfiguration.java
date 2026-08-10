package com.msvanegasg.facturaelectronica.catalog.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageVersionedCatalogUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.CatalogAuditEventPort;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.VersionedCatalogRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.application.usecase.VersionedCatalogManagementService;

@Configuration
public class CatalogUseCaseConfiguration {

    @Bean
    ManageVersionedCatalogUseCase manageVersionedCatalogUseCase(
            VersionedCatalogRepositoryPort versionedCatalogRepositoryPort,
            CatalogAuditEventPort catalogAuditEventPort) {
        return new VersionedCatalogManagementService(versionedCatalogRepositoryPort, catalogAuditEventPort);
    }
}
