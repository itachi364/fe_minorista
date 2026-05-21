package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.ExpenseJpaEntity;

public interface ExpenseJpaRepository extends JpaRepository<ExpenseJpaEntity, UUID> {

    Optional<ExpenseJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<ExpenseJpaEntity> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);
}
