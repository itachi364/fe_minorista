package com.msvanegasg.facturaelectronica.tenant.domain.model;

public enum InvoicingObligationStatus {
    OBLIGATED,
    NOT_OBLIGATED_VERIFIED,
    VOLUNTARY_ELECTRONIC,
    REVIEW_REQUIRED,
    TRANSITION_TO_OBLIGATED;

    public static InvoicingObligationStatus afterReevaluation(InvoicingObligationStatus previous,
            InvoicingObligationStatus calculated) {
        if (previous == NOT_OBLIGATED_VERIFIED && calculated == OBLIGATED) {
            return TRANSITION_TO_OBLIGATED;
        }
        return calculated;
    }
}
