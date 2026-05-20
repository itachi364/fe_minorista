package com.msvanegasg.facturaelectronica.billing.domain.model;

public enum ElectronicDocumentStatus {
    DRAFT,
    CALCULATED,
    NUMBER_ASSIGNED,
    SENT_TO_PROVIDER,
    VALIDATED,
    REJECTED,
    FAILED,
    CONTINGENCY,
    CANCELLED_BY_NOTE,
    ADJUSTED
}
