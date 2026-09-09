package com.msvanegasg.facturaelectronica.tenant.application.usecase;

import java.util.UUID;

public class InvoicingObligationNotFoundException extends RuntimeException {
    public InvoicingObligationNotFoundException(UUID companyId) {
        super("La empresa no tiene una clasificacion de obligacion de facturar: " + companyId);
    }
}
