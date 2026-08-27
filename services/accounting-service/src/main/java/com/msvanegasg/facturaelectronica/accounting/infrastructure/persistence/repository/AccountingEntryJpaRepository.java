package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntryStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountingEntryJpaEntity;

public interface AccountingEntryJpaRepository extends JpaRepository<AccountingEntryJpaEntity, UUID> {

    long countByAccountingRuleId(UUID accountingRuleId);

    boolean existsByCompanyIdAndSourceTypeAndSourceId(
            UUID companyId,
            AccountingSourceType sourceType,
            UUID sourceId);

    Optional<AccountingEntryJpaEntity> findByCompanyIdAndSourceTypeAndSourceId(
            UUID companyId,
            AccountingSourceType sourceType,
            UUID sourceId);

    List<AccountingEntryJpaEntity> findByCompanyIdAndStatusAndEntryDateBetween(
            UUID companyId,
            AccountingEntryStatus status,
            LocalDate fromDate,
            LocalDate toDate);
}
