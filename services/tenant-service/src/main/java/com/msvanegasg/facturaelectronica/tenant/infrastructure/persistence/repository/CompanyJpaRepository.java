package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity.CompanyJpaEntity;

public interface CompanyJpaRepository extends JpaRepository<CompanyJpaEntity, UUID> {

    boolean existsByIdentificationTypeIdAndIdentificationNumber(UUID identificationTypeId, String identificationNumber);
}
