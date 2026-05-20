package com.msvanegasg.facturaelectronica.expenses.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.enums.Estado;
import com.msvanegasg.facturaelectronica.exception.gasto.GastoInactivoException;
import com.msvanegasg.facturaelectronica.expenses.application.dto.ExpenseCommand;
import com.msvanegasg.facturaelectronica.expenses.application.port.out.ExpenseCatalogPort;
import com.msvanegasg.facturaelectronica.expenses.application.port.out.ExpenseRepositoryPort;
import com.msvanegasg.facturaelectronica.expenses.domain.model.Expense;
import com.msvanegasg.facturaelectronica.expenses.domain.model.ExpenseTypeSummary;
import com.msvanegasg.facturaelectronica.expenses.domain.model.PaymentMethodSummary;

class ExpenseManagementServiceTest {

    @Test
    void createExpenseStartsActiveAndLoadsCatalogSummaries() {
        InMemoryExpenseRepository repository = new InMemoryExpenseRepository();
        ExpenseManagementService service = new ExpenseManagementService(repository, new FakeExpenseCatalog());

        Expense result = service.create(command("Papeleria"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.description()).isEqualTo("Papeleria");
        assertThat(result.expenseType().name()).isEqualTo("Administrativo");
        assertThat(result.paymentMethod().name()).isEqualTo("Efectivo");
        assertThat(result.active()).isTrue();
    }

    @Test
    void updateExpenseRejectsInactiveExpense() {
        InMemoryExpenseRepository repository = new InMemoryExpenseRepository();
        repository.save(expense(1L, false));
        ExpenseManagementService service = new ExpenseManagementService(repository, new FakeExpenseCatalog());

        assertThatThrownBy(() -> service.update(1L, command("Actualizado")))
                .isInstanceOf(GastoInactivoException.class);
    }

    @Test
    void disableExpenseMarksItInactive() {
        InMemoryExpenseRepository repository = new InMemoryExpenseRepository();
        repository.save(expense(1L, true));
        ExpenseManagementService service = new ExpenseManagementService(repository, new FakeExpenseCatalog());

        service.disable(1L);

        assertThat(repository.findById(1L).orElseThrow().active()).isFalse();
    }

    @Test
    void findByDescriptionReturnsExpense() {
        InMemoryExpenseRepository repository = new InMemoryExpenseRepository();
        repository.save(expense(1L, true));
        ExpenseManagementService service = new ExpenseManagementService(repository, new FakeExpenseCatalog());

        Expense result = service.findByDescription("Papeleria");

        assertThat(result.amount()).isEqualByComparingTo("120000");
    }

    private static ExpenseCommand command(String description) {
        return new ExpenseCommand(
                LocalDateTime.of(2026, 5, 12, 10, 0),
                BigDecimal.valueOf(120000),
                description,
                3L,
                4L,
                "https://evidencia.local/gasto.pdf",
                Estado.PROCESADO);
    }

    private static Expense expense(Long id, boolean active) {
        return Expense.restore(
                id,
                LocalDateTime.of(2026, 5, 12, 10, 0),
                BigDecimal.valueOf(120000),
                "Papeleria",
                new ExpenseTypeSummary(3L, "Administrativo", "Gastos administrativos"),
                new PaymentMethodSummary(4L, "Efectivo", "Pago en efectivo"),
                "https://evidencia.local/gasto.pdf",
                Estado.PROCESADO,
                active);
    }

    private static final class InMemoryExpenseRepository implements ExpenseRepositoryPort {

        private long nextId = 1L;
        private final Map<Long, Expense> expenses = new LinkedHashMap<>();

        @Override
        public Expense save(Expense expense) {
            Expense toSave = expense.id() == null
                    ? Expense.restore(nextId++, expense.date(), expense.amount(), expense.description(),
                            expense.expenseType(), expense.paymentMethod(), expense.evidenceUrl(), expense.status(),
                            expense.active())
                    : expense;
            expenses.put(toSave.id(), toSave);
            return toSave;
        }

        @Override
        public Optional<Expense> findById(Long id) {
            return Optional.ofNullable(expenses.get(id));
        }

        @Override
        public List<Expense> findActive() {
            return expenses.values().stream().filter(Expense::active).toList();
        }

        @Override
        public List<Expense> findByStatus(Estado status) {
            return expenses.values().stream().filter(expense -> expense.status() == status).toList();
        }

        @Override
        public List<Expense> findByExpenseType(Long expenseTypeId) {
            return expenses.values().stream()
                    .filter(expense -> expense.expenseType().id().equals(expenseTypeId))
                    .toList();
        }

        @Override
        public List<Expense> findByPaymentMethod(Long paymentMethodId) {
            return expenses.values().stream()
                    .filter(expense -> expense.paymentMethod().id().equals(paymentMethodId))
                    .toList();
        }

        @Override
        public Optional<Expense> findByDescriptionContainingIgnoreCase(String description) {
            return expenses.values().stream()
                    .filter(expense -> expense.description().toLowerCase().contains(description.toLowerCase()))
                    .findFirst();
        }
    }

    private static final class FakeExpenseCatalog implements ExpenseCatalogPort {

        @Override
        public ExpenseTypeSummary findExpenseType(Long id) {
            return new ExpenseTypeSummary(id, "Administrativo", "Gastos administrativos");
        }

        @Override
        public PaymentMethodSummary findPaymentMethod(Long id) {
            return new PaymentMethodSummary(id, "Efectivo", "Pago en efectivo");
        }
    }
}
