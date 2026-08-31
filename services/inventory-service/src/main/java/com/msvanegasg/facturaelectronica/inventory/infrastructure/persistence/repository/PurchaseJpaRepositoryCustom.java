package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseStatus;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.PurchaseJpaEntity;

public interface PurchaseJpaRepositoryCustom {

    List<PurchaseJpaEntity> findPurchasesDynamic(UUID companyId, PurchaseStatus status, UUID supplierId,
            Instant from, Instant to);
}
