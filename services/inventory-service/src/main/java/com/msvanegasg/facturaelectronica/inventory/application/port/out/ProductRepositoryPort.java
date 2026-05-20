package com.msvanegasg.facturaelectronica.inventory.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.Product;

public interface ProductRepositoryPort {

    Optional<Product> findByCompanyIdAndId(UUID companyId, UUID id);

    boolean existsByCompanyIdAndSku(UUID companyId, String sku);

    Product save(Product product);
}
