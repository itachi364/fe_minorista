package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.CompanyRoleJpaEntity;

public interface CompanyRoleJpaRepository extends JpaRepository<CompanyRoleJpaEntity, UUID> {

    Optional<CompanyRoleJpaEntity> findByIdAndCompanyId(UUID id, UUID companyId);

    List<CompanyRoleJpaEntity> findByCompanyIdOrderByNameAsc(UUID companyId);

    List<CompanyRoleJpaEntity> findByCompanyIdAndIdInAndActiveTrue(UUID companyId, Iterable<UUID> ids);
}
