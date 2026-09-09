package com.msvanegasg.facturaelectronica.tenant.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyInvoicingObligationSnapshot;

public interface InvoicingObligationRepositoryPort {
    CompanyInvoicingObligationSnapshot saveAsCurrent(CompanyInvoicingObligationSnapshot snapshot);
    Optional<CompanyInvoicingObligationSnapshot> findCurrent(UUID companyId);
    List<CompanyInvoicingObligationSnapshot> findHistory(UUID companyId);
    long nextVersion(UUID companyId);
    BigDecimal findUvtValue(LocalDate operationDate);
}
