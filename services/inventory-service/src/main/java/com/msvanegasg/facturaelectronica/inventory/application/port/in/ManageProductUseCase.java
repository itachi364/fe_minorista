package com.msvanegasg.facturaelectronica.inventory.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.application.dto.CreateProductCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ProductResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.StockAvailabilityResult;

public interface ManageProductUseCase {

    ProductResult create(CreateProductCommand command);

    ProductResult findById(UUID companyId, UUID productId);

    StockAvailabilityResult checkAvailability(UUID companyId, UUID productId, BigDecimal quantity);
}
