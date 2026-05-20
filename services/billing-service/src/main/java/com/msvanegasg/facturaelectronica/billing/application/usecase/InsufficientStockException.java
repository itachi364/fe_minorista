package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(UUID productId) {
        super("stock is insufficient for product: " + productId);
    }
}
