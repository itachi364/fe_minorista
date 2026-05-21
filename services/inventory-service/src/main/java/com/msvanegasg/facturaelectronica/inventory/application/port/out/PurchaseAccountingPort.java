package com.msvanegasg.facturaelectronica.inventory.application.port.out;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;

public interface PurchaseAccountingPort {

    void applyConfirmedPurchase(Purchase purchase, UUID createdBy);
}
