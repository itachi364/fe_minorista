package com.msvanegasg.facturaelectronica.identity.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.CompanyMembership;

public interface CompanyMembershipRepositoryPort {

    CompanyMembership save(CompanyMembership membership);

    Optional<CompanyMembership> findByIdAndCompanyId(UUID membershipId, UUID companyId);

    Optional<CompanyMembership> findByCompanyIdAndUserId(UUID companyId, UUID userId);

    List<CompanyMembership> findByUserId(UUID userId);

    boolean existsByCompanyId(UUID companyId);
}
