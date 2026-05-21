package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayable;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayableStatus;

public interface AccountsPayableRepositoryPort {

    Optional<AccountsPayable> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<AccountsPayable> findByCompanyIdAndSource(UUID companyId, AccountingSourceType sourceType, UUID sourceId);

    List<AccountsPayable> find(UUID companyId, AccountsPayableStatus status, UUID supplierId, LocalDate from,
            LocalDate to);

    AccountsPayable save(AccountsPayable payable);
}
