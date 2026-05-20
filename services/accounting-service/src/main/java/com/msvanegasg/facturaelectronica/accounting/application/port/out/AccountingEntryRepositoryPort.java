package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.util.UUID;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntry;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

public interface AccountingEntryRepositoryPort {

    boolean existsByCompanyIdAndSource(UUID companyId, AccountingSourceType sourceType, UUID sourceId);

    Optional<AccountingEntry> findByCompanyIdAndSource(UUID companyId, AccountingSourceType sourceType, UUID sourceId);

    AccountingEntry save(AccountingEntry entry);

    List<AccountingEntry> findPostedByCompanyIdAndEntryDateBetween(
            UUID companyId,
            LocalDate fromDate,
            LocalDate toDate);
}
