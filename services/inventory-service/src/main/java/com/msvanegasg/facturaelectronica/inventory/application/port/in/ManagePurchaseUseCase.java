package com.msvanegasg.facturaelectronica.inventory.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.application.dto.CreatePurchaseCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseQuery;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseResult;

public interface ManagePurchaseUseCase {

    PurchaseResult create(CreatePurchaseCommand command);

    PurchaseResult confirm(UUID companyId, UUID purchaseId, UUID createdBy);

    List<PurchaseResult> find(PurchaseQuery query);
}
