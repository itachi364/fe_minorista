package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.ThirdPartyFiscalProfile;

public interface ThirdPartyFiscalProfilePort {

    Optional<ThirdPartyFiscalProfile> findByCompanyIdAndId(UUID companyId, UUID thirdPartyId);
}
