package com.msvanegasg.facturaelectronica.catalog.application.port.out;

import java.util.List;
import java.util.Optional;

import com.msvanegasg.facturaelectronica.catalog.domain.model.Product;

public interface ProductRepositoryPort {

    List<Product> findAll();

    List<Product> findActive();

    List<Product> findInactive();

    List<Product> findByNameContainingIgnoreCase(String name);

    Optional<Product> findById(Long id);

    Optional<Product> findByBarcode(Long barcode);

    Product save(Product product);
}
