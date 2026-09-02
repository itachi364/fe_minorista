package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.ProductJpaEntity;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

    Optional<ProductJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<ProductJpaEntity> findByCompanyIdAndBarcodeAndActiveTrue(UUID companyId, String barcode);

    Optional<ProductJpaEntity> findByCompanyIdAndBarcode(UUID companyId, String barcode);

    List<ProductJpaEntity> findByCompanyIdOrderByNameAsc(UUID companyId);

    List<ProductJpaEntity> findByCompanyIdAndActiveOrderByNameAsc(UUID companyId, boolean active);

    boolean existsByCompanyIdAndSku(UUID companyId, String sku);

    boolean existsByCompanyIdAndSkuAndIdNot(UUID companyId, String sku, UUID id);

    boolean existsByCompanyIdAndBarcodeAndIdNot(UUID companyId, String barcode, UUID id);
}
