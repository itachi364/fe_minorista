package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivable;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivableStatus;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountsReceivableJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.AccountsReceivableJpaRepository;

@ExtendWith(MockitoExtension.class)
class AccountsReceivablePersistenceAdapterTest {

    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");
    private static final UUID RECEIVABLE_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final UUID CUSTOMER_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID SOURCE_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");

    @Mock
    private AccountsReceivableJpaRepository repository;

    @Test
    void savesAndFindsReceivablesByCompanyStatusAndCustomer() {
        AccountsReceivablePersistenceAdapter adapter = new AccountsReceivablePersistenceAdapter(repository);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findByCompanyIdAndStatusAndDueDateBetweenOrderByDueDateAsc(COMPANY_ID,
                AccountsReceivableStatus.OPEN, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31)))
                .thenReturn(List.of(entity(CUSTOMER_ID), entity(UUID.randomUUID())));

        AccountsReceivable saved = adapter.save(AccountsReceivable.open(RECEIVABLE_ID, COMPANY_ID, CUSTOMER_ID,
                AccountingSourceType.SALE, SOURCE_ID, LocalDate.of(2026, 5, 20), LocalDate.of(2026, 5, 31),
                money("119000.00"), "sale-credit-1", NOW));
        List<AccountsReceivable> result = adapter.find(COMPANY_ID, AccountsReceivableStatus.OPEN, CUSTOMER_ID,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));

        assertThat(saved.idempotencyKey()).isEqualTo("sale-credit-1");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).customerId()).isEqualTo(CUSTOMER_ID);
    }

    private static AccountsReceivableJpaEntity entity(UUID customerId) {
        AccountsReceivableJpaEntity entity = new AccountsReceivableJpaEntity();
        entity.setId(RECEIVABLE_ID);
        entity.setCompanyId(COMPANY_ID);
        entity.setCustomerId(customerId);
        entity.setSourceType(AccountingSourceType.SALE);
        entity.setSourceId(SOURCE_ID);
        entity.setIssueDate(LocalDate.of(2026, 5, 20));
        entity.setDueDate(LocalDate.of(2026, 5, 31));
        entity.setTotalAmount(money("119000.00"));
        entity.setPaidAmount(BigDecimal.ZERO);
        entity.setStatus(AccountsReceivableStatus.OPEN);
        entity.setIdempotencyKey("sale-credit-1");
        entity.setCreatedAt(NOW);
        return entity;
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}