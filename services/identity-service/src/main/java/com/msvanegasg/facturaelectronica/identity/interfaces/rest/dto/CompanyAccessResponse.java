package com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto;

import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.RoleCode;

public record CompanyAccessResponse(UUID companyId, Set<RoleCode> roles, Set<PermissionCode> permissions) {
}
