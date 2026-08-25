package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.IssuerProfile;

public interface IssuerProfileRepositoryPort {
    IssuerProfile save(IssuerProfile issuerProfile);

    IssuerProfile saveAsOnlyActive(IssuerProfile issuerProfile);

    Optional<IssuerProfile> findActiveByCompanyId(UUID companyId);

    Optional<IssuerProfile> findByCompanyIdAndId(UUID companyId, UUID issuerId);

    List<IssuerProfile> findByCompanyId(UUID companyId);
}
