package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity.CompanyFileAssetJpaEntity;

public interface CompanyFileAssetJpaRepository extends JpaRepository<CompanyFileAssetJpaEntity, UUID> {

    Optional<CompanyFileAssetJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);
}
