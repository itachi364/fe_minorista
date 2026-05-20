package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.UUID;

public class SaleNotFoundException extends RuntimeException {

    public SaleNotFoundException(UUID id) {
        super("sale not found: " + id);
    }
}
