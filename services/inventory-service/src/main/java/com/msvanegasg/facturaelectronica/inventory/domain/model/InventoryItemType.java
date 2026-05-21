package com.msvanegasg.facturaelectronica.inventory.domain.model;

public enum InventoryItemType {
    PHYSICAL_GOOD(true, true, true),
    SERVICE(true, false, false),
    SUPPLY(false, true, true);

    private final boolean defaultSaleEnabled;
    private final boolean defaultPurchaseEnabled;
    private final boolean defaultStockTracked;

    InventoryItemType(boolean defaultSaleEnabled, boolean defaultPurchaseEnabled, boolean defaultStockTracked) {
        this.defaultSaleEnabled = defaultSaleEnabled;
        this.defaultPurchaseEnabled = defaultPurchaseEnabled;
        this.defaultStockTracked = defaultStockTracked;
    }

    public boolean defaultSaleEnabled() {
        return defaultSaleEnabled;
    }

    public boolean defaultPurchaseEnabled() {
        return defaultPurchaseEnabled;
    }

    public boolean defaultStockTracked() {
        return defaultStockTracked;
    }
}
