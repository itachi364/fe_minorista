package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountsReceivableCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.GenerateAccountingEntryCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.RegisterReceivablePaymentCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsReceivablePaymentRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsReceivableRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivable;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivablePayment;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivableStatus;

@ExtendWith(MockitoExtension.class)
class AccountsReceivableManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");
    private static final UUID RECEIVABLE_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final UUID PAYMENT_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID SOURCE_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID CUSTOMER_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    private AccountsReceivableRepositoryPort receivableRepository;
    @Mock
    private AccountsReceivablePaymentRepositoryPort paymentRepository;
    @Mock
    private GenerateAccountingEntryUseCase accountingEntryUseCase;
    @Mock
    private IdGeneratorPort idGenerator;

    @Test
    void createsReceivableUsingIdempotency() {
        when(receivableRepository.findByCompanyIdAndIdempotencyKey(COMPANY_ID, "sale-credit-1"))
                .thenReturn(Optional.empty());
        when(receivableRepository.findByCompanyIdAndSource(COMPANY_ID, AccountingSourceType.SALE, SOURCE_ID))
                .thenReturn(Optional.empty());
        when(idGenerator.newId()).thenReturn(RECEIVABLE_ID);
        when(receivableRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        AccountsReceivableManagementService service = service();

        var result = service.create(new CreateAccountsReceivableCommand(COMPANY_ID, CUSTOMER_ID,
                AccountingSourceType.SALE, SOURCE_ID, LocalDate.of(2026, 5, 20), LocalDate.of(2026, 6, 20),
                money("119000.00"), "sale-credit-1"));

        assertThat(result.id()).isEqualTo(RECEIVABLE_ID);
        assertThat(result.status()).isEqualTo(AccountsReceivableStatus.OPEN);
        assertThat(result.balance()).isEqualByComparingTo("119000.00");
        ArgumentCaptor<GenerateAccountingEntryCommand> entryCaptor =
                ArgumentCaptor.forClass(GenerateAccountingEntryCommand.class);
        verify(accountingEntryUseCase).generate(entryCaptor.capture());
        assertThat(entryCaptor.getValue().eventType().name()).isEqualTo("ACCOUNT_RECEIVABLE_REGISTERED");
        assertThat(entryCaptor.getValue().sourceType()).isEqualTo(AccountingSourceType.ADJUSTMENT);
        assertThat(entryCaptor.getValue().sourceId()).isEqualTo(RECEIVABLE_ID);
        assertThat(entryCaptor.getValue().thirdpartyId()).isEqualTo(CUSTOMER_ID);
        assertThat(entryCaptor.getValue().total()).isEqualByComparingTo("119000.00");
    }

    @Test
    void registerPartialPaymentReducesBalanceAndPostsAccounting() {
        when(receivableRepository.findByCompanyIdAndId(COMPANY_ID, RECEIVABLE_ID)).thenReturn(Optional.of(receivable()));
        when(receivableRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(idGenerator.newId()).thenReturn(PAYMENT_ID);
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        AccountsReceivableManagementService service = service();

        var result = service.registerPayment(new RegisterReceivablePaymentCommand(COMPANY_ID, RECEIVABLE_ID,
                LocalDate.of(2026, 5, 25), money("40000.00"), "BANK_TRANSFER", "RC-1", null));

        assertThat(result.receivable().status()).isEqualTo(AccountsReceivableStatus.PARTIALLY_PAID);
        assertThat(result.receivable().balance()).isEqualByComparingTo("79000.00");
        ArgumentCaptor<AccountsReceivablePayment> paymentCaptor = ArgumentCaptor.forClass(AccountsReceivablePayment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().amount()).isEqualByComparingTo("40000.00");
        ArgumentCaptor<GenerateAccountingEntryCommand> entryCaptor =
                ArgumentCaptor.forClass(GenerateAccountingEntryCommand.class);
        verify(accountingEntryUseCase).generate(entryCaptor.capture());
        assertThat(entryCaptor.getValue().sourceType()).isEqualTo(AccountingSourceType.ACCOUNTS_RECEIVABLE_PAYMENT);
        assertThat(entryCaptor.getValue().thirdpartyId()).isEqualTo(CUSTOMER_ID);
        assertThat(entryCaptor.getValue().total()).isEqualByComparingTo("40000.00");
    }

    @Test
    void rejectsOverpayment() {
        AccountsReceivable receivable = receivable();

        assertThatThrownBy(() -> receivable.applyPayment(money("120000.00")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("payment amount exceeds receivable balance");
    }

    private AccountsReceivableManagementService service() {
        return new AccountsReceivableManagementService(receivableRepository, paymentRepository, accountingEntryUseCase,
                idGenerator, CLOCK);
    }

    private static AccountsReceivable receivable() {
        return AccountsReceivable.open(RECEIVABLE_ID, COMPANY_ID, CUSTOMER_ID, AccountingSourceType.SALE, SOURCE_ID,
                LocalDate.of(2026, 5, 20), LocalDate.of(2026, 6, 20), money("119000.00"), "sale-credit-1", NOW);
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
