package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record WithholdingCalculationResult(
        UUID companyId,
        UUID thirdPartyId,
        ThirdPartyFiscalProfileCommand thirdPartyFiscalSnapshot,
        List<WithholdingCalculationItemResult> items,
        BigDecimal grossAmount,
        BigDecimal taxAmount,
        BigDecimal withholdingTotal,
        BigDecimal netPayable) {
}
