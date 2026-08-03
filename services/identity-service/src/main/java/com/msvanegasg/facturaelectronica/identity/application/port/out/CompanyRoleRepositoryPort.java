package com.msvanegasg.facturaelectronica.identity.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.CompanyRole;
import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionDescriptor;

public interface CompanyRoleRepositoryPort {

    List<PermissionDescriptor> listActivePermissions();

    CompanyRole save(CompanyRole role);

    Optional<CompanyRole> findByIdAndCompanyId(UUID roleId, UUID companyId);

    List<CompanyRole> findByCompanyId(UUID companyId);

    List<CompanyRole> findActiveAssignedRoles(UUID companyId, UUID userId);

    List<UUID> findAssignedCompanyIds(UUID userId);

    void replaceUserRoleAssignments(UUID companyId, UUID userId, Set<UUID> roleIds, UUID assignedBy, Instant assignedAt);

    void revokeUserRoleAssignment(UUID companyId, UUID userId, UUID roleId, UUID revokedBy, Instant revokedAt);

    default Set<PermissionCode> findEffectivePermissions(UUID companyId, UUID userId) {
        return findActiveAssignedRoles(companyId, userId).stream()
                .filter(CompanyRole::active)
                .flatMap(role -> role.permissionCodes().stream())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
