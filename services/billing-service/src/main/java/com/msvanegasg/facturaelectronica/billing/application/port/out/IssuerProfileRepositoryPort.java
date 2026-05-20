package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.IssuerProfile;

public interface IssuerProfileRepositoryPort {
    IssuerProfile save(IssuerProfile issuerProfile);

    Optional<IssuerProfile> findActiveByCompanyId(UUID companyId);
}
