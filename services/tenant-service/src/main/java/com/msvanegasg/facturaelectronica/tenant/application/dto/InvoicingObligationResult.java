package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyInvoicingObligationSnapshot;
import com.msvanegasg.facturaelectronica.tenant.domain.model.InvoicingObligationInput;
import com.msvanegasg.facturaelectronica.tenant.domain.model.InvoicingObligationStatus;

public record InvoicingObligationResult(
        UUID id,
        UUID companyId,
        long version,
        InvoicingObligationStatus status,
        String decisionCode,
        List<String> decisionReasons,
        InvoicingObligationInput input,
        BigDecimal uvtValue,
        String normativeRuleSetVersion,
        UUID evaluatedBy,
        Instant evaluatedAt) {

    public static InvoicingObligationResult from(CompanyInvoicingObligationSnapshot snapshot) {
        return new InvoicingObligationResult(snapshot.id(), snapshot.companyId(), snapshot.version(),
                snapshot.status(), snapshot.decisionCode(), snapshot.decisionReasons(), snapshot.input(),
                snapshot.uvtValue(), snapshot.normativeRuleSetVersion(), snapshot.evaluatedBy(), snapshot.evaluatedAt());
    }
}
