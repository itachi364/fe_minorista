package com.msvanegasg.facturaelectronica.expenses.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.expenses.application.port.in.ManageExpenseUseCase;
import com.msvanegasg.facturaelectronica.expenses.application.port.out.ExpenseCatalogPort;
import com.msvanegasg.facturaelectronica.expenses.application.port.out.ExpenseRepositoryPort;
import com.msvanegasg.facturaelectronica.expenses.application.usecase.ExpenseManagementService;

@Configuration
public class ExpensesUseCaseConfiguration {

    @Bean
    ManageExpenseUseCase manageExpenseUseCase(ExpenseRepositoryPort expenseRepositoryPort,
            ExpenseCatalogPort expenseCatalogPort) {
        return new ExpenseManagementService(expenseRepositoryPort, expenseCatalogPort);
    }
}
