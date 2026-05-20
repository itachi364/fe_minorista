package com.msvanegasg.facturaelectronica.catalog.application.port.in;

import java.util.List;

import com.msvanegasg.facturaelectronica.catalog.application.dto.ExpenseTypeCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.ExpenseType;

public interface ManageExpenseTypeUseCase {

    List<ExpenseType> findAll();

    List<ExpenseType> findActive();

    List<ExpenseType> findInactive();

    ExpenseType findById(Long id);

    ExpenseType create(ExpenseTypeCommand command);

    ExpenseType update(Long id, ExpenseTypeCommand command);

    void disable(Long id);

    void enable(Long id);
}
