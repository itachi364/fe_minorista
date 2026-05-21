package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.ServiceSupplyReferenceJpaEntity;

public interface ServiceSupplyReferenceJpaRepository extends JpaRepository<ServiceSupplyReferenceJpaEntity, UUID> {

    boolean existsByCompanyIdAndServiceProductIdAndSupplyProductId(UUID companyId, UUID serviceProductId,
            UUID supplyProductId);

    List<ServiceSupplyReferenceJpaEntity> findByCompanyIdAndServiceProductIdAndActiveTrue(UUID companyId,
            UUID serviceProductId);
}
