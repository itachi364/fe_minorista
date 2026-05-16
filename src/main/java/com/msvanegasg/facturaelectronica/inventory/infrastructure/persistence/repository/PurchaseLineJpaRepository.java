package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.PurchaseLineJpaEntity;

public interface PurchaseLineJpaRepository extends JpaRepository<PurchaseLineJpaEntity, Long> {

    List<PurchaseLineJpaEntity> findByCompraIdCompra(Long idCompra);

    List<PurchaseLineJpaEntity> findByProducto(Long producto);
}
