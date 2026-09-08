package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyTaxProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyTaxProfile;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity.CompanyTaxProfileJpaEntity;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.repository.CompanyTaxProfileJpaRepository;

@Component
public class CompanyTaxProfilePersistenceAdapter implements CompanyTaxProfileRepositoryPort {
    private final CompanyTaxProfileJpaRepository repository;

    public CompanyTaxProfilePersistenceAdapter(CompanyTaxProfileJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<CompanyTaxProfile> findByCompanyId(UUID companyId) {
        return repository.findById(companyId).map(this::toDomain);
    }

    @Override
    public CompanyTaxProfile save(CompanyTaxProfile profile) {
        return toDomain(repository.save(toEntity(profile)));
    }

    private CompanyTaxProfileJpaEntity toEntity(CompanyTaxProfile profile) {
        return new CompanyTaxProfileJpaEntity(profile.companyId(), profile.companySize(),
                profile.financialReportingGroup(), profile.taxRegime(), profile.rutResponsibilities(),
                profile.vatResponsible(), profile.withholdingAgent(), profile.vatWithholdingAgent(),
                profile.icaWithholdingAgent(), profile.largeTaxpayer(), profile.selfWithholding(),
                profile.simpleRegime(), profile.icaMunicipalityCode(), profile.ciiuCodes(), profile.updatedBy(),
                profile.updatedAt());
    }

    private CompanyTaxProfile toDomain(CompanyTaxProfileJpaEntity entity) {
        return new CompanyTaxProfile(entity.getCompanyId(), entity.getCompanySize(),
                entity.getFinancialReportingGroup(), entity.getTaxRegime(), entity.getRutResponsibilities(),
                entity.isVatResponsible(), entity.isWithholdingAgent(), entity.isVatWithholdingAgent(),
                entity.isIcaWithholdingAgent(), entity.isLargeTaxpayer(), entity.isSelfWithholding(),
                entity.isSimpleRegime(), entity.getIcaMunicipalityCode(), entity.getCiiuCodes(), entity.getUpdatedBy(),
                entity.getUpdatedAt());
    }
}
