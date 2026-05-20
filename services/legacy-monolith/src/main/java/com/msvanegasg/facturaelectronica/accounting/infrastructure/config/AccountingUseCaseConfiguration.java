package com.msvanegasg.facturaelectronica.accounting.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountingRulesUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageChartOfAccountsUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.QueryAccountingBooksUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingEntryRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.AccountingRuleManagementService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.ChartOfAccountsService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.GenerateAccountingEntryService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.QueryAccountingBooksService;

@Configuration
public class AccountingUseCaseConfiguration {

    @Bean
    ManageChartOfAccountsUseCase manageChartOfAccountsUseCase(
            AccountRepositoryPort accountRepository,
            IdGeneratorPort idGenerator) {
        return new ChartOfAccountsService(accountRepository, idGenerator);
    }

    @Bean
    ManageAccountingRulesUseCase manageAccountingRulesUseCase(
            AccountingRuleRepositoryPort ruleRepository,
            AccountRepositoryPort accountRepository,
            IdGeneratorPort idGenerator) {
        return new AccountingRuleManagementService(ruleRepository, accountRepository, idGenerator);
    }

    @Bean
    GenerateAccountingEntryUseCase generateAccountingEntryUseCase(
            AccountingRuleRepositoryPort ruleRepository,
            AccountRepositoryPort accountRepository,
            AccountingEntryRepositoryPort entryRepository,
            IdGeneratorPort idGenerator) {
        return new GenerateAccountingEntryService(ruleRepository, accountRepository, entryRepository, idGenerator);
    }

    @Bean
    QueryAccountingBooksUseCase queryAccountingBooksUseCase(
            AccountingEntryRepositoryPort entryRepository,
            AccountRepositoryPort accountRepository) {
        return new QueryAccountingBooksService(entryRepository, accountRepository);
    }
}
