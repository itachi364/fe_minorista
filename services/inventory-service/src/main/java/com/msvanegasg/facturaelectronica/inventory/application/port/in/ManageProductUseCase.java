package com.msvanegasg.facturaelectronica.inventory.application.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.application.dto.CreateProductCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ProductResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.StockAvailabilityResult;

public interface ManageProductUseCase {

    ProductResult create(CreateProductCommand command);

    ProductResult update(UUID companyId, UUID productId, CreateProductCommand command);

    ProductResult deactivate(UUID companyId, UUID productId);

    ProductResult findById(UUID companyId, UUID productId);

    ProductResult findByBarcode(UUID companyId, String barcode);

    ProductResult findByBarcode(UUID companyId, String barcode, boolean includeInactive);

    List<ProductResult> findStock(UUID companyId, Boolean active);

    StockAvailabilityResult checkAvailability(UUID companyId, UUID productId, BigDecimal quantity);
}
