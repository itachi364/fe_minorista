package com.msvanegasg.facturaelectronica.billing.domain.model;

public enum ElectronicDocumentType {
    NON_FISCAL_SALE,
    ELECTRONIC_POS,
    ELECTRONIC_INVOICE,
    CREDIT_NOTE,
    DEBIT_NOTE,
    POS_ADJUSTMENT_NOTE;

    public boolean isSaleDocument() {
        return this == ELECTRONIC_INVOICE || this == ELECTRONIC_POS || this == NON_FISCAL_SALE;
    }

    public boolean requiresDianConfiguration() {
        return this != NON_FISCAL_SALE;
    }
}
