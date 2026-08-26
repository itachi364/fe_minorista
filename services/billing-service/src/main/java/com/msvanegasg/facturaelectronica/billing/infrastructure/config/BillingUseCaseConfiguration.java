package com.msvanegasg.facturaelectronica.billing.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.billing.application.port.in.ManageFiscalNoteUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ManageCompanyFiscalPolicyUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ManageSaleUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.AssignFiscalNumberUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ConfigureIssuerProfileUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.CreateNumberingResolutionUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.QueryFiscalConfigurationUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.AccountingEntryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.AuditEventPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.CompanyFiscalPolicyRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentProviderPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FinalConsumerProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalDocumentUsagePort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalNoteProviderPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalNoteRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.InventoryAvailabilityPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.InventoryMovementPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.LicenseValidationPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IssuerProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.NumberingResolutionRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.OperationalPinValidationPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.SaleDocumentTypeOverrideRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.SaleRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.usecase.AssignFiscalNumberService;
import com.msvanegasg.facturaelectronica.billing.application.usecase.CompanyFiscalPolicyService;
import com.msvanegasg.facturaelectronica.billing.application.usecase.ConfigureIssuerProfileService;
import com.msvanegasg.facturaelectronica.billing.application.usecase.CreateNumberingResolutionService;
import com.msvanegasg.facturaelectronica.billing.application.usecase.FiscalNoteManagementService;
import com.msvanegasg.facturaelectronica.billing.application.usecase.QueryFiscalConfigurationService;
import com.msvanegasg.facturaelectronica.billing.application.usecase.SaleManagementService;

import com.msvanegasg.facturaelectronica.eventing.DomainEventPublisherPort;

@Configuration
public class BillingUseCaseConfiguration {

    @Bean
    ManageSaleUseCase manageSaleUseCase(SaleRepositoryPort saleRepository,
            InventoryAvailabilityPort inventoryAvailability, ElectronicDocumentProviderPort providerPort,
            InventoryMovementPort inventoryMovementPort, AccountingEntryPort accountingEntryPort,
            AuditEventPort auditEventPort, FinalConsumerProfileRepositoryPort finalConsumerProfileRepository,
            LicenseValidationPort licenseValidationPort, FiscalDocumentUsagePort fiscalDocumentUsagePort,
            CompanyFiscalPolicyRepositoryPort companyFiscalPolicyRepository,
            SaleDocumentTypeOverrideRepositoryPort saleDocumentTypeOverrideRepository,
            OperationalPinValidationPort operationalPinValidationPort,
            AssignFiscalNumberUseCase assignFiscalNumberUseCase, DomainEventPublisherPort eventPublisher,
            IdGeneratorPort idGenerator, ClockPort clock) {
        return new SaleManagementService(saleRepository, inventoryAvailability, providerPort, inventoryMovementPort,
                accountingEntryPort, auditEventPort, finalConsumerProfileRepository, licenseValidationPort,
                fiscalDocumentUsagePort, companyFiscalPolicyRepository, saleDocumentTypeOverrideRepository,
                operationalPinValidationPort, assignFiscalNumberUseCase, eventPublisher, idGenerator, clock);
    }

    @Bean
    ConfigureIssuerProfileUseCase configureIssuerProfileUseCase(IssuerProfileRepositoryPort issuerProfileRepository,
            IdGeneratorPort idGenerator) {
        return new ConfigureIssuerProfileService(issuerProfileRepository, idGenerator);
    }

    @Bean
    CreateNumberingResolutionUseCase createNumberingResolutionUseCase(
            NumberingResolutionRepositoryPort numberingResolutionRepository, IdGeneratorPort idGenerator) {
        return new CreateNumberingResolutionService(numberingResolutionRepository, idGenerator);
    }

    @Bean
    AssignFiscalNumberUseCase assignFiscalNumberUseCase(IssuerProfileRepositoryPort issuerProfileRepository,
            NumberingResolutionRepositoryPort numberingResolutionRepository) {
        return new AssignFiscalNumberService(issuerProfileRepository, numberingResolutionRepository);
    }

    @Bean
    QueryFiscalConfigurationUseCase queryFiscalConfigurationUseCase(IssuerProfileRepositoryPort issuerProfileRepository,
            NumberingResolutionRepositoryPort numberingResolutionRepository) {
        return new QueryFiscalConfigurationService(issuerProfileRepository, numberingResolutionRepository);
    }

    @Bean
    ManageFiscalNoteUseCase manageFiscalNoteUseCase(FiscalNoteRepositoryPort noteRepository,
            SaleRepositoryPort saleRepository, FiscalNoteProviderPort providerPort,
            LicenseValidationPort licenseValidationPort, FiscalDocumentUsagePort fiscalDocumentUsagePort,
            AssignFiscalNumberUseCase assignFiscalNumberUseCase, IdGeneratorPort idGenerator, ClockPort clock) {
        return new FiscalNoteManagementService(noteRepository, saleRepository, providerPort, licenseValidationPort,
                fiscalDocumentUsagePort, assignFiscalNumberUseCase, idGenerator, clock);
    }

    @Bean
    ManageCompanyFiscalPolicyUseCase manageCompanyFiscalPolicyUseCase(
            CompanyFiscalPolicyRepositoryPort companyFiscalPolicyRepository, ClockPort clock) {
        return new CompanyFiscalPolicyService(companyFiscalPolicyRepository, clock);
    }
}
