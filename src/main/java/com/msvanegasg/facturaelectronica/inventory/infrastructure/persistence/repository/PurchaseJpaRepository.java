package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.enums.Estado;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.PurchaseJpaEntity;

public interface PurchaseJpaRepository extends JpaRepository<PurchaseJpaEntity, Long> {

    List<PurchaseJpaEntity> findByActivoTrue();

    List<PurchaseJpaEntity> findByActivoFalse();

    List<PurchaseJpaEntity> findByEstado(Estado estado);
}
