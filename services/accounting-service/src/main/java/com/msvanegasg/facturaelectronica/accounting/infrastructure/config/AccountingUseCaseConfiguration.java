package com.msvanegasg.facturaelectronica.accounting.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.accounting.application.port.in.InitializeBasicAccountingSetupUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.CalculateWithholdingsUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.CalculateFiscalDocumentUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ConfigureAccountingUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.DiagnoseAccountingReadinessUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountsPayableUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountsReceivableUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountingRulesUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageChartOfAccountsUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageExpenseUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageFiscalCatalogUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageFiscalLegalSourcesUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageFiscalComplianceUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageMunicipalFiscalPackagesUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.QueryAccountingBooksUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.QueryWithholdingSnapshotsUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsPayablePaymentRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsReceivablePaymentRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsReceivableRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsPayableRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingEntryRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.CompanyTaxProfilePort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.ExpenseRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalParameterRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalLegalSourceRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalComplianceRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalDocumentCalculationRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.MunicipalFiscalPackageRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.MunicipalityCatalogPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.ThirdPartyFiscalProfilePort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.WithholdingCalculationSnapshotRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.WithholdingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.AccountsPayableManagementService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.AccountsReceivableManagementService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.BasicAccountingSetupService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.AccountingConfigurationService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.AccountingReadinessDiagnosticService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.AccountingRuleManagementService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.ChartOfAccountsService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.ExpenseManagementService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.GenerateAccountingEntryService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.FiscalCatalogManagementService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.FiscalLegalSourceManagementService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.FiscalComplianceService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.FiscalDocumentCalculationService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.MunicipalFiscalPackageService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.QueryAccountingBooksService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.QueryWithholdingSnapshotsService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.WithholdingCalculationService;
import com.msvanegasg.facturaelectronica.eventing.DomainEventPublisherPort;

@Configuration
public class AccountingUseCaseConfiguration {

    @Bean
    Clock accountingClock() {
        return Clock.systemUTC();
    }

    @Bean
    InitializeBasicAccountingSetupUseCase initializeBasicAccountingSetupUseCase(
            AccountRepositoryPort accountRepository,
            AccountingRuleRepositoryPort ruleRepository,
            IdGeneratorPort idGenerator) {
        return new BasicAccountingSetupService(accountRepository, ruleRepository, idGenerator);
    }

    @Bean
    ConfigureAccountingUseCase configureAccountingUseCase(
            AccountRepositoryPort accountRepository,
            AccountingRuleRepositoryPort ruleRepository,
            AccountingEntryRepositoryPort entryRepository,
            IdGeneratorPort idGenerator) {
        return new AccountingConfigurationService(accountRepository, ruleRepository, entryRepository, idGenerator);
    }

    @Bean
    ManageChartOfAccountsUseCase manageChartOfAccountsUseCase(
            AccountRepositoryPort accountRepository,
            AccountingEntryRepositoryPort entryRepository,
            IdGeneratorPort idGenerator) {
        return new ChartOfAccountsService(accountRepository, entryRepository, idGenerator);
    }

    @Bean
    ManageAccountingRulesUseCase manageAccountingRulesUseCase(
            AccountingRuleRepositoryPort ruleRepository,
            AccountRepositoryPort accountRepository,
            AccountingEntryRepositoryPort entryRepository,
            IdGeneratorPort idGenerator) {
        return new AccountingRuleManagementService(ruleRepository, accountRepository, entryRepository, idGenerator);
    }

    @Bean
    DiagnoseAccountingReadinessUseCase diagnoseAccountingReadinessUseCase(
            AccountingRuleRepositoryPort ruleRepository,
            AccountRepositoryPort accountRepository) {
        return new AccountingReadinessDiagnosticService(ruleRepository, accountRepository);
    }

    @Bean
    GenerateAccountingEntryUseCase generateAccountingEntryUseCase(
            AccountingRuleRepositoryPort ruleRepository,
            AccountRepositoryPort accountRepository,
            AccountingEntryRepositoryPort entryRepository,
            DomainEventPublisherPort eventPublisher,
            IdGeneratorPort idGenerator, Clock accountingClock) {
        return new GenerateAccountingEntryService(ruleRepository, accountRepository, entryRepository, eventPublisher,
                idGenerator, accountingClock);
    }

    @Bean
    CalculateWithholdingsUseCase calculateWithholdingsUseCase(
            WithholdingRuleRepositoryPort ruleRepository,
            WithholdingCalculationSnapshotRepositoryPort snapshotRepository,
            ThirdPartyFiscalProfilePort thirdPartyFiscalProfilePort,
            CompanyTaxProfilePort companyTaxProfilePort,
            IdGeneratorPort idGenerator,
            Clock accountingClock, FiscalParameterRepositoryPort parameterRepository) {
        return new WithholdingCalculationService(ruleRepository, snapshotRepository, thirdPartyFiscalProfilePort,
                idGenerator, accountingClock, parameterRepository, companyTaxProfilePort);
    }

    @Bean
    CalculateFiscalDocumentUseCase calculateFiscalDocumentUseCase(CalculateWithholdingsUseCase lineCalculator,
            FiscalDocumentCalculationRepositoryPort repository, CompanyTaxProfilePort companyTaxProfilePort,
            ThirdPartyFiscalProfilePort thirdPartyFiscalProfilePort, IdGeneratorPort idGenerator,
            Clock accountingClock) {
        return new FiscalDocumentCalculationService(lineCalculator, repository, companyTaxProfilePort,
                thirdPartyFiscalProfilePort, idGenerator, accountingClock);
    }

    @Bean
    ManageFiscalCatalogUseCase manageFiscalCatalogUseCase(WithholdingRuleRepositoryPort ruleRepository,
            FiscalParameterRepositoryPort parameterRepository, IdGeneratorPort idGenerator) {
        return new FiscalCatalogManagementService(ruleRepository, parameterRepository, idGenerator);
    }

    @Bean
    ManageFiscalLegalSourcesUseCase manageFiscalLegalSourcesUseCase(FiscalLegalSourceRepositoryPort repository,
            IdGeneratorPort idGenerator, Clock accountingClock) {
        return new FiscalLegalSourceManagementService(repository, idGenerator, accountingClock);
    }

    @Bean
    ManageMunicipalFiscalPackagesUseCase manageMunicipalFiscalPackagesUseCase(
            MunicipalFiscalPackageRepositoryPort repository, MunicipalityCatalogPort municipalityCatalog,
            IdGeneratorPort idGenerator) {
        return new MunicipalFiscalPackageService(repository, municipalityCatalog, idGenerator);
    }

    @Bean
    ManageFiscalComplianceUseCase manageFiscalComplianceUseCase(FiscalComplianceRepositoryPort repository) {
        return new FiscalComplianceService(repository);
    }

    @Bean
    QueryWithholdingSnapshotsUseCase queryWithholdingSnapshotsUseCase(
            WithholdingCalculationSnapshotRepositoryPort snapshotRepository) {
        return new QueryWithholdingSnapshotsService(snapshotRepository);
    }

    @Bean
    QueryAccountingBooksUseCase queryAccountingBooksUseCase(
            AccountingEntryRepositoryPort entryRepository,
            AccountRepositoryPort accountRepository) {
        return new QueryAccountingBooksService(entryRepository, accountRepository);
    }

    @Bean
    ManageExpenseUseCase manageExpenseUseCase(ExpenseRepositoryPort expenseRepository,
            AccountsPayableRepositoryPort payableRepository, GenerateAccountingEntryUseCase accountingEntryUseCase,
            IdGeneratorPort idGenerator, Clock accountingClock,
            CalculateWithholdingsUseCase calculateWithholdingsUseCase) {
        return new ExpenseManagementService(expenseRepository, payableRepository, accountingEntryUseCase, idGenerator,
                accountingClock, calculateWithholdingsUseCase);
    }


    @Bean
    ManageAccountsReceivableUseCase manageAccountsReceivableUseCase(
            AccountsReceivableRepositoryPort receivableRepository,
            AccountsReceivablePaymentRepositoryPort paymentRepository,
            GenerateAccountingEntryUseCase accountingEntryUseCase,
            IdGeneratorPort idGenerator, Clock accountingClock) {
        return new AccountsReceivableManagementService(receivableRepository, paymentRepository, accountingEntryUseCase,
                idGenerator, accountingClock);
    }
    @Bean
    ManageAccountsPayableUseCase manageAccountsPayableUseCase(AccountsPayableRepositoryPort payableRepository,
            AccountsPayablePaymentRepositoryPort paymentRepository, GenerateAccountingEntryUseCase accountingEntryUseCase,
            IdGeneratorPort idGenerator, Clock accountingClock) {
        return new AccountsPayableManagementService(payableRepository, paymentRepository, accountingEntryUseCase,
                idGenerator, accountingClock);
    }
}
