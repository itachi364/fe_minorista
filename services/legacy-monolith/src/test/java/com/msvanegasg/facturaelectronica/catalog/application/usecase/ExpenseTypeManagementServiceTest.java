package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.catalog.application.dto.ExpenseTypeCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.ExpenseTypeRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.ExpenseType;
import com.msvanegasg.facturaelectronica.exception.TipoGastoNotFoundException;

class ExpenseTypeManagementServiceTest {

    @Test
    void createExpenseTypeStartsActiveAndPersistsIt() {
        InMemoryExpenseTypeRepository repository = new InMemoryExpenseTypeRepository();
        ExpenseTypeManagementService service = new ExpenseTypeManagementService(repository);

        ExpenseType expenseType = service.create(new ExpenseTypeCommand("Servicios", "Gastos de servicios"));

        assertThat(expenseType.id()).isEqualTo(1L);
        assertThat(expenseType.name()).isEqualTo("Servicios");
        assertThat(expenseType.description()).isEqualTo("Gastos de servicios");
        assertThat(expenseType.active()).isTrue();
    }

    @Test
    void updateExpenseTypeKeepsIdentifierAndActiveState() {
        InMemoryExpenseTypeRepository repository = new InMemoryExpenseTypeRepository();
        repository.save(ExpenseType.restore(7L, "Servicios", "Inicial", true));
        ExpenseTypeManagementService service = new ExpenseTypeManagementService(repository);

        ExpenseType updated = service.update(7L, new ExpenseTypeCommand("Arriendo", "Canon mensual"));

        assertThat(updated.id()).isEqualTo(7L);
        assertThat(updated.name()).isEqualTo("Arriendo");
        assertThat(updated.description()).isEqualTo("Canon mensual");
        assertThat(updated.active()).isTrue();
    }

    @Test
    void disableAndEnableExpenseType() {
        InMemoryExpenseTypeRepository repository = new InMemoryExpenseTypeRepository();
        repository.save(ExpenseType.restore(7L, "Servicios", null, true));
        ExpenseTypeManagementService service = new ExpenseTypeManagementService(repository);

        service.disable(7L);
        assertThat(repository.findById(7L).orElseThrow().active()).isFalse();

        service.enable(7L);
        assertThat(repository.findById(7L).orElseThrow().active()).isTrue();
    }

    @Test
    void findByIdRejectsMissingExpenseType() {
        ExpenseTypeManagementService service = new ExpenseTypeManagementService(new InMemoryExpenseTypeRepository());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(TipoGastoNotFoundException.class);
    }

    private static final class InMemoryExpenseTypeRepository implements ExpenseTypeRepositoryPort {

        private long nextId = 1L;
        private final Map<Long, ExpenseType> expenseTypes = new LinkedHashMap<>();

        @Override
        public List<ExpenseType> findAll() {
            return List.copyOf(expenseTypes.values());
        }

        @Override
        public List<ExpenseType> findActive() {
            return expenseTypes.values().stream().filter(ExpenseType::active).toList();
        }

        @Override
        public List<ExpenseType> findInactive() {
            return expenseTypes.values().stream().filter(expenseType -> !expenseType.active()).toList();
        }

        @Override
        public Optional<ExpenseType> findById(Long id) {
            return Optional.ofNullable(expenseTypes.get(id));
        }

        @Override
        public ExpenseType save(ExpenseType expenseType) {
            ExpenseType toSave = expenseType.id() == null
                    ? ExpenseType.restore(nextId++, expenseType.name(), expenseType.description(), expenseType.active())
                    : expenseType;
            expenseTypes.put(toSave.id(), toSave);
            return toSave;
        }
    }
}
