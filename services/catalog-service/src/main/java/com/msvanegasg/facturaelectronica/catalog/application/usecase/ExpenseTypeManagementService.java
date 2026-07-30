package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import java.util.List;
import java.util.Objects;

import com.msvanegasg.facturaelectronica.catalog.application.dto.ExpenseTypeCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageExpenseTypeUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.ExpenseTypeRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.ExpenseType;
import com.msvanegasg.facturaelectronica.exception.TipoGastoNotFoundException;

public class ExpenseTypeManagementService implements ManageExpenseTypeUseCase {

    private final ExpenseTypeRepositoryPort expenseTypeRepository;

    public ExpenseTypeManagementService(ExpenseTypeRepositoryPort expenseTypeRepository) {
        this.expenseTypeRepository = Objects.requireNonNull(expenseTypeRepository);
    }

    @Override
    public List<ExpenseType> findAll() {
        return expenseTypeRepository.findAll();
    }

    @Override
    public List<ExpenseType> findActive() {
        return expenseTypeRepository.findActive();
    }

    @Override
    public List<ExpenseType> findInactive() {
        return expenseTypeRepository.findInactive();
    }

    @Override
    public ExpenseType findById(Long id) {
        return expenseTypeRepository.findById(id)
                .orElseThrow(() -> new TipoGastoNotFoundException(id));
    }

    @Override
    public ExpenseType create(ExpenseTypeCommand command) {
        Objects.requireNonNull(command, "command is required");
        return expenseTypeRepository.save(ExpenseType.create(command.name(), command.description()));
    }

    @Override
    public ExpenseType update(Long id, ExpenseTypeCommand command) {
        Objects.requireNonNull(command, "command is required");
        ExpenseType existing = findById(id);
        return expenseTypeRepository.save(existing.update(command.name(), command.description()));
    }

    @Override
    public void disable(Long id) {
        ExpenseType existing = findById(id);
        expenseTypeRepository.save(existing.disable());
    }

    @Override
    public void enable(Long id) {
        ExpenseType existing = findById(id);
        if (!existing.active()) {
            expenseTypeRepository.save(existing.enable());
        }
    }
}
