package com.msvanegasg.facturaelectronica.inventory.application.usecase;

public class ProductAlreadyExistsException extends RuntimeException {

    public ProductAlreadyExistsException(String sku) {
        super("Product already exists for sku: " + sku);
    }
}
