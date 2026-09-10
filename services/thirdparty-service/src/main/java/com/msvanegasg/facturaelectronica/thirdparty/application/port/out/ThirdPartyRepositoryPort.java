package com.msvanegasg.facturaelectronica.thirdparty.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdParty;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdPartyRole;

public interface ThirdPartyRepositoryPort {

    ThirdParty save(ThirdParty thirdParty);

    Optional<ThirdParty> findByCompanyIdAndId(UUID companyId, UUID id);

    default Optional<ThirdParty> findEffective(UUID companyId, UUID id, LocalDate effectiveOn) {
        return findByCompanyIdAndId(companyId, id);
    }

    Optional<ThirdParty> findByCompanyIdAndDocument(UUID companyId, Integer identificationTypeCode,
            String identificationNumber);

    List<ThirdParty> findByCompanyIdAndRole(UUID companyId, ThirdPartyRole role, Boolean active);

    List<ThirdParty> findByCompanyIdAndRoleAndIdentificationNumberPrefix(UUID companyId, ThirdPartyRole role,
            Boolean active, String identificationNumberPrefix);

    boolean existsByCompanyIdAndDocument(UUID companyId, Integer identificationTypeCode, String identificationNumber);
}
