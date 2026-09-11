package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionOperations;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingEntryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsPayableResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.ConfirmPurchaseFiscalCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalDocumentCalculationResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalDocumentLineCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.CalculateFiscalDocumentUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountsPayableUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalConfirmationProcessRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntryStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayableStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;

@ExtendWith(MockitoExtension.class)
class PurchaseFiscalConfirmationServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SOURCE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SUPPLIER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID PROCESS_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID CALCULATION_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID ENTRY_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID PAYABLE_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final Instant NOW = Instant.parse("2026-09-11T12:00:00Z");

    @Mock private CalculateFiscalDocumentUseCase fiscalDocumentUseCase;
    @Mock private GenerateAccountingEntryUseCase accountingEntryUseCase;
    @Mock private ManageAccountsPayableUseCase accountsPayableUseCase;
    @Mock private FiscalConfirmationProcessRepositoryPort processRepository;
    @Mock private IdGeneratorPort idGenerator;
    @Mock private TransactionOperations transactions;
    @Mock private TransactionStatus transactionStatus;

    @BeforeEach
    void executeTransactionCallbacks() {
        when(transactions.execute(any())).thenAnswer(invocation -> invocation
                .<org.springframework.transaction.support.TransactionCallback<Object>>getArgument(0)
                .doInTransaction(transactionStatus));
        when(idGenerator.newId()).thenReturn(PROCESS_ID);
    }

    @Test
    void confirmsFiscalCalculationEntryAndPayableAsOneOperation() {
        when(fiscalDocumentUseCase.calculate(any())).thenReturn(fiscalResult());
        when(accountingEntryUseCase.generate(any())).thenReturn(entryResult());
        when(accountsPayableUseCase.create(any())).thenReturn(payableResult());

        var result = service().confirm(command());

        assertThat(result.status()).isEqualTo("COMPLETED");
        assertThat(result.fiscalCalculation().calculationId()).isEqualTo(CALCULATION_ID);
        assertThat(result.accountingEntry().id()).isEqualTo(ENTRY_ID);
        assertThat(result.accountsPayable().id()).isEqualTo(PAYABLE_ID);
        verify(processRepository).markCompleted(COMPANY_ID, AccountingSourceType.PURCHASE, SOURCE_ID,
                CALCULATION_ID, ENTRY_ID, PAYABLE_ID, NOW);
        verify(processRepository, never()).markFailed(any(), any(), any(), any(), any(), any());
    }

    @Test
    void recordsFailureWhenATransactionalEffectCannotBeCompleted() {
        when(fiscalDocumentUseCase.calculate(any())).thenReturn(fiscalResult());
        when(accountingEntryUseCase.generate(any())).thenThrow(new IllegalStateException("account mapping missing"));

        assertThatThrownBy(() -> service().confirm(command()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("account mapping");

        verify(processRepository).markFailed(PROCESS_ID, COMPANY_ID, AccountingSourceType.PURCHASE, SOURCE_ID,
                "account mapping missing", NOW);
        verify(accountsPayableUseCase, never()).create(any());
    }

    @Test
    void preservesOriginalFailureWhenFailureTrackingAlsoFails() {
        when(fiscalDocumentUseCase.calculate(any())).thenReturn(fiscalResult());
        IllegalStateException original = new IllegalStateException("account mapping missing");
        IllegalStateException tracking = new IllegalStateException("failure tracking unavailable");
        when(accountingEntryUseCase.generate(any())).thenThrow(original);
        doThrow(tracking).when(processRepository).markFailed(any(), any(), any(), any(), any(), any());

        assertThatThrownBy(() -> service().confirm(command()))
                .isSameAs(original)
                .satisfies(failure -> assertThat(failure.getSuppressed()).containsExactly(tracking));
    }

    private PurchaseFiscalConfirmationService service() {
        return new PurchaseFiscalConfirmationService(fiscalDocumentUseCase, accountingEntryUseCase,
                accountsPayableUseCase, processRepository, idGenerator, transactions,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static ConfirmPurchaseFiscalCommand command() {
        return new ConfirmPurchaseFiscalCommand(COMPANY_ID, SOURCE_ID, SUPPLIER_ID,
                LocalDate.of(2026, 9, 11), "11001", true, LocalDate.of(2026, 10, 11), money("100000"),
                money("19000"), money("119000"), null,
                List.of(new FiscalDocumentLineCommand(UUID.randomUUID(), "GENERAL_PURCHASE", null,
                        money("100000"), money("19000"))));
    }

    private static FiscalDocumentCalculationResult fiscalResult() {
        return new FiscalDocumentCalculationResult(CALCULATION_ID, COMPANY_ID, FiscalOperationType.PURCHASE,
                SUPPLIER_ID, LocalDate.of(2026, 9, 11), "11001", AccountingSourceType.PURCHASE, SOURCE_ID,
                "hash", "CONFIRMED", List.of(), money("119000"), money("2500"), money("116500"), Map.of(), NOW);
    }

    private static AccountingEntryResult entryResult() {
        return new AccountingEntryResult(ENTRY_ID, COMPANY_ID, LocalDate.of(2026, 9, 11), "Factura de compra",
                AccountingSourceType.PURCHASE, SOURCE_ID, UUID.randomUUID(), AccountingEntryStatus.POSTED,
                money("119000"), money("119000"), List.of());
    }

    private static AccountsPayableResult payableResult() {
        return new AccountsPayableResult(PAYABLE_ID, COMPANY_ID, SUPPLIER_ID, AccountingSourceType.PURCHASE,
                SOURCE_ID, LocalDate.of(2026, 9, 11), LocalDate.of(2026, 10, 11), money("116500"),
                BigDecimal.ZERO, money("116500"), AccountsPayableStatus.OPEN, NOW);
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
