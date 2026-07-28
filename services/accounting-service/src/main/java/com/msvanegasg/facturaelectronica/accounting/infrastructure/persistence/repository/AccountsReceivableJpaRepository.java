package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivableStatus;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountsReceivableJpaEntity;

public interface AccountsReceivableJpaRepository extends JpaRepository<AccountsReceivableJpaEntity, UUID> {

    Optional<AccountsReceivableJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<AccountsReceivableJpaEntity> findByCompanyIdAndSourceTypeAndSourceId(UUID companyId,
            AccountingSourceType sourceType, UUID sourceId);

    Optional<AccountsReceivableJpaEntity> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);

    List<AccountsReceivableJpaEntity> findByCompanyIdAndDueDateBetweenOrderByDueDateAsc(UUID companyId,
            LocalDate from, LocalDate to);

    List<AccountsReceivableJpaEntity> findByCompanyIdAndStatusAndDueDateBetweenOrderByDueDateAsc(UUID companyId,
            AccountsReceivableStatus status, LocalDate from, LocalDate to);
}