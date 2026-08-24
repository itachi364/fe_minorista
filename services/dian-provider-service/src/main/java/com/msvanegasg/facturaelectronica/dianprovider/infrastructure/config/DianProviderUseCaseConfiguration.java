package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianConfigurationRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianIdentifierCalculationPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianSignaturePort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianSubmissionTraceRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianTechnicalArtifactPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianTechnicalValidationPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianTransportPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.FiscalArtifactStoragePort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.FiscalDocumentXmlBuilderPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.ProviderSubmissionRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.SecretVaultPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.usecase.DianConfigurationManagementService;
import com.msvanegasg.facturaelectronica.dianprovider.application.usecase.DianProviderSubmissionService;

@Configuration
public class DianProviderUseCaseConfiguration {

    @Bean
    DianProviderSubmissionService dianProviderSubmissionService(ProviderSubmissionRepositoryPort repository,
            DianConfigurationRepositoryPort configurationRepository, DianTechnicalArtifactPort technicalArtifacts,
            FiscalDocumentXmlBuilderPort xmlBuilder, DianIdentifierCalculationPort identifierCalculator,
            DianSignaturePort signature, DianTechnicalValidationPort technicalValidation, DianTransportPort transport,
            FiscalArtifactStoragePort artifactStorage, DianSubmissionTraceRepositoryPort traceRepository,
            IdGeneratorPort idGenerator, ClockPort clock, DianProviderProperties properties) {
        return new DianProviderSubmissionService(repository, configurationRepository, technicalArtifacts, xmlBuilder,
                identifierCalculator, signature, technicalValidation, transport, artifactStorage, traceRepository,
                idGenerator, clock, properties);
    }

    @Bean
    DianConfigurationManagementService dianConfigurationManagementService(DianConfigurationRepositoryPort repository,
            SecretVaultPort secretVault, DianTechnicalArtifactPort technicalArtifacts, IdGeneratorPort idGenerator,
            ClockPort clock) {
        return new DianConfigurationManagementService(repository, secretVault, technicalArtifacts, idGenerator, clock);
    }
}
