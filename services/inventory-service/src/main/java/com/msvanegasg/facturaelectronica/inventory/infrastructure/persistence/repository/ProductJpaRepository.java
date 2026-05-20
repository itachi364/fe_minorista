package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.ProductJpaEntity;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

    Optional<ProductJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    boolean existsByCompanyIdAndSku(UUID companyId, String sku);
}
