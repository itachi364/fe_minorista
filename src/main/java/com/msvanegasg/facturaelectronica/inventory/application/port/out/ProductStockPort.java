package com.msvanegasg.facturaelectronica.inventory.application.port.out;

public interface ProductStockPort {

    void increaseStock(Long barcode, Integer quantity);
}
