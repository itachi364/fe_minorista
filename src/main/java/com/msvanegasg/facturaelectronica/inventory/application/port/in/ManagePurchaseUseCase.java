package com.msvanegasg.facturaelectronica.inventory.application.port.in;

import java.util.List;

import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseCommand;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseLine;

public interface ManagePurchaseUseCase {

    Purchase create(PurchaseCommand command);

    Purchase updateIfPending(Long purchaseId, PurchaseCommand command);

    Purchase findById(Long purchaseId);

    List<Purchase> findActive();

    List<PurchaseLine> findDetailsByPurchaseId(Long purchaseId);
}
