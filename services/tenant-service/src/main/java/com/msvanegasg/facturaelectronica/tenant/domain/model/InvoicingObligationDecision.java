package com.msvanegasg.facturaelectronica.tenant.domain.model;

import java.util.List;

public record InvoicingObligationDecision(
        InvoicingObligationStatus status,
        String decisionCode,
        List<String> reasons) {
}
