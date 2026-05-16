package com.msvanegasg.facturaelectronica.billing.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.billing.application.port.in.AssignFiscalNumberUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ConfigureIssuerProfileUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.CreateNumberingResolutionUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.IssueElectronicPosUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.QueryElectronicPosDocumentUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.RegisterProviderSubmissionOutcomeUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.SubmitElectronicDocumentToProviderUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.SubmitElectronicPosDocumentUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.DianProviderPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentLifecycleRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentTraceEventRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicPosDocumentRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalAuditEventRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IssuerProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.NumberingResolutionRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ProviderSubmissionRecordRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.usecase.AssignFiscalNumberService;
import com.msvanegasg.facturaelectronica.billing.application.usecase.ConfigureIssuerProfileService;
import com.msvanegasg.facturaelectronica.billing.application.usecase.CreateNumberingResolutionService;
import com.msvanegasg.facturaelectronica.billing.application.usecase.IssueElectronicPosService;
import com.msvanegasg.facturaelectronica.billing.application.usecase.QueryElectronicPosDocumentService;
import com.msvanegasg.facturaelectronica.billing.application.usecase.RegisterProviderSubmissionOutcomeService;
import com.msvanegasg.facturaelectronica.billing.application.usecase.SubmitElectronicDocumentToProviderService;
import com.msvanegasg.facturaelectronica.billing.application.usecase.SubmitElectronicPosDocumentService;
import com.msvanegasg.facturaelectronica.billing.infrastructure.provider.DummyDianProviderAdapter;
import com.msvanegasg.facturaelectronica.billing.infrastructure.transaction.TransactionalAssignFiscalNumberUseCase;

@Configuration
@EnableConfigurationProperties(DianMockProviderProperties.class)
public class BillingUseCaseConfiguration {

    @Bean
    DianProviderPort dianProviderPort(
            DianMockProviderProperties properties,
            @Value("${dian.provider.mode:mock}") String providerMode) {
        if (!"mock".equalsIgnoreCase(providerMode)) {
            throw new IllegalStateException("Only DIAN provider mode 'mock' is available in this build");
        }
        return new DummyDianProviderAdapter(
                properties.getDefaultStatus(),
                properties.getErrorCode(),
                properties.getErrorMessage());
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
        AssignFiscalNumberUseCase service = new AssignFiscalNumberService(issuerProfileRepository,
                numberingResolutionRepository);
        return new TransactionalAssignFiscalNumberUseCase(service);
    }

    @Bean
    IssueElectronicPosUseCase issueElectronicPosUseCase(AssignFiscalNumberUseCase assignFiscalNumberUseCase,
            ElectronicPosDocumentRepositoryPort posDocumentRepository, IdGeneratorPort idGenerator, ClockPort clock) {
        return new IssueElectronicPosService(assignFiscalNumberUseCase, posDocumentRepository, idGenerator, clock);
    }

    @Bean
    QueryElectronicPosDocumentUseCase queryElectronicPosDocumentUseCase(
            ElectronicPosDocumentRepositoryPort posDocumentRepository) {
        return new QueryElectronicPosDocumentService(posDocumentRepository);
    }

    @Bean
    SubmitElectronicDocumentToProviderUseCase submitElectronicDocumentToProviderUseCase(DianProviderPort dianProvider,
            ProviderSubmissionRecordRepositoryPort submissionRecordRepository, IdGeneratorPort idGenerator,
            ClockPort clock) {
        return new SubmitElectronicDocumentToProviderService(dianProvider, submissionRecordRepository, idGenerator,
                clock);
    }

    @Bean
    RegisterProviderSubmissionOutcomeUseCase registerProviderSubmissionOutcomeUseCase(
            ElectronicDocumentLifecycleRepositoryPort documentRepository,
            ElectronicDocumentTraceEventRepositoryPort traceEventRepository,
            FiscalAuditEventRepositoryPort auditEventRepository,
            IdGeneratorPort idGenerator,
            ClockPort clock) {
        return new RegisterProviderSubmissionOutcomeService(documentRepository, traceEventRepository,
                auditEventRepository, idGenerator, clock);
    }

    @Bean
    SubmitElectronicPosDocumentUseCase submitElectronicPosDocumentUseCase(
            ElectronicPosDocumentRepositoryPort posDocumentRepository,
            ElectronicDocumentLifecycleRepositoryPort lifecycleRepository,
            SubmitElectronicDocumentToProviderUseCase providerSubmissionUseCase,
            RegisterProviderSubmissionOutcomeUseCase registerOutcomeUseCase,
            ClockPort clock) {
        return new SubmitElectronicPosDocumentService(posDocumentRepository, lifecycleRepository,
                providerSubmissionUseCase, registerOutcomeUseCase, clock);
    }
}
