package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity.CompanyLicenseJpaEntity;

public interface CompanyLicenseJpaRepository extends JpaRepository<CompanyLicenseJpaEntity, UUID> {

    Optional<CompanyLicenseJpaEntity> findByCompanyId(UUID companyId);
}
