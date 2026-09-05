package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingReadinessMissingItemResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingReadinessResult;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.DiagnoseAccountingReadinessUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRule;

public class AccountingReadinessDiagnosticService implements DiagnoseAccountingReadinessUseCase {

    private static final String ACCOUNTING_MODULE = "Configuracion contable";

    private final AccountingRuleRepositoryPort ruleRepository;
    private final AccountRepositoryPort accountRepository;

    public AccountingReadinessDiagnosticService(AccountingRuleRepositoryPort ruleRepository,
            AccountRepositoryPort accountRepository) {
        this.ruleRepository = Objects.requireNonNull(ruleRepository);
        this.accountRepository = Objects.requireNonNull(accountRepository);
    }

    @Override
    public AccountingReadinessResult diagnose(UUID companyId, AccountingEventType eventType) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(eventType, "eventType is required");
        return ruleRepository.findActiveByCompanyIdAndEventType(companyId, eventType)
                .map(rule -> diagnoseRule(companyId, eventType, rule))
                .orElseGet(() -> missingRule(companyId, eventType));
    }

    private AccountingReadinessResult diagnoseRule(UUID companyId, AccountingEventType eventType,
            AccountingRule rule) {
        List<AccountingReadinessMissingItemResult> missing = new ArrayList<>();
        List<String> checkedAccountCodes = rule.lines().stream()
                .map(line -> line.accountCode())
                .distinct()
                .toList();
        checkedAccountCodes.forEach(accountCode -> {
            boolean accountReady = accountRepository.findByCompanyIdAndCode(companyId, accountCode)
                    .filter(Account::active)
                    .isPresent();
            if (!accountReady) {
                missing.add(new AccountingReadinessMissingItemResult(
                        "ACCOUNT_NOT_ACTIVE",
                        ACCOUNTING_MODULE,
                        "La cuenta PUC " + accountCode + " no existe o esta inactiva.",
                        "Crea o activa la cuenta PUC " + accountCode + " antes de ejecutar " + label(eventType) + "."));
            }
        });
        return new AccountingReadinessResult(companyId, eventType, missing.isEmpty(), rule.id(), checkedAccountCodes,
                List.copyOf(missing));
    }

    private static AccountingReadinessResult missingRule(UUID companyId, AccountingEventType eventType) {
        return new AccountingReadinessResult(companyId, eventType, false, null, List.of(),
                List.of(new AccountingReadinessMissingItemResult(
                        "ACCOUNTING_RULE_NOT_ACTIVE",
                        ACCOUNTING_MODULE,
                        "No existe una regla contable activa para " + label(eventType) + ".",
                        "Crea una regla contable activa para " + label(eventType)
                                + " desde Configuracion contable.")));
    }

    private static String label(AccountingEventType eventType) {
        return switch (eventType) {
            case SALE_CONFIRMED -> "ventas confirmadas";
            case PURCHASE_CONFIRMED -> "compras confirmadas";
            case INVENTORY_REPLENISHMENT_CONFIRMED -> "reabastecimiento de inventario";
            case EXPENSE_CONFIRMED, OPERATING_EXPENSE_CONFIRMED -> "egresos operativos";
            case ASSET_PURCHASE_CONFIRMED -> "compras de activos";
            case ACCOUNT_RECEIVABLE_REGISTERED -> "deudores";
            case ACCOUNTS_PAYABLE_PAYMENT_REGISTERED -> "pagos de cuentas por pagar";
            case ACCOUNTS_RECEIVABLE_PAYMENT_REGISTERED -> "recaudos de cuentas por cobrar";
            case PAYROLL_DAILY_PAYMENT_REGISTERED -> "pagos diarios de nomina";
            case CREDIT_NOTE_VALIDATED -> "notas credito";
            case ADJUSTMENT_CONFIRMED -> "ajustes contables";
        };
    }
}
