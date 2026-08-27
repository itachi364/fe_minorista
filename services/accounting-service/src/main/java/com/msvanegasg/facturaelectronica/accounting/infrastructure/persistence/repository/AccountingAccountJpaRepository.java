package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountingAccountJpaEntity;

public interface AccountingAccountJpaRepository extends JpaRepository<AccountingAccountJpaEntity, UUID> {

    Optional<AccountingAccountJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<AccountingAccountJpaEntity> findByCompanyIdAndCode(UUID companyId, String code);

    List<AccountingAccountJpaEntity> findByCompanyIdOrderByCodeAsc(UUID companyId);

    List<AccountingAccountJpaEntity> findByCompanyIdAndActiveOrderByCodeAsc(UUID companyId, Boolean active);
}
