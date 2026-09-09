package com.msvanegasg.facturaelectronica.tenant.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CompanyInvoicingObligationSnapshot(
        UUID id,
        UUID companyId,
        long version,
        boolean current,
        InvoicingObligationStatus status,
        String decisionCode,
        List<String> decisionReasons,
        InvoicingObligationInput input,
        BigDecimal uvtValue,
        String normativeRuleSetVersion,
        UUID evaluatedBy,
        Instant evaluatedAt) {
}
