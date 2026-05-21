package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.Expense;

public interface ExpenseRepositoryPort {

    Optional<Expense> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<Expense> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);

    Expense save(Expense expense);
}
