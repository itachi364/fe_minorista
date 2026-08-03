package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.identity.application.port.out.CompanyRoleRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.domain.model.CompanyRole;
import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionDescriptor;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.CompanyRoleJpaEntity;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.CompanyUserRoleAssignmentJpaEntity;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.PermissionCatalogJpaEntity;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository.CompanyRoleJpaRepository;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository.CompanyUserRoleAssignmentJpaRepository;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository.PermissionCatalogJpaRepository;

@Component
public class CompanyRolePersistenceAdapter implements CompanyRoleRepositoryPort {

    private final PermissionCatalogJpaRepository permissionRepository;
    private final CompanyRoleJpaRepository roleRepository;
    private final CompanyUserRoleAssignmentJpaRepository assignmentRepository;

    public CompanyRolePersistenceAdapter(PermissionCatalogJpaRepository permissionRepository,
            CompanyRoleJpaRepository roleRepository,
            CompanyUserRoleAssignmentJpaRepository assignmentRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<PermissionDescriptor> listActivePermissions() {
        return permissionRepository.findByActiveTrueOrderByScopeAscModuleAscCodeAsc().stream()
                .map(CompanyRolePersistenceAdapter::toDescriptor)
                .toList();
    }

    @Override
    public CompanyRole save(CompanyRole role) {
        return toDomain(roleRepository.save(toEntity(role)));
    }

    @Override
    public java.util.Optional<CompanyRole> findByIdAndCompanyId(UUID roleId, UUID companyId) {
        return roleRepository.findByIdAndCompanyId(roleId, companyId).map(CompanyRolePersistenceAdapter::toDomain);
    }

    @Override
    public List<CompanyRole> findByCompanyId(UUID companyId) {
        return roleRepository.findByCompanyIdOrderByNameAsc(companyId).stream()
                .map(CompanyRolePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<CompanyRole> findActiveAssignedRoles(UUID companyId, UUID userId) {
        List<UUID> roleIds = assignmentRepository.findByCompanyIdAndUserIdAndRevokedAtIsNull(companyId, userId).stream()
                .map(CompanyUserRoleAssignmentJpaEntity::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleRepository.findByCompanyIdAndIdInAndActiveTrue(companyId, roleIds).stream()
                .map(CompanyRolePersistenceAdapter::toDomain)
                .toList();
    }

        @Override
    public List<UUID> findAssignedCompanyIds(UUID userId) {
        return assignmentRepository.findActiveCompanyIdsByUserId(userId);
    }

    @Override
    @Transactional
    public void replaceUserRoleAssignments(UUID companyId, UUID userId, Set<UUID> roleIds, UUID assignedBy,
            Instant assignedAt) {
        Set<UUID> requested = Set.copyOf(roleIds);
        List<CompanyUserRoleAssignmentJpaEntity> current = assignmentRepository
                .findByCompanyIdAndUserIdAndRevokedAtIsNull(companyId, userId);
        for (CompanyUserRoleAssignmentJpaEntity assignment : current) {
            if (!requested.contains(assignment.getRoleId())) {
                assignment.setRevokedAt(assignedAt);
                assignment.setRevokedBy(assignedBy);
                assignmentRepository.save(assignment);
            }
        }
        Set<UUID> active = current.stream()
                .filter(assignment -> requested.contains(assignment.getRoleId()))
                .map(CompanyUserRoleAssignmentJpaEntity::getRoleId)
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));
        for (UUID roleId : requested) {
            if (active.contains(roleId)) {
                continue;
            }
            CompanyUserRoleAssignmentJpaEntity assignment = assignmentRepository
                    .findByCompanyIdAndUserIdAndRoleId(companyId, userId, roleId)
                    .orElseGet(CompanyUserRoleAssignmentJpaEntity::new);
            assignment.setCompanyId(companyId);
            assignment.setUserId(userId);
            assignment.setRoleId(roleId);
            assignment.setAssignedBy(assignedBy);
            assignment.setAssignedAt(assignedAt);
            assignment.setRevokedAt(null);
            assignment.setRevokedBy(null);
            assignmentRepository.save(assignment);
        }
    }

    @Override
    public void revokeUserRoleAssignment(UUID companyId, UUID userId, UUID roleId, UUID revokedBy, Instant revokedAt) {
        assignmentRepository.findByCompanyIdAndUserIdAndRoleId(companyId, userId, roleId).ifPresent(assignment -> {
            assignment.setRevokedAt(revokedAt);
            assignment.setRevokedBy(revokedBy);
            assignmentRepository.save(assignment);
        });
    }

    private static PermissionDescriptor toDescriptor(PermissionCatalogJpaEntity entity) {
        return new PermissionDescriptor(entity.getCode(), entity.getScope(), entity.getModule(), entity.getDescription(),
                entity.isActive());
    }

    private static CompanyRoleJpaEntity toEntity(CompanyRole role) {
        CompanyRoleJpaEntity entity = new CompanyRoleJpaEntity();
        entity.setId(role.id());
        entity.setCompanyId(role.companyId());
        entity.setName(role.name());
        entity.setDescription(role.description());
        entity.setPermissionCodes(role.permissionCodes());
        entity.setSystemSeed(role.systemSeed());
        entity.setActive(role.active());
        entity.setCreatedBy(role.createdBy());
        entity.setCreatedAt(role.createdAt());
        entity.setUpdatedAt(role.updatedAt());
        return entity;
    }

    private static CompanyRole toDomain(CompanyRoleJpaEntity entity) {
        return new CompanyRole(entity.getId(), entity.getCompanyId(), entity.getName(), entity.getDescription(),
                entity.getPermissionCodes(), entity.isSystemSeed(), entity.isActive(), entity.getCreatedBy(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
