package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.FinalConsumerProfileJpaEntity;

public interface FinalConsumerProfileJpaRepository extends JpaRepository<FinalConsumerProfileJpaEntity, UUID> {

    Optional<FinalConsumerProfileJpaEntity> findFirstByCompanyIdAndProfileCodeAndActiveTrue(UUID companyId,
            String profileCode);

    Optional<FinalConsumerProfileJpaEntity> findFirstByCompanyIdIsNullAndProfileCodeAndActiveTrue(String profileCode);
}
