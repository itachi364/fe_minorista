package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity.CompanyBrandingJpaEntity;

public interface CompanyBrandingJpaRepository extends JpaRepository<CompanyBrandingJpaEntity, UUID> {
}
