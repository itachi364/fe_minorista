package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;
import com.msvanegasg.facturaelectronica.billing.domain.model.NumberingResolution;

public interface NumberingResolutionRepositoryPort {
    NumberingResolution save(NumberingResolution numberingResolution);

    Optional<NumberingResolution> findActiveResolution(UUID companyId, ElectronicDocumentType documentType,
            FiscalEnvironment environment, LocalDate documentDate);

    List<NumberingResolution> findByCompanyId(UUID companyId, ElectronicDocumentType documentType, Boolean active);

    Optional<NumberingResolution> findByCompanyIdAndId(UUID companyId, UUID resolutionId);

    NumberingResolution saveAsOnlyActive(NumberingResolution numberingResolution);
}
