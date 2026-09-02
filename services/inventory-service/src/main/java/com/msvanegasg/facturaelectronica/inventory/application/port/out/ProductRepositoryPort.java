package com.msvanegasg.facturaelectronica.inventory.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.Product;

public interface ProductRepositoryPort {

    Optional<Product> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<Product> findActiveByCompanyIdAndBarcode(UUID companyId, String barcode);

    Optional<Product> findByCompanyIdAndBarcode(UUID companyId, String barcode);

    List<Product> findByCompanyId(UUID companyId, Boolean active);

    boolean existsByCompanyIdAndSku(UUID companyId, String sku);

    boolean existsByCompanyIdAndSkuAndIdNot(UUID companyId, String sku, UUID id);

    boolean existsByCompanyIdAndBarcodeAndIdNot(UUID companyId, String barcode, UUID id);

    Product save(Product product);
}
