package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.CompanyFiscalPolicy;

public interface CompanyFiscalPolicyRepositoryPort {

    CompanyFiscalPolicy save(CompanyFiscalPolicy policy);

    Optional<CompanyFiscalPolicy> findByCompanyId(UUID companyId);

    static CompanyFiscalPolicyRepositoryPort defaultsOnly() {
        return new CompanyFiscalPolicyRepositoryPort() {
            @Override
            public CompanyFiscalPolicy save(CompanyFiscalPolicy policy) {
                return policy;
            }

            @Override
            public Optional<CompanyFiscalPolicy> findByCompanyId(UUID companyId) {
                return Optional.of(CompanyFiscalPolicy.defaults(companyId));
            }
        };
    }
}
