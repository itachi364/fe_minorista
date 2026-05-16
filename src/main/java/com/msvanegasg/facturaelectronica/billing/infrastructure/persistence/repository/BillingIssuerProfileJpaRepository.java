package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.BillingIssuerProfileJpaEntity;

public interface BillingIssuerProfileJpaRepository extends JpaRepository<BillingIssuerProfileJpaEntity, UUID> {

    Optional<BillingIssuerProfileJpaEntity> findFirstByCompanyIdAndActiveTrueOrderByIdDesc(UUID companyId);
}
