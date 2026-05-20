package com.msvanegasg.facturaelectronica.inventory.domain.model;

public enum InventoryMovementType {
    PURCHASE_IN,
    SALE_OUT,
    RETURN_IN,
    ADJUSTMENT_IN,
    ADJUSTMENT_OUT;

    public boolean increasesStock() {
        return this == PURCHASE_IN || this == RETURN_IN || this == ADJUSTMENT_IN;
    }
}
