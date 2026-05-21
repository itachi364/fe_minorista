package com.msvanegasg.facturaelectronica.accounting.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountsPayableUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountingRulesUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageChartOfAccountsUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageExpenseUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.QueryAccountingBooksUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsPayablePaymentRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsPayableRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingEntryRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.ExpenseRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.AccountsPayableManagementService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.AccountingRuleManagementService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.ChartOfAccountsService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.ExpenseManagementService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.GenerateAccountingEntryService;
import com.msvanegasg.facturaelectronica.accounting.application.usecase.QueryAccountingBooksService;

@Configuration
public class AccountingUseCaseConfiguration {

    @Bean
    Clock accountingClock() {
        return Clock.systemUTC();
    }

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

    @Bean
    ManageExpenseUseCase manageExpenseUseCase(ExpenseRepositoryPort expenseRepository,
            AccountsPayableRepositoryPort payableRepository, GenerateAccountingEntryUseCase accountingEntryUseCase,
            IdGeneratorPort idGenerator, Clock accountingClock) {
        return new ExpenseManagementService(expenseRepository, payableRepository, accountingEntryUseCase, idGenerator,
                accountingClock);
    }

    @Bean
    ManageAccountsPayableUseCase manageAccountsPayableUseCase(AccountsPayableRepositoryPort payableRepository,
            AccountsPayablePaymentRepositoryPort paymentRepository, GenerateAccountingEntryUseCase accountingEntryUseCase,
            IdGeneratorPort idGenerator, Clock accountingClock) {
        return new AccountsPayableManagementService(payableRepository, paymentRepository, accountingEntryUseCase,
                idGenerator, accountingClock);
    }
}
