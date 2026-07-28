package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.CompanyMembershipJpaEntity;

public interface CompanyMembershipJpaRepository extends JpaRepository<CompanyMembershipJpaEntity, UUID> {

    Optional<CompanyMembershipJpaEntity> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<CompanyMembershipJpaEntity> findByCompanyIdAndUserId(UUID companyId, UUID userId);

    List<CompanyMembershipJpaEntity> findByUserIdAndActiveTrue(UUID userId);

    boolean existsByCompanyId(UUID companyId);
}
