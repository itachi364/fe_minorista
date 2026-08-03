package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.CompanyUserRoleAssignmentId;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.CompanyUserRoleAssignmentJpaEntity;

public interface CompanyUserRoleAssignmentJpaRepository
        extends JpaRepository<CompanyUserRoleAssignmentJpaEntity, CompanyUserRoleAssignmentId> {

    List<CompanyUserRoleAssignmentJpaEntity> findByCompanyIdAndUserIdAndRevokedAtIsNull(UUID companyId, UUID userId);

    Optional<CompanyUserRoleAssignmentJpaEntity> findByCompanyIdAndUserIdAndRoleId(UUID companyId, UUID userId,
            UUID roleId);

    @Query("select distinct a.companyId from CompanyUserRoleAssignmentJpaEntity a where a.userId = :userId and a.revokedAt is null")
    List<UUID> findActiveCompanyIdsByUserId(UUID userId);
}
