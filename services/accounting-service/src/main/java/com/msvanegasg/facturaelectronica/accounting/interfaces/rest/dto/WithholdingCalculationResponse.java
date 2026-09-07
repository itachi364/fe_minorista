package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record WithholdingCalculationResponse(
        UUID companyId,
        UUID thirdPartyId,
        ThirdPartyFiscalProfileResponse thirdPartyFiscalSnapshot,
        List<WithholdingCalculationItemResponse> items,
        BigDecimal grossAmount,
        BigDecimal taxAmount,
        BigDecimal withholdingTotal,
        BigDecimal netPayable) {
}
