package com.msvanegasg.facturaelectronica.identity.application.dto;

import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.RoleCode;

public record CompanyAccessResult(UUID companyId, Set<RoleCode> roles, Set<PermissionCode> permissions) {
}
