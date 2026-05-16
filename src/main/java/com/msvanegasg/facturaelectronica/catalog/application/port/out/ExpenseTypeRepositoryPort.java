package com.msvanegasg.facturaelectronica.catalog.application.port.out;

import java.util.List;
import java.util.Optional;

import com.msvanegasg.facturaelectronica.catalog.domain.model.ExpenseType;

public interface ExpenseTypeRepositoryPort {

    List<ExpenseType> findAll();

    List<ExpenseType> findActive();

    List<ExpenseType> findInactive();

    Optional<ExpenseType> findById(Long id);

    ExpenseType save(ExpenseType expenseType);
}
