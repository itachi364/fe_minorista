package com.msvanegasg.facturaelectronica.catalog.application.port.in;

import java.util.List;

import com.msvanegasg.facturaelectronica.catalog.application.dto.IncreaseProductStockCommand;
import com.msvanegasg.facturaelectronica.catalog.application.dto.ProductCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Product;

public interface ManageProductUseCase {

    List<Product> findAll();

    List<Product> findActive();

    List<Product> findInactive();

    List<Product> findByName(String name);

    Product findById(Long id);

    Product findByBarcode(Long barcode);

    Product create(ProductCommand command);

    Product update(Long id, ProductCommand command);

    void disable(Long id);

    void enable(Long id);

    Product increaseStock(IncreaseProductStockCommand command);
}
