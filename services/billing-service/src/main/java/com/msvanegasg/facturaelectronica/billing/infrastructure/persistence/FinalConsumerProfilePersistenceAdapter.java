package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.billing.application.port.out.FinalConsumerProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.FinalConsumerProfile;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.FinalConsumerProfileJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.FinalConsumerProfileJpaRepository;

@Component
public class FinalConsumerProfilePersistenceAdapter implements FinalConsumerProfileRepositoryPort {

    private static final String FINAL_CONSUMER = "FINAL_CONSUMER";

    private final FinalConsumerProfileJpaRepository repository;

    public FinalConsumerProfilePersistenceAdapter(FinalConsumerProfileJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<FinalConsumerProfile> findActiveForCompanyOrGlobal(UUID companyId) {
        return repository.findFirstByCompanyIdAndProfileCodeAndActiveTrue(companyId, FINAL_CONSUMER)
                .or(() -> repository.findFirstByCompanyIdIsNullAndProfileCodeAndActiveTrue(FINAL_CONSUMER))
                .map(FinalConsumerProfilePersistenceAdapter::toDomain);
    }

    private static FinalConsumerProfile toDomain(FinalConsumerProfileJpaEntity entity) {
        return new FinalConsumerProfile(entity.getId(), entity.getCompanyId(), entity.getProfileCode(),
                entity.getIdentificationTypeCode(), entity.getIdentificationNumber(), entity.getDisplayName(),
                entity.isActive(), entity.getSource(), entity.getSourceVersion(), entity.getUpdatedAt());
    }
}
