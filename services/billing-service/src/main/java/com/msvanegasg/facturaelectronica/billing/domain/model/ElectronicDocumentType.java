package com.msvanegasg.facturaelectronica.billing.domain.model;

public enum ElectronicDocumentType {
    ELECTRONIC_POS,
    ELECTRONIC_INVOICE,
    CREDIT_NOTE,
    DEBIT_NOTE,
    POS_ADJUSTMENT_NOTE;

    public boolean isSaleDocument() {
        return this == ELECTRONIC_INVOICE || this == ELECTRONIC_POS;
    }
}
