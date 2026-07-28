package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.ExpenseQuery;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Expense;

public interface ExpenseRepositoryPort {

    Optional<Expense> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<Expense> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);

    List<Expense> find(ExpenseQuery query);

    Expense save(Expense expense);
}
