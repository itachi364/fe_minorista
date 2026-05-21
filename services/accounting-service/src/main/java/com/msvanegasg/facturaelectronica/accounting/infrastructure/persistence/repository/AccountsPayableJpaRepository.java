package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayableStatus;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountsPayableJpaEntity;

public interface AccountsPayableJpaRepository extends JpaRepository<AccountsPayableJpaEntity, UUID> {

    Optional<AccountsPayableJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<AccountsPayableJpaEntity> findByCompanyIdAndSourceTypeAndSourceId(UUID companyId,
            AccountingSourceType sourceType, UUID sourceId);

    List<AccountsPayableJpaEntity> findByCompanyIdAndDueDateBetweenOrderByDueDateAsc(UUID companyId, LocalDate from,
            LocalDate to);

    List<AccountsPayableJpaEntity> findByCompanyIdAndStatusAndDueDateBetweenOrderByDueDateAsc(UUID companyId,
            AccountsPayableStatus status, LocalDate from, LocalDate to);
}
