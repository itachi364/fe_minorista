package com.msvanegasg.facturaelectronica.inventory.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.application.dto.CreatePurchaseCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseResult;

public interface ManagePurchaseUseCase {

    PurchaseResult create(CreatePurchaseCommand command);

    PurchaseResult confirm(UUID companyId, UUID purchaseId, UUID createdBy);
}
