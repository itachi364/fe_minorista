package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.billing.application.port.out.CompanyFiscalPolicyRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.CompanyFiscalPolicy;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.CompanyFiscalPolicyJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.CompanyFiscalPolicyJpaRepository;

@Component
public class CompanyFiscalPolicyPersistenceAdapter implements CompanyFiscalPolicyRepositoryPort {

    private final CompanyFiscalPolicyJpaRepository repository;

    public CompanyFiscalPolicyPersistenceAdapter(CompanyFiscalPolicyJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public CompanyFiscalPolicy save(CompanyFiscalPolicy policy) {
        return toDomain(repository.save(toEntity(policy)));
    }

    @Override
    public Optional<CompanyFiscalPolicy> findByCompanyId(UUID companyId) {
        return repository.findById(companyId).map(CompanyFiscalPolicyPersistenceAdapter::toDomain);
    }

    private static CompanyFiscalPolicyJpaEntity toEntity(CompanyFiscalPolicy policy) {
        CompanyFiscalPolicyJpaEntity entity = new CompanyFiscalPolicyJpaEntity();
        entity.setCompanyId(policy.companyId());
        entity.setDefaultSaleDocumentType(policy.defaultSaleDocumentType());
        entity.setAllowDocumentTypeOverride(policy.allowDocumentTypeOverride());
        entity.setRequirePinForOverride(policy.requirePinForOverride());
        entity.setUpdatedAt(policy.updatedAt());
        return entity;
    }

    private static CompanyFiscalPolicy toDomain(CompanyFiscalPolicyJpaEntity entity) {
        return new CompanyFiscalPolicy(entity.getCompanyId(), entity.getDefaultSaleDocumentType(),
                entity.isAllowDocumentTypeOverride(), entity.isRequirePinForOverride(), entity.getUpdatedAt());
    }
}
