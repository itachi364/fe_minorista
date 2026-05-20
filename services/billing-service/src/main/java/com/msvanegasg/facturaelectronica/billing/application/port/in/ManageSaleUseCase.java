package com.msvanegasg.facturaelectronica.billing.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreateSaleCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleResult;

public interface ManageSaleUseCase {

    SaleResult create(CreateSaleCommand command);

    SaleResult confirm(UUID companyId, UUID saleId, String idempotencyKey);

    SaleResult findById(UUID companyId, UUID saleId);
}
