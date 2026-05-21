package com.msvanegasg.facturaelectronica.thirdparty.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.thirdparty.application.dto.ThirdPartyCommand;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.ThirdPartyResult;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdPartyRole;

public interface ManageThirdPartyUseCase {

    ThirdPartyResult create(ThirdPartyCommand command);

    ThirdPartyResult update(UUID companyId, UUID id, ThirdPartyCommand command);

    ThirdPartyResult findById(UUID companyId, UUID id);

    List<ThirdPartyResult> findByRole(UUID companyId, ThirdPartyRole role, Boolean active);

    ThirdPartyResult findByDocument(UUID companyId, String identificationTypeCode, String identificationNumber);

    void activate(UUID companyId, UUID id);

    void deactivate(UUID companyId, UUID id);
}
