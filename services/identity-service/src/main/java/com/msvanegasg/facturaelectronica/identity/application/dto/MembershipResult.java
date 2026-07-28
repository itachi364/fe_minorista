package com.msvanegasg.facturaelectronica.identity.application.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.CompanyMembership;
import com.msvanegasg.facturaelectronica.identity.domain.model.RoleCode;

public record MembershipResult(UUID id, UUID companyId, UUID userId, Set<RoleCode> roles, boolean active,
        Instant createdAt, Instant updatedAt) {

    public static MembershipResult from(CompanyMembership membership) {
        return new MembershipResult(membership.id(), membership.companyId(), membership.userId(), membership.roles(),
                membership.active(), membership.createdAt(), membership.updatedAt());
    }
}
