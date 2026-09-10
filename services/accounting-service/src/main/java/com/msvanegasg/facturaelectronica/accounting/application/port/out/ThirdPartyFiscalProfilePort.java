package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.ThirdPartyFiscalProfile;

public interface ThirdPartyFiscalProfilePort {

    Optional<ThirdPartyFiscalProfile> findByCompanyIdAndId(UUID companyId, UUID thirdPartyId);

    default Optional<ThirdPartyFiscalProfile> findByCompanyIdAndIdAndDate(UUID companyId, UUID thirdPartyId,
            LocalDate effectiveOn) {
        return findByCompanyIdAndId(companyId, thirdPartyId);
    }
}
