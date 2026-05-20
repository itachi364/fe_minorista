package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.StockBalanceId;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.StockBalanceJpaEntity;

public interface StockBalanceJpaRepository extends JpaRepository<StockBalanceJpaEntity, StockBalanceId> {

    Optional<StockBalanceJpaEntity> findByCompanyIdAndProductId(UUID companyId, UUID productId);
}
