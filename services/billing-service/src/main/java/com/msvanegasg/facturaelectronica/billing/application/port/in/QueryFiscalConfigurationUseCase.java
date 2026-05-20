package com.msvanegasg.facturaelectronica.billing.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.IssuerProfileResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.NumberingResolutionResult;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

public interface QueryFiscalConfigurationUseCase {
    IssuerProfileResult findCurrentIssuer(UUID companyId);

    List<NumberingResolutionResult> findNumberingResolutions(UUID companyId, ElectronicDocumentType documentType,
            Boolean active);
}
