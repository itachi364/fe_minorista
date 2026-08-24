package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.entity.DianCompanyConfigurationJpaEntity;

public interface DianCompanyConfigurationJpaRepository extends JpaRepository<DianCompanyConfigurationJpaEntity, UUID> {

    Optional<DianCompanyConfigurationJpaEntity> findByCompanyId(UUID companyId);
}
