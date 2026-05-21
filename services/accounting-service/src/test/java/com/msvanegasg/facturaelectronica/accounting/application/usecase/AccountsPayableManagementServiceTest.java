package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.msvanegasg.facturaelectronica.accounting.application.dto.GenerateAccountingEntryCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.RegisterPayablePaymentCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsPayablePaymentRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsPayableRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayable;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayablePayment;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayableStatus;

@ExtendWith(MockitoExtension.class)
class AccountsPayableManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");
    private static final UUID PAYABLE_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID PAYMENT_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID SOURCE_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID SUPPLIER_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    private AccountsPayableRepositoryPort payableRepository;
    @Mock
    private AccountsPayablePaymentRepositoryPort paymentRepository;
    @Mock
    private GenerateAccountingEntryUseCase accountingEntryUseCase;
    @Mock
    private IdGeneratorPort idGenerator;

    @Test
    void registerPartialPaymentReducesBalanceAndPostsAccounting() {
        when(payableRepository.findByCompanyIdAndId(COMPANY_ID, PAYABLE_ID)).thenReturn(Optional.of(payable()));
        when(payableRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(idGenerator.newId()).thenReturn(PAYMENT_ID);
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        AccountsPayableManagementService service = service();

        var result = service.registerPayment(new RegisterPayablePaymentCommand(COMPANY_ID, PAYABLE_ID,
                LocalDate.of(2026, 5, 25), money("40000.00"), "BANK_TRANSFER", "TRX-1", null));

        assertThat(result.payable().status()).isEqualTo(AccountsPayableStatus.PARTIALLY_PAID);
        assertThat(result.payable().balance()).isEqualByComparingTo("79000.00");
        ArgumentCaptor<AccountsPayablePayment> paymentCaptor = ArgumentCaptor.forClass(AccountsPayablePayment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().amount()).isEqualByComparingTo("40000.00");
        ArgumentCaptor<GenerateAccountingEntryCommand> entryCaptor =
                ArgumentCaptor.forClass(GenerateAccountingEntryCommand.class);
        verify(accountingEntryUseCase).generate(entryCaptor.capture());
        assertThat(entryCaptor.getValue().sourceType()).isEqualTo(AccountingSourceType.ACCOUNTS_PAYABLE_PAYMENT);
        assertThat(entryCaptor.getValue().total()).isEqualByComparingTo("40000.00");
    }

    private AccountsPayableManagementService service() {
        return new AccountsPayableManagementService(payableRepository, paymentRepository, accountingEntryUseCase,
                idGenerator, CLOCK);
    }

    private static AccountsPayable payable() {
        return AccountsPayable.open(PAYABLE_ID, COMPANY_ID, SUPPLIER_ID, AccountingSourceType.EXPENSE, SOURCE_ID,
                LocalDate.of(2026, 5, 20), LocalDate.of(2026, 6, 20), money("119000.00"), NOW);
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
