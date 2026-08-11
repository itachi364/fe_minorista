package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleLineResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingSetupResult;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.InitializeBasicAccountingSetupUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingAmountType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntrySide;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRule;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRuleLine;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

public class BasicAccountingSetupService implements InitializeBasicAccountingSetupUseCase {

    private static final String TEMPLATE_NAME = "BASIC_COLOMBIA_SMALL_BUSINESS";

    private final AccountRepositoryPort accountRepository;
    private final AccountingRuleRepositoryPort ruleRepository;
    private final IdGeneratorPort idGenerator;

    public BasicAccountingSetupService(AccountRepositoryPort accountRepository,
            AccountingRuleRepositoryPort ruleRepository, IdGeneratorPort idGenerator) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.ruleRepository = Objects.requireNonNull(ruleRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public AccountingSetupResult initialize(UUID companyId) {
        Objects.requireNonNull(companyId, "companyId is required");
        List<AccountResult> accounts = accountTemplates().stream()
                .map(template -> ensureAccount(companyId, template))
                .map(BasicAccountingSetupService::toResult)
                .toList();
        List<AccountingRuleResult> rules = ruleTemplates().stream()
                .map(template -> replaceActiveRule(companyId, template))
                .map(BasicAccountingSetupService::toResult)
                .toList();
        return new AccountingSetupResult(companyId, TEMPLATE_NAME, accounts, rules);
    }

    private Account ensureAccount(UUID companyId, AccountTemplate template) {
        return accountRepository.findByCompanyIdAndCode(companyId, template.code())
                .map(account -> account.active() ? account : accountRepository.save(Account.restore(account.id(),
                        account.companyId(), account.code(), account.name(), account.parentAccountId(), true)))
                .orElseGet(() -> accountRepository.save(Account.create(idGenerator.newId(), companyId,
                        template.code(), template.name(), null)));
    }

    private AccountingRule replaceActiveRule(UUID companyId, RuleTemplate template) {
        ruleRepository.findActiveByCompanyIdAndEventType(companyId, template.eventType())
                .ifPresent(rule -> ruleRepository.save(rule.deactivate()));
        AccountingRule rule = AccountingRule.create(idGenerator.newId(), companyId, template.eventType(),
                template.sourceType(), template.name(), template.lines().stream()
                        .map(line -> AccountingRuleLine.create(line.accountCode(), line.side(), line.amountType(),
                                line.description()))
                        .toList());
        return ruleRepository.save(rule);
    }

    private static List<AccountTemplate> accountTemplates() {
        return List.of(
                new AccountTemplate("1105", "Caja"),
                new AccountTemplate("1110", "Bancos"),
                new AccountTemplate("1305", "Clientes"),
                new AccountTemplate("1435", "Inventarios"),
                new AccountTemplate("2205", "Proveedores nacionales"),
                new AccountTemplate("2408", "Impuesto sobre las ventas"),
                new AccountTemplate("4135", "Ingresos operacionales"),
                new AccountTemplate("5105", "Gastos de personal"),
                new AccountTemplate("5135", "Gastos operacionales"));
    }

    private static List<RuleTemplate> ruleTemplates() {
        return List.of(
                new RuleTemplate(AccountingEventType.SALE_CONFIRMED, AccountingSourceType.SALE,
                        "Venta POS/factura - plantilla basica", List.of(
                                new RuleLineTemplate("1105", AccountingEntrySide.DEBIT, AccountingAmountType.TOTAL,
                                        "Caja o medio de pago"),
                                new RuleLineTemplate("4135", AccountingEntrySide.CREDIT, AccountingAmountType.SUBTOTAL,
                                        "Ingreso operacional"),
                                new RuleLineTemplate("2408", AccountingEntrySide.CREDIT, AccountingAmountType.TAX_TOTAL,
                                        "IVA generado"))),
                new RuleTemplate(AccountingEventType.PURCHASE_CONFIRMED, AccountingSourceType.PURCHASE,
                        "Compra inventario - plantilla basica", List.of(
                                new RuleLineTemplate("1435", AccountingEntrySide.DEBIT, AccountingAmountType.SUBTOTAL,
                                        "Inventario"),
                                new RuleLineTemplate("2408", AccountingEntrySide.DEBIT, AccountingAmountType.TAX_TOTAL,
                                        "IVA descontable"),
                                new RuleLineTemplate("2205", AccountingEntrySide.CREDIT, AccountingAmountType.TOTAL,
                                        "Proveedor"))),
                new RuleTemplate(AccountingEventType.EXPENSE_CONFIRMED, AccountingSourceType.EXPENSE,
                        "Gasto operativo - plantilla basica", List.of(
                                new RuleLineTemplate("5135", AccountingEntrySide.DEBIT, AccountingAmountType.SUBTOTAL,
                                        "Gasto operacional"),
                                new RuleLineTemplate("2408", AccountingEntrySide.DEBIT, AccountingAmountType.TAX_TOTAL,
                                        "IVA descontable"),
                                new RuleLineTemplate("2205", AccountingEntrySide.CREDIT, AccountingAmountType.TOTAL,
                                        "Proveedor o cuenta por pagar"))),
                new RuleTemplate(AccountingEventType.ACCOUNTS_PAYABLE_PAYMENT_REGISTERED,
                        AccountingSourceType.ACCOUNTS_PAYABLE_PAYMENT,
                        "Pago cuenta por pagar - plantilla basica", List.of(
                                new RuleLineTemplate("2205", AccountingEntrySide.DEBIT, AccountingAmountType.TOTAL,
                                        "Disminucion proveedor"),
                                new RuleLineTemplate("1105", AccountingEntrySide.CREDIT, AccountingAmountType.TOTAL,
                                        "Salida de caja"))),
                new RuleTemplate(AccountingEventType.ACCOUNTS_RECEIVABLE_PAYMENT_REGISTERED,
                        AccountingSourceType.ACCOUNTS_RECEIVABLE_PAYMENT,
                        "Recaudo cuenta por cobrar - plantilla basica", List.of(
                                new RuleLineTemplate("1105", AccountingEntrySide.DEBIT, AccountingAmountType.TOTAL,
                                        "Entrada de caja"),
                                new RuleLineTemplate("1305", AccountingEntrySide.CREDIT, AccountingAmountType.TOTAL,
                                        "Disminucion cartera clientes"))),
                new RuleTemplate(AccountingEventType.PAYROLL_DAILY_PAYMENT_REGISTERED,
                        AccountingSourceType.PAYROLL_DAILY_PAYMENT,
                        "Pago diario verbal o jornal - plantilla basica", List.of(
                                new RuleLineTemplate("5105", AccountingEntrySide.DEBIT, AccountingAmountType.TOTAL,
                                        "Costo de personal pagado por jornal"),
                                new RuleLineTemplate("1105", AccountingEntrySide.CREDIT, AccountingAmountType.TOTAL,
                                        "Salida de caja"))));
    }

    private static AccountResult toResult(Account account) {
        return new AccountResult(account.id(), account.companyId(), account.code(), account.name(), account.category(),
                account.level(), account.nature(), account.parentAccountId(), account.active());
    }

    private static AccountingRuleResult toResult(AccountingRule rule) {
        return new AccountingRuleResult(rule.id(), rule.companyId(), rule.eventType(), rule.sourceType(), rule.name(),
                rule.lines().stream().map(line -> new AccountingRuleLineResult(line.accountCode(), line.side(),
                        line.amountType(), line.description())).toList(), rule.active());
    }

    private record AccountTemplate(String code, String name) {
    }

    private record RuleTemplate(AccountingEventType eventType, AccountingSourceType sourceType, String name,
            List<RuleLineTemplate> lines) {
    }

    private record RuleLineTemplate(String accountCode, AccountingEntrySide side, AccountingAmountType amountType,
            String description) {
    }
}
