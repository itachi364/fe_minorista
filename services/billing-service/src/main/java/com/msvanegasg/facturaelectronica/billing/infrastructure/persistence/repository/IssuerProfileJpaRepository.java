package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.IssuerProfileJpaEntity;

public interface IssuerProfileJpaRepository extends JpaRepository<IssuerProfileJpaEntity, UUID> {
    Optional<IssuerProfileJpaEntity> findFirstByCompanyIdAndActiveTrueOrderByIdDesc(UUID companyId);
}
