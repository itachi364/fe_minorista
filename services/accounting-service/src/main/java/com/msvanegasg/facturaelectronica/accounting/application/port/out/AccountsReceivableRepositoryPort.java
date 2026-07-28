package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivable;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivableStatus;

public interface AccountsReceivableRepositoryPort {

    Optional<AccountsReceivable> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<AccountsReceivable> findByCompanyIdAndSource(UUID companyId, AccountingSourceType sourceType,
            UUID sourceId);

    Optional<AccountsReceivable> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);

    List<AccountsReceivable> find(UUID companyId, AccountsReceivableStatus status, UUID customerId, LocalDate from,
            LocalDate to);

    AccountsReceivable save(AccountsReceivable receivable);
}