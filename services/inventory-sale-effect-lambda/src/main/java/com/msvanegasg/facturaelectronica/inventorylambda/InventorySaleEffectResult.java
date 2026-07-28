package com.msvanegasg.facturaelectronica.inventorylambda;

public record InventorySaleEffectResult(boolean processed, boolean duplicate, boolean ignored, int movementCount) {

    public static InventorySaleEffectResult asProcessed(int movementCount) {
        return new InventorySaleEffectResult(true, false, false, movementCount);
    }

    public static InventorySaleEffectResult asDuplicate() {
        return new InventorySaleEffectResult(false, true, false, 0);
    }

    public static InventorySaleEffectResult asIgnored() {
        return new InventorySaleEffectResult(false, false, true, 0);
    }
}
