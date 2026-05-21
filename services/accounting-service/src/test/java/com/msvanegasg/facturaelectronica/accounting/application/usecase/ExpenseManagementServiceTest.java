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

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateExpenseCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.GenerateAccountingEntryCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsPayableRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.ExpenseRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayable;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Expense;
import com.msvanegasg.facturaelectronica.accounting.domain.model.ExpenseStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.PaymentCondition;

@ExtendWith(MockitoExtension.class)
class ExpenseManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");
    private static final UUID EXPENSE_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID PAYABLE_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID SUPPLIER_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    private ExpenseRepositoryPort expenseRepository;
    @Mock
    private AccountsPayableRepositoryPort payableRepository;
    @Mock
    private GenerateAccountingEntryUseCase accountingEntryUseCase;
    @Mock
    private IdGeneratorPort idGenerator;

    @Test
    void createsCreditExpenseWithDueDate() {
        when(expenseRepository.findByCompanyIdAndIdempotencyKey(COMPANY_ID, "expense-1")).thenReturn(Optional.empty());
        when(idGenerator.newId()).thenReturn(EXPENSE_ID);
        when(expenseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ExpenseManagementService service = service();

        var result = service.create(command());

        assertThat(result.status()).isEqualTo(ExpenseStatus.PENDING);
        assertThat(result.paymentCondition()).isEqualTo(PaymentCondition.CREDIT);
        assertThat(result.dueDate()).isEqualTo(LocalDate.of(2026, 6, 20));
    }

    @Test
    void confirmCreditExpensePostsAccountingAndCreatesPayable() {
        when(expenseRepository.findByCompanyIdAndId(COMPANY_ID, EXPENSE_ID)).thenReturn(Optional.of(expense()));
        when(expenseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(payableRepository.findByCompanyIdAndSource(COMPANY_ID, AccountingSourceType.EXPENSE, EXPENSE_ID))
                .thenReturn(Optional.empty());
        when(idGenerator.newId()).thenReturn(PAYABLE_ID);
        when(payableRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ExpenseManagementService service = service();

        var result = service.confirm(COMPANY_ID, EXPENSE_ID);

        assertThat(result.status()).isEqualTo(ExpenseStatus.CONFIRMED);
        ArgumentCaptor<GenerateAccountingEntryCommand> entryCaptor =
                ArgumentCaptor.forClass(GenerateAccountingEntryCommand.class);
        verify(accountingEntryUseCase).generate(entryCaptor.capture());
        assertThat(entryCaptor.getValue().sourceType()).isEqualTo(AccountingSourceType.EXPENSE);
        ArgumentCaptor<AccountsPayable> payableCaptor = ArgumentCaptor.forClass(AccountsPayable.class);
        verify(payableRepository).save(payableCaptor.capture());
        assertThat(payableCaptor.getValue().totalAmount()).isEqualByComparingTo("119000.00");
    }

    private ExpenseManagementService service() {
        return new ExpenseManagementService(expenseRepository, payableRepository, accountingEntryUseCase, idGenerator,
                CLOCK);
    }

    private static CreateExpenseCommand command() {
        return new CreateExpenseCommand(COMPANY_ID, SUPPLIER_ID, LocalDate.of(2026, 5, 20),
                "Servicio publico energia", money("100000.00"), money("19000.00"), money("119000.00"),
                PaymentCondition.CREDIT, LocalDate.of(2026, 6, 20), "https://example.local/evidence.pdf",
                "expense-1");
    }

    private static Expense expense() {
        return Expense.pending(EXPENSE_ID, COMPANY_ID, SUPPLIER_ID, LocalDate.of(2026, 5, 20),
                "Servicio publico energia", money("100000.00"), money("19000.00"), money("119000.00"),
                PaymentCondition.CREDIT, LocalDate.of(2026, 6, 20), null, "expense-1", NOW);
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
