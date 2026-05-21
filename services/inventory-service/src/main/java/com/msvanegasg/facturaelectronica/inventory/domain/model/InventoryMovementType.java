package com.msvanegasg.facturaelectronica.inventory.domain.model;

public enum InventoryMovementType {
    PURCHASE_IN,
    SALE_OUT,
    RETURN_IN,
    ADJUSTMENT_IN,
    ADJUSTMENT_OUT,
    CONSUMPTION_OUT,
    WASTE_OUT;

    public boolean increasesStock() {
        return this == PURCHASE_IN || this == RETURN_IN || this == ADJUSTMENT_IN;
    }

    public boolean requiresReason() {
        return this == CONSUMPTION_OUT || this == WASTE_OUT;
    }
}
