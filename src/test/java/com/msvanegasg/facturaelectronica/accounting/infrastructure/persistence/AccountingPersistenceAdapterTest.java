package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountCategory;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountNature;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingAmountType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntry;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntryLine;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntrySide;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntryStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRule;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRuleLine;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountingEntryJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountingEntryLineJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountingRuleJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountingRuleLineJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.AccountingAccountJpaRepository;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.AccountingEntryJpaRepository;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.AccountingEntryLineJpaRepository;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.AccountingRuleJpaRepository;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.AccountingRuleLineJpaRepository;

@ExtendWith(MockitoExtension.class)
class AccountingPersistenceAdapterTest {

    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");
    private static final UUID ACCOUNT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID RULE_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID ENTRY_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID LINE_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final UUID SOURCE_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

    @Mock
    private AccountingAccountJpaRepository accountRepository;

    @Mock
    private AccountingRuleJpaRepository ruleRepository;

    @Mock
    private AccountingRuleLineJpaRepository ruleLineRepository;

    @Mock
    private AccountingEntryJpaRepository entryRepository;

    @Mock
    private AccountingEntryLineJpaRepository entryLineRepository;

    @Test
    void accountAdapterStoresAndRestoresPucClassification() {
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        AccountingAccountPersistenceAdapter adapter = new AccountingAccountPersistenceAdapter(accountRepository);

        Account saved = adapter.save(Account.create(ACCOUNT_ID, COMPANY_ID, "1105", "Caja", null));

        assertThat(saved.id()).isEqualTo(ACCOUNT_ID);
        assertThat(saved.category()).isEqualTo(AccountCategory.ASSET);
        assertThat(saved.nature()).isEqualTo(AccountNature.DEBIT);
    }

    @Test
    void ruleAdapterRestoresActiveRuleWithOrderedLines() {
        when(ruleRepository.findByCompanyIdAndEventTypeAndActiveTrue(COMPANY_ID, AccountingEventType.SALE_CONFIRMED))
                .thenReturn(Optional.of(ruleEntity()));
        when(ruleLineRepository.findByRuleIdOrderByLineOrderAsc(RULE_ID))
                .thenReturn(List.of(ruleLine("1105", 1), ruleLine("4135", 2)));
        AccountingRulePersistenceAdapter adapter = new AccountingRulePersistenceAdapter(ruleRepository,
                ruleLineRepository);

        Optional<AccountingRule> result = adapter.findActiveByCompanyIdAndEventType(
                COMPANY_ID,
                AccountingEventType.SALE_CONFIRMED);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().lines()).extracting("accountCode").containsExactly("1105", "4135");
    }

    @Test
    void ruleAdapterReplacesLinesWhenSaving() {
        when(ruleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(ruleLineRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        AccountingRulePersistenceAdapter adapter = new AccountingRulePersistenceAdapter(ruleRepository,
                ruleLineRepository);

        AccountingRule saved = adapter.save(AccountingRule.create(
                RULE_ID,
                COMPANY_ID,
                AccountingEventType.SALE_CONFIRMED,
                AccountingSourceType.SALE,
                "Venta POS",
                List.of(
                        AccountingRuleLine.create("1105", AccountingEntrySide.DEBIT, AccountingAmountType.TOTAL,
                                null),
                        AccountingRuleLine.create("4135", AccountingEntrySide.CREDIT, AccountingAmountType.SUBTOTAL,
                                null))));

        assertThat(saved.lines()).hasSize(2);
        verify(ruleLineRepository).deleteByRuleId(RULE_ID);
    }

    @Test
    void entryAdapterStoresAndQueriesPostedEntriesWithLines() {
        when(entryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(entryLineRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(entryRepository.findByCompanyIdAndStatusAndEntryDateBetween(
                COMPANY_ID,
                AccountingEntryStatus.POSTED,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31)))
                .thenReturn(List.of(entryEntity()));
        when(entryLineRepository.findByEntryIdOrderByLineOrderAsc(ENTRY_ID)).thenReturn(List.of(
                entryLine(LINE_ID, "1105", "Caja", money("119.00"), money("0.00")),
                entryLine(UUID.randomUUID(), "4135", "Ingresos", money("0.00"), money("119.00"))));
        AccountingEntryPersistenceAdapter adapter = new AccountingEntryPersistenceAdapter(entryRepository,
                entryLineRepository);
        AccountingEntry entry = AccountingEntry.post(
                ENTRY_ID,
                COMPANY_ID,
                LocalDate.of(2026, 5, 15),
                "Venta POS",
                AccountingSourceType.SALE,
                SOURCE_ID,
                List.of(
                        AccountingEntryLine.create(LINE_ID, ACCOUNT_ID, "1105", "Caja", null, money("119.00"),
                                money("0.00"), "Caja"),
                        AccountingEntryLine.create(UUID.randomUUID(), ACCOUNT_ID, "4135", "Ingresos", null,
                                money("0.00"), money("119.00"), "Ingresos")));

        AccountingEntry saved = adapter.save(entry);
        List<AccountingEntry> postedEntries = adapter.findPostedByCompanyIdAndEntryDateBetween(
                COMPANY_ID,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31));

        assertThat(saved.debitTotal()).isEqualByComparingTo("119.00");
        assertThat(postedEntries).hasSize(1);
        assertThat(postedEntries.get(0).lines()).hasSize(2);
        verify(entryLineRepository).deleteByEntryId(ENTRY_ID);
    }

    private static AccountingRuleJpaEntity ruleEntity() {
        return AccountingRuleJpaEntity.builder()
                .id(RULE_ID)
                .companyId(COMPANY_ID)
                .eventType(AccountingEventType.SALE_CONFIRMED)
                .sourceType(AccountingSourceType.SALE)
                .name("Venta POS")
                .active(true)
                .build();
    }

    private static AccountingRuleLineJpaEntity ruleLine(String accountCode, int order) {
        return AccountingRuleLineJpaEntity.builder()
                .id(UUID.randomUUID())
                .ruleId(RULE_ID)
                .lineOrder(order)
                .accountCode(accountCode)
                .side(order == 1 ? AccountingEntrySide.DEBIT : AccountingEntrySide.CREDIT)
                .amountType(order == 1 ? AccountingAmountType.TOTAL : AccountingAmountType.SUBTOTAL)
                .build();
    }

    private static AccountingEntryJpaEntity entryEntity() {
        return AccountingEntryJpaEntity.builder()
                .id(ENTRY_ID)
                .companyId(COMPANY_ID)
                .entryDate(LocalDate.of(2026, 5, 15))
                .description("Venta POS")
                .sourceType(AccountingSourceType.SALE)
                .sourceId(SOURCE_ID)
                .status(AccountingEntryStatus.POSTED)
                .debitTotal(money("119.00"))
                .creditTotal(money("119.00"))
                .build();
    }

    private static AccountingEntryLineJpaEntity entryLine(
            UUID id,
            String code,
            String name,
            BigDecimal debit,
            BigDecimal credit) {
        return AccountingEntryLineJpaEntity.builder()
                .id(id)
                .entryId(ENTRY_ID)
                .lineOrder(1)
                .accountId(ACCOUNT_ID)
                .accountCode(code)
                .accountName(name)
                .debitAmount(debit)
                .creditAmount(credit)
                .description(name)
                .build();
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
